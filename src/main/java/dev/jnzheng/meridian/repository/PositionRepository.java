package dev.jnzheng.meridian.repository;

import dev.jnzheng.meridian.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
}
