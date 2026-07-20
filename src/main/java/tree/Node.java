package tree;

import data.Data;

/**
 * La classe astratta Node modella l'astrazione dell'entità nodo (fogliare o intermedio)
 * dell'albero di decisione.
 */
public abstract class Node {

    private static int idNodeCount = 0;

    private int idNode;
    private int beginExampleIndex;
    private int endExampleIndex;
    private double variance;

    /**
     * Costruttore di classe. Inizializza gli attributi e delega il calcolo
     * della varianza (SSE) a un metodo dedicato.
     *
     * @param trainingSet       Oggetto di classe Data contenente il training set completo
     * @param beginExampleIndex Indice iniziale del sotto-insieme di training
     * @param endExampleIndex   Indice finale del sotto-insieme di training
     */
    public Node(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        this.idNode = idNodeCount++;
        this.beginExampleIndex = beginExampleIndex;
        this.endExampleIndex = endExampleIndex;
        this.variance = calculateVariance(trainingSet, beginExampleIndex, endExampleIndex);
    }

    /**
     * Metodo helper che esegue matematicamente il calcolo della varianza locale (SSE)
     * nel sotto-insieme di training assegnato.
     *
     * @param trainingSet Il dataset di riferimento
     * @param begin       Indice di partenza
     * @param end         Indice di fine
     * @return Il valore dello SSE calcolato
     */
    protected double calculateVariance(Data trainingSet, int begin, int end) {
        double sum = 0.0;
        double sumSq = 0.0;
        int n = end - begin + 1;

        // Ottimizzazione: SSE = Σ(yi^2) - (Σyi)^2 / n
        for (int i = begin; i <= end; i++) {
            double y = trainingSet.getClassValue(i);
            sum += y;
            sumSq += (y * y);
        }

        return sumSq - ((sum * sum) / n);
    }

    /**
     * Restituisce l'identificativo numerico del nodo corrente.
     *
     * @return L'identificativo numerico del nodo (idNode)
     */
    public int getIdNode() {
        return this.idNode;
    }

    /**
     * Restituisce l'indice iniziale del sotto-insieme di esempi coperti dal nodo corrente.
     *
     * @return L'indice del primo esempio nel training set complessivo
     */
    public int getBeginExampleIndex() {
        return this.beginExampleIndex;
    }

    /**
     * Restituisce l'indice finale del sotto-insieme di esempi coperti dal nodo corrente.
     *
     * @return L'indice dell'ultimo esempio nel training set complessivo
     */
    public int getEndExampleIndex() {
        return this.endExampleIndex;
    }

    /**
     * Restituisce il valore dello SSE (varianza) calcolato per l'attributo di classe.
     *
     * @return Il valore di varianza associato al nodo
     */
    public double getVariance() {
        return this.variance;
    }

    /**
     * Metodo astratto che restituisce il numero di nodi figli generati dallo split corrente.
     * L'implementazione varia a seconda della tipologia del nodo concreto.
     *
     * @return Il numero di nodi figli sottostanti
     */
    public abstract int getNumberOfChildren();

    /**
     * Concatena in una stringa le informazioni salienti del nodo corrente,
     * includendo l'intervallo degli esempi coperti e il valore di varianza.
     *
     * @return La rappresentazione testuale dello stato del nodo
     */
    @Override
    public String toString() {
        return "[Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance:" + variance;
    }
}