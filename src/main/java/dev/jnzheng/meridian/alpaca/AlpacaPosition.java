package dev.jnzheng.meridian.alpaca;

import java.math.BigDecimal;

public record AlpacaPosition(String symbol, BigDecimal qty, BigDecimal cost_basis) {}
