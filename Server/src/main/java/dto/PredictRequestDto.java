package dto;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object (DTO) inviato dal client per avanzare nella predizione
 * guidata stateless lungo l'albero di regressione.
 *
 * @param tableName nome del modello/albero memorizzato nel server o su disco
 * @param path      sequenza ordinata 0-based degli indici dei rami selezionati a partire dalla radice
 * @author Marco Diana
 * @version 1.0
 */
public record PredictRequestDto(
        String tableName,
        List<Integer> path
) implements Serializable {

    /** Identificativo di versione per la serializzazione. */
    private static final long serialVersionUID = 1L;
}
