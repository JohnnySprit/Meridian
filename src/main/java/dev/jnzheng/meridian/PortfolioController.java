package dev.jnzheng.meridian;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(HttpSession session) {
        Object userIdAttr = session.getAttribute("userId");
        if (userIdAttr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not logged in. Visit /oauth/alpaca/login first");
        }
        UUID userId = (UUID) userIdAttr;
        return ResponseEntity.ok(portfolioService.getSummary(userId));
    }
}
