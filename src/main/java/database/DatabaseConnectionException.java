package database;

/**
 * Segnala un errore durante l'apertura o la chiusura di una connessione al database.
 */
public class DatabaseConnectionException extends Exception {
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
