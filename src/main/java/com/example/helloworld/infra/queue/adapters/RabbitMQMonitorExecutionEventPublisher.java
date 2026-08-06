package com.example.helloworld.infra.queue.adapters;

import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import com.example.helloworld.infra.queue.MonitorExecutionResolvedEvent;
import com.example.helloworld.infra.queue.RabbitMQConfig;
import com.example.helloworld.service.ports.IMonitorExecutionEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQMonitorExecutionEventPublisher implements IMonitorExecutionEventPublisher {

    @Autowired
    public RabbitTemplate rabbitTemplate;


    @Override
    public void sendResolvedEvent(MonitorExecutionResolvedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RESOLVED_ROUTING_KEY,
                event
        );
    }

    @Override
    public void sendFailedEvent(MonitorExecutionFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.INCIDENT_ROUTING_KEY,
                event
        );
    }
}
