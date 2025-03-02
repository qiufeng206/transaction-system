package com.transaction.exception;

/**
 * 交易不存在时抛出的自定义异常
 */
public class TransactionNotFoundException extends RuntimeException {
    // 可选：显式声明序列化版本UID（防止序列化不一致问题）
    private static final long serialVersionUID = 1L;

    // 带错误消息的构造方法
    public TransactionNotFoundException(String message) {
        super(message);
    }

}
