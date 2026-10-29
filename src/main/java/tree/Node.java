package tree;

import java.io.Serializable;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * Classe astratta che modella un generico nodo dell'albero di regressione.
 * Ogni nodo (interno o foglia) tiene traccia del sottoinsieme di esempi
 * che gli compete (range [beginExampleIndex, endExampleIndex] nella matrice Data)
 * e della varianza (SSE) di quel sottoinsieme rispetto all'attributo di classe.
 *
 * La varianza SSE è la misura di eterogeneità usata dall'algoritmo:
 *   SSE(S) = Σ(yi - ȳ)² = Σyi² - (Σyi)²/|S|
 * Un nodo con SSE=0 contiene esempi con identico valore di classe → foglia ideale.
 */
abstract class Node implements Serializable {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Contatore statico condiviso tra tutte le istanze.
	 * Ogni nuovo nodo riceve un identificatore unico crescente.
	 */
	static int idNodeCount = 0;

	/** Identificatore univoco del nodo, assegnato al momento della creazione. */
	int idNode;

	/** Primo indice (incluso) del sottoinsieme di esempi gestito da questo nodo. */
	int beginExampleIndex;

	/** Ultimo indice (incluso) del sottoinsieme di esempi gestito da questo nodo. */
	int endExampleIndex;

	/**
	 * Varianza SSE (Sum of Squared Errors) calcolata sull'attributo di classe
	 * per il sottoinsieme [beginExampleIndex, endExampleIndex].
	 * Misura quanto sono eterogenei i valori target in questo nodo.
	 */
	double variance;

	/**
	 * Costruttore: assegna un ID univoco al nodo, memorizza il range di esempi
	 * e calcola la varianza SSE sull'attributo di classe nel range dato.
	 *
	 * Formula SSE equivalente (numericamente stabile):
	 *   SSE = Σyi² - (Σyi)² / n
	 *
	 * @param trainingSet       dataset contenente gli esempi
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 */
	Node(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
		this.idNode = idNodeCount++;
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;

		// Calcola SSE con la formula Σyi² - (Σyi)²/n
		// Più efficiente di calcolare prima la media e poi la somma degli scarti
		int n = endExampleIndex - beginExampleIndex + 1;
		double sum = 0;
		double sumSq = 0;
		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			double yi = trainingSet.getClassValue(i);
			sum += yi;
			sumSq += yi * yi;
		}
		variance = sumSq - (sum * sum) / n;
	}

	/**
	 * Restituisce l'identificatore univoco del nodo.
	 *
	 * @return ID del nodo
	 */
	int getIdNode() {
		return idNode;
	}

	/**
	 * Restituisce il primo indice del sottoinsieme di esempi.
	 *
	 * @return beginExampleIndex
	 */
	int getBeginExampleIndex() {
		return beginExampleIndex;
	}

	/**
	 * Restituisce l'ultimo indice del sottoinsieme di esempi.
	 *
	 * @return endExampleIndex
	 */
	int getEndExampleIndex() {
		return endExampleIndex;
	}

	/**
	 * Restituisce la varianza SSE del sottoinsieme di esempi del nodo.
	 * Nelle sottoclassi può essere ridefinito (es. SplitNode restituisce
	 * la somma delle varianze dei figli anziché quella del nodo corrente).
	 *
	 * @return varianza SSE
	 */
	double getVariance() {
		return variance;
	}

	/**
	 * Restituisce il numero di figli del nodo.
	 * I nodi foglia restituiranno 0; i nodi di split restituiranno
	 * il numero di partizioni generate dallo split.
	 *
	 * @return numero di nodi figli
	 */
	abstract int getNumberOfChildren();

	/**
	 * Rappresentazione testuale del nodo: range di esempi e varianza SSE.
	 * Usato come base da SplitNode e LeafNode nel loro toString().
	 *
	 * @return stringa nel formato "[Examples:begin-end] variance:val"
	 */
	public String toString() {
		return "[Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance:" + variance;
	}
}
