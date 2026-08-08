package dev.jnzheng.meridian.repository;

import dev.jnzheng.meridian.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
