package dev.jnzheng.meridian;

import java.math.BigDecimal;

public record PositionLine(
        String symbol,
        BigDecimal qty,
        BigDecimal marketValue,
        BigDecimal pnl,
        BigDecimal weight
) {}
