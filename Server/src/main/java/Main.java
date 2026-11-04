import server.MultiServer;

/**
 * Avvia il server in ascolto sulla porta predefinita.
 */
public class Main {

    /**
     * Punto di ingresso del server: istanzia {@link MultiServer} sulla porta 8080
     * (oppure sulla porta passata come primo argomento da riga di comando).
     *
     * @param args argomenti da riga di comando (opzionale: porta TCP)
     */
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new MultiServer(port);
    }
}
