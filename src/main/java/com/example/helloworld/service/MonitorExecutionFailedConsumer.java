package com.example.helloworld.service;

import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import com.example.helloworld.infra.queue.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MonitorExecutionFailedConsumer {
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    public IncidentService incidentService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handle(MonitorExecutionFailedEvent event) {
        System.out.println("EVENTO DE FALHA CRIADO: " + event);
        incidentService.createIncident(event);
    }
}
