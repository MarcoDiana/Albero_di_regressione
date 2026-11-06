/**
 * Gestisce l'accesso al livello di persistenza relazionale (MySQL), l'introspezione dei metadati dello schema
 * e l'estrazione delle transazioni per la costruzione del dataset di addestramento.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link database.DbAccess}: componente responsabile della gestione del ciclo di vita della connessione JDBC
 *       verso la base di dati MySQL (database predefinito {@code MapDB}, utente {@code MapUser}), inclusa l'inizializzazione
 *       del driver e la chiusura sicura delle risorse.</li>
 *   <li>{@link database.TableSchema}: incapsula i metadati relazionali della tabella estratti tramite JDBC
 *       ({@link java.sql.DatabaseMetaData}), discriminando colonne discrete (stringhe) e numeriche (target o attributi continui).</li>
 *   <li>{@link database.TableData}: esegue le query SQL per estrarre le tuple di addestramento
 *       sotto forma di oggetti {@link database.Example} o insiemi di valori distinti per ciascun attributo.</li>
 *   <li>{@link database.Column}: descrittore di colonna relazionale composto da nome e tipo semantico.</li>
 *   <li>{@link database.Example}: struttura dati che rappresenta una riga/tupla estratta dal database,
 *       con supporto a ordinamento, indicizzazione e iterazione.</li>
 *   <li>{@link database.DatabaseConnectionException}: eccezione sollevata in caso di fallimento della connessione JDBC
 *       o assenza dei driver del database.</li>
 *   <li>{@link database.EmptySetException}: eccezione sollevata quando una query di estrazione dati restituisce un result set vuoto.</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package database;
