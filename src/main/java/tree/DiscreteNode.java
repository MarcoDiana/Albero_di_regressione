package tree;

import data.Attribute;
import data.Data;
import data.Attribute;
import data.Data;
import data.DiscreteAttribute;
import java.util.ArrayList;

/**
 * La classe DiscreteNode estende SplitNode e modella un nodo di split (bivio)
 * basato su un attributo indipendente di tipo discreto (nominale).
 */
public class DiscreteNode extends SplitNode {

    /**
     * Costruttore della classe. Invoca il costruttore della superclasse SplitNode,
     * passandogli l'attributo discreto su cui si intende effettuare la divisione dei dati.
     * La superclasse si occuperà automaticamente di ordinare i dati e avviare la
     * generazione dei rami chiamando il metodo setSplitInfo().
     *
     * @param trainingSet       Oggetto contenente il training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione di training
     * @param endExampleIndex   Indice di fine della porzione di training
     * @param attribute         Attributo discreto indipendente sul quale si definisce lo split
     */
    public DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }

    /**
     * Implementazione del metodo astratto ereditato da SplitNode.
     * Analizza la porzione di training set (già ordinata dalla superclasse) per individuare
     * i valori discreti distinti assunti dall'attributo. Per ogni valore distinto,
     * crea un oggetto SplitInfo che modella il rispettivo ramo (figlio) e lo salva nell'array mapSplit.
     *
     * @param trainingSet       Training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione
     * @param endExampleIndex   Indice di fine della porzione
     * @param attribute         Attributo di riferimento (discreto)
     */
    @Override
    protected void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {

        // Inizializza la lista dinamica mapSplit ereditata dalla superclasse
        super.mapSplit = new ArrayList<SplitInfo>();

        int childIndex = 0;
        int currentBegin = beginExampleIndex;
        Object currentValue = trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

        // Un solo ciclo per scorrere i dati e generare i rami mano a mano
        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object nextValue = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (!currentValue.equals(nextValue)) {
                // Il valore è cambiato: chiudiamo la partizione corrente aggiungendo il nuovo SplitInfo
                super.mapSplit.add(new SplitInfo(currentValue, currentBegin, i - 1, childIndex));
                childIndex++;
                currentBegin = i;
                currentValue = nextValue;
            }
        }
        // Aggiunta dell'ultima partizione residua
        super.mapSplit.add(new SplitInfo(currentValue, currentBegin, endExampleIndex, childIndex));
    }

    /**
     * Confronta il valore in input con i valori di test di ciascun ramo (SplitInfo).
     *
     * @param value Il valore dell'attributo discreto dell'esempio da testare
     * @return L'identificativo del ramo (indice dell'array mapSplit) se il test è positivo,
     *         oppure -1 se il valore non corrisponde a nessun ramo (sconosciuto).
     */
    @Override
    public int testCondition(Object value) {
        for (int i = 0; i < super.mapSplit.size(); i++) {
            if (super.mapSplit.get(i).getSplitValue().equals(value)) {
                return i;
            }
        }
        // Il lancio della UnknownValueException verrà implementato nelle esercitazioni future
        return -1;
    }

    /**
     * Specializza la stampa del nodo aggiungendo la dicitura "DISCRETE "
     * prima delle informazioni formattate dalla superclasse.
     *
     * @return Stringa descrittiva del nodo discreto
     */
    @Override
    public String toString() {
        return "DISCRETE " + super.toString();
    }
}