package dev.jnzheng.meridian.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A person in Meridian's database.
 * Created the first time they finish "Sign in with Alpaca".
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // Meridian's internal id (used in our session)

    @Column(unique = true, nullable = false)
    private String alpacaAccountId; // Alpaca's id for this brokerage account

    @Column(unique = true)
    private String email; // optional; may come from Alpaca account info

    private Instant createdAt;

    public User(String alpacaAccountId, String email, Instant createdAt) {
        this.alpacaAccountId = alpacaAccountId;
        this.email = email;
        this.createdAt = createdAt;
    }

    public User() {}
}
