package tree;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * Classe astratta che modella un nodo interno (di split) dell'albero di regressione.
 * Estende Node aggiungendo la logica di partizionamento: il sottoinsieme di esempi
 * viene diviso in più partizioni (figli) in base ai valori di un attributo.
 *
 * Contiene la classe interna SplitInfo che descrive ogni singola partizione
 * (figlio) prodotta dallo split: valore discriminante, range di esempi, indice del figlio.
 *
 * Le sottoclassi concrete (es. DiscreteNode) implementano:
 *   - setSplitInfo(): come ricavare le partizioni per uno specifico tipo di attributo
 *   - testCondition(): su quale ramo instradare un nuovo esempio
 */
abstract class SplitNode extends Node {

	/**
	 * Classe interna che raccoglie le informazioni descrittive di una singola
	 * partizione (figlio) prodotta dallo split.
	 * Ogni SplitInfo corrisponde a un ramo dell'albero che parte da questo nodo.
	 */
	class SplitInfo {
		/** Valore dell'attributo che caratterizza questa partizione. */
		Object splitValue;

		/** Primo indice degli esempi appartenenti a questa partizione (incluso). */
		int beginIndex;

		/** Ultimo indice degli esempi appartenenti a questa partizione (incluso). */
		int endIndex;

		/** Numero (indice) del figlio: 0 per il primo ramo, 1 per il secondo, ecc. */
		int numberChild;

		/**
		 * Operatore di confronto usato nella formulazione della regola.
		 * Per attributi discreti è sempre "=" (valore uguale).
		 * Per attributi continui potrebbe essere "<=" o ">".
		 */
		String comparator = "=";

		/**
		 * Costruttore base: usato per split discreti dove il comparatore è sempre "=".
		 *
		 * @param splitValue  valore che identifica questa partizione
		 * @param beginIndex  primo indice del range (incluso)
		 * @param endIndex    ultimo indice del range (incluso)
		 * @param numberChild indice del figlio (0-based)
		 */
		SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild) {
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
		}

