package dev.jnzheng.meridian;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummary(
        BigDecimal totalMarketValue,
        BigDecimal totalPnl,
        BigDecimal volatility,
        String largestPositionSymbol,
        BigDecimal largestPositionWeight,
        List<PositionLine> positions,
        List<Headline> headlines,
        String analysis
) {}
