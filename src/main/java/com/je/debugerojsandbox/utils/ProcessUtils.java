package com.je.debugerojsandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.je.debugerojsandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ProcessUtils {

    public static ExecuteMessage runProcessAndGetMessage(Process compileProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        StopWatch stopWatch = new StopWatch();
        try {
            // 等待编译完成
            stopWatch.start();
            Integer compileStatus = compileProcess.waitFor();
            executeMessage.setStatus(compileStatus);
            if (compileStatus == 0) {
                // 正常退出
                log.info(opName + "成功, 退出码: {}", compileStatus);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                executeMessage.setMessage(stringBuilder.toString());
            } else {
                // 编译失败
                log.info(opName + "失败,退出码: {}", compileStatus);
                // 读取正常的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                // 逐行读取
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                executeMessage.setMessage(stringBuilder.toString());
                // 分批次读取错误的输出
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                StringBuilder errorStringBuilder = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorStringBuilder.append(line);
                }
                executeMessage.setErrorMessage(errorStringBuilder.toString());
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

}
