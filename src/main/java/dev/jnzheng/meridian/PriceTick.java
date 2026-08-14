package dev.jnzheng.meridian;

import java.math.BigDecimal;
import java.time.Instant;

//need event shape since Kafka only ships bytes.
//simply just a record data holder for fields + constructor + getters
//this event is the price for a symbol at a certain time
public record PriceTick(String symbol, BigDecimal price, Instant timestamp) {}
