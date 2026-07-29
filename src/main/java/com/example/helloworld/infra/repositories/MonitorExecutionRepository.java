package com.example.helloworld.infra.repositories;

import com.example.helloworld.domain.entities.MonitorExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface MonitorExecutionRepository extends JpaRepository<MonitorExecution, UUID> {
}
