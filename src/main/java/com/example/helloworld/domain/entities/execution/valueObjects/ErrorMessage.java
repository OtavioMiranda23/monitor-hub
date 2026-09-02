package com.example.helloworld.domain.entities.execution.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ErrorMessage(String value) {
    public ErrorMessage {
        if (value.length() > 2000) {
            value = value.substring(0, 2000);
        }
    }
}
