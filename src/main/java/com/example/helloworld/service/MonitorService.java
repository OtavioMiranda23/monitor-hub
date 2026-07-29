package com.example.helloworld.service;

import com.example.helloworld.controller.dto.CreateMonitorRequest;
import com.example.helloworld.domain.entities.ExecutionStatus;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.domain.entities.MonitorExecution;
import com.example.helloworld.domain.entities.MonitorType;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
}
