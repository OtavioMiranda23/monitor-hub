package com.example.helloworld.domain.entities.valueObjects;

import java.time.Duration;

public record Timeout(Duration value) {
    public Timeout {
        if (value == null) {
            throw new IllegalArgumentException("Timeout não pode ser null");
        }
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("O timeout precisa ter um valor maior que 0");
        }
    }
}
