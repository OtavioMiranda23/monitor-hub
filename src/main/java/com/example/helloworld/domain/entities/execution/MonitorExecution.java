package com.example.helloworld.domain.entities.execution;

import com.example.helloworld.domain.entities.execution.converters.ResponseTimeConverter;
import com.example.helloworld.domain.entities.execution.valueObjects.ErrorMessage;
import com.example.helloworld.domain.entities.execution.valueObjects.ResponseTime;
import com.example.helloworld.domain.entities.monitor.MonitorEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monitor_executions")
public class MonitorExecution {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private MonitorEntity monitor;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Getter
    private Integer httpStatusCode;

    @Convert(converter = ResponseTimeConverter.class)
    private ResponseTime responseTime;

    @Column(length = 2000)
    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "errorMessage")
    )
    private ErrorMessage errorMessage;

    @Column(nullable = false)
    private Instant checkedAt;

    @PrePersist
    protected void onCreate() {
        checkedAt = Instant.now();
    }
    public MonitorExecution(
            MonitorEntity monitor,
            ExecutionStatus status,
            Integer httpStatusCode,
            ResponseTime responseTime
            ) {
        this.monitor = monitor;
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        this.responseTime = responseTime;
    }

    public String getErrorMessage() {
        return this.errorMessage.value();
    }

    public Duration getResponseTime() {
        return responseTime.value();
    }
}
