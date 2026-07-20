package tree;

/**
 * La classe UnknownValueException modella un'eccezione personalizzata
 * per la gestione di risposte errate o fuori range fornite dall'utente
 * durante la fase di predizione interattiva.
 *
 * Questa eccezione viene sollevata nel caso in cui:
 * - L'utente inserisca da tastiera un indice non corrispondente a nessuno dei figli del nodo corrente.
 * - L'acquisizione del valore risulti mancante o del tutto incompatibile.
 */
public class UnknownValueException extends Exception {

    /**
     * Costruttore di default.
     * Crea una nuova eccezione senza un messaggio di dettaglio specifico.
     */
    public UnknownValueException() {
        super();
    }

    /**
     * Costruttore con messaggio personalizzato.
     *
     * @param message Il messaggio testuale che descrive l'errore (ad esempio i limiti del range consentito).
     *                Verrà mostrato a schermo per avvisare l'utente dell'inserimento non valido.
     */
    public UnknownValueException(String message) {
        super(message);
    }
}