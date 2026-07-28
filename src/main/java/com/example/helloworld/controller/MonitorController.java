package com.example.helloworld.controller;

import com.example.helloworld.controller.dto.CreateMonitorRequest;
import com.example.helloworld.domain.entities.MonitorEntity;
import com.example.helloworld.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/monitors")
public class MonitorController {
    @Autowired
    public MonitorService monitorService;

    @PostMapping
    public ResponseEntity<MonitorEntity> create(@RequestBody CreateMonitorRequest request) {
        var monitor = monitorService.createMonitor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(monitor);
    }
}
