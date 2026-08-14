package dev.jnzheng.meridian;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceTestController {

    private final StringRedisTemplate redisTemplate;

    public PriceTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/test/price/{symbol}")
    public ResponseEntity<String> getLatestPrice(@PathVariable String symbol) {
        String price = redisTemplate.opsForValue().get("price:" + symbol);
        if (price == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(price);
    }
}