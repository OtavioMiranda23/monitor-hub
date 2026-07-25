package com.example.helloworld.service;

import com.example.helloworld.controller.dto.CreateMonitorRequest;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.domain.entities.MonitorType;
import com.example.helloworld.infra.repositories.MonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClient;

import java.util.Date;

@Service
public class MonitorService {
    @Autowired
    public MonitorRepository monitorRepository;
    public MonitorEntity createMonitor(CreateMonitorRequest monitorRequest) {
        var stopWatch = new StopWatch();
        stopWatch.start();
        var defaultClient = RestClient.create();
//        ResponseEntity<String> client = defaultClient
//                .get()
//                .uri(monitorRequest.url())
//                .retrieve()
//                .toEntity(String.class);
//        stopWatch.stop();
        Long reqIntervalMillis = stopWatch.getTotalTimeMillis();
        Integer intervalToRunSeconds = 300;
//        var statusCode = client.getStatusCode();
//        var body = client.getBody();
//        var headers = client.getHeaders();
//        System.out.println(statusCode);
//        System.out.println("BODY: " + body);
//        System.out.println("HEADERS: " + headers);
        Integer timeoutMilliseconds = 5000;
        var monitor = new MonitorEntity(
                monitorRequest.name(),
                monitorRequest.url(),
                MonitorType.HTTP,
                intervalToRunSeconds,
                timeoutMilliseconds
                );
        MonitorEntity savedMonitor = monitorRepository.save(monitor);
        return savedMonitor;
    }
}
