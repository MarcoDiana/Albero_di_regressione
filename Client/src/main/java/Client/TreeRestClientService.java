package Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.PredictRequestDto;
import dto.PredictionStepDto;
import dto.TreeRequestDto;
import dto.TreeResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Servizio lato client che gestisce la comunicazione HTTP REST con il backend
 * (porta 8080) utilizzando Spring {@link RestClient}.
 *
 * @author Marco Diana
 * @version 1.0
 */
@Service
public class TreeRestClientService {

    /** Parser Jackson per deserializzare il body di errore JSON. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** Client HTTP dichiarativo di Spring. */
    private final RestClient restClient;

    /**
     * Costruttore predefinito senza argomenti per Spring Bean instantiation.
     */
    public TreeRestClientService() {
        this(RestClient.builder(), "http://localhost:8080");
    }

    /**
     * Costruttore primario con iniezione del builder auto-configurato da Spring.
     *
     * @param restClientBuilder builder per RestClient
     * @param baseUrl           URL base del backend REST (letto da application.properties)
     */
    @Autowired
    public TreeRestClientService(RestClient.Builder restClientBuilder,
                                 @Value("${tree.server.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String errorMsg = "Errore di comunicazione con il server (HTTP " + response.getStatusCode() + ")";
                    try {
                        byte[] bytes = response.getBody().readAllBytes();
                        if (bytes.length > 0) {
                            Map<?, ?> map = OBJECT_MAPPER.readValue(bytes, Map.class);
                            if (map != null && map.containsKey("error") && map.get("error") != null) {
                                errorMsg = map.get("error").toString();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    throw new RuntimeException(errorMsg);
                })
                .build();
    }

    /**
     * Costruttore secondario per test o istanziazione manuale con URL specifico.
     *
     * @param baseUrl URL base del backend REST
     */
    public TreeRestClientService(String baseUrl) {
        this(RestClient.builder(), baseUrl);
    }

    /**
     * Invia una richiesta HTTP POST per apprendere l'albero dalla tabella MySQL indicata.
     *
     * @param tableName nome della tabella di training
     * @return risposta contenente le regole e il nodo radice
     * @throws RuntimeException in caso di errore restituito dal server o fallimento della comunicazione HTTP
     */
    public TreeResponseDto learnTree(String tableName) {
        return restClient.post()
                .uri("/api/trees/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TreeRequestDto(tableName))
                .retrieve()
                .body(TreeResponseDto.class);
    }

    /**
     * Invia una richiesta HTTP POST per caricare l'albero da un file serializzato {@code .dmp}.
     *
     * @param tableName nome del file da caricare
     * @return risposta contenente le regole e il nodo radice
     * @throws RuntimeException in caso di errore restituito dal server o fallimento della comunicazione HTTP
     */
    public TreeResponseDto loadTree(String tableName) {
        return restClient.post()
                .uri("/api/trees/load")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TreeRequestDto(tableName))
                .retrieve()
                .body(TreeResponseDto.class);
    }

    /**
     * Invia una richiesta HTTP POST per navigare l'albero lungo il percorso indicato.
     *
     * @param tableName nome della tabella o del modello
     * @param path      lista ordinata degli indici dei rami scelti
     * @return DTO descrittivo del nodo raggiunto
     * @throws RuntimeException in caso di errore restituito dal server o fallimento della comunicazione HTTP
     */
    public PredictionStepDto predictStep(String tableName, List<Integer> path) {
        return restClient.post()
                .uri("/api/trees/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PredictRequestDto(tableName, path))
                .retrieve()
                .body(PredictionStepDto.class);
    }
}
