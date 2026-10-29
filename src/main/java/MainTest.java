import java.io.IOException;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;
import tree.UnknownValueException;
import utility.Keyboard;

/**
 * Classe di test per l'algoritmo di apprendimento dell'albero di regressione.
 *
 * Un menù consente di scegliere se:
 *   1. apprendere un albero da un training set e salvarlo su file;
 *   2. caricare da file un albero precedentemente appreso.
 * In entrambi i casi l'albero ottenuto può essere usato per predire il valore
 * di classe di nuovi esempi inseriti da tastiera.
 */
class MainTest {

	/** Estensione del file in cui viene salvato l'albero appreso. */
	private static final String TREE_FILE_EXTENSION = ".dmp";

	/**
	 * Apprende un albero di regressione da un training set e lo salva su file.
	 * Il file ha il nome del training set (senza estensione) seguito da ".dmp".
	 *
	 * @return albero appreso, oppure null se il training set non è acquisibile
	 *         o l'albero non può essere salvato
	 */
	private static RegressionTree learnTree() {
		System.out.println("Training set:");
		String fileName = Keyboard.readString();

		System.out.println("Starting data acquisition phase!");
		Data trainingSet;
		try {
			trainingSet = new Data(fileName);
		} catch (TrainingDataException e) {
			System.out.println(e);
			return null;
		}

		System.out.println("Starting learning phase!");
		RegressionTree tree = new RegressionTree(trainingSet);
		tree.printRules();
		tree.printTree();

		int dot = fileName.lastIndexOf('.');
		String treeFile = (dot > 0 ? fileName.substring(0, dot) : fileName) + TREE_FILE_EXTENSION;
		try {
			tree.salva(treeFile);
			System.out.println("Tree saved in " + treeFile);
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
		return tree;
	}

	/**
	 * Carica da file un albero di regressione precedentemente salvato.
	 *
	 * @return albero caricato, oppure null se il file non è leggibile
	 */
	private static RegressionTree loadTree() {
		System.out.println("Tree file:");
		String fileName = Keyboard.readString();
		try {
			RegressionTree tree = RegressionTree.carica(fileName);
			tree.printRules();
			tree.printTree();
			return tree;
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e);
		}
		return null;
	}

	/**
	 * Fase di predizione: acquisisce da tastiera i valori di nuovi esempi
	 * e ne visualizza il valore di classe predetto, finché l'utente lo desidera.
	 * Le risposte non valide sono segnalate senza interrompere l'esecuzione.
	 *
	 * @param tree albero di regressione da usare per la predizione
	 */
	private static void predict(RegressionTree tree) {
		char risp;
		do {
			System.out.println("Starting prediction phase!");
			try {
				System.out.println(tree.predictClass());
			} catch (UnknownValueException e) {
				System.out.println(e);
			}
			do {
				System.out.println("Would you repeat ? (y/n)");
				risp = Keyboard.readChar();
			} while (risp != 'y' && risp != 'n');
		} while (risp == 'y');
	}

	/**
	 * Punto di ingresso del programma: mostra il menù di scelta e,
	 * se l'albero è disponibile, avvia la fase di predizione.
	 *
	 * @param args argomenti della riga di comando (non utilizzati)
	 */
	public static void main(String[] args) {
		int scelta;
		do {
			System.out.println("Learn Regression Tree from data [1]");
			System.out.println("Load Regression Tree from archive [2]");
			scelta = Keyboard.readInt();
		} while (scelta != 1 && scelta != 2);

		RegressionTree tree = (scelta == 1) ? learnTree() : loadTree();
		if (tree != null)
			predict(tree);
	}
}
