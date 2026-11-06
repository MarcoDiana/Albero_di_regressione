package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point dell'applicazione Spring Boot per il backend dell'Albero di Regressione.
 *
 * Configura la scansione dei componenti Spring, inizializza il server embedded
 * Tomcat sulla porta 8080 ed espone la documentazione interattiva OpenAPI / Swagger UI
 * all'indirizzo {@code http://localhost:8080/swagger-ui.html}.
 */
@SpringBootApplication(scanBasePackages = {"server", "tree", "data", "database", "dto"})
public class ServerApplication {

    /**
     * Metodo di avvio dell'applicazione Spring Boot.
     *
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
