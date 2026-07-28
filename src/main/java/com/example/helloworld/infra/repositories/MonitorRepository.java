package com.example.helloworld.infra.repositories;

import com.example.helloworld.domain.entities.MonitorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonitorRepository extends JpaRepository<MonitorEntity, UUID> {
}
