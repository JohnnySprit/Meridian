package dev.jnzheng.meridian.alpaca.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;


//reads the alpaca.oauth.* values from application-local.properties. Spring fills this in at startup for injection instead of hardcoding
@ConfigurationProperties(prefix = "alpaca.oauth")
public record AlpacaOAuthProperties(
        String clientId, //basically the "app id" setup from Alpaca
        String clientSecret, //the password
        String redirectUri, //where Alpaca sends the user back after approval
        String env //paper or live mode
) {}
