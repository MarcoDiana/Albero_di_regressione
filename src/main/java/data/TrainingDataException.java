package data;

/**
 * Eccezione sollevata in caso di acquisizione errata del training set.
 * Copre i casi di file inesistente o non leggibile, schema mancante,
 * training set vuoto e training set privo di variabile target numerica.
 */
public class TrainingDataException extends Exception {

	/**
	 * Costruttore: crea l'eccezione con un messaggio descrittivo dell'errore.
	 *
	 * @param message descrizione del problema riscontrato durante l'acquisizione
	 */
	public TrainingDataException(String message) {
		super(message);
	}

	/**
	 * Costruttore: crea l'eccezione incapsulando la causa originaria
	 * (es. FileNotFoundException), in modo da non perderne lo stack trace.
	 *
	 * @param message descrizione del problema riscontrato durante l'acquisizione
	 * @param cause   eccezione che ha originato l'errore
	 */
	public TrainingDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
