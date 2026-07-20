package tree;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe ContinuousNode estende SplitNode e modella un nodo di split
 * (bivio) basato su un attributo indipendente di tipo continuo (numerico).
 * A differenza degli attributi discreti che creano un ramo per ogni valore
 * nominale, un attributo continuo genera sempre e solo due rami, separando
 * il dataset in base a una soglia numerica (minore/uguale e maggiore).
 */
public class ContinuousNode extends SplitNode {

    /**
     * Costruttore della classe. Invoca il costruttore della superclasse SplitNode,
     * passandogli l'attributo continuo su cui si intende effettuare la divisione dei dati.
     * La superclasse si occuperà automaticamente di ordinare i dati e avviare la
     * generazione dei due rami chiamando il metodo setSplitInfo().
     *
     * @param trainingSet       Oggetto contenente il training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione di training
     * @param endExampleIndex   Indice di fine della porzione di training
     * @param attribute         Attributo continuo indipendente sul quale si definisce lo split
     */
    public ContinuousNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, ContinuousAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }

    /**
     * Implementazione del metodo astratto per determinare la soglia di taglio
     * (split value) ottimale. Il metodo esplora tutti i possibili punti di
     * divisione tra i valori continui adiacenti, calcola la varianza risultante
     * per ciascuno split e seleziona quello che minimizza la varianza complessiva.
     * Genera sempre due oggetti SplitInfo ("<=" e ">").
     *
     * @param trainingSet       Training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione
     * @param endExampleIndex   Indice di fine della porzione
     * @param attribute         Attributo di riferimento (continuo)
     */
    @Override
    protected void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        // Valore su cui testare il taglio
        Double currentSplitValue = (Double) trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());
        double bestInfoVariance = 0;
        List<SplitInfo> bestMapSplit = null;

        // Scansioniamo tutti gli esempi per trovare il punto di taglio ottimale
        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Double value = (Double) trainingSet.getExplanatoryValue(i, attribute.getIndex());

            // Se il valore cambia rispetto al precedente, è un potenziale punto di taglio
            if (value.doubleValue() != currentSplitValue.doubleValue()) {

                // Calcoliamo la varianza se tagliassimo qui
                double localVariance = new LeafNode(trainingSet, beginExampleIndex, i - 1).getVariance();
                double candidateSplitVariance = localVariance;
                localVariance = new LeafNode(trainingSet, i, endExampleIndex).getVariance();
                candidateSplitVariance += localVariance;

                // Se è il primo taglio che testiamo, lo impostiamo come migliore di default
                if (bestMapSplit == null) {
                    bestMapSplit = new ArrayList<SplitInfo>();
                    bestMapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex, i - 1, 0, "<="));
                    bestMapSplit.add(new SplitInfo(currentSplitValue, i, endExampleIndex, 1, ">"));
                    bestInfoVariance = candidateSplitVariance;
                }
                // Altrimenti, confrontiamo la varianza di questo taglio con la migliore trovata finora
                else {
                    if (candidateSplitVariance < bestInfoVariance) {
                        bestInfoVariance = candidateSplitVariance;
                        bestMapSplit.set(0, new SplitInfo(currentSplitValue, beginExampleIndex, i - 1, 0, "<="));
                        bestMapSplit.set(1, new SplitInfo(currentSplitValue, i, endExampleIndex, 1, ">"));
                    }
                }
                currentSplitValue = value;
            }
        }

        // Assegniamo la lista migliore individuata
        super.mapSplit = bestMapSplit;

        // Rimuoviamo il secondo ramo se risulta vuoto
        if ((mapSplit.get(1).getBeginIndex() == mapSplit.get(1).getEndIndex())) {
            mapSplit.remove(1);
        }
    }

    /**
     * Confronta il valore in input numerico con la soglia di taglio (split value)
     * e valuta l'operatore matematico (<= o >) per decidere in quale ramo scendere.
     *
     * @param value Il valore (Double) dell'attributo continuo dell'esempio da testare
     * @return L'identificativo del ramo (indice dell'array mapSplit) superato dal test
     */
    @Override
    public int testCondition(Object value) {
        Double val = (Double) value;
        for (int i = 0; i < super.mapSplit.size(); i++) {
            SplitInfo info = super.mapSplit.get(i);
            Double splitVal = (Double) info.getSplitValue();
            String comp = info.getComparator();

            if (comp.equals("<=") && val <= splitVal) {
                return i;
            } else if (comp.equals(">") && val > splitVal) {
                return i;
            }
        }
        return -1; // Valore sconosciuto o imprevisto
    }

    /**
     * Specializza la stampa del nodo aggiungendo la dicitura "CONTINUOUS "
     * prima delle informazioni formattate dalla superclasse.
     *
     * @return Stringa descrittiva del nodo continuo
     */
    @Override
    public String toString() {
        return "CONTINUOUS " + super.toString();
    }
}