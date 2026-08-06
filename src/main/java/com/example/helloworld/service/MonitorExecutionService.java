package com.example.helloworld.service;

import com.example.helloworld.domain.entities.ExecutionStatus;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.domain.entities.MonitorExecution;
import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import com.example.helloworld.infra.queue.MonitorExecutionResolvedEvent;
import com.example.helloworld.service.ports.IMonitorExecutionEventPublisher;
import com.example.helloworld.infra.repositories.IncidentRepository;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MonitorExecutionService {
    @Autowired
    public MonitorRepository monitorRepository;

    @Autowired
    public MonitorExecutionRepository monitorExecutionRepository;

    @Autowired
    public IncidentRepository incidentRepository;

    @Autowired
    public IMonitorExecutionEventPublisher eventPublisher;

    @Value("${monitor.batch-size}")
    private Integer batchSize;
    @Scheduled(cron = "${monitor.scan.cron}")
    public void scanMonitors() {
        try (ExecutorService executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {
            var monitors = this.findMonitorsToScan();
            monitors.forEach(this::requestBatch);
        }
    }

    public void requestBatch(MonitorEntity monitor) {
        var stopWatch = new StopWatch();
        stopWatch.start();
        var result = this.probeTarget(monitor);
        if (stopWatch.isRunning()) stopWatch.stop();
        Long reqIntervalMillis = stopWatch.getTotalTimeMillis();
        var monitorExecution = this.createMonitorExecution(monitor, result, reqIntervalMillis);
        monitor.setNextExecution();
        var newMonitor = this.monitorRepository.save(monitor);
        if (monitorExecution.getStatus() == ExecutionStatus.FAILURE || monitorExecution.getStatus() == ExecutionStatus.TIMEOUT) {
            this.emitFailedEvent(monitorExecution, newMonitor.getId());
            return;
        }
        this.emitResolveIncident(monitorExecution);
    }

    private List<MonitorEntity> findMonitorsToScan() {
        Pageable pageable = PageRequest.of(0, this.batchSize);
        return this.monitorRepository.findMonitorsToScanNow(Instant.now(), pageable);
    }

    private RestClient getRestClient(Integer connectTimeout, Integer readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));
        return RestClient
                .builder().
                requestFactory(requestFactory)
                .build();

    }

    private ProbeResult probeTarget(MonitorEntity monitor) {
        try {
            var client = this.getRestClient(
                    monitor.getTimeoutMilliseconds(),
                    monitor.getTimeoutMilliseconds()
            );
            ResponseEntity<String> response = client
                    .get()
                    .uri(monitor.getUrl())
                    .retrieve()
                    .toEntity(String.class);
            return new ProbeResult(ExecutionStatus.SUCCESS, Optional.of(response.getStatusCode().value()));
        } catch (ResourceAccessException ex) {
            return new ProbeResult(ExecutionStatus.TIMEOUT, Optional.empty());
        } catch (RestClientResponseException ex) {
            return new ProbeResult(ExecutionStatus.FAILURE, Optional.of(ex.getStatusCode().value()));
        } catch (Exception ex) {
            System.err.println("Erro inesperado: " + ex.getMessage());
            return new ProbeResult(ExecutionStatus.FAILURE, Optional.empty());
        }
    }

    private record ProbeResult(ExecutionStatus executionStatus, Optional<Integer> statusCode){}

    private MonitorExecution createMonitorExecution(MonitorEntity monitor, ProbeResult result, Long requestTime) {
        var monitorExecution = new MonitorExecution(
                monitor,
                result.executionStatus,
                result.statusCode.orElse(500),
                requestTime
        );
        return this.monitorExecutionRepository.save(monitorExecution);
    }


    private void emitFailedEvent(MonitorExecution execution, UUID monitorId) {
        var event = new MonitorExecutionFailedEvent(
                monitorId,
                execution.getId(),
                execution.getStatus()
        );
        eventPublisher.sendFailedEvent(event);
    }

    private void emitResolveIncident(MonitorExecution execution) {
        if (!execution.getStatus().equals(ExecutionStatus.SUCCESS)) return;
        var incidents = this.incidentRepository.findAllByMonitorId(
                execution.getMonitor().getId()
        );
        if (incidents.isEmpty() || incidents.get().isEmpty()) return;
        var incident = incidents.get().getLast();
        boolean hasIncidentNotResolved = incident.getResolvedAt() == null;
        if (hasIncidentNotResolved) {
            var event = new MonitorExecutionResolvedEvent(execution.getMonitor().getId(), execution.getId());
            eventPublisher.sendResolvedEvent(event);

        }
    }
}
