package data;

/**
 * La classe TrainingDataException modella un'eccezione personalizzata (checked exception)
 * per la gestione degli errori legati all'acquisizione del Training Set.
 *
 * Questa eccezione viene sollevata tipicamente in situazioni anomale quali:
 * - Il file contenente il dataset è inesistente o inaccessibile.
 * - Manca lo schema formale all'interno del file.
 * - Il training set è completamente vuoto.
 * - Manca la variabile target numerica (attributo di classe) necessaria per la regressione.
 */
public class TrainingDataException extends Exception {

    /**
     * Costruttore di default.
     * Crea una nuova eccezione senza un messaggio di dettaglio specifico.
     */
    public TrainingDataException() {
        super();
    }

    /**
     * Costruttore con messaggio personalizzato.
     *
     * @param message Il messaggio testuale che descrive la natura specifica dell'errore.
     *                Verrà mostrato durante lo stack trace o catturato nel blocco catch.
     */
    public TrainingDataException(String message) {
        super(message);
    }

    /**
     * Costruttore con messaggio personalizzato e causa (eccezione originale scatenante).
     * Particolarmente utile per "incapsulare" un'eccezione di I/O (come FileNotFoundException)
     * e passarla al chiamante preservando lo stack trace originale.
     *
     * @param message Il messaggio testuale descrittivo
     * @param cause   L'eccezione originale che ha causato questo errore (es. IOException)
     */
    public TrainingDataException(String message, Throwable cause) {
        super(message, cause);
    }
}