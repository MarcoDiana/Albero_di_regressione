/**
 * Package contenente i Data Transfer Object (DTO) utilizzati per la comunicazione
 * HTTP REST / JSON tra il server Spring Boot e il client web Vaadin Flow.
 * <p>
 * Il package include:
 * <ul>
 *   <li>{@link dto.TreeRequestDto}: incapsula il nome della tabella o file per le operazioni di apprendimento e caricamento;</li>
 *   <li>{@link dto.TreeResponseDto}: contiene le regole dell'albero e il nodo radice restituito dal server;</li>
 *   <li>{@link dto.PredictRequestDto}: incapsula il percorso decisionale inviato per navigare l'albero;</li>
 *   <li>{@link dto.PredictionStepDto}: descrive lo stato del nodo raggiunto (foglia con valore predetto o nodo interno di split).</li>
 * </ul>
 *
 * @author Marco Diana
 * @version 1.0
 */
package dto;
