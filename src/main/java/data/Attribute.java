package data;

/**
 * Classe astratta che modella un generico attributo del training set.
 * Funge da superclasse comune per DiscreteAttribute e ContinuousAttribute,
 * permettendo di trattare attributi di tipo diverso in modo uniforme
 * (es. nella lista explanatorySet di Data).
 */
public abstract class Attribute {

	/** Nome simbolico dell'attributo (es. "motor", "class"). */
	private String name;

	/**
	 * Indice numerico che identifica la posizione dell'attributo
	 * nella matrice data[][] della classe Data.
	 */
	private int index;

	/**
	 * Costruttore: inizializza nome e indice dell'attributo.
	 * Accessibile dalle sottoclassi tramite super(name, index).
	 *
	 * @param name  nome simbolico dell'attributo
	 * @param index posizione nella matrice data[][]
	 */
	protected Attribute(String name, int index) {
		this.name = name;
		this.index = index;
	}

	/**
	 * Restituisce il nome simbolico dell'attributo.
	 *
	 * @return nome dell'attributo
	 */
	public String getName() {
		return name;
	}

	/**
	 * Restituisce l'indice numerico dell'attributo.
	 *
	 * @return indice dell'attributo nella matrice data[][]
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Restituisce una rappresentazione testuale dell'attributo.
	 * Necessario per il corretto funzionamento di Data.main(), che stampa:
	 *   System.out.println("ORDER BY " + trainingSet.getExplanatoryAttribute(j))
	 * Senza questo override, l'operatore + invocherebbe Object.toString()
	 * producendo l'indirizzo dell'oggetto invece del nome dell'attributo.
	 *
	 * @return nome dell'attributo
	 */
	public String toString() {
		return name;
	}
}
