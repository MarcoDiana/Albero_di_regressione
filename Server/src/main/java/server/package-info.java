/**
 * Implementa l'architettura di rete server multithread basata su Java Socket per la gestione concorrente
 * delle richieste dei client remoti.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link server.MultiServer}: server di ascolto principale su porta TCP (predefinita 8080);
 *       accetta le connessioni in ingresso e alloca per ciascun client un thread di servizio indipendente.</li>
 *   <li>{@link server.ServerOneClient}: thread operaio (worker) dedicato alla gestione dell'intera sessione
 *       di un singolo client; gestisce il protocollo applicativo a messaggi su flussi {@link java.io.ObjectInputStream}
 *       e {@link java.io.ObjectOutputStream}, coordinando i comandi di addestramento da database MySQL,
 *       salvataggio/caricamento su file {@code .dmp} e predizione interattiva.</li>
 *   <li>{@link server.UnknownValueException}: eccezione controllata sollevata durante la predizione
 *       quando il client invia un valore di risposta non valido o fuori dal dominio consentito.</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package server;
