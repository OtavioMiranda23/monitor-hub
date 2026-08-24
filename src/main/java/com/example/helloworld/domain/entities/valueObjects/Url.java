package com.example.helloworld.domain.entities.valueObjects;

import java.net.URI;

public record Url(
        String value
) {
    public Url {
        try {
            URI uri = URI.create(value);

            if (!"http".equalsIgnoreCase(uri.getScheme())
            && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("A URL deve usar HTTP ou HTTPS");
            }
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("URL invalida");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URL invalida", e);
        }
    }
}
