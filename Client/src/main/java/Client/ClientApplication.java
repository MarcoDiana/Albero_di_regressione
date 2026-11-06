package Client;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point dell'applicazione Spring Boot Web con Vaadin Flow.
 * Avvia il frontend web reattivo sulla porta 8081.
 */
@SpringBootApplication(scanBasePackages = {"Client", "dto"})
public class ClientApplication implements AppShellConfigurator {

    /**
     * Metodo di avvio dell'applicazione Client Spring Boot / Vaadin Flow.
     *
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
