package com.example.helloworld.domain.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private MonitorEntity monitor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime resolvedAt;

    @Column(length = 2000)
    private String cause;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }

    // getters e setters
}
