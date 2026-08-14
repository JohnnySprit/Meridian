package dev.jnzheng.meridian;

import dev.jnzheng.meridian.alpaca.AlpacaPosition;
import markets.alpaca.client.openapi.broker.model.Portfolio;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioService {
    private final PositionService positionService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public PortfolioService(PositionService positionService, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper){
        this.positionService = positionService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public PortfolioSummary getSummary(UUID userId) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalPnl = BigDecimal.ZERO;
        List<AlpacaPosition> positions = objectMapper.readValue(
                positionService.getPositions(userId),
                new TypeReference<List<AlpacaPosition>>() {
                }
        );

        for (AlpacaPosition p : positions) {
            String priceStr = stringRedisTemplate.opsForValue().get("price:" + p.symbol());
            if (priceStr == null) {
                // skip or mark as "price unavailable" — don't NPE
                continue;
            }
            BigDecimal price = new BigDecimal(priceStr);
            BigDecimal marketValue = p.qty().multiply(price);
            BigDecimal pnl = marketValue.subtract(p.cost_basis());
            totalValue = totalValue.add(marketValue);
            totalPnl = totalPnl.add(pnl);
        }
        // 1. fetch + parse positions
        // 2. for each position:
        //      price = redis.get("price:" + symbol)
        //      marketValue = qty * price
        //      pnl = marketValue - cost_basis
        // 3. sum marketValue and pnl
        // 4. return summary (+ per-position breakdown is nice for debugging)
        return new PortfolioSummary(totalValue, totalPnl);
    }
}
