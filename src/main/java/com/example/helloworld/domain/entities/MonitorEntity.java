package com.example.helloworld.domain.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monitors")
@NoArgsConstructor
@AllArgsConstructor
public class MonitorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorType type;

    @Column(nullable = false)
    private Integer intervalToRunSeconds;

    @Column(nullable = false)
    private Integer timeoutMilliseconds;

    @Column(nullable = false)
    private Integer expectedStatusCode = 200;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "monitor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MonitorExecution> executions = new ArrayList<>();

    @OneToMany(
            mappedBy = "monitor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Incident> incidents = new ArrayList<>();

    public MonitorEntity(
            String name,
            String url,
            MonitorType type,
            Integer intervalToRunSeconds,
            Integer timeoutMilliseconds
    ) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.intervalToRunSeconds = intervalToRunSeconds;
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
