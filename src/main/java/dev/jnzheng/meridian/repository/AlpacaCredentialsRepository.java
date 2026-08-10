package dev.jnzheng.meridian.repository;

import dev.jnzheng.meridian.entity.AlpacaCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlpacaCredentialsRepository extends JpaRepository<AlpacaCredentials, UUID> {

    Optional<AlpacaCredentials> findByUserId(UUID userId);
}
