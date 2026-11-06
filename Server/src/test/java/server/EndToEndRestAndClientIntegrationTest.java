package server;

import dto.PredictRequestDto;
import dto.PredictionStepDto;
import dto.TreeRequestDto;
import dto.TreeResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test End-to-End (E2E) per il backend Spring Boot REST e il contratto client.
 *
 * Verifica in modo live su porta effimera:
 * 1. Apprendimento da MySQL (tabella MapDB.provaC), generazione delle regole e radice
 * 2. Navigazione stateless passo-passo fino alle foglie con predizione numerica continua
 * 3. Caricamento da archivio serializzato .dmp
 * 4. Gestione errori HTTP 400 (Bad Request) e HTTP 404 (Not Found)
 * 5. Disponibilità e correttezza delle specifiche OpenAPI (/v3/api-docs) e Swagger UI (/swagger-ui.html)
 */
@SpringBootTest(classes = ServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Test di integrazione End-to-End REST e Client per Albero di Regressione")
class EndToEndRestAndClientIntegrationTest {

    static {
        System.setProperty("spring.classformat.ignore", "true");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private RestClient restClient;

    @BeforeEach
    void setUpClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanUpDmpFiles() {
        File fileC = new File("provaC.dmp");
        if (fileC.exists()) {
            fileC.delete();
        }
        File fileLower = new File("provac.dmp");
        if (fileLower.exists()) {
            fileLower.delete();
        }
    }

    @Test
    @DisplayName("E2E: Apprendimento albero da MySQL (provaC) -> verifica regole e radice dello split")
    void testLearnTreeFromDatabaseE2E() {
        TreeRequestDto request = new TreeRequestDto("provaC");

        ResponseEntity<TreeResponseDto> response = testRestTemplate.postForEntity(
                "/api/trees/learn",
                request,
                TreeResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "L'apprendimento deve restituire 200 OK");
        TreeResponseDto body = response.getBody();
        assertNotNull(body, "Il corpo della risposta non deve essere nullo");
        assertEquals("provaC", body.tableName());
        assertNotNull(body.treeRules(), "Le regole non devono essere nulle");
        assertTrue(body.treeRules().contains("RULES"), "Le regole devono contenere l'intestazione RULES");
        assertTrue(body.treeRules().contains("Class=1.0"), "Le regole devono contenere la foglia con Class=1.0");
        assertTrue(body.treeRules().contains("Class=10.0"), "Le regole devono contenere la foglia con Class=10.0");

        PredictionStepDto rootStep = body.rootStep();
        assertNotNull(rootStep, "Il rootStep non deve essere nullo");
        assertFalse(rootStep.isLeaf(), "Il nodo radice di provaC non deve essere una foglia");
        assertEquals("X", rootStep.attributeName(), "Lo split radice per provaC deve essere sull'attributo X");
        assertNotNull(rootStep.branchOptions(), "Le opzioni di scelta dello split non devono essere nulle");
        assertEquals(2, rootStep.branchOptions().size(), "Lo split discreto su X deve avere 2 rami (A e B)");

        assertTrue(new File("provaC.dmp").exists(), "L'albero addestrato deve essere stato salvato su disco in provaC.dmp");
    }

    @Test
    @DisplayName("E2E: Navigazione stateless passo-passo fino alle foglie coerente con il protocollo socket")
    void testPredictStepStatelessNavigationE2E() {
        // Step 1: Apprendimento iniziale per popolare la cache del server
        TreeResponseDto learnResult = restClient.post()
                .uri("/api/trees/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TreeRequestDto("provaC"))
                .retrieve()
                .body(TreeResponseDto.class);
        assertNotNull(learnResult);

        // Step 2: Navigazione verso il ramo destro (indice 1: X = B) -> deve raggiungere direttamente la foglia
        PredictRequestDto predictBranch1 = new PredictRequestDto("provaC", List.of(1));
        PredictionStepDto leafStepB = restClient.post()
                .uri("/api/trees/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(predictBranch1)
                .retrieve()
                .body(PredictionStepDto.class);

        assertNotNull(leafStepB);
        assertTrue(leafStepB.isLeaf(), "Il percorso [1] su X=B deve condurre a una foglia");
        assertEquals(10.0, leafStepB.predictedValue(), 1e-6, "Il valore continuo per il ramo B deve essere 10.0");
        assertTrue(leafStepB.branchOptions() == null || leafStepB.branchOptions().isEmpty(), "Una foglia ha branchOptions nullo o vuoto");

        // Step 3: Navigazione verso il ramo sinistro (indice 0: X = A) -> split continuo su Y
        PredictRequestDto predictBranch0 = new PredictRequestDto("provaC", List.of(0));
        PredictionStepDto splitStepY = restClient.post()
                .uri("/api/trees/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(predictBranch0)
                .retrieve()
                .body(PredictionStepDto.class);

        assertNotNull(splitStepY);
        assertFalse(splitStepY.isLeaf(), "Il percorso [0] su X=A conduce allo split continuo Y");
        assertEquals("Y", splitStepY.attributeName(), "L'attributo dello split deve essere Y");
        assertEquals(2, splitStepY.branchOptions().size(), "Lo split continuo su Y ha 2 rami (<=2.0 e >2.0)");

        // Step 4: Sottoramo [0, 0] (Y <= 2.0) -> foglia con valore 1.0
        PredictRequestDto predictBranch00 = new PredictRequestDto("provaC", List.of(0, 0));
        PredictionStepDto leafStepY0 = restClient.post()
                .uri("/api/trees/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(predictBranch00)
                .retrieve()
                .body(PredictionStepDto.class);

        assertNotNull(leafStepY0);
        assertTrue(leafStepY0.isLeaf());
        assertEquals(1.0, leafStepY0.predictedValue(), 1e-6);

        // Step 5: Sottoramo [0, 1] (Y > 2.0) -> foglia con valore 1.5
        PredictRequestDto predictBranch01 = new PredictRequestDto("provaC", List.of(0, 1));
        PredictionStepDto leafStepY1 = restClient.post()
                .uri("/api/trees/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(predictBranch01)
                .retrieve()
                .body(PredictionStepDto.class);

        assertNotNull(leafStepY1);
        assertTrue(leafStepY1.isLeaf());
        assertEquals(1.5, leafStepY1.predictedValue(), 1e-6);
    }

    @Test
    @DisplayName("E2E: Caricamento modello da file .dmp e predizione stateless")
    void testLoadFromFileAndPredictE2E() {
        // Apprendi e salva
        restClient.post()
                .uri("/api/trees/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TreeRequestDto("provaC"))
                .retrieve()
                .body(TreeResponseDto.class);

        // Carica da archivio .dmp
        ResponseEntity<TreeResponseDto> loadResponse = testRestTemplate.postForEntity(
                "/api/trees/load",
                new TreeRequestDto("provaC.dmp"),
                TreeResponseDto.class
        );

        assertEquals(HttpStatus.OK, loadResponse.getStatusCode());
        TreeResponseDto loadBody = loadResponse.getBody();
        assertNotNull(loadBody);
        assertEquals("provaC", loadBody.tableName());
        assertFalse(loadBody.rootStep().isLeaf());
        assertEquals("X", loadBody.rootStep().attributeName());

        // Esegui predizione sul modello caricato
        PredictionStepDto predictResult = restClient.post()
                .uri("/api/trees/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PredictRequestDto("provaC", List.of(1)))
                .retrieve()
                .body(PredictionStepDto.class);

        assertNotNull(predictResult);
        assertTrue(predictResult.isLeaf());
        assertEquals(10.0, predictResult.predictedValue(), 1e-6);
    }

    @Test
    @DisplayName("E2E: Gestione errori 400 Bad Request e 404 Not Found")
    void testErrorHandlingE2E() {
        // 400 su tabella inesistente nel database
        HttpClientErrorException.BadRequest badRequestTable = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> restClient.post()
                        .uri("/api/trees/learn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new TreeRequestDto("tabella_inesistente_xyz_123"))
                        .retrieve()
                        .body(String.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, badRequestTable.getStatusCode());
        assertTrue(badRequestTable.getResponseBodyAsString().contains("error"));

        // 400 su tabella vuota o nulla
        HttpClientErrorException.BadRequest badRequestNull = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> restClient.post()
                        .uri("/api/trees/learn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new TreeRequestDto("   "))
                        .retrieve()
                        .body(String.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, badRequestNull.getStatusCode());

        // 404 su file .dmp non presente su disco
        HttpClientErrorException.NotFound notFoundFile = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restClient.post()
                        .uri("/api/trees/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new TreeRequestDto("archivio_fantasma_xyz.dmp"))
                        .retrieve()
                        .body(String.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, notFoundFile.getStatusCode());
        assertTrue(notFoundFile.getResponseBodyAsString().contains("File non trovato"));

        // 400 su indice di percorso non valido (fuori limite)
        restClient.post()
                .uri("/api/trees/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TreeRequestDto("provaC"))
                .retrieve()
                .body(TreeResponseDto.class);

        HttpClientErrorException.BadRequest invalidPathEx = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> restClient.post()
                        .uri("/api/trees/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new PredictRequestDto("provaC", List.of(99)))
                        .retrieve()
                        .body(String.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, invalidPathEx.getStatusCode());
        assertTrue(invalidPathEx.getResponseBodyAsString().contains("error"));
    }

    @Test
    @DisplayName("E2E: Disponibilità e validità della specifica OpenAPI e della documentazione Swagger UI")
    void testOpenApiAndSwaggerUiAvailabilityE2E() {
        // Verifica OpenAPI JSON v3
        ResponseEntity<String> apiDocsResponse = testRestTemplate.getForEntity("/v3/api-docs", String.class);
        assertEquals(HttpStatus.OK, apiDocsResponse.getStatusCode());
        String apiDocsBody = apiDocsResponse.getBody();
        assertNotNull(apiDocsBody);
        assertTrue(apiDocsBody.contains("openapi"), "La specifica OpenAPI deve contenere la chiave 'openapi'");
        assertTrue(apiDocsBody.contains("/api/trees/learn"), "La specifica deve documentare l'endpoint /api/trees/learn");
        assertTrue(apiDocsBody.contains("/api/trees/load"), "La specifica deve documentare l'endpoint /api/trees/load");
        assertTrue(apiDocsBody.contains("/api/trees/predict"), "La specifica deve documentare l'endpoint /api/trees/predict");

        // Verifica endpoint Swagger UI HTML
        ResponseEntity<String> swaggerResponse = testRestTemplate.getForEntity("/swagger-ui.html", String.class);
        assertTrue(
                swaggerResponse.getStatusCode().is2xxSuccessful() || swaggerResponse.getStatusCode().is3xxRedirection(),
                "Lo Swagger UI deve rispondere con successo 200 o redirect 302 verso la UI interattiva"
        );

        // Verifica diretta della pagina Swagger UI
        ResponseEntity<String> swaggerIndexResponse = testRestTemplate.getForEntity("/swagger-ui/index.html", String.class);
        assertEquals(HttpStatus.OK, swaggerIndexResponse.getStatusCode(), "L'interfaccia Swagger UI index.html deve restituire 200 OK");
        String swaggerHtml = swaggerIndexResponse.getBody();
        assertNotNull(swaggerHtml);
        assertTrue(swaggerHtml.toLowerCase().contains("swagger") || swaggerHtml.contains("html"),
                "La risposta deve contenere il codice HTML di Swagger UI");
    }
}
