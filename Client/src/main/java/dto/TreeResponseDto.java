package dto;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) restituito dal server in risposta a una richiesta
 * di apprendimento da database o caricamento da file.
 *
 * @param tableName nome della tabella o del modello caricato con successo
 * @param treeRules rappresentazione testuale completa dell'albero e delle regole di decisione IF-THEN
 * @param rootStep  informazioni sul nodo radice per inizializzare l'interfaccia di predizione
 * @author Marco Diana
 * @version 1.0
 */
public record TreeResponseDto(
        String tableName,
        String treeRules,
        PredictionStepDto rootStep
) implements Serializable {

    /** Identificativo di versione per la serializzazione. */
    private static final long serialVersionUID = 1L;
}
