package com.example.helloworld.controller;

import com.example.helloworld.domain.entities.Incident;
import com.example.helloworld.domain.entities.IncidentStatus;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.infra.repositories.IncidentRepository;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import com.example.helloworld.service.MonitorExecutionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MonitorIncidentE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private MonitorExecutionRepository monitorExecutionRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private MonitorExecutionService monitorExecutionService;

    private HttpServer server;
    private ExecutorService serverExecutor;

    @BeforeEach
    void setUp() throws Exception {
        incidentRepository.deleteAll();
        monitorExecutionRepository.deleteAll();
        monitorRepository.deleteAll();

        serverExecutor = Executors.newCachedThreadPool();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException ignored) {
            }
        });
        server.createContext("/ok", exchange -> {
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(serverExecutor);
        server.start();
    }

    @AfterEach
    void tearDown() {
        serverExecutor.shutdownNow();
        server.stop(0);
    }

    @Test
    void shouldCreateIncidentOnTimeoutAndResolveItAfterUrlIsFixed() throws Exception {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String requestJson = """
                {
                    "name": "Monitor E2E",
                    "url": "%s/slow"
                }
                """.formatted(baseUrl);

        String responseBody = mockMvc.perform(post("/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID monitorId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        scheduleNextScanNow(monitorId);

        monitorExecutionService.scanMonitors();

        Incident incident = awaitIncidentCreated(monitorId, Duration.ofSeconds(30));
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(incident.getResolvedAt()).isNull();

        MonitorEntity monitor = monitorRepository.findById(monitorId).orElseThrow();
        monitor.setUrl(baseUrl + "/ok");
        monitor.setNextExecution(Instant.now().minusSeconds(60));
        monitorRepository.save(monitor);

        monitorExecutionService.scanMonitors();

        Incident resolved = awaitIncidentResolved(incident.getId(), Duration.ofSeconds(20));
        assertThat(resolved.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(resolved.getResolvedAt()).isNotNull();
    }

    private void scheduleNextScanNow(UUID monitorId) {
        MonitorEntity monitor = monitorRepository.findById(monitorId).orElseThrow();
        monitor.setNextExecution(Instant.now().minusSeconds(60));
        monitorRepository.save(monitor);
    }

    private Incident awaitIncidentCreated(UUID monitorId, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            var incident = incidentRepository.findByMonitorId(monitorId);
            if (incident.isPresent()) {
                return incident.get();
            }
            Thread.sleep(200);
        }
        throw new AssertionError("Incidente não foi criado em " + timeout.getSeconds() + "s");
    }

    private Incident awaitIncidentResolved(UUID incidentId, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            var incident = incidentRepository.findById(incidentId);
            if (incident.isPresent() && incident.get().getStatus() == IncidentStatus.RESOLVED) {
                return incident.get();
            }
            Thread.sleep(200);
        }
        throw new AssertionError("Incidente não foi resolvido em " + timeout.getSeconds() + "s");
    }
}
