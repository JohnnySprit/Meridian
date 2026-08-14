package dev.jnzheng.meridian.alpaca;

import markets.alpaca.client.AlpacaClient;
import markets.alpaca.client.AlpacaCredentials;
import markets.alpaca.client.TradingApiEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlpacaClientConfig {

    @Bean
    public AlpacaClient createClient(AlpacaProperties alpacaProperties) {
        return AlpacaClient.builder(new AlpacaCredentials(alpacaProperties.apiKey(), alpacaProperties.apiSecret()))
                .tradingEnvironment(TradingApiEnvironment.PAPER)
                .build();
    }
}
