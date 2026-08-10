package dev.jnzheng.meridian.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

//stores alpaca access token per user
@Getter
@Setter
@Entity
@Table(name = "credentials")
public class AlpacaCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; //which Meridian user this token belongs to

    @Column(nullable = false)
    private String accessToken; //key that lets us call Alpaca as this user

    private String scope; //permissions granted (writing, data, trading)

    private String environment; //paper or live (only need paper for the purpose of analysis here)

    private Instant linkedAt; //when the token was saved

    public AlpacaCredentials(User user, String accessToken, String scope, String environment, Instant linkedAt) {
        this.user = user;
        this.accessToken = accessToken;
        this.scope = scope;
        this.environment = environment;
        this.linkedAt = linkedAt;
    }

    public AlpacaCredentials() {}
}
