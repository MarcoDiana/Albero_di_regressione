/**
 * Fornisce le componenti server per l'Albero di Regressione, supportando sia l'architettura
 * server multithread basata su Java Socket TCP, sia il backend moderno Spring Boot con API REST
 * e documentazione OpenAPI / Swagger UI.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link server.ServerApplication}: entry point dell'applicazione Spring Boot backend (porta 8080);</li>
 *   <li>{@link server.TreeService}: servizio applicativo con cache concorrente thread-safe ({@link java.util.concurrent.ConcurrentHashMap})
 *       per l'apprendimento da MySQL, caricamento da disco {@code .dmp} e predizione stateless;</li>
 *   <li>{@link server.TreeController}: controller REST che espone gli endpoint {@code /api/trees/learn},
 *       {@code /api/trees/load} e {@code /api/trees/predict} con annotazioni OpenAPI Swagger;</li>
 *   <li>{@link server.GlobalExceptionHandler}: gestore centralizzato delle eccezioni ({@code @RestControllerAdvice})
 *       che mappa le eccezioni in risposte HTTP 400, 404 e 500 con payload JSON;</li>
 *   <li>{@link server.MultiServer}: server socket TCP classico su porta 8080 che accetta connessioni concorrenti;</li>
 *   <li>{@link server.ServerOneClient}: thread operaio per la gestione della sessione di un singolo client socket TCP;</li>
 *   <li>{@link server.UnknownValueException}: eccezione controllata sollevata durante la predizione quando
 *       un valore acquisito non individua alcun ramo valido del nodo di split.</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package server;
