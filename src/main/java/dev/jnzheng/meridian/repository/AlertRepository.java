package dev.jnzheng.meridian.repository;

import dev.jnzheng.meridian.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
}
