package tree;

import data.Attribute;
import data.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che modella un nodo interno (di split) dell'albero di regressione.
 * Estende Node aggiungendo la logica di partizionamento: il sottoinsieme di esempi
 * viene diviso in più partizioni (figli) in base ai valori di un attributo.
 * <p>
 * Contiene la classe interna SplitInfo che descrive ogni singola partizione
 * (figlio) prodotta dallo split: valore discriminante, range di esempi, indice del figlio.
 * <p>
 * Gli split sono confrontabili rispetto alla varianza: lo split con varianza
 * minore è il migliore (Comparable).
 * <p>
 * Le sottoclassi concrete (es. DiscreteNode) implementano:
 * - setSplitInfo(): come ricavare le partizioni per uno specifico tipo di attributo
 * - testCondition(): su quale ramo instradare un nuovo esempio
 *
 * @author Marco Diana
 * @version 1.0
 */
abstract class SplitNode extends Node implements Comparable<SplitNode> {

    /** Identificativo di versione per la serializzazione. */
    private static final long serialVersionUID = 1L;

    /**
     * Classe interna che raccoglie le informazioni descrittive di una singola
     * partizione (figlio) prodotta dallo split.
     * Ogni SplitInfo corrisponde a un ramo dell'albero che parte da questo nodo.
     */
    class SplitInfo implements Serializable {

        /**
         * Identificativo di versione per la serializzazione.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Valore dell'attributo che caratterizza questa partizione.
         */
        private Object splitValue;

        /**
         * Primo indice degli esempi appartenenti a questa partizione (incluso).
         */
        private int beginIndex;

        /**
         * Ultimo indice degli esempi appartenenti a questa partizione (incluso).
         */
        private int endIndex;

        /**
         * Numero (indice) del figlio: 0 per il primo ramo, 1 per il secondo, ecc.
         */
        private int numberChild;

        /**
         * Operatore di confronto usato nella formulazione della regola.
         * Per attributi discreti è sempre "=" (valore uguale).
         * Per attributi continui è "&lt;=" oppure "&gt;".
         */
        private String comparator = "=";

        /**
         * Costruttore base: usato per split discreti, dove il comparatore è sempre "=".
         *
         * @param splitValue  valore che identifica questa partizione
         * @param beginIndex  primo indice del range (incluso)
         * @param endIndex    ultimo indice del range (incluso)
         * @param numberChild indice del figlio (0-based)
         */
        SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild) {
            this.splitValue = splitValue;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.numberChild = numberChild;
        }

