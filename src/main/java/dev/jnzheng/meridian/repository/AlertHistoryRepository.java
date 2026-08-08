package dev.jnzheng.meridian.repository;

import dev.jnzheng.meridian.entity.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertHistoryRepository extends JpaRepository<AlertHistory, UUID> {
}
