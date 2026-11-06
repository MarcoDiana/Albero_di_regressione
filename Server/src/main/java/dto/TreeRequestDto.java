package dto;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) che incapsula la richiesta di apprendimento
 * di un albero da database MySQL o di caricamento da file serializzato {@code .dmp}.
 *
 * @param tableName nome della tabella di database o dell'archivio {@code .dmp} da elaborare
 * @author Marco Diana
 * @version 1.0
 */
public record TreeRequestDto(String tableName) implements Serializable {

    /** Identificativo di versione per la serializzazione. */
    private static final long serialVersionUID = 1L;
}
