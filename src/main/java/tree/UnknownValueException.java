package tree;

/**
 * Eccezione sollevata durante la predizione di un nuovo esempio quando
 * il valore acquisito per un attributo è mancante o fuori range, cioè non
 * permette di selezionare alcun ramo valido del nodo di split corrente.
 */
public class UnknownValueException extends Exception {

	/**
	 * Costruttore: crea l'eccezione con un messaggio descrittivo.
	 *
	 * @param message descrizione dell'errore (es. il range di risposte ammesse)
	 */
	public UnknownValueException(String message) {
		super(message);
	}
}
