/**
 * Fornisce le interfacce utente del client per l'interazione con il server
 * di regressione, supportando sia l'interfaccia a riga di comando (CLI) via socket TCP,
 * sia l'applicazione web reattiva Spring Boot con interfaccia grafica Vaadin Flow e client REST.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link Client.ClientApplication}: punto di ingresso dell'applicazione web Spring Boot con Vaadin Flow;</li>
 *   <li>{@link Client.MainView}: vista web reattiva per l'apprendimento, il caricamento e la predizione guidata interattiva;</li>
 *   <li>{@link Client.TreeRestClientService}: servizio client HTTP basato su {@code RestClient} per la comunicazione con le API REST del server;</li>
 *   <li>{@link Client.Main}: punto di ingresso dell'interfaccia a riga di comando (CLI) con connessione socket TCP
 *       verso {@code server.MultiServer}.</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package Client;
