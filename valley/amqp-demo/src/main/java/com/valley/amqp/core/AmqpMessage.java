package com.valley.amqp.core;

import java.io.Serializable;

/**
 * @ProjectName: amqp
 * @Package: com.valley.amqp.core
 * @ClassName: AmqpMessage
 * @Description: rabbit消息封装类
 * @Author: wangzq
 * @Date: 2021/5/17 上午11:52
 */
public class AmqpMessage implements Serializable {

    private static final long serialVersionUID = 7648366869148752619L;

    private Long messageId;

    private String message;


    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
