package server;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

/**
 * Controller di reindirizzamento per la radice del server REST.
 * Reindirizza le richieste verso la documentazione interattiva Swagger UI.
 *
 * @author Marco Diana
 * @version 1.0
 */
@Controller
public class RootController {

    /**
     * Costruttore predefinito.
     */
    public RootController() {
    }

    /**
     * Reindirizza la richiesta alla radice verso Swagger UI.
     *
     * @return risposta HTTP 302 Redirect verso Swagger UI
     */
    @Operation(hidden = true)
    @GetMapping("/")
    public ResponseEntity<Void> redirectToSwagger() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/swagger-ui.html"))
                .build();
    }
}
