package dev.jnzheng.meridian.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    private BigDecimal thresholdValue;

    private boolean active;

    private Instant createdAt;
}
