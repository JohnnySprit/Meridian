package dev.jnzheng.meridian;

import java.math.BigDecimal;

public record PortfolioSummary(
        BigDecimal totalMarketValue,
        BigDecimal totalPnl,
        BigDecimal volatility
) {}
