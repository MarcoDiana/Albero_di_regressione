package database;

/**
 * Segnala un errore durante l'apertura o la chiusura di una connessione al database.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class DatabaseConnectionException extends Exception {

    /** Identificativo di versione per la serializzazione. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea un'eccezione con un messaggio descrittivo.
     *
     * @param message messaggio che descrive l'errore
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * Crea un'eccezione con un messaggio descrittivo e la causa originaria.
     *
     * @param message messaggio che descrive l'errore
     * @param cause   causa originaria dell'errore
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
