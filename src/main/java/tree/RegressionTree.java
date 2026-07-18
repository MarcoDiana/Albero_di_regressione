package tree;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * La classe RegressionTree modella l'entità dell'intero albero di decisione
 * (di regressione) come un insieme di sotto-alberi, applicando di fatto il Pattern Composite.
 */
public class RegressionTree {

    /** Radice del sotto-albero corrente (può essere uno SplitNode o un LeafNode). */
    private Node root;

    /** Array di sotto-alberi originanti nel nodo root (un elemento per ogni figlio del nodo). */
    private RegressionTree[] childTree;

    /**
     * Costruttore di default. Istanzia un sotto-albero "vuoto" dell'intero albero,
     * che verrà poi popolato tramite l'algoritmo di induzione.
     */
    public RegressionTree() {
        // Costruttore vuoto come da specifiche
    }

    /**
     * Costruttore principale. Avvia l'induzione dell'albero a partire dagli esempi
     * di training in input. Il limite minimo di esempi per foglia è fissato al 10%
     * della grandezza totale del training set.
     *
     * @param trainingSet Oggetto contenente il training set complessivo
     */
    public RegressionTree(Data trainingSet) {
        int numberOfExamplesPerLeaf = (int) (trainingSet.getNumberOfExamples() * 0.10);
        learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1, numberOfExamplesPerLeaf);
    }

    /**
     * Verifica se il sotto-insieme corrente può essere trasformato in un nodo foglia,
     * controllando se il numero di esempi che contiene è minore o uguale alla soglia.
     *
     * @param trainingSet             Training set complessivo
     * @param begin                   Indice iniziale della porzione
     * @param end                     Indice finale della porzione
     * @param numberOfExamplesPerLeaf Numero minimo/massimo che una foglia deve contenere per fermare lo split
     * @return true se deve diventare foglia, false altrimenti
     */
    private boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
        int currentExamples = end - begin + 1;
        return currentExamples <= numberOfExamplesPerLeaf;
    }

    /**
     * Determina e restituisce il nodo di split migliore per il sotto-insieme corrente,
     * cioè quello che minimizza la varianza (miglior Information Gain).
     *
     * @param trainingSet Training set complessivo
     * @param begin       Indice iniziale della porzione
     * @param end         Indice finale della porzione
     * @return Il nodo SplitNode ottimale individuato
     */
    private SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
        SplitNode bestSplit = null;
        double minVariance = Double.MAX_VALUE;

        int numAttributes = trainingSet.getNumberOfExplanatoryAttributes();

        // Testa tutti gli attributi indipendenti per trovare lo split migliore
        for (int i = 0; i < numAttributes; i++) {
            Attribute attr = trainingSet.getExplanatoryAttribute(i);

            // In questa esercitazione assumiamo attributi discreti
            DiscreteNode currentNode = new DiscreteNode(trainingSet, begin, end, (DiscreteAttribute) attr);

            // Se la varianza dello split è inferiore alla minima trovata finora, aggiorna
            if (currentNode.getVariance() < minVariance) {
                minVariance = currentNode.getVariance();
                bestSplit = currentNode;
            }
        }

        // Una volta trovato il miglior split, bisogna riordinare definitivamente
        // la matrice dei dati rispetto all'attributo vincente
        trainingSet.sort(bestSplit.getAttribute(), begin, end);

        return bestSplit;
    }

    /**
     * Motore ricorsivo per l'induzione dell'albero (Algoritmo Top-Down).
     *
     * @param trainingSet             Training set complessivo
     * @param begin                   Indice iniziale della porzione
     * @param end                     Indice finale della porzione
     * @param numberOfExamplesPerLeaf Soglia di arresto
     */
    private void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
        // Controllo condizione di terminazione: diventiamo una foglia?
        if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)) {
            this.root = new LeafNode(trainingSet, begin, end);
        } else {
            // Altrimenti creiamo un nodo di divisione
            this.root = determineBestSplitNode(trainingSet, begin, end);

            // Ricorsione sui figli
            if (this.root.getNumberOfChildren() > 0) {
                this.childTree = new RegressionTree[this.root.getNumberOfChildren()];

                for (int i = 0; i < this.root.getNumberOfChildren(); i++) {
                    this.childTree[i] = new RegressionTree();
                    SplitNode.SplitInfo info = ((SplitNode) this.root).getSplitInfo(i);

                    // Invocazione ricorsiva sul sotto-insieme assegnato al figlio i-esimo
                    this.childTree[i].learnTree(trainingSet, info.getBeginIndex(), info.getEndIndex(), numberOfExamplesPerLeaf);
                }
            } else {
                // Raro caso: lo split non ha generato figli, si forza a nodo foglia
                this.root = new LeafNode(trainingSet, begin, end);
            }
        }
    }

    /**
     * Scandisce ciascun ramo dell'albero e concatena le informazioni per stampare a video
     * i percorsi logici che portano alle predizioni finali (regole "IF... THEN...").
     */
    public void printRules() {
        printRules("");
    }

    /**
     * Metodo di supporto ricorsivo per la stampa delle regole formattate (AND).
     *
     * @param current Stringa che accumula il percorso corrente durante la ricorsione
     */
    private void printRules(String current) {
        if (root instanceof LeafNode) {
            // Se siamo arrivati in fondo, stampa la condizione e la classe predetta
            System.out.println(current + " ==> Class=" + ((LeafNode) root).getPredictedClassValue());
        } else {
            // Se siamo in un bivio, aggiungi il pezzo di condizione e continua a scendere
            SplitNode splitNode = (SplitNode) root;
            Attribute attribute = splitNode.getAttribute();

            for (int i = 0; i < childTree.length; i++) {
                SplitNode.SplitInfo splitInfo = splitNode.getSplitInfo(i);
                String newCurrent = current;

                if (current.isEmpty()) {
                    newCurrent = attribute.getName() + splitInfo.getComparator() + splitInfo.getSplitValue();
                } else {
                    newCurrent += " AND " + attribute.getName() + splitInfo.getComparator() + splitInfo.getSplitValue();
                }

                childTree[i].printRules(newCurrent);
            }
        }
    }

    /**
     * Restituisce una stringa formattata con tutte le informazioni strutturali dell'albero
     * (inclusa l'intestazione).
     *
     * @return L'intero albero in formato testuale
     */
    public String printTree() {
        return "********* TREE **********\n" + this.toString() + "*************************\n";
    }

    /**
     * Metodo ricorsivo che costruisce la stringa con i dettagli dell'albero
     * navigando in profondità nei sotto-alberi childTree.
     *
     * @return Stringa descrittiva dei nodi
     */
    @Override
    public String toString() {
        StringBuilder tree = new StringBuilder();
        tree.append(root.toString());

        if (root instanceof SplitNode) {
            for (int i = 0; i < childTree.length; i++) {
                tree.append(childTree[i].toString());
            }
        }
        return tree.toString();
    }
}