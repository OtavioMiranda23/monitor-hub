package com.example.helloworld.domain.entities.monitor.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Name(String value) {
    public Name {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }
        if(value.length() < 2) {
            throw new IllegalArgumentException("O nome deve ter no mínimo 2 caracteres");
        }
    }
}
