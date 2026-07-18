package tree;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

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

        // FASE 1: Individuare quanti valori distinti (quanti rami) assume l'attributo.
        // Essendo i dati già ordinati, i valori identici sono contigui.
        int distinctValuesCount = 1;
        Object currentValue = trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object nextValue = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (!currentValue.equals(nextValue)) {
                distinctValuesCount++;
                currentValue = nextValue;
            }
        }

        // FASE 2: Istanziare l'array mapSplit della superclasse con la giusta dimensione
        super.mapSplit = new SplitInfo[distinctValuesCount];

        // FASE 3: Popolare l'array creando gli oggetti SplitInfo per ogni partizione.
        int childIndex = 0;
        int currentBegin = beginExampleIndex;
        currentValue = trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object nextValue = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (!currentValue.equals(nextValue)) {
                // Il valore è cambiato: chiudiamo la partizione corrente creando lo SplitInfo
                super.mapSplit[childIndex] = new SplitInfo(currentValue, currentBegin, i - 1, childIndex);
                childIndex++;
                currentBegin = i;
                currentValue = nextValue;
            }
        }
        // Non dimentichiamo di chiudere e aggiungere l'ultimissima partizione
        super.mapSplit[childIndex] = new SplitInfo(currentValue, currentBegin, endExampleIndex, childIndex);
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
        for (int i = 0; i < super.mapSplit.length; i++) {
            if (super.mapSplit[i].getSplitValue().equals(value)) {
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