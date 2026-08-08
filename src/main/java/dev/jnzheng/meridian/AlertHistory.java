package dev.jnzheng.meridian;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "alert_history")
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    private BigDecimal valueAtTrigger;

    private Instant triggeredAt;
}
