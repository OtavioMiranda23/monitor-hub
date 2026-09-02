package com.example.helloworld.domain.entities.execution.converters;

import com.example.helloworld.domain.entities.execution.valueObjects.ResponseTime;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter
public class ResponseTimeConverter implements AttributeConverter<ResponseTime, Duration> {
    @Override
    public Duration convertToDatabaseColumn(ResponseTime responseTime) {
        if (responseTime == null) return null;
        return responseTime.value();
    }

    @Override
    public ResponseTime convertToEntityAttribute(Duration value) {
        if (value == null) return null;
        return new ResponseTime(value);
    }
}
