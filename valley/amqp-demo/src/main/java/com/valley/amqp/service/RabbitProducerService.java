package com.valley.amqp.service;

/**
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.service.impl
 * @ClassName: RabbitmqService
 * @Description: rabbitmq服务接口
 * @Author: wangzq
 * @Date: 2021/5/11 上午11:57
 */
public interface RabbitProducerService {

    /**
     *功能描述 工作模型发送消息
     * @author wangzq
     * @param message
     * @return
     */
    void sendWork(String message);

    /**
     *功能描述: 直连模型发送消息
     * @author wangzq
     * @param message
     * @return
     */
    void sendDirect(String message);

    /**
     *功能描述: 广播模型发送消息
     * @author wangzq
     * @param exchangeFanout, message
     * @return
     */
    void sendPublish(String exchangeFanout, String message);

   /**
    *功能描述: topic模型发送消息
    * @author wangzq
    * @param exchangeTopic
    * @param routingKey
    * @param message
    * @return
    */
    void sendTopic(String exchangeTopic, String routingKey, String message);
}
