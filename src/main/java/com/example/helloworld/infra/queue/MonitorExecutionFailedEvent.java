package com.example.helloworld.infra.queue;

import com.example.helloworld.domain.entities.ExecutionStatus;
import com.example.helloworld.service.IncidentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

public record MonitorExecutionFailedEvent(
        UUID monitorId,
        UUID executionId,
        ExecutionStatus status
) {
    @Service
    public static class IncidentConsumer {
        @Autowired
        public IncidentService incidentService;

        @RabbitListener(queues = RabbitMQConfig.INCIDENT_QUEUE)
        public void consume(MonitorExecutionFailedEvent event) {
            incidentService.createIncident(event);
        }

        @RabbitListener(queues = RabbitMQConfig.RESOLVED_INCIDENT_QUEUE)
        public void consume(MonitorExecutionResolvedEvent event) {
            incidentService.resolveIncident(event);
        }
    }
}
