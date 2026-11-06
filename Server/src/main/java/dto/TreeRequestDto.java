package dto;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) che incapsula la richiesta di apprendimento
 * di un albero da database MySQL o di caricamento da file serializzato {@code .dmp}.
 *
 * @param tableName nome della tabella di database o dell'archivio {@code .dmp} da elaborare
 */
public record TreeRequestDto(String tableName) implements Serializable {
}
