package database;

/**
 * Segnala che una tabella o una raccolta di dati non contiene elementi.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class EmptySetException extends Exception {

    /** Identificativo di versione per la serializzazione. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'eccezione con un messaggio descrittivo.
     *
     * @param message descrizione del set vuoto
     */
    public EmptySetException(String message) {
        super(message);
    }

    /**
     * Crea l'eccezione con un messaggio descrittivo e la causa originaria.
     *
     * @param message descrizione del set vuoto
     * @param cause   causa originaria dell'errore
     */
    public EmptySetException(String message, Throwable cause) {
        super(message, cause);
    }
}
