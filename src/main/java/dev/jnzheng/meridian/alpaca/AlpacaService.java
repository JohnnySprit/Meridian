package dev.jnzheng.meridian.alpaca;

import dev.jnzheng.meridian.kafka.KafkaProducerService;
import dev.jnzheng.meridian.kafka.PriceTick;
import markets.alpaca.client.AlpacaClient;
import markets.alpaca.client.data.StockTradesRequest;
import markets.alpaca.client.openapi.data.http.ApiException;
import markets.alpaca.client.openapi.data.model.StockHistoricalFeed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AlpacaService {

    private AlpacaClient alpacaClient;
    private KafkaProducerService kafkaProducerService;
    private String[] tickers = {"AMZN", "AAPL", "AMPX"};


    public AlpacaService(AlpacaClient alpacaClient, KafkaProducerService kafkaProducerService) {
        this.alpacaClient = alpacaClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedRate = 15000)
    public void peekPrices() throws ApiException {
        for (int i = 0; i < tickers.length; ++i){
            var response = alpacaClient.stocks()
                    .tradesForSymbol(StockTradesRequest.builder()
                            .symbols(tickers[i])
                            .feed(StockHistoricalFeed.IEX)
                            .limit(1)
                            .build()
                    );
            var trade = response.getTrades().get(0);
            PriceTick tick = new PriceTick(tickers[i], BigDecimal.valueOf(trade.getP()), trade.getT().toInstant());
            kafkaProducerService.sendMessage(tickers[i], tick);
        }
    }
}
