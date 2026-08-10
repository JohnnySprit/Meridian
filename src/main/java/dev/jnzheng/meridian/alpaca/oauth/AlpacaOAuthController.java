package dev.jnzheng.meridian.alpaca.oauth;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

//handles HTTP/session and then calls the service.
@RestController
@RequestMapping("/oauth/alpaca")
public class AlpacaOAuthController {

    private final AlpacaOAuthService oauthService;

    //spring should inject the service here automatically
    public AlpacaOAuthController(AlpacaOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    //flow: user opens asks to "sign in with Alpaca", goes here and generates a random state that Alpaca needs
    @GetMapping("/login")
    public RedirectView login(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);
        return new RedirectView(oauthService.buildAuthorizeUrl(state));
    }

    //redirects here with code + state. Check state, then call service with the auth code
    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session
    ) {
        //rejects if state doesn't match what was generated earlier
        if (!state.equals(session.getAttribute("oauth_state"))) {
            return ResponseEntity.badRequest().body("Invalid OAuth state — start again at /oauth/alpaca/login");
        }
        session.removeAttribute("oauth_state");

        try {
            //trades code for the accessToken, then saves user + token in DB
            var user = oauthService.completeLogin(code);
            //remember who is logged in for later requests
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            //show the real reason instead of a blank Whitelabel 500 page
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
