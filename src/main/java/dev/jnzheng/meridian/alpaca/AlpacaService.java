package dev.jnzheng.meridian.alpaca;

import markets.alpaca.client.AlpacaClient;
import markets.alpaca.client.openapi.data.api.StockApi;
import markets.alpaca.client.openapi.data.http.ApiException;
import markets.alpaca.client.openapi.data.model.StockBar;
import markets.alpaca.client.openapi.data.model.StockBarsRespSingle;
import markets.alpaca.client.openapi.data.model.StockHistoricalFeed;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlpacaService {

    private static final Duration BARS_TTL = Duration.ofHours(1);

    private final StockApi stockApi;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public AlpacaService(
            AlpacaClient alpacaClient,
            StringRedisTemplate redis,
            ObjectMapper objectMapper
    ) {
        this.stockApi = new StockApi(alpacaClient.newDataClient());
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    // daily closes for volatility. redis TTL cache so /portfolio does not re-download 90 bars every refresh.
    public List<BigDecimal> dailyCloses(String symbol) throws ApiException {
        String cacheKey = "bars:" + symbol;
        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return objectMapper.readValue(cached, new TypeReference<List<BigDecimal>>() {
            });
        }

        List<BigDecimal> closes = fetchDailyClosesFromAlpaca(symbol);
        if (!closes.isEmpty()) {
            redis.opsForValue().set(cacheKey, objectMapper.writeValueAsString(closes), BARS_TTL);
        }
        return closes;
    }

    private List<BigDecimal> fetchDailyClosesFromAlpaca(String symbol) throws ApiException {
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
