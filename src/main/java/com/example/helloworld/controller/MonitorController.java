package com.example.helloworld.controller;

import com.example.helloworld.controller.dto.CreateMonitorRequest;
import com.example.helloworld.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/monitors")
public class MonitorController {
    @Autowired
    public MonitorService monitorService;
    @PostMapping
    public String create(@RequestBody CreateMonitorRequest request) {
        monitorService.createMonitor(request);

        return "Hello World!";

    }
}
