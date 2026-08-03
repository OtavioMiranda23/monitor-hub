package com.example.helloworld.service;

import com.example.helloworld.domain.entities.Incident;
import com.example.helloworld.domain.entities.IncidentStatus;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.domain.entities.MonitorExecution;
import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import com.example.helloworld.infra.queue.MonitorExecutionResolvedEvent;
import com.example.helloworld.infra.repositories.IncidentRepository;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class IncidentService {

    @Autowired
    private MonitorRepository monitorRepository;
    @Autowired
    private MonitorExecutionRepository monitorExecutionRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    public Incident createIncident(MonitorExecutionFailedEvent failedEvent) {
        var monitorExecution = this.monitorExecutionRepository
                .findById(failedEvent.executionId())
                .orElseThrow(() -> new EntityNotFoundException("Monitor Execution Not Found: "
                        +  failedEvent.executionId()));

        var incidentCause = monitorExecution.getErrorMessage();
        var monitor = this.monitorExecutionRepository
                .findById(failedEvent.executionId())
                .orElseThrow(() -> new EntityNotFoundException("Monitor Execution Not Found: "));
        var incident = new Incident(
                monitor.getMonitor(),
                IncidentStatus.OPEN,
                Instant.now(),
                incidentCause
        );
         Optional<Incident> optionalIncident = incidentRepository
                 .findByMonitorId(failedEvent.executionId());
        return optionalIncident.orElseGet(() -> incidentRepository.save(incident));
    }

    @Transactional
    public void resolveIncident(MonitorExecutionResolvedEvent event) {
        MonitorEntity monitorEntity =this.monitorRepository.findById(event.monitorId()).orElseThrow(() ->
                new EntityNotFoundException("Monitor Execution Not Found: " + event.monitorId()));
        var lastIncidentOfMonitor = monitorEntity.getIncidents().getLast();
        lastIncidentOfMonitor.setResolvedAt(Instant.now());
        lastIncidentOfMonitor.setStatus(IncidentStatus.RESOLVED);

        this.monitorRepository.save(monitorEntity);
    }
}
