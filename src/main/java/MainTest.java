import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/**
 * Classe di test per l'algoritmo di apprendimento dell'albero di regressione.
 *
 * Carica il dataset prova.dat (2 attributi discreti X e Y, 15 esempi),
 * apprende un RegressionTree e ne stampa:
 *   1. Le regole IF-THEN estratte (printRules)
 *   2. La struttura completa dell'albero con le informazioni dei nodi (printTree)
 */
class MainTest {

	/**
	 * Punto di ingresso del programma di test.
	 *
	 * @param args argomenti della riga di comando (non utilizzati)
	 * @throws TrainingDataException se il file prova.dat non è trovato nella directory corrente
	 */
	public static void main(String[] args) throws TrainingDataException {
		// Carica il training set da file
		Data trainingSet = new Data("prova.dat");

		// Apprende l'albero di regressione sul training set
		// La soglia di stop è fissata internamente al 10% degli esempi
		RegressionTree tree = new RegressionTree(trainingSet);

		// Stampa le regole IF-THEN estratte dall'albero
		tree.printRules();

		// Stampa la struttura completa dell'albero (nodi, range, varianze)
		tree.printTree();
	}
}
