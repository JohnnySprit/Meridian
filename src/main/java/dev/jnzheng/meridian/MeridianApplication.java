package dev.jnzheng.meridian;

import dev.jnzheng.meridian.alpaca.oauth.AlpacaOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
//tell Spring to load AlpacaOAuthProperties from alpaca.oauth.* in the properties files
@EnableConfigurationProperties(AlpacaOAuthProperties.class)
public class MeridianApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeridianApplication.class, args);
	}

}
