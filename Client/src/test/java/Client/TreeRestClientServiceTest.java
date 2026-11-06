package Client;

import dto.PredictionStepDto;
import dto.TreeResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("Test unitari e d'integrazione client HTTP per TreeRestClientService")
class TreeRestClientServiceTest {

    @Test
    @DisplayName("learnTree: invia richiesta POST corretta e deserializza TreeResponseDto")
    void testLearnTreeSuccess() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        TreeRestClientService service = new TreeRestClientService(builder, "http://localhost:8080");

        String mockResponseBody = """
                {
                    "tableName": "provaC",
                    "treeRules": "RULES: X=A AND Y<=2.0 ==> Class=1.0",
                    "rootStep": {
                        "isLeaf": false,
                        "predictedValue": null,
                        "attributeName": "X",
                        "branchOptions": ["=A", "=B"]
                    }
                }
                """;

        mockServer.expect(requestTo("http://localhost:8080/api/trees/learn"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tableName").value("provaC"))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        TreeResponseDto response = service.learnTree("provaC");

        mockServer.verify();
        assertNotNull(response);
        assertEquals("provaC", response.tableName());
        assertTrue(response.treeRules().contains("RULES"));
        assertNotNull(response.rootStep());
        assertFalse(response.rootStep().isLeaf());
        assertEquals("X", response.rootStep().attributeName());
        assertEquals(2, response.rootStep().branchOptions().size());
    }

    @Test
    @DisplayName("loadTree: invia richiesta POST corretta e deserializza TreeResponseDto")
    void testLoadTreeSuccess() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        TreeRestClientService service = new TreeRestClientService(builder, "http://localhost:8080");

        String mockResponseBody = """
                {
                    "tableName": "provaC",
                    "treeRules": "RULES: X=B ==> Class=10.0",
                    "rootStep": {
                        "isLeaf": false,
                        "predictedValue": null,
                        "attributeName": "X",
                        "branchOptions": ["=A", "=B"]
                    }
                }
                """;

        mockServer.expect(requestTo("http://localhost:8080/api/trees/load"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tableName").value("provaC"))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        TreeResponseDto response = service.loadTree("provaC");

        mockServer.verify();
        assertNotNull(response);
        assertEquals("provaC", response.tableName());
        assertFalse(response.rootStep().isLeaf());
    }

    @Test
    @DisplayName("predictStep: invia percorso rami e riceve foglia con valore numerico")
    void testPredictStepSuccess() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        TreeRestClientService service = new TreeRestClientService(builder, "http://localhost:8080");

        String mockResponseBody = """
                {
                    "isLeaf": true,
                    "predictedValue": 10.0,
                    "attributeName": null,
                    "branchOptions": null
                }
                """;

        mockServer.expect(requestTo("http://localhost:8080/api/trees/predict"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tableName").value("provaC"))
                .andExpect(jsonPath("$.path[0]").value(1))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        PredictionStepDto step = service.predictStep("provaC", List.of(1));

        mockServer.verify();
        assertNotNull(step);
        assertTrue(step.isLeaf());
        assertEquals(10.0, step.predictedValue(), 1e-6);
        assertNull(step.branchOptions());
    }

    @Test
    @DisplayName("Gestione errori server: estrae messaggio descrittivo da body JSON di errore")
    void testErrorHandlingWithJsonMessage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        TreeRestClientService service = new TreeRestClientService(builder, "http://localhost:8080");

        String errorBody = """
                {
                    "error": "Tabella inesistente nel database MapDB"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8080/api/trees/learn"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body(errorBody).contentType(MediaType.APPLICATION_JSON));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.learnTree("inesistente"));

        mockServer.verify();
        assertEquals("Tabella inesistente nel database MapDB", thrown.getMessage());
    }
}
