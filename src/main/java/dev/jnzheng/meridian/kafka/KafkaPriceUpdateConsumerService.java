package dev.jnzheng.meridian.kafka;

import dev.jnzheng.meridian.entity.PriceSnapshot;
import dev.jnzheng.meridian.repository.PriceSnapshotRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class KafkaPriceUpdateConsumerService {

    private final PriceSnapshotRepository priceSnapshotRepository;
    private final StringRedisTemplate redisTemplate;

    public KafkaPriceUpdateConsumerService(PriceSnapshotRepository priceSnapshotRepository, StringRedisTemplate redisTemplate){
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "price-updates")
    void onMessage(PriceTick tick) {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setSymbol(tick.symbol());
        snapshot.setPrice(tick.price());
        snapshot.setTimestamp(tick.timestamp());

        priceSnapshotRepository.save(snapshot);
        redisTemplate.opsForValue().set("price:" + tick.symbol(), String.valueOf(tick.price()));
    }
}
