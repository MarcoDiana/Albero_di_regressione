/**
 * Fornisce le classi e gli algoritmi per l'induzione ricorsiva, la rappresentazione,
 * la serializzazione e la predizione mediante Alberi di Regressione (Regression Tree).
 * <p>
 * Il modello adotta il criterio della minimizzazione della Somma dei Quadrati degli Errori (SSE)
 * per determinare i punti di partizionamento ottimi sui nodi interni.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link tree.RegressionTree}: modello complessivo dell'albero di regressione; implementa l'algoritmo
 *       di apprendimento ricorsivo, la selezione dello split migliore tramite {@link java.util.TreeSet},
 *       la navigazione guidata per la predizione del valore target, la formattazione testuale delle regole di decisione
 *       e la persistenza/ripristino su stream binari (file {@code .dmp}).</li>
 *   <li>{@link tree.Node}: classe astratta base per i nodi dell'albero; incapsula l'intervallo degli indici
 *       degli esempi di addestramento {@code [begin, end]} e il calcolo della varianza/SSE del nodo.</li>
 *   <li>{@link tree.LeafNode}: nodo foglia terminale dell'albero; calcola ed espone il valore continuo predetto
 *       (media aritmetica della variabile target nel sottoinsieme associato).</li>
 *   <li>{@link tree.SplitNode}: nodo interno astratto di diramazione; definisce la condizione di split,
 *       l'attributo discriminante, l'insieme dei figli e la classe interna {@link tree.SplitNode.SplitInfo}
 *       per la memorizzazione dei dettagli della partizione.</li>
 *   <li>{@link tree.DiscreteNode}: nodo di diramazione per attributi discreti; genera un ramo per ciascun
 *       valore distinto dell'attributo categorico.</li>
 *   <li>{@link tree.ContinuousNode}: nodo di diramazione per attributi continui; genera uno split binario ottimo
 *       basato sul confronto {@code <= soglia} e {@code > soglia}.</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package tree;