		/**
		 * Costruttore esteso: permette di specificare un comparatore personalizzato.
		 * Utile per attributi continui (es. "<=", ">").
		 *
		 * @param splitValue  valore che identifica questa partizione
		 * @param beginIndex  primo indice del range (incluso)
		 * @param endIndex    ultimo indice del range (incluso)
		 * @param numberChild indice del figlio (0-based)
		 * @param comparator  operatore di confronto ("=", "<=", ">", ...)
		 */
		SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild, String comparator) {
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
			this.comparator = comparator;
		}

		/** Restituisce il primo indice del range di esempi di questa partizione. */
		int getBeginindex() {
			return beginIndex;
		}

		/** Restituisce l'ultimo indice del range di esempi di questa partizione. */
		int getEndIndex() {
			return endIndex;
		}

		/** Restituisce il valore dell'attributo che discrimina questa partizione. */
		Object getSplitValue() {
			return splitValue;
		}

		/** Restituisce l'operatore di confronto ("=" per discreti). */
		String getComparator() {
			return comparator;
		}

		/**
		 * Rappresentazione testuale della partizione: indice del figlio,
		 * valore discriminante e range di esempi.
		 */
		public String toString() {
			return "child " + numberChild + " split value" + comparator + splitValue
					+ "[Examples:" + beginIndex + "-" + endIndex + "]";
		}
	}

	// -------------------------------------------------------------------------
	// Campi di SplitNode
	// -------------------------------------------------------------------------

	/** Attributo su cui viene effettuato lo split. */
	Attribute attribute;

	/**
	 * Array delle partizioni prodotte dallo split.
	 * mapSplit[i] descrive il range di esempi e il valore discriminante
	 * per il figlio i-esimo. Popolato da setSplitInfo() nelle sottoclassi.
	 */
	SplitInfo[] mapSplit;

	/**
	 * Varianza totale dello split: somma degli SSE dei nodi figli.
	 * Più è basso questo valore, più lo split è "buono" (omogeneizza le partizioni).
	 * Usato da determineBestSplitNode() in RegressionTree per confrontare split alternativi.
	 */
	double splitVariance;

	// -------------------------------------------------------------------------
	// Metodi astratti da implementare nelle sottoclassi
	// -------------------------------------------------------------------------

	/**
	 * Popola mapSplit[] con le informazioni sulle partizioni dello split.
	 * Implementato differentemente per attributi discreti e continui.
	 *
	 * @param trainingSet       dataset (già ordinato per attributo nel range)
	 * @param beginExampelIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo su cui si sta splittando
	 */
	abstract void setSplitInfo(Data trainingSet, int beginExampelIndex, int endExampleIndex, Attribute attribute);

	/**
	 * Determina su quale ramo instradare un esempio dato il valore del suo attributo.
	 *
	 * @param value valore dell'attributo per l'esempio da classificare
	 * @return indice del figlio corrispondente (0-based), o -1 se non trovato
	 */
	abstract int testCondition(Object value);

	// -------------------------------------------------------------------------
	// Costruttore
	// -------------------------------------------------------------------------

	/**
	 * Costruttore: inizializza il nodo di split.
	 *   1. Chiama Node() per calcolare SSE e assegnare l'ID al nodo
	 *   2. Ordina il sottoinsieme di esempi per l'attributo dato (necessario per setSplitInfo)
	 *   3. Invoca setSplitInfo() (implementato nelle sottoclassi) per popolare mapSplit[]
	 *   4. Calcola splitVariance come somma degli SSE delle foglie dei figli
	 *
	 * Nota: per calcolare la varianza di ogni figlio si creano istanze temporanee
	 * di LeafNode (che computano l'SSE nel loro costruttore).
	 *
	 * @param trainingSet       dataset contenente gli esempi
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 * @param attribute         attributo su cui effettuare lo split
	 */
	SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex);
		this.attribute = attribute;

		// Ordina i dati per l'attributo: garantisce che esempi con stesso valore siano contigui
		trainingSet.sort(attribute, beginExampleIndex, endExampleIndex);

		// Popola mapSplit[] con le partizioni trovate dopo l'ordinamento
		setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

		// Calcola la varianza totale dello split: somma degli SSE di ogni figlio
		// Usa LeafNode temporanei perché essi calcolano l'SSE nel costruttore
		splitVariance = 0;
		for (int i = 0; i < mapSplit.length; i++) {
			double localVariance = new LeafNode(trainingSet, mapSplit[i].getBeginindex(),
					mapSplit[i].getEndIndex()).getVariance();
			splitVariance += localVariance;
		}
	}

	// -------------------------------------------------------------------------
	// Metodi concreti
	// -------------------------------------------------------------------------

	/** Restituisce l'attributo su cui è stato effettuato lo split. */
	Attribute getAttribute() {
		return attribute;
	}

	/**
	 * Restituisce la varianza totale dello split (somma SSE dei figli).
	 * Override di Node.getVariance(): in un nodo di split la varianza
	 * rilevante è quella aggregata dei figli, non quella del nodo corrente.
	 */
	double getVariance() {
		return splitVariance;
	}

	/** Restituisce il numero di partizioni (figli) prodotte dallo split. */
	int getNumberOfChildren() {
		return mapSplit.length;
	}

	/**
	 * Restituisce le informazioni sulla partizione del figlio i-esimo.
	 *
	 * @param child indice del figlio (0-based)
	 * @return oggetto SplitInfo con range e valore discriminante del figlio
	 */
	SplitInfo getSplitInfo(int child) {
		return mapSplit[child];
	}

	/**
	 * Restituisce una stringa che rappresenta la "domanda" dello split,
	 * utile per mostrare le condizioni all'utente o in un'interfaccia grafica.
	 * Formato per ogni partizione: "i: attributo=valore\n"
	 *
	 * @return stringa con tutte le opzioni dello split
	 */
	String formulateQuery() {
		String query = "";
		for (int i = 0; i < mapSplit.length; i++)
			query += (i + ":" + attribute + mapSplit[i].getComparator() + mapSplit[i].getSplitValue()) + "\n";
		return query;
	}

	/**
	 * Rappresentazione testuale del nodo di split: mostra attributo, info del nodo (da Node),
	 * varianza dello split e l'elenco di tutte le partizioni.
	 */
	public String toString() {
		String v = "SPLIT : attribute=" + attribute + " " + super.toString()
				+ " Split Variance: " + getVariance() + "\n";
		for (int i = 0; i < mapSplit.length; i++) {
			v += "\t" + mapSplit[i] + "\n";
		}
		return v;
	}
}
