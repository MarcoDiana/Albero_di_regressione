package server;

import dto.PredictRequestDto;
import dto.PredictionStepDto;
import dto.TreeRequestDto;
import dto.TreeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller REST che espone le API per l'apprendimento, il caricamento
 * e la predizione tramite Albero di Regressione.
 */
@RestController
@RequestMapping("/api/trees")
@Tag(name = "Regression Tree API", description = "Endpoint REST per la gestione dell'Albero di Regressione")
public class TreeController {

    /** Riferimento al servizio applicativo degli alberi. */
    private final TreeService treeService;

    /**
     * Costruttore con iniezione del servizio.
     *
     * @param treeService istanza del servizio degli alberi
     */
    public TreeController(TreeService treeService) {
        this.treeService = treeService;
    }

    /**
     * Apprende un albero di regressione dal database MySQL per la tabella indicata,
     * serializza il modello e restituisce le regole e il nodo radice.
     *
     * @param request DTO contenente il nome della tabella
     * @return risposta HTTP 200 con le regole e il nodo radice
     * @throws Exception se si verificano errori di training o accesso al database
     */
    @Operation(summary = "Apprende un albero di regressione da database MySQL",
            description = "Carica i dati di training dalla tabella specificata in MapDB, addestra l'albero, lo serializza in formato .dmp e restituisce le regole e il nodo radice.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Albero appreso con successo",
                    content = @Content(schema = @Schema(implementation = TreeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dati di training non validi o errore nei parametri",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/learn")
    public ResponseEntity<TreeResponseDto> learn(@RequestBody TreeRequestDto request) throws Exception {
        TreeResponseDto response = treeService.learnFromDb(request.tableName());
        return ResponseEntity.ok(response);
    }

    /**
     * Carica un albero di regressione da archivio serializzato {@code .dmp}.
     *
     * @param request DTO contenente il nome del file
     * @return risposta HTTP 200 con le regole e il nodo radice
     * @throws Exception se il file non e' trovato o corrotto
     */
    @Operation(summary = "Carica un albero di regressione da file serializzato .dmp",
            description = "Carica l'albero salvato su disco dal file specificato, lo memorizza nella cache e restituisce regole e nodo radice.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Albero caricato con successo",
                    content = @Content(schema = @Schema(implementation = TreeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "File .dmp non trovato su disco",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Errore durante la deserializzazione o parametri errati",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/load")
    public ResponseEntity<TreeResponseDto> load(@RequestBody TreeRequestDto request) throws Exception {
        TreeResponseDto response = treeService.loadFromFile(request.tableName());
        return ResponseEntity.ok(response);
    }

    /**
     * Esegue un passo di navigazione stateless nell'albero lungo la sequenza di indici dei rami.
     *
     * @param request DTO contenente il nome della tabella/albero e la lista degli indici
     * @return risposta HTTP 200 con lo stato del nodo raggiunto (foglia o split)
     * @throws Exception se il percorso o il modello sono invalidi
     */
    @Operation(summary = "Esegue un passo di navigazione/predizione nell'albero",
            description = "Naviga l'albero seguendo la lista di indici fornita in path e restituisce il nodo raggiunto (foglia con valore predetto o split con opzioni).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passo di predizione calcolato con successo",
                    content = @Content(schema = @Schema(implementation = PredictionStepDto.class))),
            @ApiResponse(responseCode = "400", description = "Percorso non valido o errore nei parametri",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Albero non trovato in cache ne' su disco",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/predict")
    public ResponseEntity<PredictionStepDto> predict(@RequestBody PredictRequestDto request) throws Exception {
        PredictionStepDto response = treeService.predictStep(request.tableName(), request.path());
        return ResponseEntity.ok(response);
    }
}
