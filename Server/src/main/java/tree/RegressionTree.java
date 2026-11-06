package tree;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import dto.PredictionStepDto;
import server.UnknownValueException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Albero di regressione appreso induttivamente da un training set.
 * <p>
 * La struttura è ricorsiva: ogni RegressionTree ha un nodo radice (root)
 * che può essere un LeafNode (foglia) o un SplitNode (nodo interno).
 * Se è un nodo interno, childTree[] contiene un sotto-albero per ogni partizione.
 * <p>
 * Algoritmo di apprendimento (learnTree):
 * 1. Se il sottoinsieme di esempi è abbastanza piccolo (isLeaf) → foglia
 * 2. Altrimenti cerca il miglior attributo di split (determineBestSplitNode)
 * 3. Se lo split produce più di 1 partizione → ricorsione sui figli
 * 4. Altrimenti (tutti gli esempi hanno lo stesso valore per tutti gli attributi) → foglia
 * <p>
 * Criterio di split: minimizzazione della varianza totale (somma degli SSE dei figli).
 * Soglia di stop: il sottoinsieme ha al più il 10% degli esempi del training set.
 */
public class RegressionTree implements Serializable {

    /**
     * Identificativo di versione per la serializzazione.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Nodo radice del sotto-albero corrente.
     * È un LeafNode se questo albero è una foglia,
     * oppure un SplitNode (DiscreteNode o ContinuousNode) se è un nodo interno.
     */
    private Node root;

    /**
     * Array dei sotto-alberi figli, uno per ogni partizione del nodo radice.
     * È null se root è un LeafNode (le foglie non hanno figli).
     */
    private RegressionTree[] childTree;

    /**
     * Costruttore senza argomenti: crea un albero vuoto.
     * Usato internamente da learnTree() per istanziare i sotto-alberi figli
     * prima di chiamare ricorsivamente learnTree() su di essi.
     */
    private RegressionTree() {
    }

