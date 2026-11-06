/**
 * Fornisce l'interfaccia utente a riga di comando (CLI) del Client per l'interazione
 * remota con il server di regressione.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link Client.Main}: punto di ingresso dell'applicazione client; stabilisce la connessione socket TCP
 *       verso il {@link server.MultiServer}, guida l'utente nella scelta delle operazioni (apprendimento da DB con salvataggio,
 *       caricamento da archivio {@code .dmp}, navigazione guidata delle regole e predizione continua)
 *       e gestisce i cicli di interazione e recupero da errori.</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package Client;
