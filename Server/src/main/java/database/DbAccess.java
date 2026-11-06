package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce la connessione al database e ne fornisce l'accesso.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class DbAccess {

    /** Nome della classe del driver JDBC MySQL. */
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    /** Schema JDBC usato per costruire l'URL di connessione. */
    private static final String DBMS = "jdbc:mysql";

    /** Porta su cui il DBMS MySQL accetta le connessioni. */
    private static final int PORT = 3306;

    /** Indirizzo del server del database. */
    private String SERVER = "localhost";

    /** Nome del database a cui connettersi. */
    private String DATABASE = "MapDB";

    /** Nome dell'utente per l'accesso alla base di dati. */
    private String USER_ID = "MapUser";

    /** Password dell'utente del database. */
    private String PASSWORD = "map";

    /** Connessione JDBC attiva, se inizializzata. */
    private Connection conn;

    /**
     * Costruttore predefinito: inizializza l'accesso al database con i parametri configurati.
     */
    public DbAccess() {
    }

    /**
     * Carica il driver JDBC e apre la connessione al database configurato.
     *
     * @throws DatabaseConnectionException se il driver non è disponibile o non è
     *                                     possibile stabilire la connessione
     */
    public void initConnection() throws DatabaseConnectionException {
        try {
            Class.forName(DRIVER_CLASS_NAME);

            String url = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE;

            conn = DriverManager.getConnection(url, USER_ID, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new DatabaseConnectionException("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to connect to the database: " + e.getMessage());
        }
    }

    /**
     * Restituisce la connessione JDBC corrente.
     *
     * @return la connessione, oppure {@code null} se non è stata inizializzata
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Restituisce la connessione JDBC corrente.
     *
     * @return la connessione, oppure {@code null} se non è stata inizializzata
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Chiude la connessione JDBC, se è stata inizializzata.
     *
     * @throws DatabaseConnectionException se si verifica un errore durante la chiusura
     */
    public void closeConnection() throws DatabaseConnectionException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Failed to close the database connection: " + e.getMessage());
            }
        }
    }
}
