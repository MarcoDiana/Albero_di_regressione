package data;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rappresenta un attributo discreto del training set.
 * Un attributo discreto ha un dominio finito e noto di valori ammissibili,
 * memorizzati in un insieme ordinato (TreeSet) privo di duplicati.
 * Implementa Iterable per consentire di scandire i valori con un ciclo for-each.
 *
 * Esempio (da servo.dat):
 *   motor con valori {A, B, C, D, E}
 *   pgain con valori {3, 4, 5, 6}
 *
 * @author Marco Diana
 * @version 1.0
 */
public class DiscreteAttribute extends Attribute implements Iterable<String> {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/** Insieme ordinato dei valori distinti che l'attributo può assumere. */
	private Set<String> values = new TreeSet<String>();

	/**
	 * Costruttore: invoca il costruttore della superclasse e inserisce
	 * nell'insieme i valori discreti ammissibili.
	 *
	 * @param name   nome simbolico dell'attributo
	 * @param index  posizione nella matrice data[][]
	 * @param values valori del dominio dell'attributo (gli eventuali duplicati vengono scartati)
	 */
	public DiscreteAttribute(String name, int index, String[] values) {
		super(name, index);
		for (int i = 0; i < values.length; i++)
			this.values.add(values[i]);
	}

	/**
	 * Restituisce il numero di valori distinti del dominio (cardinalità).
	 *
	 * @return cardinalità dell'insieme dei valori
	 */
	public int getNumberOfDistinctValues() {
		return values.size();
	}

	/**
	 * Restituisce un iteratore sui valori del dominio, in ordine lessicografico.
	 *
	 * @return iteratore sui valori discreti dell'attributo
	 */
	@Override
	public Iterator<String> iterator() {
		return values.iterator();
	}
}
