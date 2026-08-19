package dev.jnzheng.meridian.alpaca;

import markets.alpaca.client.AlpacaClient;
import markets.alpaca.client.openapi.data.api.NewsApi;
import markets.alpaca.client.openapi.data.api.StockApi;
import markets.alpaca.client.openapi.data.http.ApiException;
import markets.alpaca.client.openapi.data.model.News;
import markets.alpaca.client.openapi.data.model.NewsResp;
import markets.alpaca.client.openapi.data.model.StockBar;
import markets.alpaca.client.openapi.data.model.StockBarsRespSingle;
import markets.alpaca.client.openapi.data.model.StockHistoricalFeed;
import dev.jnzheng.meridian.Headline;
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
    private static final Duration NEWS_TTL = Duration.ofMinutes(20);

    private final StockApi stockApi;
    private final NewsApi newsApi;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public AlpacaService(
            AlpacaClient alpacaClient,
            StringRedisTemplate redis,
            ObjectMapper objectMapper
    ) {
        var dataClient = alpacaClient.newDataClient();
        this.stockApi = new StockApi(dataClient);
        this.newsApi = new NewsApi(dataClient);
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

    // recent headlines for held symbols. cached because news is shared across users and rate-limited.
    public List<Headline> headlines(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        List<String> unique = new ArrayList<>();
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            if (symbol == null || symbol.isBlank()) {
                continue;
            }
            if (!unique.contains(symbol)) {
                unique.add(symbol);
            }
        }
        if (unique.isEmpty()) {
            return List.of();
        }

        java.util.Collections.sort(unique);
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < unique.size(); i++) {
            if (i > 0) {
                joined.append(",");
            }
            joined.append(unique.get(i));
        }
        String cacheKey = "news:" + joined;

        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<List<Headline>>() {
                });
            }

            NewsResp resp = newsApi.news(
                    OffsetDateTime.now().minusDays(7),
                    OffsetDateTime.now(),
                    "desc",
                    joined.toString(),
                    10,
                    false,
                    true,
                    null
            );
            List<Headline> headlines = toHeadlines(resp);
            if (!headlines.isEmpty()) {
                redis.opsForValue().set(cacheKey, objectMapper.writeValueAsString(headlines), NEWS_TTL);
            }
            return headlines;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static List<Headline> toHeadlines(NewsResp resp) {
        if (resp == null || resp.getNews() == null) {
            return List.of();
        }
        List<Headline> headlines = new ArrayList<>();
        List<News> articles = resp.getNews();
        for (int i = 0; i < articles.size(); i++) {
            News article = articles.get(i);
            if (article.getHeadline() == null || article.getHeadline().isBlank()) {
                continue;
            }
            headlines.add(new Headline(
                    article.getHeadline(),
                    article.getSource(),
                    urlString(article.getUrl())
            ));
        }
        return headlines;
    }

    private static String urlString(java.net.URI url) {
        if (url == null) {
            return null;
        }
        return url.toString();
    }
}
