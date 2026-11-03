package tree;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * Nodo di split discreto dell'albero di regressione.
 * Implementa uno split su un attributo discreto: il training set viene
 * partizionato in tanti sottoinsiemi quanti sono i valori distinti
 * dell'attributo presenti nel range corrente di esempi.
 *
 * Esempio con attributo "motor" (valori A,B,C,D,E):
 *   se nel range corrente compaiono solo A e B, lo split produce 2 figli.
 */
class DiscreteNode extends SplitNode {

	/**
	 * Costruttore: delega a SplitNode, che ordina i dati per l'attributo,
	 * invoca setSplitInfo() e calcola la varianza dello split.
	 *
	 * @param trainingSet       training set contenente gli esempi
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo discreto su cui effettuare lo split
	 */
	DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
	}

	/**
	 * Popola mapSplit con le informazioni sulle partizioni dello split.
	 *
	 * Presupposto: i dati nel range [beginExampleIndex, endExampleIndex] sono già
	 * ordinati per l'attributo (garantito da SplitNode). Gli esempi con lo stesso
	 * valore sono quindi contigui: basta scorrere il range una volta sola per
	 * individuare i confini di ogni partizione.
	 *
	 * Per ogni valore distinto trovato si crea uno SplitInfo con:
	 *   - splitValue: il valore dell'attributo
	 *   - beginIndex/endIndex: estremi del blocco di esempi con quel valore
	 *   - numberChild: indice progressivo del figlio (0, 1, 2, ...)
	 *
	 * @param trainingSet       training set (già ordinato per l'attributo nel range)
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo su cui si sta effettuando lo split
	 */
	@Override
	protected void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
		int rangeStart = beginExampleIndex;
		String currentValue = (String) trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

		for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
			String val = (String) trainingSet.getExplanatoryValue(i, attribute.getIndex());
			if (!val.equals(currentValue)) {
				// Cambio di valore: chiude il blocco precedente e ne apre uno nuovo
				mapSplit.add(new SplitInfo(currentValue, rangeStart, i - 1, mapSplit.size()));
				rangeStart = i;
				currentValue = val;
			}
		}
		// Ultimo blocco, non chiuso dal ciclo
		mapSplit.add(new SplitInfo(currentValue, rangeStart, endExampleIndex, mapSplit.size()));
	}

	/**
	 * Verifica su quale ramo instradare un esempio dato il valore del suo attributo.
	 * Confronta il valore con lo splitValue di ogni SplitInfo e restituisce
	 * l'indice del figlio corrispondente.
	 *
	 * @param value valore dell'attributo discreto dell'esempio da classificare
	 * @return indice del figlio (0-based) che corrisponde al valore, o -1 se non trovato
	 */
	@Override
	int testCondition(Object value) {
		for (SplitInfo info : mapSplit) {
			if (value.equals(info.getSplitValue()))
				return info.getNumberChild();
		}
		// Valore assente dal training set: nessun ramo applicabile
		return -1;
	}

	/**
	 * Rappresentazione testuale del nodo di split discreto.
	 * Aggiunge il prefisso "DISCRETE" alla stringa prodotta da SplitNode.toString()
	 * per distinguere questo tipo di nodo dagli split continui.
	 *
	 * @return stringa nel formato "DISCRETE SPLIT : attribute=X [...] Split Variance: val",
	 *         seguita da una riga per ogni partizione
	 */
	@Override
	public String toString() {
		return super.toString().replaceFirst("SPLIT :", "DISCRETE SPLIT :");
	}
}
