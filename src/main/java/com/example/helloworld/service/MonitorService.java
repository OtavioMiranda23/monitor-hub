package com.example.helloworld.service;
import com.example.helloworld.controller.dto.CreateMonitorRequest;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.domain.entities.MonitorType;
import com.example.helloworld.infra.repositories.MonitorRepository;
import com.example.helloworld.infra.repositories.dto.MonitorSummary;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.UUID;


@Service
public class MonitorService {
    @Autowired
    public MonitorRepository monitorRepository;

    public MonitorEntity createMonitor(CreateMonitorRequest monitorRequest) {
        Integer intervalToRunSeconds = 300;
        Integer timeoutMilliseconds = 5000;
        var monitor = new MonitorEntity(
                monitorRequest.name(),
                monitorRequest.url(),
                MonitorType.HTTP,
                intervalToRunSeconds,
                timeoutMilliseconds
                );
        monitor.setNextExecution(Instant.now());
        Boolean hasMonitorWithSameUrl = this.monitorRepository.existsByUrl(monitorRequest.url());
        if (hasMonitorWithSameUrl) {
            throw new ResourceAccessException("Monitor already exists");
        }
        return monitorRepository.save(monitor);
    }

    public MonitorSummary findById(UUID id) {
       return this.monitorRepository.findMonitorSummary(id)
               .orElseThrow(() -> new EntityNotFoundException("Monitor not found"));
    }
}
