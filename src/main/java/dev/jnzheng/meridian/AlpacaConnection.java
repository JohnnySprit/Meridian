package dev.jnzheng.meridian;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import markets.alpaca.client.AlpacaClient;
import markets.alpaca.client.AlpacaCredentials;
import markets.alpaca.client.TradingApiEnvironment;
import markets.alpaca.client.data.StockTradesRequest;
import markets.alpaca.client.openapi.data.model.StockHistoricalFeed;

public class AlpacaConnection {
    public static void main(String[] args) throws Exception {
        var props = new Properties();
        props.load(Files.newInputStream(Path.of("src/main/resources/application-local.properties")));

        var client = AlpacaClient.builder(new AlpacaCredentials(
                        props.getProperty("alpaca.api-key"),
                        props.getProperty("alpaca.api-secret")))
                .tradingEnvironment(TradingApiEnvironment.PAPER)
                .build();

        System.out.println(client.stocks().tradesForSymbol(StockTradesRequest.builder()
                .symbols("AMZN")
                .feed(StockHistoricalFeed.IEX)
                .limit(10)
                .build()));
    }
}
