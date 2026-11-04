package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce la connessione al database e ne fornisce l'accesso.
 */
public class DbAccess {

    /** Nome della classe del driver JDBC MySQL. */
    private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    /** Schema JDBC usato per costruire l'URL di connessione. */
    private final String DBMS = "jdbc:mysql";

    /** Indirizzo del server del database. */
    private String SERVER = "localhost";

    /** Nome del database a cui connettersi. */
    private String DATABASE = "MapUser";

    /** Password dell'utente del database. */
    private String PASSWORD = "map";

    /** Connessione JDBC attiva, se inizializzata. */
    private Connection conn;

    /**
     * Carica il driver JDBC e apre la connessione al database configurato.
     *
     * @throws DatabaseConnectionException se il driver non è disponibile o non è
     *                                     possibile stabilire la connessione
     */
    public void initConnection() throws DatabaseConnectionException {
        try {
            Class.forName(DRIVER_CLASS_NAME);

            String url = DBMS + "://" + SERVER + "/" + DATABASE;

            conn = DriverManager.getConnection(url, "root", PASSWORD);
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