package tree;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;

/**
 * Nodo di split continuo dell'albero di regressione.
 * A differenza di un attributo discreto, che origina un ramo per ogni valore,
 * un attributo continuo origina al più due rami, separati da una soglia numerica:
 *   - ramo 0: esempi con valore &lt;= soglia
 *   - ramo 1: esempi con valore &gt; soglia
 *
 * La soglia scelta è quella che minimizza la somma degli SSE dei due rami.
 *
 * @author Marco Diana
 * @version 1.0
 */
class ContinuousNode extends SplitNode {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore: delega a SplitNode, che ordina i dati per l'attributo,
	 * invoca setSplitInfo() e calcola la varianza dello split.
	 *
	 * @param trainingSet       training set contenente gli esempi
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo continuo su cui effettuare lo split
	 */
	ContinuousNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, ContinuousAttribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
	}

	/**
	 * Determina la soglia di taglio ottimale e popola mapSplit con i due rami.
	 *
	 * Presupposto: i dati nel range sono ordinati per l'attributo, perciò le soglie
	 * candidate sono i valori in corrispondenza dei quali il valore cambia.
	 * Per ogni candidata si calcola la somma degli SSE delle due partizioni
	 * e si conserva la migliore.
	 *
	 * Se tutti gli esempi hanno lo stesso valore non esiste alcuna soglia valida:
	 * mapSplit contiene allora un solo ramo, che copre l'intero range.
	 *
	 * @param trainingSet       training set (già ordinato per l'attributo nel range)
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo su cui si sta effettuando lo split
	 */
	@Override
	protected void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
		Double currentSplitValue = (Double) trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());
		double bestVariance = Double.MAX_VALUE;
		Double bestSplitValue = null;
		int bestCut = -1;

		for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
			Double value = (Double) trainingSet.getExplanatoryValue(i, attribute.getIndex());
			if (value.doubleValue() != currentSplitValue.doubleValue()) {
				// Taglio candidato tra gli esempi i-1 e i: valuta la varianza dei due rami
				double candidateVariance = new LeafNode(trainingSet, beginExampleIndex, i - 1).getVariance()
						+ new LeafNode(trainingSet, i, endExampleIndex).getVariance();
				if (candidateVariance < bestVariance) {
					bestVariance = candidateVariance;
					bestSplitValue = currentSplitValue;
					bestCut = i;
				}
				currentSplitValue = value;
			}
		}

		if (bestCut < 0) {
			// Valore costante: nessuna soglia possibile, un unico ramo
			mapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex, endExampleIndex, 0, "<="));
		} else {
			mapSplit.add(new SplitInfo(bestSplitValue, beginExampleIndex, bestCut - 1, 0, "<="));
			mapSplit.add(new SplitInfo(bestSplitValue, bestCut, endExampleIndex, 1, ">"));
		}
	}

	/**
	 * Verifica su quale ramo instradare un esempio, confrontando il suo valore
	 * con la soglia di split secondo l'operatore del ramo (&lt;= oppure &gt;).
	 *
	 * @param value valore (Double) dell'attributo continuo dell'esempio da classificare
	 * @return indice del ramo (0-based) il cui test è positivo, o -1 se nessuno lo è
	 */
	@Override
	int testCondition(Object value) {
		double val = (Double) value;
		for (SplitInfo info : mapSplit) {
			double threshold = (Double) info.getSplitValue();
			if (info.getComparator().equals("<=") && val <= threshold)
				return info.getNumberChild();
			if (info.getComparator().equals(">") && val > threshold)
				return info.getNumberChild();
		}
		return -1;
	}

	/**
	 * Rappresentazione testuale del nodo di split continuo.
	 * Aggiunge il prefisso "CONTINUOUS" alla stringa prodotta da SplitNode.toString()
	 * per distinguere questo tipo di nodo dagli split discreti.
	 *
	 * @return stringa nel formato "CONTINUOUS SPLIT : attribute=Y [...] Split Variance: val",
	 *         seguita da una riga per ogni partizione
	 */
	@Override
	public String toString() {
		return super.toString().replaceFirst("SPLIT :", "CONTINUOUS SPLIT :");
	}
}
