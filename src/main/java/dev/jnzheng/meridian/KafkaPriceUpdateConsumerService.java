package dev.jnzheng.meridian;

import dev.jnzheng.meridian.entity.PriceSnapshot;
import dev.jnzheng.meridian.repository.PriceSnapshotRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class KafkaPriceUpdateConsumerService {

    private final PriceSnapshotRepository priceSnapshotRepository;

    public KafkaPriceUpdateConsumerService(PriceSnapshotRepository priceSnapshotRepository){
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    @KafkaListener(topics = "price-updates")
    void onMessage(PriceTick tick) {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setSymbol(tick.symbol());
        snapshot.setPrice(tick.price());
        snapshot.setTimestamp(tick.timestamp());

        priceSnapshotRepository.save(snapshot);
    }
}
