package dev.jnzheng.meridian.alpaca.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jnzheng.meridian.entity.AlpacaCredentials;
import dev.jnzheng.meridian.entity.User;
import dev.jnzheng.meridian.repository.AlpacaCredentialsRepository;
import dev.jnzheng.meridian.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

//talks to Alpaca, then saves user and gets access token. Controller calls this to help its HTTP calls
@Service
public class AlpacaOAuthService {

    private final AlpacaOAuthProperties oauth;
    private final UserRepository userRepository;
    private final AlpacaCredentialsRepository credentialsRepository;
    private final RestClient restClient;

    public AlpacaOAuthService(
            AlpacaOAuthProperties oauth,
            UserRepository userRepository,
            AlpacaCredentialsRepository credentialsRepository
    ) {
        this.oauth = oauth;
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.restClient = RestClient.create();
    }

    //builds the Alpaca API call
    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://app.alpaca.markets/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", oauth.clientId())
                .queryParam("redirect_uri", oauth.redirectUri())
                .queryParam("state", state)
                .queryParam("scope", "account:write data")
                .queryParam("env", oauth.env())
                .toUriString();
    }

    //flow: exchange code → token, fetch account, create/find User, save token
    public User completeLogin(String code) {
        TokenResponse token = exchangeCode(code);
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new IllegalStateException("Alpaca token response missing access_token");
        }

        AccountResponse account = fetchAccount(token.accessToken());
        if (account == null || account.id() == null || account.id().isBlank()) {
            throw new IllegalStateException("Alpaca account response missing id");
        }

        User user = userRepository.findByAlpacaAccountId(account.id())
                .orElseGet(() -> userRepository.save(new User(account.id(), account.email(), Instant.now())));

        AlpacaCredentials credentials = credentialsRepository.findByUserId(user.getId())
                .orElseGet(AlpacaCredentials::new);
        credentials.setUser(user);
        credentials.setAccessToken(token.accessToken());
        credentials.setScope(token.scope());
        credentials.setEnvironment(oauth.env());
        credentials.setLinkedAt(Instant.now());
        credentialsRepository.save(credentials);

        return user;
    }

    //exchange the temp code for a lasting accessToken
    private TokenResponse exchangeCode(String code) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", oauth.clientId());
        form.add("client_secret", oauth.clientSecret());
        form.add("redirect_uri", oauth.redirectUri());

        return restClient.post()
                .uri("https://api.alpaca.markets/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(status -> status.isError(), (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new IllegalStateException(
                            "Alpaca token exchange failed (" + response.getStatusCode() + "): " + body);
                })
                .body(TokenResponse.class);
    }

    //finds the Alpaca account that the token belongs to
    private AccountResponse fetchAccount(String accessToken) {
        String host = "paper".equalsIgnoreCase(oauth.env())
                ? "https://paper-api.alpaca.markets"
                : "https://api.alpaca.markets";

        return restClient.get()
                .uri(host + "/v2/account")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.isError(), (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new IllegalStateException(
                            "Alpaca account fetch failed (" + response.getStatusCode() + "): " + body);
                })
                .body(AccountResponse.class);
    }

    //Alpaca JSON uses snake_case field names like access_token
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            String scope
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AccountResponse(String id, String email) {}
}
