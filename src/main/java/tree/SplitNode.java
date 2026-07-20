package tree;

import data.Attribute;
import data.Attribute;
import data.Data;
import java.util.List;

/**
 * La classe astratta SplitNode estende Node e modella l'astrazione
 * dell'entità nodo di split (sia esso continuo o discreto).
 */
public abstract class SplitNode extends Node implements Comparable<SplitNode> {

    /**
     * Inner class che aggrega tutte le informazioni riguardanti un nodo di split.
     * Memorizza le condizioni del test e i range coperti dai vari figli.
     */
    protected class SplitInfo {

        /** Valore dell'attributo indipendente che definisce la condizione di split. */
        Object splitValue;

        /** Indice iniziale nel training set degli esempi coperti da questo ramo. */
        int beginIndex;

        /** Indice finale nel training set degli esempi coperti da questo ramo. */
        int endIndex;

        /** Numero identificativo (ID) del figlio generato dallo split. */
        int numberChild;

        /** Operatore matematico ("=" per discreti, "<=" o ">" per continui). Default a "=". */
        String comparator = "=";

        /**
         * Costruttore base per split a valori discreti (imposta di default comparator a "=").
         *
         * @param splitValue  Il valore assunto dall'attributo nel ramo
         * @param beginIndex  Indice di inizio nel training set
         * @param endIndex    Indice di fine nel training set
         * @param numberChild Numero identificativo del figlio
         */
        SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild) {
            this(splitValue, beginIndex, endIndex, numberChild, "=");
        }

