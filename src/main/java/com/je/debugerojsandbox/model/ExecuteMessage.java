package com.je.debugerojsandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装控制台返回信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteMessage {

    private Integer exitValue;

    private Integer status;

    private String message;

    private String errorMessage;

    // 执行时间
    private Long time;

    private Long memory;

}
