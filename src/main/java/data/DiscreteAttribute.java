package data;

/**
 * Rappresenta un attributo discreto del training set.
 * Un attributo discreto ha un dominio finito e noto di valori ammissibili,
 * memorizzati nell'array values[].
 *
 * Esempio (da servo.dat):
 *   motor con valori {A, B, C, D, E}
 *   pgain con valori {3, 4, 5, 6}
 */
public class DiscreteAttribute extends Attribute {

	/** Array dei valori distinti che l'attributo può assumere. */
	private String[] values;

	/**
	 * Costruttore: invoca il costruttore della superclasse e salva
	 * l'array dei valori discreti ammissibili.
	 *
	 * @param name   nome simbolico dell'attributo
	 * @param index  posizione nella matrice data[][]
	 * @param values array dei valori distinti del dominio dell'attributo
	 */
	public DiscreteAttribute(String name, int index, String[] values) {
		super(name, index);
		this.values = values;
	}

	/**
	 * Restituisce il numero di valori distinti del dominio (cardinalità).
	 * Utile per iterare sui valori possibili durante la costruzione dell'albero.
	 *
	 * @return lunghezza dell'array values[]
	 */
	public int getNumberOfDistinctValues() {
		return values.length;
	}

	/**
	 * Restituisce il valore discreto alla posizione i nell'array values[].
	 *
	 * @param i indice del valore da restituire
	 * @return stringa corrispondente al valore alla posizione i
	 */
	public String getValue(int i) {
		return values[i];
	}
}
