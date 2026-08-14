package dev.jnzheng.meridian.alpaca;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "alpaca")
public record AlpacaProperties(
        String apiKey,
        String apiSecret
) {}
