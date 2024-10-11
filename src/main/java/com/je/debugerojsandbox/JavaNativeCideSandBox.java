package com.je.debugerojsandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.je.debugerojsandbox.model.ExecuteCodeRequest;
import com.je.debugerojsandbox.model.ExecuteCodeResponse;
import com.je.debugerojsandbox.model.ExecuteMessage;
import com.je.debugerojsandbox.model.JudgeInfo;
import com.je.debugerojsandbox.security.DefaultSecurityManager;
import com.je.debugerojsandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class JavaNativeCideSandBox implements CodeSandBox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static void main(String[] args) {
        JavaNativeCideSandBox javaNativeCodeSandbox = new JavaNativeCideSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();

        // 增加权限限制
        System.setSecurityManager(new DefaultSecurityManager());

        // 创建临时文件，将代码写入文件
        String userDir = System.getProperty("user.dir");
        String gloableCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，不存在则创建
        if (!FileUtil.exist(gloableCodePathName)) {
            FileUtil.mkdir(gloableCodePathName);
        }
        // 把用户提交的代码隔离存放
        String userCodeParentPath = gloableCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, "utf-8");
        // 执行用户代码
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        Process compileProcess = null;
        try {
            compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage compileExecuteMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
        } catch (IOException e) {
            return this.compileError(e);
        }

        // 执行代码 得到输出结果
        List<String> inputList = executeCodeRequest.getInputList();
        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        String language = executeCodeRequest.getLanguage();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            Process runProcess = null;
            try {
                runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteMessage runExecuteMessage = ProcessUtils.runProcessAndGetMessage(runProcess,"运行");
                executeMessagesList.add(runExecuteMessage);
            } catch (IOException e) {
                return this.compileError(e);
            }
        }

        // 整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取最大值，只要有一个超时，就可以判定该程序超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessagesList) {
            String errorMessage = executeMessage.getErrorMessage();
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            if (StringUtils.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 执行中存在错误，使用枚举类给一个状态
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
        }
        // 正常运行完成
        if (outputList.size() == executeMessagesList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);

        // 设置判题信息
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // TODO 使用 Process获取到 Java程序的内存占用 可作为扩展点，Java原生实现
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 执行完毕删除文件
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除文件夹" + userCodeParentPath + (del ? "成功": "失败"));
        }
        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse compileError(Throwable e) {
        // 错误处理，提高程序健壮性
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
