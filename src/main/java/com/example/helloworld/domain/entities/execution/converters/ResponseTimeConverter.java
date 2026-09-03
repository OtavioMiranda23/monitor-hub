package com.example.helloworld.domain.entities.execution.converters;

import com.example.helloworld.domain.entities.execution.valueObjects.ResponseTime;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter
public class ResponseTimeConverter implements AttributeConverter<ResponseTime, Long> {
    @Override
    public Long convertToDatabaseColumn(ResponseTime responseTime) {
        if (responseTime == null) return null;
        return responseTime.value().toMillis();
    }

    @Override
    public ResponseTime convertToEntityAttribute(Long millis) {
        if (millis == null) return null;
        return new ResponseTime(Duration.ofMillis(millis));
    }
}
