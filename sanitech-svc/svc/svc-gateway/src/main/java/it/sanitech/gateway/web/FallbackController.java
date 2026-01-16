package it.sanitech.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * Endpoint di fallback locale del gateway.
 *
 * <p>
 * Viene invocato dai filtri di Circuit Breaker quando un microservizio downstream non è disponibile.
 * Restituisce una risposta in formato RFC 7807 (Problem Details).
 * </p>
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/{service}")
    public ResponseEntity<ProblemDetail> fallback(@PathVariable String service) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Il servizio '" + service + "' non è temporaneamente disponibile. Riprovare più tardi."
        );
        problem.setTitle("Servizio non disponibile");
        problem.setType(URI.create("https://sanitech.example/problems/service-unavailable"));
        problem.setProperty("service", service);
        problem.setProperty("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }
}
