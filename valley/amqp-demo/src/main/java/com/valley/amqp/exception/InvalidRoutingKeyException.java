package com.valley.amqp.exception;

/**
 * @ProjectName: amqp
 * @Package: com.valley.amqp.exception
 * @ClassName: InvalidRoutingKeyException
 * @Description: 无效的路由异常
 * @Author: wangzq
 * @Date: 2021/5/15 上午11:51
 */
public class InvalidRoutingKeyException extends RuntimeException {

    public InvalidRoutingKeyException(String message) {
        super(message);
    }
}
