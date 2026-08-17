package dev.jnzheng.meridian;

import dev.jnzheng.meridian.alpaca.AlpacaPosition;
import dev.jnzheng.meridian.alpaca.AlpacaService;
import markets.alpaca.client.openapi.data.http.ApiException;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioService {
    private final PositionService positionService;
    private final ObjectMapper objectMapper;
    private final AlpacaService alpacaService;

    public PortfolioService(
            PositionService positionService,
            ObjectMapper objectMapper,
            AlpacaService alpacaService
    ) {
        this.positionService = positionService;
        this.objectMapper = objectMapper;
        this.alpacaService = alpacaService;
    }

    public PortfolioSummary getSummary(UUID userId) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalPnl = BigDecimal.ZERO;
        List<ValuedPosition> valued = new ArrayList<>();
        List<AlpacaPosition> positions = objectMapper.readValue(
                positionService.getPositions(userId),
                new TypeReference<List<AlpacaPosition>>() {
                }
        );

        for (AlpacaPosition p : positions) {
            if (p.market_value() == null) {
                continue;
            }
            BigDecimal marketValue = p.market_value();
            BigDecimal pnl = BigDecimal.ZERO;
            if (p.unrealized_pl() != null) {
                pnl = p.unrealized_pl();
            }
            totalValue = totalValue.add(marketValue);
            totalPnl = totalPnl.add(pnl);
            valued.add(new ValuedPosition(marketValue, volatilityFor(p.symbol())));
        }

        return new PortfolioSummary(totalValue, totalPnl, weightedVolatility(valued, totalValue));
    }

    private BigDecimal volatilityFor(String symbol) {
        try {
            return volatility(alpacaService.dailyCloses(symbol));
        } catch (ApiException e) {
            return null;
        }
    }

    //calculates how much of the portfolio each position represents, multiply the weight by its volatility, then add everything together
    private static BigDecimal weightedVolatility(List<ValuedPosition> valued, BigDecimal totalValue) {
        if (totalValue.signum() == 0) {
            return null;
        }
        BigDecimal weighted = BigDecimal.ZERO;
        boolean any = false;
        for (ValuedPosition v : valued) {
            if (v.volatility() == null) {
                continue;
            }
            any = true;
            BigDecimal weight = v.marketValue().divide(totalValue, 8, RoundingMode.HALF_UP);
            weighted = weighted.add(weight.multiply(v.volatility()));
        }
        if (any) {
            return weighted.setScale(6, RoundingMode.HALF_UP);
        }
        return null;
    }

    private record ValuedPosition(BigDecimal marketValue, BigDecimal volatility) {}

    //calculates individual stock historical volatility from daily prices.
    static BigDecimal volatility(List<BigDecimal> dailyPrices) {
        if (dailyPrices == null || dailyPrices.size() < 2) {
            return null;
        }

        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < dailyPrices.size(); ++i) {
            double prevPrice = dailyPrices.get(i - 1).doubleValue();
            double currPrice = dailyPrices.get(i).doubleValue();
            if (prevPrice == 0.0) {
                continue;
            }
            returns.add((currPrice - prevPrice) / prevPrice);
        }

        if (returns.size() < 2) {
            return null;
        }

        double sum = 0.0;
        for (int i = 0; i < returns.size(); ++i) {
            sum += returns.get(i);
        }
        double mean = sum / returns.size();

        double sumSquared = 0.0;
        for (int i = 0; i < returns.size(); i++) {
            double diff = returns.get(i) - mean;
            sumSquared += diff * diff;
        }
        double stdDev = Math.sqrt(sumSquared / (returns.size() - 1));
        return BigDecimal.valueOf(stdDev).setScale(6, RoundingMode.HALF_UP);
    }

}
