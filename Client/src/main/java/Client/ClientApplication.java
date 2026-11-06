package Client;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Entry point dell'applicazione Spring Boot Web con Vaadin Flow.
 * Avvia il frontend web reattivo sulla porta 8081.
 *
 * @author Marco Diana
 * @version 1.0
 */
@SpringBootApplication(scanBasePackages = {"Client", "dto"})
public class ClientApplication implements AppShellConfigurator {

    /**
     * Costruttore predefinito dell'applicazione Web Vaadin Flow.
     */
    public ClientApplication() {
    }

    /**
     * Metodo di avvio dell'applicazione Client Spring Boot / Vaadin Flow.
     * Configura la modalità non headless per consentire l'interazione con il desktop
     * e l'apertura automatica del browser all'avvio.
     *
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(ClientApplication.class)
                .headless(false)
                .run(args);
    }
}
