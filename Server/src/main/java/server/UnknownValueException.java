package server;

/**
 * Eccezione sollevata durante la predizione di un nuovo esempio quando
 * il valore acquisito per un attributo è mancante o fuori range, cioè non
 * permette di selezionare alcun ramo valido del nodo di split corrente.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class UnknownValueException extends Exception {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore: crea l'eccezione con un messaggio descrittivo.
	 *
	 * @param message descrizione dell'errore (es. il range di risposte ammesse)
	 */
	public UnknownValueException(String message) {
		super(message);
	}

	/**
	 * Costruttore: crea l'eccezione con un messaggio descrittivo e la causa originaria.
	 *
	 * @param message descrizione dell'errore
	 * @param cause   eccezione che ha originato il problema
	 */
	public UnknownValueException(String message, Throwable cause) {
		super(message, cause);
	}
}
