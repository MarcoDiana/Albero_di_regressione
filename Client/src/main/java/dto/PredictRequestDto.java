package dto;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object (DTO) inviato dal client per avanzare nella predizione
 * guidata stateless lungo l'albero di regressione.
 *
 * @param tableName nome del modello/albero memorizzato nel server o su disco
 * @param path      sequenza ordinata 0-based degli indici dei rami selezionati a partire dalla radice
 */
public record PredictRequestDto(
        String tableName,
        List<Integer> path
) implements Serializable {
}
