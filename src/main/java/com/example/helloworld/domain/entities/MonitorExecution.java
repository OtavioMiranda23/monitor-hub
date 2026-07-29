package com.example.helloworld.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "monitor_executions")
public class MonitorExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private MonitorEntity monitor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    private Integer httpStatusCode;

    private Long responseTimeMilliseconds;

    @Column(length = 2000)
    private String errorMessage;

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
            Long responseTimeMilliseconds
            ) {
        this.monitor = monitor;
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMilliseconds = responseTimeMilliseconds;
    }
}
