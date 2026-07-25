package com.example.helloworld.domain.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "monitor_executions")
public class MonitorExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private LocalDateTime checkedAt;

    @PrePersist
    protected void onCreate() {
        checkedAt = LocalDateTime.now();
    }

    // getters e setters
}
