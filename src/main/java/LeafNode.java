/**
 * Nodo foglia dell'albero di regressione.
 * Una foglia non genera ulteriori ramificazioni: contiene la predizione
 * del valore di classe per tutti gli esempi che vi affluiscono.
 *
 * Il valore predetto è la media aritmetica dei valori di classe
 * degli esempi nel range [beginExampleIndex, endExampleIndex].
 * Questa è la scelta ottimale (minimizza MSE locale) quando non si può
 * o non conviene splittare ulteriormente il sottoinsieme.
 */
class LeafNode extends Node {

	/**
	 * Valore di classe predetto per gli esempi che ricadono in questo nodo.
	 * Calcolato come media aritmetica dei valori target del sottoinsieme.
	 * È Double (non double) per poter restituire null in caso di nodo vuoto,
	 * coerentemente con getClassValue() di Data.
	 */
	Double predictedClassValue;

	/**
	 * Costruttore: invoca il costruttore di Node (calcola SSE e assegna ID),
	 * poi calcola il valore predetto come media dei valori di classe del sottoinsieme.
	 *
	 * @param trainingSet       dataset contenente gli esempi
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 */
	LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
		super(trainingSet, beginExampleIndex, endExampleIndex);

		// Calcola la media dei valori di classe: ȳ = Σyi / n
		// Questo valore minimizza l'MSE su questo sottoinsieme
		int n = endExampleIndex - beginExampleIndex + 1;
		double sum = 0;
		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			sum += trainingSet.getClassValue(i);
		}
		predictedClassValue = sum / n;
	}

	/**
	 * Restituisce il valore di classe predetto per gli esempi di questo nodo.
	 *
	 * @return media dei valori di classe del sottoinsieme
	 */
	Double getPredictedClassValue() {
		return predictedClassValue;
	}

	/**
	 * Un nodo foglia non ha figli: restituisce sempre 0.
	 *
	 * @return 0
	 */
	int getNumberOfChildren() {
		return 0;
	}

	/**
	 * Rappresentazione testuale del nodo foglia: mostra il valore predetto
	 * e le informazioni di base (range esempi, varianza) ereditate da Node.
	 *
	 * @return stringa nel formato "LEAF : class=val [Examples:begin-end] variance:sse"
	 */
	public String toString() {
		return "LEAF : class=" + predictedClassValue + " " + super.toString() + "\n";
	}
}
