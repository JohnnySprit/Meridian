package dev.jnzheng.meridian;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

//check current user session, call service with it, and return the response from Alpaca
@RestController
@RequestMapping("/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public ResponseEntity<?> getPositions(HttpSession session) {
        // set during OAuth callback
        Object userIdAttr = session.getAttribute("userId");
        if (userIdAttr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not logged in. Visit /oauth/alpaca/login first");
        }

        UUID userId = (UUID) userIdAttr;
        String positionsJson = positionService.getPositions(userId);
        return ResponseEntity.ok(positionsJson);
    }
}
