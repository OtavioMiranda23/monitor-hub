package com.example.helloworld.service;

import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.domain.entities.MonitorExecution;
import com.example.helloworld.domain.entities.MonitorType;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScanMonitorsIntegrationTest {

    @Autowired
    private MonitorExecutionService monitorExecutionService;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private MonitorExecutionRepository monitorExecutionRepository;

    @BeforeEach
    void setUp() {
        monitorExecutionRepository.deleteAll();
        monitorRepository.deleteAll();
    }

    @Test
    void shouldCreateMonitorExecutionAndUpdateMonitorNextExecution() {
        var monitor = new MonitorEntity(
                "Monitor Teste",
                "http://localhost:1",
                MonitorType.HTTP,
                300,
                100
        );
        monitor.setNextExecution(Instant.now().minusSeconds(60));
        monitor = monitorRepository.save(monitor);

        monitorExecutionService.scanMonitors();

        List<MonitorExecution> executions = monitorExecutionRepository.findAll();
        assertThat(executions).hasSize(1);
        assertThat(executions.get(0).getMonitor().getId()).isEqualTo(monitor.getId());

        MonitorEntity updatedMonitor = monitorRepository.findById(monitor.getId()).orElseThrow();
        assertThat(updatedMonitor.getNextExecution()).isAfter(monitor.getNextExecution());
    }
}
