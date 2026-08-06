package com.example.helloworld.infra.exception;

public class MonitorAlreadyExistsException extends RuntimeException {
    public MonitorAlreadyExistsException(String message) {
        super(message);
    }
}
