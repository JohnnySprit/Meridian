package dev.jnzheng.meridian;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    //declares topic for Kafka,
    @Bean
    public NewTopic priceUpdates(){
        return TopicBuilder.name("price-updates").build();
    }
}
