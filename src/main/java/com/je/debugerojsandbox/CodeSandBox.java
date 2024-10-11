package com.je.debugerojsandbox;


import com.je.debugerojsandbox.model.ExecuteCodeResponse;
import com.je.debugerojsandbox.model.ExecuteCodeRequest;

public interface CodeSandBox {


    /**
     * 执行代码
     * @param excuteCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest excuteCodeRequest);

    /**
     * 可扩展查看代码沙箱状态接口
     */
}
