package com.example.helloworld.controller;

import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import com.example.helloworld.infra.repositories.dto.MonitorSummary;
import com.example.helloworld.service.MonitorExecutionService;
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
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MonitorSummaryE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private MonitorExecutionRepository monitorExecutionRepository;

    @Autowired
    private MonitorExecutionService monitorExecutionService;

    private HttpServer server;
    private ExecutorService serverExecutor;

    @BeforeEach
    void setUp() throws Exception {
        monitorExecutionRepository.deleteAll();
        monitorRepository.deleteAll();

        serverExecutor = Executors.newCachedThreadPool();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> {
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            byte[] body = "error".getBytes();
            exchange.sendResponseHeaders(500, body.length);
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
    void shouldReturnUpMonitorSummaryConformingToDto() throws Exception {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        UUID monitorId = createMonitor(baseUrl + "/ok");

        scheduleNextScanNow(monitorId);
        monitorExecutionService.scanMonitors();

        String responseBody = mockMvc.perform(get("/monitors/{id}", monitorId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MonitorSummary summary = objectMapper.readValue(responseBody, MonitorSummary.class);
        assertThat(summary.id()).isEqualTo(monitorId);
        assertThat(summary.name()).isEqualTo("Monitor Summary E2E");
        assertThat(summary.url()).isEqualTo(baseUrl + "/ok");
        assertThat(summary.isDown()).isFalse();
        assertThat(summary.httpStatus()).isEqualTo(200);
        assertThat(summary.timeToResponseMillis()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    @Test
    void shouldReturnDownMonitorSummaryConformingToDto() throws Exception {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        UUID monitorId = createMonitor(baseUrl + "/error");

        scheduleNextScanNow(monitorId);
        monitorExecutionService.scanMonitors();

        String responseBody = mockMvc.perform(get("/monitors/{id}", monitorId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MonitorSummary summary = objectMapper.readValue(responseBody, MonitorSummary.class);
        assertThat(summary.id()).isEqualTo(monitorId);
        assertThat(summary.name()).isEqualTo("Monitor Summary E2E");
        assertThat(summary.url()).isEqualTo(baseUrl + "/error");
        assertThat(summary.isDown()).isTrue();
        assertThat(summary.httpStatus()).isEqualTo(500);
        assertThat(summary.timeToResponseMillis()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    private UUID createMonitor(String url) throws Exception {
        String requestJson = """
                {
                    "name": "Monitor Summary E2E",
                    "url": "%s"
                }
                """.formatted(url);

        String responseBody = mockMvc.perform(post("/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void scheduleNextScanNow(UUID monitorId) {
        MonitorEntity monitor = monitorRepository.findById(monitorId).orElseThrow();
        monitor.setNextExecution(Instant.now().minusSeconds(60));
        monitorRepository.save(monitor);
    }
}
