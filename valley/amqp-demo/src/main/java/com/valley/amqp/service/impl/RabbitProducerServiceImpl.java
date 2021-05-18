package com.valley.amqp.service.impl;

import com.valley.amqp.service.RabbitProducerService;
import com.valley.amqp.wrapper.RabbitMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.service.impl
 * @ClassName: RabbitmqServiceImpl
 * @Description: rabbitmq服务接口实现类
 * @Author: wangzq
 * @Date: 2021/5/11 下午12:01
 */
@Service
public class RabbitProducerServiceImpl implements RabbitProducerService {

    @Resource
    private RabbitMapper rabbitMapper;


    @Override
    public void sendWork(String message) {
        rabbitMapper.sendWork(message);
    }

    @Override
    public void sendDirect(String message) {

    }


    @Override
    public void sendPublish(String exchangeFanout,  String message) {
        rabbitMapper.sendPublish(exchangeFanout,  message);
    }

    @Override
    public void sendTopic(String exchangeTopic, String routingKey, String message) {
        rabbitMapper.sendTopic(exchangeTopic, routingKey, message);
    }


}
