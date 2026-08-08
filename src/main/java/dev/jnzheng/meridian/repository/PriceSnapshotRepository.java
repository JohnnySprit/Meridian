package dev.jnzheng.meridian.repository;

import dev.jnzheng.meridian.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, UUID> {
}
