package server;

import data.Data;
import data.TrainingDataException;
import dto.PredictionStepDto;
import dto.TreeResponseDto;
import org.springframework.stereotype.Service;
import tree.RegressionTree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servizio applicativo per la gestione degli alberi di regressione.
 *
 * Mantiene una cache thread-safe in memoria degli alberi attivi e fornisce i metodi
 * per l'apprendimento da database MySQL, il caricamento/salvataggio su file {@code .dmp}
 * e la navigazione stateless per la predizione.
 */
@Service
public class TreeService {

    /** Cache concorrente che associa il nome della tabella o del modello all'albero appreso. */
    private final Map<String, RegressionTree> activeTrees = new ConcurrentHashMap<>();

    /**
     * Apprende un albero di regressione dalla tabella indicata su database MySQL,
     * serializza il modello su file {@code <tableName>.dmp}, lo memorizza nella cache
     * e restituisce la risposta contenente le regole e le informazioni sul nodo radice.
     *
     * @param tableName nome della tabella di training su database
     * @return DTO contenente il nome della tabella, le regole formattate e il nodo radice
     * @throws TrainingDataException se i dati di training sono invalidi o la tabella non esiste
     * @throws IOException           se si verifica un errore durante la serializzazione su disco
     */
    public TreeResponseDto learnFromDb(String tableName) throws TrainingDataException, IOException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della tabella non puo' essere vuoto o nullo.");
        }
        String cleanTableName = tableName.trim();
        Data data = new Data(cleanTableName);
        RegressionTree tree = new RegressionTree(data);
        String fileName = cleanTableName + ".dmp";
        tree.salva(fileName);
        activeTrees.put(cleanTableName, tree);
        activeTrees.put(fileName, tree);

        try {
            PredictionStepDto rootStep = tree.navigate(List.of());
            return new TreeResponseDto(cleanTableName, tree.getRules(), rootStep);
        } catch (UnknownValueException e) {
            throw new IllegalStateException("Errore inatteso nella navigazione della radice: " + e.getMessage(), e);
        }
    }

    /**
     * Carica un albero di regressione da file serializzato {@code .dmp}, lo memorizza
     * nella cache attiva e restituisce le regole estratte e il nodo radice.
     *
     * @param tableName nome del file (con o senza estensione {@code .dmp}) da caricare
     * @return DTO contenente il nome dell'albero, le regole e il nodo radice
     * @throws FileNotFoundException  se il file indicato non esiste su disco
     * @throws IOException            se si verifica un errore durante la lettura da disco
     * @throws ClassNotFoundException se il formato dell'oggetto serializzato non e' riconosciuto
     */
    public TreeResponseDto loadFromFile(String tableName) throws IOException, ClassNotFoundException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del file/tabella non puo' essere vuoto o nullo.");
        }
        String cleanTableName = tableName.trim();
        String fileName = cleanTableName.endsWith(".dmp") ? cleanTableName : cleanTableName + ".dmp";
        String baseName = cleanTableName.endsWith(".dmp")
                ? cleanTableName.substring(0, cleanTableName.length() - 4)
                : cleanTableName;

        RegressionTree tree = RegressionTree.carica(fileName);
        activeTrees.put(baseName, tree);
        activeTrees.put(fileName, tree);

        try {
            PredictionStepDto rootStep = tree.navigate(List.of());
            return new TreeResponseDto(baseName, tree.getRules(), rootStep);
        } catch (UnknownValueException e) {
            throw new IllegalStateException("Errore inatteso nella navigazione della radice: " + e.getMessage(), e);
        }
    }

    /**
     * Esegue un passo di navigazione stateless nell'albero indicato seguendo il percorso dei rami.
     * Se l'albero non e' presente nella cache in memoria, tenta automaticamente il caricamento da disco.
     *
     * @param tableName nome della tabella o del file associato all'albero
     * @param path      lista di interi che rappresenta gli indici dei rami scelti dalla radice
     * @return DTO descrittivo del nodo raggiunto (foglia o split)
     * @throws IOException            se l'albero deve essere letto da disco e si verifica un errore I/O
     * @throws ClassNotFoundException se la classe serializzata non e' trovata
     * @throws UnknownValueException  se uno degli indici nel percorso non e' valido
     */
    public PredictionStepDto predictStep(String tableName, List<Integer> path)
            throws IOException, ClassNotFoundException, UnknownValueException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della tabella non puo' essere vuoto o nullo.");
        }
        String cleanTableName = tableName.trim();
        RegressionTree tree = activeTrees.get(cleanTableName);
        if (tree == null) {
            String fileName = cleanTableName.endsWith(".dmp") ? cleanTableName : cleanTableName + ".dmp";
            String baseName = cleanTableName.endsWith(".dmp")
                    ? cleanTableName.substring(0, cleanTableName.length() - 4)
                    : cleanTableName;
            tree = RegressionTree.carica(fileName);
            activeTrees.put(baseName, tree);
            activeTrees.put(fileName, tree);
        }
        return tree.navigate(path != null ? path : List.of());
    }

    /**
     * Svuota completamente la cache in memoria degli alberi attivi.
     * Utile per test e riallineamenti.
     */
    public void clearCache() {
        activeTrees.clear();
    }
}
