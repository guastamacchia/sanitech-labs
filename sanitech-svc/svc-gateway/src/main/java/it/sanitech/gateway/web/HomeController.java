package it.sanitech.gateway.web;

import it.sanitech.gateway.config.GatewayUiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint “home” del gateway.
 *
 * <p>
 * Per ambienti interni/staging può essere comodo redirectare {@code /} verso {@code /swagger.html}.
 * In produzione il redirect può essere disabilitato via property.
 * </p>
 */
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final GatewayUiProperties ui;

    @GetMapping("/")
    public ResponseEntity<Void> root() {
        if (!ui.isRedirectRootToSwagger()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/swagger.html")
                .build();
    }
}
