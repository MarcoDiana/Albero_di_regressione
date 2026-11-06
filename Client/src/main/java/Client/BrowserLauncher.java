package Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;

/**
 * Componente responsabile dell'apertura automatica del browser predefinito
 * all'avvio dell'applicazione Client Spring Boot.
 * <p>
 * Una volta completata l'inizializzazione del server incorporato (evento {@link ApplicationReadyEvent}),
 * tenta l'apertura tramite le API native {@link Desktop}. Qualora l'ambiente sia headless o {@link Desktop}
 * non sia supportato, ricorre a un meccanismo di fallback multipiattaforma (Windows, macOS, Linux).
 * </p>
 *
 * @author Marco Diana
 * @version 1.0
 */
@Component
public class BrowserLauncher {

    private static final Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);

    @Value("${server.port:8081}")
    private int serverPort = 8081;

    @Value("${app.browser.auto-open:true}")
    private boolean autoOpen = true;

    private String operatingSystem = System.getProperty("os.name", "").toLowerCase();

    /**
     * Costruttore predefinito del launcher.
     */
    public BrowserLauncher() {
    }

    /**
     * Intercetta l'evento di completamento dell'avvio dell'applicazione Spring Boot
     * e richiede l'apertura automatica del browser predefinito sulla porta configurata.
     *
     * @param event evento di avvio completato di Spring Boot
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!autoOpen) {
            logger.info("Apertura automatica del browser disabilitata da configurazione (app.browser.auto-open=false).");
            return;
        }

        String targetUrl = "http://localhost:" + serverPort;
        logger.info("Apertura automatica del browser predefinito all'indirizzo: {}", targetUrl);
        launchBrowser(targetUrl);
    }

    /**
     * Tenta l'apertura dell'URL fornito nel browser predefinito.
     *
     * @param url indirizzo web da aprire
     * @return {@code true} se la richiesta di apertura ha avuto esito positivo, {@code false} altrimenti
     */
    public boolean launchBrowser(String url) {
        if (url == null || url.trim().isEmpty()) {
            logger.warn("URL non valido per l'apertura del browser: '{}'", url);
            return false;
        }

        // Tentativo tramite AWT Desktop se disponibile e ambiente non headless
        try {
            if (!GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Desktop.browse() non disponibile ({}), attivazione fallback da riga di comando.", e.getMessage());
        }

        // Fallback multipiattaforma tramite processo nativo
        return launchViaCommand(url);
    }

    /**
     * Esegue il comando di sistema appropriato per il sistema operativo corrente.
     *
     * @param url indirizzo web da aprire
     * @return {@code true} se il processo è stato avviato correttamente, {@code false} altrimenti
     */
    protected boolean launchViaCommand(String url) {
        String os = (operatingSystem != null) ? operatingSystem.toLowerCase() : "";
        try {
            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                // Windows: rundll32 o cmd /c start
                processBuilder = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            } else if (os.contains("mac")) {
                // macOS: open
                processBuilder = new ProcessBuilder("open", url);
            } else {
                // Linux / Unix / BSD: xdg-open
                processBuilder = new ProcessBuilder("xdg-open", url);
            }
            processBuilder.start();
            return true;
        } catch (IOException e) {
            logger.warn("Impossibile avviare il browser tramite comando di sistema per {}: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Restituisce la porta configurata su cui è in ascolto il server web.
     *
     * @return porta del server locale
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Imposta la porta del server locale.
     *
     * @param serverPort numero di porta
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Indica se l'apertura automatica del browser all'avvio è abilitata.
     *
     * @return {@code true} se abilitata, {@code false} altrimenti
     */
    public boolean isAutoOpen() {
        return autoOpen;
    }

    /**
     * Abilita o disabilita l'apertura automatica del browser all'avvio.
     *
     * @param autoOpen {@code true} per abilitare, {@code false} per disabilitare
     */
    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    /**
     * Imposta la stringa identificativa del sistema operativo (utilizzato a scopo di test).
     *
     * @param operatingSystem nome del sistema operativo
     */
    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Restituisce la stringa identificativa del sistema operativo impostata.
     *
     * @return nome del sistema operativo
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }
}
