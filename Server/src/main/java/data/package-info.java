/**
 * Fornisce le classi e le strutture dati per la modellazione e la gestione del dataset di addestramento
 * utilizzato per l'induzione dell'albero di regressione.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link data.Attribute}: classe base astratta per la rappresentazione degli attributi esplicativi
 *       con nome simbolico e indice di colonna.</li>
 *   <li>{@link data.DiscreteAttribute}: specializzazione per attributi discreti/categorici, che gestisce
 *       l'insieme ordinato e deduplicato dei valori ammissibili tramite {@link java.util.TreeSet}.</li>
 *   <li>{@link data.ContinuousAttribute}: specializzazione per attributi continui a valori reali,
 *       impiegati per generare split basati su soglia ottima.</li>
 *   <li>{@link data.Data}: astrazione tabellare del dataset in memoria, contenente le istanze di addestramento
 *       ({@link database.Example}), la matrice degli attributi esplicativi e la colonna target continua.
 *       Fornisce metodi di calcolo statistico (SSE, varianza, media), ordinamento in-place mediante
 *       algoritmo quicksort e partizionamento.</li>
 *   <li>{@link data.TrainingDataException}: eccezione controllata sollevata in presenza di anomalie strutturali
 *       nel dataset (dataset vuoto, schema non conforme, valori mancanti o tipi non validi).</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package data;
