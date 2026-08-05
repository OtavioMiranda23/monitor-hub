package com.example.helloworld.infra.queue;

import com.example.helloworld.service.IncidentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

public record MonitorExecutionResolvedEvent(
        UUID monitorId,
        UUID executionId
) {
    @Component
    public static class MonitorExecutionFailedConsumer {
        @Autowired
        public RabbitTemplate rabbitTemplate;

        @Autowired
        public IncidentService incidentService;

        @RabbitListener(queues = RabbitMQConfig.INCIDENT_QUEUE)
        public void handle(MonitorExecutionFailedEvent event) {
            incidentService.createIncident(event);
        }
    }
}
