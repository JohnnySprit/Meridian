package dev.jnzheng.meridian;

import dev.jnzheng.meridian.alpaca.AlpacaProperties;
import dev.jnzheng.meridian.alpaca.oauth.AlpacaOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AlpacaOAuthProperties.class, AlpacaProperties.class})
public class MeridianApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeridianApplication.class, args);
	}

}
