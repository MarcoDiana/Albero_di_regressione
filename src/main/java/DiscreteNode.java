/**
 * Nodo di split discreto dell'albero di regressione.
 * Implementa uno split su un attributo discreto: il dataset viene
 * partizionato in tanti sottoinsiemi quanti sono i valori distinti
 * dell'attributo presenti nel range corrente di esempi.
 *
 * Esempio con attributo "motor" (valori A,B,C,D,E):
 *   Se nel range corrente compaiono solo A e B, lo split produce 2 figli.
 *
 * Il criterio di scelta del miglior split è la riduzione della varianza:
 * si sceglie l'attributo che produce la somma di SSE più bassa tra i figli.
 */
class DiscreteNode extends SplitNode {

	/**
	 * Costruttore: delega a SplitNode, che a sua volta:
	 *   1. chiama Node() per calcolare SSE e assegnare l'ID
	 *   2. ordina i dati per attributo (trainingSet.sort)
	 *   3. invoca setSplitInfo() per popolare mapSplit[]
	 *   4. calcola splitVariance come somma degli SSE dei figli
	 *
	 * @param trainingSet       dataset contenente gli esempi
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo discreto su cui effettuare lo split
	 */
	DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
	}

	/**
	 * Popola l'array mapSplit[] con le informazioni sulle partizioni dello split.
	 *
	 * Presupposto: i dati nel range [beginExampleIndex, endExampleIndex] sono già
	 * ordinati per attributo (garantito da SplitNode prima di chiamare questo metodo).
	 * Quindi esempi con lo stesso valore dell'attributo sono contigui: basta scorrere
	 * il range una volta per trovare i confini di ogni partizione.
	 *
	 * Per ogni valore distinto trovato si crea un SplitInfo con:
	 *   - splitValue: il valore dell'attributo
	 *   - beginIndex/endIndex: estremi del blocco di esempi con quel valore
	 *   - numberChild: indice progressivo del figlio (0, 1, 2, ...)
	 *
	 * @param trainingSet       dataset (già ordinato per attributo nel range)
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo su cui si sta splittando
	 */
	void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {

		// Prima passata: conta i valori distinti presenti nel range
		// (potrebbero essere meno dei valori teorici dell'attributo)
		int distinctCount = 1;
		String currentValue = (String) trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());
		for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
			String val = (String) trainingSet.getExplanatoryValue(i, attribute.getIndex());
			if (!val.equals(currentValue)) {
				distinctCount++;
				currentValue = val;
			}
		}

		// Alloca l'array con il numero esatto di partizioni trovate
		mapSplit = new SplitInfo[distinctCount];

		// Seconda passata: crea un SplitInfo per ogni blocco contiguo di valore uguale
		int splitIdx = 0;
		int rangeStart = beginExampleIndex;
		currentValue = (String) trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

		for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
			String val = (String) trainingSet.getExplanatoryValue(i, attribute.getIndex());
			if (!val.equals(currentValue)) {
				// Trovato cambio di valore: chiude il blocco precedente
				mapSplit[splitIdx] = new SplitInfo(currentValue, rangeStart, i - 1, splitIdx);
				splitIdx++;
				rangeStart = i;
				currentValue = val;
			}
		}
		// Aggiunge l'ultimo blocco (non chiuso dal ciclo)
		mapSplit[splitIdx] = new SplitInfo(currentValue, rangeStart, endExampleIndex, splitIdx);
	}

	/**
	 * Testa su quale ramo inviare un esempio dato il valore del suo attributo.
	 * Confronta il valore ricevuto con il splitValue di ogni SplitInfo e
	 * restituisce l'indice del figlio corrispondente.
	 *
	 * @param value valore dell'attributo discreto dell'esempio da classificare
	 * @return indice del figlio (0-based) che corrisponde al valore, o -1 se non trovato
	 */
	int testCondition(Object value) {
		for (int i = 0; i < mapSplit.length; i++) {
			if (value.equals(mapSplit[i].getSplitValue())) {
				// numberChild è uguale a i nel nostro setSplitInfo (assegnato sequenzialmente)
				return mapSplit[i].numberChild;
			}
		}
		// Valore non presente nel training set: non dovrebbe accadere in fase di apprendimento
		return -1;
	}

	/**
	 * Rappresentazione testuale del nodo di split discreto.
	 * Aggiunge il prefisso "DISCRETE" alla stringa prodotta da SplitNode.toString()
	 * per distinguere visivamente questo tipo di nodo da eventuali altri split (es. continui).
	 *
	 * @return stringa nel formato "DISCRETE SPLIT : attribute=X [...] Split Variance: val"
	 *         seguita da una riga per ogni partizione (child)
	 */
	public String toString() {
		// Usa il formato di SplitNode ma specifica il tipo "DISCRETE"
		return super.toString().replaceFirst("SPLIT :", "DISCRETE SPLIT :");
	}
}
