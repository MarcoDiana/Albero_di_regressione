package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test d'integrazione socket TCP su ServerOneClient con porta effimera")
class SocketClientServerIntegrationTest {

    private ServerSocket serverSocket;
    private int port;
    private ExecutorService executor;

    @BeforeEach
    void setUpServer() throws IOException {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        executor = Executors.newCachedThreadPool();

        executor.submit(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    new ServerOneClient(socket);
                }
            } catch (IOException e) {
                // socket chiuso per shutdown del test
            }
        });
    }

    @AfterEach
    void tearDownServer() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (executor != null) {
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        new File("provac.dmp").delete();
    }

    @Test
    @DisplayName("Flusso nominale completo: 0 (acquisizione) -> 1 (apprendimento e salvataggio) -> 3 (predizione)")
    void testFullFlowAcquireLearnPredict() throws Exception {
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.writeObject(0);
            out.writeObject("provac");
            out.flush();
            assertEquals("OK", in.readObject());

            out.writeObject(1);
            out.flush();
            assertEquals("OK", in.readObject());
            assertTrue(new File("provac.dmp").exists());

            out.writeObject(3);
            out.flush();
            assertEquals("QUERY", in.readObject());
            String query = (String) in.readObject();
            assertNotNull(query);

            out.writeObject(1);
            out.flush();

            assertEquals("OK", in.readObject());
            Double predictedValue = (Double) in.readObject();
            assertEquals(10.0, predictedValue, 1e-6);
        }
    }

    @Test
    @DisplayName("Flusso caricamento archivio: 2 (carica .dmp) -> 3 (predizione)")
    void testLoadArchiveAndPredictFlow() throws Exception {
        try (Socket client1 = new Socket("localhost", port);
             ObjectOutputStream out1 = new ObjectOutputStream(client1.getOutputStream());
             ObjectInputStream in1 = new ObjectInputStream(client1.getInputStream())) {

            out1.writeObject(0);
            out1.writeObject("provac");
            out1.flush();
            assertEquals("OK", in1.readObject());

            out1.writeObject(1);
            out1.flush();
            assertEquals("OK", in1.readObject());
        }

        try (Socket client2 = new Socket("localhost", port);
             ObjectOutputStream out2 = new ObjectOutputStream(client2.getOutputStream());
             ObjectInputStream in2 = new ObjectInputStream(client2.getInputStream())) {

            out2.writeObject(2);
            out2.writeObject("provac.dmp");
            out2.flush();
            assertEquals("OK", in2.readObject());

            out2.writeObject(3);
            out2.flush();
            assertEquals("QUERY", in2.readObject());
            in2.readObject();

            out2.writeObject(1);
            out2.flush();

            assertEquals("OK", in2.readObject());
            Double predictedValue = (Double) in2.readObject();
            assertEquals(10.0, predictedValue, 1e-6);
        }
    }

    @Test
    @DisplayName("Gestione errori applicativi: tabella inesistente, archivio mancante e comando sconosciuto")
    void testErrorHandlingOverSocket() throws Exception {
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.writeObject(0);
            out.writeObject("tabella_inesistente_xyz");
            out.flush();
            String res0 = (String) in.readObject();
            assertTrue(res0.contains("TrainingDataException"));

            out.writeObject(2);
            out.writeObject("inesistente.dmp");
            out.flush();
            String res2 = (String) in.readObject();
            assertTrue(res2.contains("FileNotFoundException"));

            out.writeObject(999);
            out.flush();
            String resUnknown = (String) in.readObject();
            assertEquals("Invalid command: 999", resUnknown);
        }
    }
}
