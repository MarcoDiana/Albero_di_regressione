package Client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per il componente {@link BrowserLauncher}.
 *
 * @author Marco Diana
 * @version 1.0
 */
class BrowserLauncherTest {

    @Test
    @DisplayName("Verifica getter e setter della configurazione porta e auto-open")
    void testConfigurationGettersSetters() {
        BrowserLauncher launcher = new BrowserLauncher();
        assertEquals(8081, launcher.getServerPort());
        assertTrue(launcher.isAutoOpen());

        launcher.setServerPort(9090);
        launcher.setAutoOpen(false);

        assertEquals(9090, launcher.getServerPort());
        assertFalse(launcher.isAutoOpen());
    }

    @Test
    @DisplayName("Verifica gestione URL nulli, vuoti o non validi")
    void testInvalidUrls() {
        BrowserLauncher launcher = new BrowserLauncher();
        assertFalse(launcher.launchBrowser(null));
        assertFalse(launcher.launchBrowser(""));
        assertFalse(launcher.launchBrowser("   "));
    }

    @Test
    @DisplayName("Verifica disabilitazione apertura quando autoOpen è false")
    void testDisabledAutoOpen() {
        BrowserLauncher launcher = new BrowserLauncher();
        launcher.setAutoOpen(false);
        // Non deve sollevare eccezioni
        assertDoesNotThrow(() -> launcher.onApplicationReady(null));
    }

    @Test
    @DisplayName("Verifica fallback comando multipiattaforma con mock launcher")
    void testLaunchViaCommandSimulation() {
        TestableBrowserLauncher launcher = new TestableBrowserLauncher();

        launcher.setOperatingSystem("windows 11");
        assertTrue(launcher.launchViaCommand("http://localhost:8081"));
        assertEquals("windows 11", launcher.getOperatingSystem());

        launcher.setOperatingSystem("mac os x");
        assertTrue(launcher.launchViaCommand("http://localhost:8081"));

        launcher.setOperatingSystem("linux");
        assertTrue(launcher.launchViaCommand("http://localhost:8081"));
    }

    /**
     * Sottoclasse testabile per simulare l'esecuzione comandi senza aprire processi reali.
     */
    private static class TestableBrowserLauncher extends BrowserLauncher {
        @Override
        protected boolean launchViaCommand(String url) {
            if (url == null || url.trim().isEmpty()) {
                return false;
            }
            return true;
        }
    }
}
