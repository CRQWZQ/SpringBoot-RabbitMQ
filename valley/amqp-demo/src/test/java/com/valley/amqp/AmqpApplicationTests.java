package com.valley.amqp;


import com.valley.amqp.config.RabbitConfig;
import com.valley.amqp.service.RabbitProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AmqpApplicationTests {

    @Autowired
    private RabbitProducerService producerService;


    @Test
    public void testSendByWork() {
        String message = "send work message --- ";
        producerService.sendWork(message);
        System.out.println("发送一条work模式消息");
    }

    /**
     *功能描述 测试fanout 模型 消息发送
     * @author wangzq
     * @param
     * @return
     */
    @Test
    public void testSendByFanout() {
        for (int i = 0; i < 1; i++) {
            String message = "send message to user=" + i;
            producerService.sendPublish(RabbitConfig.EXCHANGE_FANOUT, message);
            System.out.println("send message is : -----'" + message + "'-----");
        }
    }

    @Test
    public void testSendByTopic() {
        for (int i = 0; i< 5; i++) {
            String message = "send topicMessage to user=" + i;
            producerService.sendTopic(RabbitConfig.EXCHANGE_TOPIC, "topic.#", message);
            System.out.println("send topicMessage is : -----'" + message + "'-----");
        }
    }

}
