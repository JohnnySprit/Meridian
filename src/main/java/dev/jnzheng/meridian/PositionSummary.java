package dev.jnzheng.meridian;

import java.math.BigDecimal;

public record PositionSummary(
        String symbol,
        BigDecimal qty,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal costBasis,
        BigDecimal pnl
) {}
