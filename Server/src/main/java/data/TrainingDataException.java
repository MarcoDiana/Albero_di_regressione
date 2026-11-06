package data;

/**
 * Eccezione sollevata quando non è possibile caricare correttamente il training
 * set dal database, per esempio per errori di connessione, schema non valido,
 * tabella vuota o attributo target non numerico.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class TrainingDataException extends Exception {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore: crea l'eccezione con un messaggio descrittivo dell'errore.
	 *
	 * @param message descrizione del problema riscontrato durante l'acquisizione
	 */
	public TrainingDataException(String message) {
		super(message);
	}

	/**
	 * Crea l'eccezione incapsulando la causa originaria, in modo da conservarne
	 * lo stack trace.
	 *
	 * @param message descrizione del problema riscontrato durante l'acquisizione
	 * @param cause   eccezione che ha originato l'errore
	 */
	public TrainingDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