        /**
         * Costruttore esteso: permette di specificare il comparatore.
         * Utile per attributi continui (es. "&lt;=", "&gt;").
         *
         * @param splitValue  valore che identifica questa partizione
         * @param beginIndex  primo indice del range (incluso)
         * @param endIndex    ultimo indice del range (incluso)
         * @param numberChild indice del figlio (0-based)
         * @param comparator  operatore di confronto ("=", "&lt;=", "&gt;", ...)
         */
        SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild, String comparator) {
            this(splitValue, beginIndex, endIndex, numberChild);
            this.comparator = comparator;
        }

        /**
         * Restituisce il primo indice del range di esempi di questa partizione.
         *
         * @return primo indice (incluso)
         */
        int getBeginIndex() {
            return beginIndex;
        }

        /**
         * Restituisce l'ultimo indice del range di esempi di questa partizione.
         *
         * @return ultimo indice (incluso)
         */
        int getEndIndex() {
            return endIndex;
        }

        /**
         * Restituisce il valore dell'attributo che discrimina questa partizione.
         *
         * @return valore dello split
         */
        Object getSplitValue() {
            return splitValue;
        }

        /**
         * Restituisce l'indice del figlio associato a questa partizione.
         *
         * @return indice del figlio (0-based)
         */
        int getNumberChild() {
            return numberChild;
        }

        /**
         * Restituisce l'operatore di confronto del test.
         *
         * @return "=" per attributi discreti, "&lt;=" o "&gt;" per attributi continui
         */
        String getComparator() {
            return comparator;
        }

        /**
         * Rappresentazione testuale della partizione: indice del figlio,
         * valore discriminante e range di esempi.
         *
         * @return stringa nel formato "child i split value=v[Examples:begin-end]"
         */
        @Override
        public String toString() {
            return "child " + numberChild + " split value" + comparator + splitValue
                    + "[Examples:" + beginIndex + "-" + endIndex + "]";
        }
    }

    /**
     * Attributo indipendente su cui viene effettuato lo split.
     */
    private Attribute attribute;

    /**
     * Partizioni prodotte dallo split: mapSplit.get(i) descrive il range di esempi
     * e il valore discriminante del figlio i-esimo.
     * Popolata da setSplitInfo() nelle sottoclassi.
     */
    protected List<SplitInfo> mapSplit = new ArrayList<SplitInfo>();

    /**
     * Varianza totale dello split: somma degli SSE dei nodi figli.
     * Più è basso questo valore, più lo split omogeneizza le partizioni.
     * Usata da RegressionTree per confrontare split alternativi.
     */
    private double splitVariance;

    /**
     * Popola mapSplit con le informazioni sulle partizioni dello split.
     * Implementato diversamente per attributi discreti e continui.
     *
     * @param trainingSet       training set (già ordinato per l'attributo nel range)
     * @param beginExampleIndex primo indice del sottoinsieme (incluso)
     * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
     * @param attribute         attributo su cui si sta effettuando lo split
     */
    protected abstract void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex,
                                         Attribute attribute);

    /**
     * Determina su quale ramo instradare un esempio dato il valore del suo attributo.
     *
     * @param value valore dell'attributo per l'esempio da classificare
     * @return indice del figlio corrispondente (0-based), o -1 se nessun ramo supera il test
     */
    abstract int testCondition(Object value);

    /**
     * Costruttore: inizializza il nodo di split.
     * 1. Invoca il costruttore di Node (calcola SSE e assegna l'ID)
     * 2. Ordina il sottoinsieme di esempi per l'attributo (necessario a setSplitInfo)
     * 3. Invoca setSplitInfo() per popolare mapSplit
     * 4. Calcola splitVariance come somma degli SSE dei figli
     * <p>
     * Per calcolare la varianza di ogni figlio si usano istanze temporanee
     * di LeafNode, che computano l'SSE nel proprio costruttore.
     *
     * @param trainingSet       training set contenente gli esempi
     * @param beginExampleIndex primo indice del sottoinsieme (incluso)
     * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
     * @param attribute         attributo su cui effettuare lo split
     */
    SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex);
        this.attribute = attribute;

        // L'ordinamento rende contigui gli esempi con lo stesso valore dell'attributo
        trainingSet.sort(attribute, beginExampleIndex, endExampleIndex);
        setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

        // La varianza dello split è la somma degli SSE delle partizioni
        splitVariance = 0;
        for (SplitInfo info : mapSplit)
            splitVariance += new LeafNode(trainingSet, info.getBeginIndex(), info.getEndIndex()).getVariance();
    }

    /**
     * Restituisce l'attributo su cui è stato effettuato lo split.
     *
     * @return attributo di split
     */
    Attribute getAttribute() {
        return attribute;
    }

    /**
     * Restituisce la varianza totale dello split (somma degli SSE dei figli).
     * Ridefinisce Node.getVariance(): in un nodo di split la varianza
     * rilevante è quella aggregata dei figli, non quella del nodo corrente.
     *
     * @return varianza dello split
     */
    @Override
    double getVariance() {
        return splitVariance;
    }

    /**
     * Restituisce il numero di partizioni (figli) prodotte dallo split.
     *
     * @return numero di rami originati dal nodo
     */
    @Override
    int getNumberOfChildren() {
        return mapSplit.size();
    }

    /**
     * Restituisce le informazioni sulla partizione del figlio i-esimo.
     *
     * @param child indice del figlio (0-based)
     * @return SplitInfo con range e valore discriminante del figlio
     */
    SplitInfo getSplitInfo(int child) {
        return mapSplit.get(child);
    }

    /**
     * Formula la "domanda" dello split concatenando, per ogni ramo, indice,
     * attributo, operatore e valore. Necessario per la predizione di nuovi esempi.
     * Formato per ogni partizione: "i:attributo=valore\n".
     *
     * @return stringa con tutte le opzioni dello split
     */
    String formulateQuery() {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < mapSplit.size(); i++)
            query.append(i).append(":").append(attribute)
                    .append(mapSplit.get(i).getComparator())
                    .append(mapSplit.get(i).getSplitValue()).append("\n");
        return query.toString();
    }

    /**
     * Confronta due nodi di split rispetto alla varianza dello split.
     *
     * @param o nodo di split da confrontare con quello corrente
     * @return -1 se la varianza del nodo corrente è minore, 1 se è maggiore, 0 se sono uguali
     */
    @Override
    public int compareTo(SplitNode o) {
        return Double.compare(splitVariance, o.splitVariance);
    }

    /**
     * Rappresentazione testuale del nodo di split: attributo, informazioni del nodo
     * (da Node), varianza dello split ed elenco di tutte le partizioni.
     *
     * @return descrizione del nodo e dei suoi rami
     */
    @Override
    public String toString() {
        StringBuilder v = new StringBuilder("SPLIT : attribute=" + attribute + " " + super.toString()
                + " Split Variance: " + getVariance() + "\n");
        for (SplitInfo info : mapSplit)
            v.append("\t").append(info).append("\n");
        return v.toString();
    }
}
