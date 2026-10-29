/**
 * Albero di regressione appreso induttivamente da un training set.
 *
 * La struttura è ricorsiva: ogni RegressionTree ha un nodo radice (root)
 * che può essere un LeafNode (foglia) o un SplitNode (nodo interno).
 * Se è un nodo interno, childTree[] contiene un sotto-albero per ogni partizione.
 *
 * Algoritmo di apprendimento (learnTree):
 *   1. Se il sottoinsieme di esempi è abbastanza piccolo (isLeaf) → foglia
 *   2. Altrimenti cerca il miglior attributo di split (determineBestSplitNode)
 *   3. Se lo split produce più di 1 partizione → ricorsione sui figli
 *   4. Altrimenti (tutti gli esempi hanno lo stesso valore per tutti gli attributi) → foglia
 *
 * Criterio di split: minimizzazione della varianza totale (somma degli SSE dei figli).
 * Soglia di stop: il sottoinsieme ha al più il 10% degli esempi del training set.
 */
class RegressionTree {

	/**
	 * Nodo radice del sotto-albero corrente.
	 * È un LeafNode se questo albero è una foglia,
	 * oppure un SplitNode (DiscreteNode) se è un nodo interno.
	 */
	Node root;

	/**
	 * Array dei sotto-alberi figli, uno per ogni partizione del nodo radice.
	 * È null se root è un LeafNode (le foglie non hanno figli).
	 */
	RegressionTree[] childTree;

	// -------------------------------------------------------------------------
	// Costruttori
	// -------------------------------------------------------------------------

	/**
	 * Costruttore senza argomenti: crea un albero vuoto.
	 * Usato internamente da learnTree() per istanziare i sotto-alberi figli
	 * prima di chiamare ricorsivamente learnTree() su di essi.
	 */
	RegressionTree() {
	}

