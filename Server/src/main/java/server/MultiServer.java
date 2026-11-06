package server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Server TCP che accetta connessioni e avvia un gestore per ogni client.
 */
public class MultiServer {
    /** Porta sulla quale il server accetta le connessioni. */
    private int PORT = 8080;

    /**
     * Crea il server sulla porta specificata e avvia l'ascolto delle connessioni.
     *
     * @param port porta sulla quale accettare le connessioni
     */
    public MultiServer(int port) {
        this.PORT = port;
        run();
    }

    /**
     * Attende le connessioni in ingresso e avvia un gestore dedicato per ciascun client.
     * Il server socket viene chiuso se l'ascolto termina a causa di un errore.
     */
    private void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                new ServerOneClient(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
