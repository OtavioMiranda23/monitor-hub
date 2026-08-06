package com.example.helloworld.service.ports;

import com.example.helloworld.infra.queue.MonitorExecutionFailedEvent;
import com.example.helloworld.infra.queue.MonitorExecutionResolvedEvent;

public interface IMonitorExecutionEventPublisher {
    void sendResolvedEvent(MonitorExecutionResolvedEvent event);
    void sendFailedEvent(MonitorExecutionFailedEvent event);
}
