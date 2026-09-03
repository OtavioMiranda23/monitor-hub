package com.example.helloworld.service;
import com.example.helloworld.controller.dto.CreateMonitorRequest;
import com.example.helloworld.domain.entities.monitor.MonitorEntity;
import com.example.helloworld.domain.entities.monitor.MonitorType;
import com.example.helloworld.domain.entities.monitor.valueObjects.IntervalToRun;
import com.example.helloworld.domain.entities.monitor.valueObjects.Name;
import com.example.helloworld.domain.entities.monitor.valueObjects.Timeout;
import com.example.helloworld.domain.entities.monitor.valueObjects.Url;
import com.example.helloworld.infra.repositories.MonitorRepository;
import com.example.helloworld.infra.exception.MonitorAlreadyExistsException;
import com.example.helloworld.infra.repositories.dto.MonitorSummary;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;


@Service
public class MonitorService {
    @Autowired
    public MonitorRepository monitorRepository;

    @Value("${monitor.initial-interval-run-seconds}")
    private Long intervalToRunSeconds;

    @Value("${monitor.timeout-milliseconds}")
    private Long timeoutMilliseconds;

    public MonitorEntity createMonitor(CreateMonitorRequest monitorRequest) {
        var name = new Name(monitorRequest.name());
        var url = new Url(monitorRequest.url());
        var intervalToRun = new IntervalToRun(Duration.ofSeconds(this.intervalToRunSeconds));
        var timeout = new Timeout(Duration.ofMillis(this.timeoutMilliseconds));
        var monitor = new MonitorEntity(
                name,
                url,
                MonitorType.HTTP,
                intervalToRun,
                timeout
                );
        Boolean hasMonitorWithSameUrl = this.monitorRepository.existsByUrl(monitorRequest.url());
        if (hasMonitorWithSameUrl) {
            throw new MonitorAlreadyExistsException("Já existe um monitor cadastrado com esta URL");
        }
        return monitorRepository.save(monitor);
    }

    public MonitorSummary findById(UUID id) {
        List<MonitorSummary> summaries = this.monitorRepository.findMonitorSummary(id, PageRequest.of(0,1));
        if (summaries.isEmpty()) throw new EntityNotFoundException("Monitor not found");
        return summaries.getFirst();
    }
}
