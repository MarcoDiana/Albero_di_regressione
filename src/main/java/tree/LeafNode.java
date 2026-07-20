package tree;

import data.Data;

/**
 * La classe LeafNode estende Node e modella l'entità nodo fogliare
 * (nodo terminale) dell'albero di decisione. Questo nodo rappresenta
 * la previsione finale della variabile target.
 */
public class LeafNode extends Node {

    /**
     * Valore dell'attributo di classe espresso (predetto) nella foglia corrente.
     * Corrisponde alla media aritmetica dei valori dell'attributo target nel sotto-insieme.
     */
    private Double predictedClassValue;

    /**
     * Costruttore della classe. Invoca il costruttore della superclasse Node
     * per inizializzare indici e varianza, dopodiché avvalora l'attributo
     * predictedClassValue eseguendo la media matematica dei valori di classe.
     *
     * @param trainingSet       Oggetto contenente il training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione di training assegnata alla foglia
     * @param endExampleIndex   Indice di fine della porzione di training assegnata alla foglia
     */
    public LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        // Invocazione al costruttore della superclasse (che calcola anche la varianza SSE)
        super(trainingSet, beginExampleIndex, endExampleIndex);

        // Calcolo del valore predetto: media aritmetica dei valori dell'attributo target
        double sum = 0.0;
        int n = endExampleIndex - beginExampleIndex + 1; // Numero totale di elementi nel nodo

        for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
            sum += trainingSet.getClassValue(i); // Somma i valori target degli esempi coperti
        }

        this.predictedClassValue = sum / n; // Calcola e assegna la media
    }

    /**
     * Restituisce il valore predetto calcolato per questo specifico nodo fogliare.
     *
     * @return Il valore numerico della previsione (di tipo Double)
     */
    public Double getPredictedClassValue() {
        return this.predictedClassValue;
    }

    /**
     * Restituisce il numero di ramificazioni (figli) originanti dal nodo.
     * Essendo la classe LeafNode un nodo terminale (foglia), restituisce costantemente 0.
     *
     * @return Il numero di figli (0)
     */
    @Override
    public int getNumberOfChildren() {
        return 0;
    }

    /**
     * Concatena il marcatore "LEAF", il valore di classe predetto
     * e le informazioni base del nodo formattate dalla superclasse (Node.toString()).
     * Esempio output: "LEAF : class=4.59 Nodo: [Examples:0-9] variance:2.98"
     *
     * @return Stringa testuale descrittiva del nodo fogliare
     */
    @Override
    public String toString() {
        return "LEAF : class=" + this.predictedClassValue + " Nodo: " + super.toString();
    }
}