    /**
     * Costruttore principale: avvia l'apprendimento dell'albero sull'intero training set.
     * La soglia di stop è fissata al 10% della dimensione del dataset (arrotondamento per difetto).
     *
     * @param trainingSet dataset di addestramento
     */
    public RegressionTree(Data trainingSet) {
        learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1,
                trainingSet.getNumberOfExamples() * 10 / 100);
    }

    /**
     * Costruisce ricorsivamente l'albero sul sottoinsieme [begin, end] del training set.
     * <p>
     * Passi:
     * 1. isLeaf? → crea una foglia con la media dei valori di classe del sottoinsieme
     * 2. determineBestSplitNode → trova l'attributo che minimizza la varianza totale
     * 3. Se il miglior split produce >1 partizione: crea i figli e ricorre
     * 4. Se lo split produce solo 1 partizione (tutti i valori uguali): crea foglia
     *
     * @param trainingSet             dataset (modificato in-place dal sort)
     * @param begin                   primo indice del sottoinsieme corrente (incluso)
     * @param end                     ultimo indice del sottoinsieme corrente (incluso)
     * @param numberOfExamplesPerLeaf soglia: se |sottoinsieme| &lt;= soglia &rarr; foglia
     */
    private void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
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
                            ((SplitNode) root).getSplitInfo(i).getBeginIndex(),
                            ((SplitNode) root).getSplitInfo(i).getEndIndex(),
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
     * @param trainingSet             dataset
     * @param begin                   primo indice del sottoinsieme (incluso)
     * @param end                     ultimo indice del sottoinsieme (incluso)
     * @param numberOfExamplesPerLeaf soglia di stop
     * @return true se il sottoinsieme è abbastanza piccolo da diventare foglia
     */
    private boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
        return (end - begin + 1) <= numberOfExamplesPerLeaf;
    }

    /**
     * Cerca il miglior attributo di split tra tutti gli attributi indipendenti,
     * quindi restituisce il nodo di split corrispondente.
     * "Migliore" significa: splitVariance minima (somma degli SSE dei figli più bassa).
     * <p>
     * I nodi di split generati per i diversi attributi sono raccolti in un TreeSet,
     * che li mantiene ordinati per varianza (SplitNode è Comparable): il primo
     * elemento è quindi il migliore.
     * <p>
     * Il tipo di nodo (DiscreteNode o ContinuousNode) è scelto a runtime in base al
     * tipo dell'attributo (RTTI).
     * <p>
     * La creazione di ogni nodo ordina il training set (in-place) per il proprio
     * attributo, perciò al termine il training set risulta ordinato per l'ultimo
     * attributo provato. Prima di restituire il nodo vincente si riordina il range
     * per il suo attributo, così che gli indici di mapSplit corrispondano
     * alla disposizione effettiva degli esempi.
     *
     * @param trainingSet training set (riordinato in-place dal metodo)
     * @param begin       primo indice del sottoinsieme (incluso)
     * @param end         ultimo indice del sottoinsieme (incluso)
     * @return nodo di split con la varianza più bassa, con gli esempi ordinati per il suo attributo
     */
    private SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
        TreeSet<SplitNode> ts = new TreeSet<SplitNode>();
        for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
            Attribute a = trainingSet.getExplanatoryAttribute(i);
            SplitNode currentNode;
            // RTTI: il tipo dell'attributo determina il tipo di nodo di split da istanziare
            if (a instanceof DiscreteAttribute)
                currentNode = new DiscreteNode(trainingSet, begin, end, (DiscreteAttribute) a);
            else
                currentNode = new ContinuousNode(trainingSet, begin, end, (ContinuousAttribute) a);
            ts.add(currentNode);
        }
        SplitNode bestNode = ts.first();
        trainingSet.sort(bestNode.getAttribute(), begin, end);
        return bestNode;
    }

    /**
     * Stampa tutte le regole IF-THEN estratte dall'albero.
     * Ogni percorso dalla radice a una foglia corrisponde a una regola:
     * X=A AND Y=B ==> Class=1.5
     * <p>
     * Delega alla versione ricorsiva con stringa vuota come prefisso iniziale.
     */
    public void printRules() {
        System.out.println("********* RULES **********\n");
        printRules("");
        System.out.println("\n**************************\n");
    }

    /**
     * Costruisce e stampa ricorsivamente le regole IF-THEN dell'albero.
     * <p>
     * Se il nodo radice è una foglia: stampa la regola completa nel formato
     * "condizioni accumulatein current ==> Class=valore"
     * <p>
     * Se il nodo radice è uno split: per ogni figlio aggiunge la condizione
     * "attributo=valore" alla stringa corrente e ricorre sul sotto-albero.
     *
     * @param current condizioni già accumulate percorrendo l'albero dalla radice
     *                (es. "X=A AND Y=B" per una chiamata ricorsiva profonda)
     */
    private void printRules(String current) {
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

    /**
     * Stampa la struttura completa dell'albero con intestazione e piè di pagina.
     */
    public void printTree() {
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
    @Override
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

    /**
     * Predice il valore di classe di un nuovo esempio interagendo con il client.
     * <p>
     * Se la radice corrente è una foglia restituisce il valore predetto; altrimenti
     * invia al client il messaggio {@code "QUERY"} seguito dalle condizioni dello split
     * ({@link SplitNode#formulateQuery()}), legge la scelta del ramo dal flusso di input
     * e prosegue ricorsivamente sul sotto-albero selezionato.
     *
     * @param out flusso di output verso il client
     * @param in  flusso di input dal client
     * @return valore di classe predetto per l'esempio
     * @throws UnknownValueException  se la risposta non individua un ramo valido del nodo di split
     * @throws IOException            se si verifica un errore di comunicazione con il client
     * @throws ClassNotFoundException se l'oggetto ricevuto dal client non è riconosciuto
     */
    public Double predictClass(ObjectOutputStream out, ObjectInputStream in)
            throws UnknownValueException, IOException, ClassNotFoundException {
        if (root instanceof LeafNode) {
            return ((LeafNode) root).getPredictedClassValue();
        } else {
            out.writeObject("QUERY");
            out.writeObject(((SplitNode) root).formulateQuery());
            int risp = Integer.parseInt(in.readObject().toString());
            if (risp < 0 || risp >= root.getNumberOfChildren())
                throw new UnknownValueException(
                        "The answer should be an integer between 0 and " + (root.getNumberOfChildren() - 1) + "!");
            return childTree[risp].predictClass(out, in);
        }
    }

    /**
     * Serializza l'albero (con i nodi e gli attributi che lo compongono) in un file.
     *
     * @param nomeFile nome del file in cui salvare l'albero
     * @throws FileNotFoundException se il file non può essere creato o aperto in scrittura
     * @throws IOException           se si verifica un errore durante la scrittura
     */
    public void salva(String nomeFile) throws FileNotFoundException, IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeFile))) {
            out.writeObject(this);
        }
    }
    /**
     * Carica da file un albero di regressione precedentemente salvato con salva().
     *
     * @param nomeFile nome del file in cui è salvato l'albero
     * @return albero di regressione contenuto nel file
     * @throws FileNotFoundException  se il file non esiste o non è leggibile
     * @throws IOException            se si verifica un errore durante la lettura
     * @throws ClassNotFoundException se il file contiene un oggetto di una classe sconosciuta
     */
    public static RegressionTree carica(String nomeFile)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeFile))) {
            return (RegressionTree) in.readObject();
        }
    }

	/**
	 * Naviga l'albero di regressione in modalita' stateless lungo la sequenza di indici
	 * di ramo specificata nel percorso {@code path}.
	 *
	 * Se il percorso conduce a un nodo foglia, restituisce un {@link PredictionStepDto}
	 * con {@code isLeaf = true} e il valore predetto calcolato dalla foglia.
	 * Se il percorso conduce a un nodo interno, restituisce un {@link PredictionStepDto}
	 * con {@code isLeaf = false}, il nome dell'attributo di split e le condizioni testuali
	 * di tutti i rami figli disponibili per la scelta successiva.
	 *
	 * @param path sequenza ordinata 0-based degli indici dei rami da percorrere a partire dalla radice;
	 *             se {@code null} o vuoto, valuta direttamente il nodo radice
	 * @return DTO descrittivo del nodo raggiunto al termine della navigazione
	 * @throws UnknownValueException se il percorso e' invalido (indice negativo, fuori limite
	 *                               o tentativo di proseguire oltre una foglia)
	 */
	public PredictionStepDto navigate(List<Integer> path) throws UnknownValueException {
		RegressionTree current = this;
		if (path != null) {
			for (Integer branchIndex : path) {
				if (branchIndex == null || !(current.root instanceof SplitNode)) {
					throw new UnknownValueException("Percorso non valido: il nodo corrente e' gia' una foglia o l'indice e' nullo.");
				}
				if (branchIndex < 0 || branchIndex >= current.childTree.length) {
					throw new UnknownValueException(
							"The answer should be an integer between 0 and " + (current.childTree.length - 1) + "!");
				}
				current = current.childTree[branchIndex];
			}
		}
		if (current.root instanceof LeafNode leaf) {
			return new PredictionStepDto(true, leaf.getPredictedClassValue(), null, List.of());
		}
		SplitNode splitNode = (SplitNode) current.root;
		List<String> options = new ArrayList<>(splitNode.getNumberOfChildren());
		for (int i = 0; i < splitNode.getNumberOfChildren(); i++) {
			SplitNode.SplitInfo info = splitNode.getSplitInfo(i);
			options.add(info.getComparator() + info.getSplitValue());
		}
		return new PredictionStepDto(false, null, splitNode.getAttribute().getName(), options);
	}

	/**
	 * Restituisce una rappresentazione testuale completa che combina la struttura
	 * dell'albero e tutte le regole di decisione IF-THEN estratte.
	 *
	 * @return stringa formattata con la struttura dell'albero e le regole
	 */
	public String getRules() {
		StringBuilder sb = new StringBuilder();
		sb.append("********* TREE **********\n\n");
		sb.append(toString());
		sb.append("\n*************************\n\n");
		sb.append("********* RULES **********\n\n");
		collectRules("", sb);
		sb.append("\n**************************\n");
		return sb.toString();
	}

	/**
	 * Metodo ausiliario ricorsivo che accumula le regole IF-THEN dell'albero in un StringBuilder.
	 *
	 * @param current condizioni gia' accumulate lungo il cammino dalla radice
	 * @param sb      buffer su cui accodare le regole
	 */
	private void collectRules(String current, StringBuilder sb) {
		if (root instanceof LeafNode leaf) {
			String prefix = current.isEmpty() ? "" : current + " ==> ";
			sb.append(prefix).append("Class=").append(leaf.getPredictedClassValue()).append("\n");
		} else {
			SplitNode splitRoot = (SplitNode) root;
			for (int i = 0; i < childTree.length; i++) {
				String condition = splitRoot.getAttribute().getName()
						+ splitRoot.getSplitInfo(i).getComparator()
						+ splitRoot.getSplitInfo(i).getSplitValue();
				String newCurrent = current.isEmpty() ? condition : current + " AND " + condition;
				childTree[i].collectRules(newCurrent, sb);
			}
		}
	}
}
