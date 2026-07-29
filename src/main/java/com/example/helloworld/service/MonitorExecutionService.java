package com.example.helloworld.service;

import com.example.helloworld.domain.entities.ExecutionStatus;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import com.example.helloworld.infra.queue.RabbitMQConfig;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
public class MonitorExecutionService {
    @Autowired
    public MonitorRepository monitorRepository;
    @Autowired
    public MonitorExecutionRepository monitorExecutionRepository;

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    //    @Scheduled(cron = "0 */5 * * * *")
    @Scheduled(fixedRate = 10000)
    public void scanMonitors() {
        Pageable pageable = PageRequest.of(0, 50);
        List<MonitorEntity> monitors = this.monitorRepository.findMonitorsToScanNow(Instant.now(), pageable);
        System.out.printf("EXECUTA findMonitorsToScanNow: " + monitors);
        try (ExecutorService executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {
            for (MonitorEntity monitor : monitors) {
                executor.submit(() -> this.requestBatch(monitor));
            }
        }
    }

    public void requestBatch(MonitorEntity monitor) {
        var executionStatus = ExecutionStatus.SUCCESS;
        HttpStatusCode statusCode = null;
        var stopWatch = new StopWatch();
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(monitor.getTimeoutMilliseconds()));
            requestFactory.setReadTimeout(Duration.ofMillis(monitor.getTimeoutMilliseconds()));
            stopWatch.start();
            var defaultClient = RestClient.builder();
            RestClient client = defaultClient
                    .requestFactory(requestFactory)
                    .build();
            ResponseEntity<String> response = client
                    .get()
                    .uri(monitor.getUrl())
                    .retrieve()
                    .toEntity(String.class);
            statusCode = response.getStatusCode();
        } catch (ResourceAccessException ex) {
            executionStatus = ExecutionStatus.TIMEOUT;
        } catch (RestClientResponseException ex) {
            executionStatus = ExecutionStatus.FAILURE;
            statusCode = ex.getStatusCode();
        } catch (Exception ex) {
            executionStatus = ExecutionStatus.FAILURE;
            System.err.println("Erro inesperado: " + ex.getMessage());
            //TODO: Construir sistema de observiblidade;
        }
        if (stopWatch.isRunning())
        {
            stopWatch.stop();
        }
        Long reqIntervalMillis = stopWatch.getTotalTimeMillis();
        var monitorExecution = new com.example.helloworld.domain.entities.MonitorExecution(
                monitor,
                executionStatus,
                statusCode != null ? statusCode.value() : 0,
                reqIntervalMillis);
        this.monitorExecutionRepository.save(monitorExecution);
        Instant nextExecution = Instant.now().plusSeconds(monitor.getIntervalToRunSeconds());
        monitor.setNextExecution(nextExecution);
        this.monitorRepository.save(monitor);
        if (monitorExecution.getStatus().equals(ExecutionStatus.FAILURE)) {
            var event = new MonitorExecutionFailedEvent(
                    monitor.getId(),
                    monitorExecution.getId(),
                    monitorExecution.getStatus()
                    );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );
        }
    }
}
