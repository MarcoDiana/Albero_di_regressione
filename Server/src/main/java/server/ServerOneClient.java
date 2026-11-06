package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/**
 * Gestisce lo scambio di oggetti con un singolo client in un thread dedicato.
 */
public class ServerOneClient extends Thread {
    /** Socket connesso al client. */
    private Socket socket;

    /** Flusso da cui vengono lette le richieste del client. */
    private ObjectInputStream in;

    /** Flusso su cui vengono inviate le risposte al client. */
    private ObjectOutputStream out;

    /**
     * Inizializza i flussi di comunicazione e avvia il thread che gestisce il client.
     *
     * @param s socket connesso al client
     * @throws IOException se non è possibile aprire i flussi sul socket
     */
    public ServerOneClient(Socket s) throws IOException {
        this.socket = s;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        start();
    }

    /**
     * Riscrive il metodo {@code run} della superclasse {@link Thread} al fine di
     * gestire le richieste del client (acquisizione dati, apprendimento e salvataggio,
     * caricamento da archivio e predizione).
     */
    @Override
    public void run() {
        Data trainingSet = null;
        RegressionTree tree = null;
        String tableName = null;

        try {
            while (true) {
                int decision = (Integer) in.readObject();

                switch (decision) {
                    case 0:
                        tableName = (String) in.readObject();
                        try {
                            trainingSet = new Data(tableName);
                            out.writeObject("OK");
                        } catch (TrainingDataException e) {
                            out.writeObject(e.toString());
                        }
                        break;

                    case 1:
                        try {
                            tree = new RegressionTree(trainingSet);
                            tree.salva(tableName + ".dmp");
                            out.writeObject("OK");
                        } catch (IOException e) {
                            out.writeObject(e.toString());
                        }
                        break;

                    case 2:
                        tableName = (String) in.readObject();
                        try {
                            String archiveName = tableName.endsWith(".dmp") ? tableName : tableName + ".dmp";
                            tree = RegressionTree.carica(archiveName);
                            out.writeObject("OK");
                        } catch (IOException | ClassNotFoundException e) {
                            out.writeObject(e.toString());
                        }
                        break;

                    case 3:
                        try {
                            Double predictedValue = tree.predictClass(out, in);
                            out.writeObject("OK");
                            out.writeObject(predictedValue);
                        } catch (UnknownValueException e) {
                            out.writeObject(e.toString());
                        }
                        break;

                    default:
                        out.writeObject("Invalid command: " + decision);
                        break;
                }
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            // Il client ha chiuso la connessione o si è verificato un errore di comunicazione.
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