        /**
         * Costruttore completo che avvalora tutti gli attributi, compreso il comparatore
         * (utile per split generici/continui).
         *
         * @param splitValue  Il valore assunto dall'attributo nel ramo
         * @param beginIndex  Indice di inizio nel training set
         * @param endIndex    Indice di fine nel training set
         * @param numberChild Numero identificativo del figlio
         * @param comparator  L'operatore matematico del test
         */
        SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild, String comparator) {
            this.splitValue = splitValue;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.numberChild = numberChild;
            this.comparator = comparator;
        }

        /**
         * Restituisce il valore discriminante dello split.
         *
         * @return Oggetto che modella il valore dello split
         */
        public Object getSplitValue() {
            return splitValue;
        }

        /**
         * Restituisce l'indice iniziale degli esempi assegnati a questo ramo.
         *
         * @return L'indice iniziale
         */
        public int getBeginIndex() {
            return beginIndex;
        }

        /**
         * Restituisce l'indice finale degli esempi assegnati a questo ramo.
         *
         * @return L'indice finale
         */
        public int getEndIndex() {
            return endIndex;
        }

        /**
         * Restituisce l'operatore matematico applicato al valore di test.
         *
         * @return La stringa rappresentante l'operatore
         */
        public String getComparator() {
            return comparator;
        }

        /**
         * Restituisce le informazioni dello split concatenando figlio, comparatore,
         * valore di test e range di esempi coperti.
         *
         * @return Stringa descrittiva dello split
         */
        @Override
        public String toString() {
            return "child " + numberChild + " split value" + comparator + splitValue +
                    "[Examples:" + beginIndex + "-" + endIndex + "]";
        }
    }

    // --- Attributi di SplitNode ---

    /** Oggetto Attribute che modella l'attributo indipendente sul quale lo split è generato. */
    private Attribute attribute;

    /** Array per memorizzare gli split candidati generati dai test sui valori dell'attributo. */
    protected List<SplitInfo> mapSplit;

    /** Attributo che contiene il valore della somma delle varianze a seguito del partizionamento indotto. */
    private double splitVariance;

    /**
     * Costruttore della classe. Invoca il costruttore della superclasse, ordina i valori
     * dell'attributo, popola l'array mapSplit e computa la varianza totale di split (SSE).
     *
     * @param trainingSet       Training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione di training
     * @param endExampleIndex   Indice di fine della porzione di training
     * @param attribute         Attributo indipendente sul quale si definisce lo split
     */
    public SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex);
        this.attribute = attribute;

        // Ordina la porzione di trainingSet rispetto all'attributo specificato
        trainingSet.sort(attribute, beginExampleIndex, endExampleIndex);

        // Genera e popola l'array mapSplit tramite l'implementazione delegata alla sottoclasse
        setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

        // Computa lo SSE (splitVariance) richiamando il metodo ereditato da Node
        // (assicurati che in Node.java il metodo di calcolo sia dichiarato come protected o package-private)
        this.splitVariance = 0.0;
        for (SplitInfo info : mapSplit) {
            this.splitVariance += calculateVariance(trainingSet, info.getBeginIndex(), info.getEndIndex());
        }
    }

    /**
     * Metodo astratto per generare le informazioni necessarie per ciascuno degli split candidati.
     * Implementato dalle classi figlie (DiscreteNode o ContinuousNode).
     *
     * @param trainingSet       Training set complessivo
     * @param beginExampleIndex Indice di inizio della porzione
     * @param endExampleIndex   Indice di fine della porzione
     * @param attribute         Attributo di riferimento
     */
    protected abstract void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute);

    /**
     * Metodo astratto per modellare la condizione di test. Determina in quale ramo inoltrare l'esempio.
     *
     * @param value Il valore dell'attributo dell'esempio da testare
     * @return Identificativo numerico del ramo (figlio) originato
     */
    public abstract int testCondition(Object value);

    /**
     * Restituisce l'oggetto per l'attributo usato nello split.
     *
     * @return L'attributo di divisione
     */
    public Attribute getAttribute() {
        return this.attribute;
    }

    /**
     * Restituisce la somma delle varianze dei rami (Split Variance) generata dallo split corrente.
     * Spesso utilizzata in contrasto con la varianza del nodo padre per calcolare l'Information Gain.
     *
     * @return La varianza calcolata post-split
     */
    @Override
    public double getVariance() {
        return this.splitVariance;
    }

    /**
     * Restituisce il numero dei rami (figli) originanti nel nodo corrente.
     *
     * @return Numero di figli generati (lunghezza dell'array mapSplit)
     */
    @Override
    public int getNumberOfChildren() {
        return this.mapSplit.size();
    }

    /**
     * Restituisce le informazioni per lo split associato allo specifico figlio indicato.
     *
     * @param child Indice numerico del figlio richiesto
     * @return Oggetto SplitInfo associato al figlio
     */
    public SplitInfo getSplitInfo(int child) {
        return this.mapSplit.get(child);
    }

    /**
     * Concatena le informazioni di ciascun test (attributo, operatore e valore)
     * in una String finale multi-linea, usata tipicamente in fase di predizione.
     *
     * @return La query di test formattata testualmente
     */
    public String formulateQuery() {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < mapSplit.size(); i++) {
            query.append(i).append(":")
                    .append(attribute.getName())
                    .append(mapSplit.get(i).getComparator())
                    .append(mapSplit.get(i).getSplitValue())
                    .append("\n");
        }
        return query.toString();
    }

    /**
     * Concatena le informazioni del nodo corrente (ereditate dalla superclasse Node)
     * e aggiunge quelle di tutti i rami di partizionamento mappati internamente.
     *
     * @return Stringa descrittiva completa del nodo di split e dei suoi rami
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("SPLIT : attribute=").append(attribute.getName())
                .append(" Nodo: ").append(super.toString())
                .append("\nSplit Variance: ").append(this.splitVariance).append("\n");

        for (int i = 0; i < mapSplit.size(); i++) {
            s.append("\t").append(mapSplit.get(i).toString()).append("\n");
        }
        return s.toString();
    }

    /**
     * Confronta la splitVariance di questo nodo con quella di un altro nodo.
     * Serve per ordinare automaticamente gli split nel TreeSet.
     *
     * @param o Il nodo di split con cui confrontarsi
     * @return 0 se uguali, -1 se questo ha varianza minore, 1 se maggiore
     */
    @Override
    public int compareTo(SplitNode o) {
        return Double.compare(this.splitVariance, o.splitVariance);
    }
}