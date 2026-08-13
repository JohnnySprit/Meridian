package dev.jnzheng.meridian;

import dev.jnzheng.meridian.alpaca.oauth.AlpacaOAuthProperties;
import dev.jnzheng.meridian.repository.AlpacaCredentialsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Loads the user's saved Alpaca token and calls Alpaca's positions API.
 */
@Service
public class PositionService {

    private final AlpacaCredentialsRepository credentialsRepository;
    private final RestClient restClient = RestClient.create();

    public PositionService(AlpacaCredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    public String getPositions(UUID userId) {
        var credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Please complete OAuth login first."));

        String host = "https://paper-api.alpaca.markets";

        return restClient.get()
                .uri(host + "/v2/positions")
                .header("Authorization", "Bearer " + credentials.getAccessToken())
                .retrieve()
                .onStatus(status -> status.isError(), (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new IllegalStateException(
                            "Alpaca positions failed (" + response.getStatusCode() + "): " + body);
                })
                .body(String.class);
    }
}
