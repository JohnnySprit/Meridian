package dev.jnzheng.meridian;

import jakarta.annotation.PostConstruct;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;


@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, PriceTick> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, PriceTick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //appends value to 'price-updates' keyed by 'key'.
    public void sendMessage(String key, PriceTick value) {
        kafkaTemplate.send("price-updates", key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null){
                        System.out.println(ex); //if there is an exception, print it
                    }
                    else {
                        System.out.println("Success"); //else, there's no problem
                    }
                });
    }

    @PostConstruct
    public void checkPriceTick(){
        PriceTick priceTick = new PriceTick("AMZN", new BigDecimal("100.00"), Instant.now());
        sendMessage("AMZN", priceTick);
    }
}
