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
@Table(name = "price_snapshots", indexes = @Index(columnList = "symbol, timestamp"))
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String symbol;

    private BigDecimal price;

    private Instant timestamp;
}
