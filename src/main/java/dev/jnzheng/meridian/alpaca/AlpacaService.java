package dev.jnzheng.meridian.alpaca;

import markets.alpaca.client.AlpacaClient;
import markets.alpaca.client.openapi.data.api.StockApi;
import markets.alpaca.client.openapi.data.http.ApiException;
import markets.alpaca.client.openapi.data.model.StockBar;
import markets.alpaca.client.openapi.data.model.StockBarsRespSingle;
import markets.alpaca.client.openapi.data.model.StockHistoricalFeed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlpacaService {

    private final StockApi stockApi;

    public AlpacaService(AlpacaClient alpacaClient) {
        this.stockApi = new StockApi(alpacaClient.newDataClient());
    }

    // Pulls last 90 1-day bars and returns closes.
    public List<BigDecimal> dailyCloses(String symbol) throws ApiException {
        StockBarsRespSingle resp = stockApi.stockBarSingle(
                symbol,
                "1Day",
                OffsetDateTime.now().minusDays(90),
                OffsetDateTime.now(),
                90,
                null,
                null,
                StockHistoricalFeed.IEX,
                null,
                null,
                null
        );
        List<StockBar> bars = resp.getBars();
        if (bars == null) {
            return List.of();
        }

        List<BigDecimal> closes = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            Double close = bars.get(i).getC();
            if (close != null) {
                closes.add(BigDecimal.valueOf(close));
            }
        }
        return closes;
    }
}
