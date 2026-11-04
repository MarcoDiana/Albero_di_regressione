package server;

import java.io.IOException;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Gestisce lo scambio di oggetti con un singolo client in un thread dedicato.
 */
public class ServerOneClient {
    /** Socket connesso al client. */
    private Socket socket;

    /** Flusso da cui vengono lette le richieste del client. */
    private ObjectInputStream in;

    /** Flusso su cui vengono inviate le risposte al client. */
    private ObjectOutputStream out;

    /**
     * Inizializza i flussi di comunicazione e avvia il thread che gestisce il client.
     *
     * @param socket socket connesso al client
     */
    public ServerOneClient(Socket socket) {
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(this::run);
        thread.start();
    }

    /**
     * Legge le richieste del client, invia una risposta per ciascuna e chiude i flussi
     * e il socket al termine della comunicazione.
     */
    public void run(){
        try {
            while (true) {
                Object request = in.readObject();
                out.writeObject("Risposta dal server");
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