	/**
	 * Costruttore principale: avvia l'apprendimento dell'albero sull'intero training set.
	 * La soglia di stop è fissata al 10% della dimensione del dataset (arrotondamento per difetto).
	 *
	 * @param trainingSet dataset di addestramento
	 */
	RegressionTree(Data trainingSet) {
		learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1,
				trainingSet.getNumberOfExamples() * 10 / 100);
	}

	// -------------------------------------------------------------------------
	// Algoritmo di apprendimento
	// -------------------------------------------------------------------------

	/**
	 * Costruisce ricorsivamente l'albero sul sottoinsieme [begin, end] del training set.
	 *
	 * Passi:
	 *   1. isLeaf? → crea una foglia con la media dei valori di classe del sottoinsieme
	 *   2. determineBestSplitNode → trova l'attributo che minimizza la varianza totale
	 *   3. Se il miglior split produce >1 partizione: crea i figli e ricorre
	 *   4. Se lo split produce solo 1 partizione (tutti i valori uguali): crea foglia
	 *
	 * @param trainingSet            dataset (modificato in-place dal sort)
	 * @param begin                  primo indice del sottoinsieme corrente (incluso)
	 * @param end                    ultimo indice del sottoinsieme corrente (incluso)
	 * @param numberOfExamplesPerLeaf soglia: se |sottoinsieme| <= soglia → foglia
	 */
	void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)) {
			// Il sottoinsieme è abbastanza piccolo: crea foglia con la media dei valori
			root = new LeafNode(trainingSet, begin, end);
		} else {
			// Cerca il miglior attributo su cui splittare
			root = determineBestSplitNode(trainingSet, begin, end);

			if (root.getNumberOfChildren() > 1) {
				// Lo split produce più partizioni: ricorre su ognuna
				childTree = new RegressionTree[root.getNumberOfChildren()];
				for (int i = 0; i < root.getNumberOfChildren(); i++) {
					childTree[i] = new RegressionTree();
					childTree[i].learnTree(trainingSet,
							((SplitNode) root).getSplitInfo(i).beginIndex,
							((SplitNode) root).getSplitInfo(i).endIndex,
							numberOfExamplesPerLeaf);
				}
			} else {
				// Lo split produce una sola partizione (es. tutti gli esempi hanno
				// lo stesso valore per ogni attributo): non ha senso splittare → foglia
				root = new LeafNode(trainingSet, begin, end);
			}
		}
	}

	/**
	 * Determina se il sottoinsieme corrente deve diventare una foglia.
	 * Condizione: il numero di esempi nel range è minore o uguale alla soglia.
	 *
	 * @param trainingSet            dataset
	 * @param begin                  primo indice del sottoinsieme (incluso)
	 * @param end                    ultimo indice del sottoinsieme (incluso)
	 * @param numberOfExamplesPerLeaf soglia di stop
	 * @return true se il sottoinsieme è abbastanza piccolo da diventare foglia
	 */
	boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		return (end - begin + 1) <= numberOfExamplesPerLeaf;
	}

	/**
	 * Cerca il miglior attributo di split tra tutti gli attributi esplicativi,
	 * quindi costruisce e restituisce il nodo di split corrispondente.
	 * "Migliore" significa: splitVariance minima (somma SSE dei figli più bassa).
	 *
	 * ATTENZIONE — effetto collaterale sul dataset:
	 * La creazione di ogni DiscreteNode ordina i dati per quell'attributo (in-place).
	 * Dopo il ciclo di valutazione, i dati risultano ordinati per l'ultimo attributo
	 * provato, NON per il migliore. Per questo motivo, dopo aver trovato l'indice
	 * del vincitore, il metodo ricrea un DiscreteNode per quell'attributo: il
	 * costruttore di SplitNode riordina i dati e ricostruisce mapSplit[] in modo
	 * coerente con il layout attuale, garantendo che i range in getSplitInfo()
	 * corrispondano effettivamente alla posizione degli esempi in memoria.
	 *
	 * @param trainingSet dataset (riordinato in-place dal metodo)
	 * @param begin       primo indice del sottoinsieme (incluso)
	 * @param end         ultimo indice del sottoinsieme (incluso)
	 * @return il SplitNode con la varianza totale più bassa, con dati allineati
	 */
	SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		// Fase 1: trova l'indice dell'attributo con varianza totale minima
		int bestIndex = 0;
		double bestVariance = Double.MAX_VALUE;
		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			// Costruisce il nodo candidato: ordina i dati e calcola la varianza
			DiscreteNode candidate = new DiscreteNode(trainingSet, begin, end,
					(DiscreteAttribute) trainingSet.getExplanatoryAttribute(i));
			if (candidate.getVariance() < bestVariance) {
				bestVariance = candidate.getVariance();
				bestIndex = i;
			}
		}
		// Fase 2: ricrea il nodo vincente per allineare i dati all'attributo migliore.
		// Dopo il ciclo i dati sono ordinati per l'ultimo attributo provato;
		// ricostruire il vincente ri-ordina i dati e aggiorna mapSplit[] correttamente.
		return new DiscreteNode(trainingSet, begin, end,
				(DiscreteAttribute) trainingSet.getExplanatoryAttribute(bestIndex));
	}

	// -------------------------------------------------------------------------
	// Stampa delle regole
	// -------------------------------------------------------------------------

	/**
	 * Stampa tutte le regole IF-THEN estratte dall'albero.
	 * Ogni percorso dalla radice a una foglia corrisponde a una regola:
	 *   X=A AND Y=B ==> Class=1.5
	 *
	 * Delega alla versione ricorsiva con stringa vuota come prefisso iniziale.
	 */
	void printRules() {
		System.out.println("********* RULES **********\n");
		printRules("");
		System.out.println("\n**************************\n");
	}

	/**
	 * Costruisce e stampa ricorsivamente le regole IF-THEN dell'albero.
	 *
	 * Se il nodo radice è una foglia: stampa la regola completa nel formato
	 *   "condizioni accumulatein current ==> Class=valore"
	 *
	 * Se il nodo radice è uno split: per ogni figlio aggiunge la condizione
	 *   "attributo=valore" alla stringa corrente e ricorre sul sotto-albero.
	 *
	 * @param current condizioni già accumulate percorrendo l'albero dalla radice
	 *                (es. "X=A AND Y=B" per una chiamata ricorsiva profonda)
	 */
	void printRules(String current) {
		if (root instanceof LeafNode) {
			// Foglia: costruisce e stampa la regola completa
			// Se current è vuoto (albero con un solo nodo), non stampa il prefisso
			String prefix = current.isEmpty() ? "" : current + " ==> ";
			System.out.println(prefix + "Class=" + ((LeafNode) root).getPredictedClassValue());
		} else {
			// Nodo interno: accoda la condizione di ogni figlio e ricorre
			SplitNode splitRoot = (SplitNode) root;
			for (int i = 0; i < childTree.length; i++) {
				// Condizione del ramo i: "nomeAttributo=valorePartizione"
				String condition = splitRoot.getAttribute().getName()
						+ splitRoot.getSplitInfo(i).getComparator()
						+ splitRoot.getSplitInfo(i).getSplitValue();
				// Concatena la condizione alle condizioni precedenti
				String newCurrent = current.isEmpty() ? condition : current + " AND " + condition;
				childTree[i].printRules(newCurrent);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Stampa dell'albero (forniti dal docente, con commenti aggiunti)
	// -------------------------------------------------------------------------

	/**
	 * Stampa la struttura completa dell'albero con intestazione e piè di pagina.
	 */
	void printTree() {
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}

	/**
	 * Rappresentazione testuale ricorsiva dell'albero.
	 * Stampa prima il nodo radice, poi i sotto-alberi figli (in profondità).
	 * Il doppio "\n" dopo root.toString() garantisce separazione visiva tra livelli.
	 *
	 * @return stringa che rappresenta l'intero (sotto-)albero
	 */
	public String toString() {
		// root.toString() già include "\n" finale (definito in LeafNode e SplitNode)
		String tree = root.toString() + "\n";
		if (root instanceof LeafNode) {
			// Foglia: nessun figlio da visitare
		} else {
			// Nodo interno: aggiunge ricorsivamente la rappresentazione di ogni figlio
			for (int i = 0; i < childTree.length; i++)
				tree += childTree[i]; // chiama childTree[i].toString()
		}
		return tree;
	}
}
