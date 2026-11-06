package server;

import data.TrainingDataException;
import database.DatabaseConnectionException;
import database.EmptySetException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Gestore globale centralizzato delle eccezioni per le API REST.
 * Converte le eccezioni sollevate dai controller e dai servizi in risposte
 * HTTP semantiche con payload JSON strutturato {@code {"error": "..."}}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestisce la mancata individuazione di un file serializzato su disco (404 Not Found).
     *
     * @param ex eccezione FileNotFoundException
     * @return risposta 404 con messaggio di errore
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "File non trovato: " + ex.getMessage()));
    }

    /**
     * Gestisce le eccezioni di dominio, validazione e navigazione (400 Bad Request).
     *
     * @param ex eccezione sollevata
     * @return risposta 400 con messaggio descrittivo
     */
    @ExceptionHandler({
            TrainingDataException.class,
            DatabaseConnectionException.class,
            EmptySetException.class,
            UnknownValueException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<Map<String, String>> handleDomainExceptions(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", msg));
    }

    /**
     * Gestisce errori di input/output e deserializzazione delle classi (400 Bad Request).
     *
     * @param ex eccezione di I/O o formato
     * @return risposta 400 con spiegazione dell'errore
     */
    @ExceptionHandler({IOException.class, ClassNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleIoAndFormatExceptions(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Errore di I/O o formato archivio: " + msg));
    }

    /**
     * Gestore di fallback per qualsiasi altra eccezione non prevista (500 Internal Server Error).
     *
     * @param ex eccezione generica
     * @return risposta 500 con messaggio di errore
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server: " + msg));
    }
}
