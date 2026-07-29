package com.example.helloworld.service;

import com.example.helloworld.domain.entities.Incident;
import com.example.helloworld.domain.entities.MonitorExecution;
import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import org.springframework.stereotype.Service;

@Service
public class IncidentService {
    public void createIncident(MonitorExecutionFailedEvent failedEvent) {
        new Incident(failedEvent.monitorId(), );
    }
}
