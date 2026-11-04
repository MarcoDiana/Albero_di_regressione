package data;

/**
 * Rappresenta un attributo continuo (a valori reali) del training set.
 * A differenza di DiscreteAttribute, non memorizza un dominio finito di valori
 * perché un attributo continuo può assumere qualsiasi valore numerico reale.
 *
 * È usato per l'attributo di classe (target), che è il valore numerico da predire,
 * e per gli attributi indipendenti numerici, che originano nodi di split continui.
 */
public class ContinuousAttribute extends Attribute {

	/**
	 * Costruttore: invoca il costruttore della superclasse.
	 * Non aggiunge attributi propri perché non è necessario
	 * vincolare il dominio a un insieme finito di valori.
	 *
	 * @param name  nome simbolico dell'attributo (es. "class")
	 * @param index posizione nella matrice data[][]
	 */
	public ContinuousAttribute(String name, int index) {
		super(name, index);
	}
}
