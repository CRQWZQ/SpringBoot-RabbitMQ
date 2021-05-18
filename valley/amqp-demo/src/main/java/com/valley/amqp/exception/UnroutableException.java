package com.valley.amqp.exception;

/**
 * @ProjectName: amqp
 * @Package: com.valley.amqp.exception
 * @ClassName: UnroutableException
 * @Description: 无法路由异常
 * @Author: wangzq
 * @Date: 2021/5/15 下午2:04
 */
public class UnroutableException extends RuntimeException{

    public UnroutableException() {
    }

    public UnroutableException(String message) {
        super(message);
    }

    public UnroutableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnroutableException(Throwable cause) {
        super(cause);
    }
}
