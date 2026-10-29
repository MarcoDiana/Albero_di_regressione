import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;
import tree.UnknownValueException;
import utility.Keyboard;

/**
 * Classe di test per l'algoritmo di apprendimento dell'albero di regressione.
 *
 * Acquisisce il nome del file del training set, apprende un RegressionTree,
 * ne stampa regole e struttura e consente all'utente di predire il valore
 * di classe di nuovi esempi inseriti da tastiera.
 */
class MainTest {

	/**
	 * Punto di ingresso del programma.
	 * Gli errori di acquisizione del training set e le risposte non valide
	 * durante la predizione vengono segnalati all'utente senza interrompere l'esecuzione.
	 *
	 * @param args argomenti della riga di comando (non utilizzati)
	 */
	public static void main(String[] args) {
		System.out.println("Training set:");
		String fileName = Keyboard.readString();

		System.out.println("Starting data acquisition phase!");
		Data trainingSet;
		try {
			trainingSet = new Data(fileName);
		} catch (TrainingDataException e) {
			System.out.println(e);
			return;
		}

		System.out.println("Starting learning phase!");
		RegressionTree tree = new RegressionTree(trainingSet);
		tree.printRules();
		tree.printTree();

		char risp = 'y';
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
}
