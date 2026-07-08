package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * La classe Data modella l'insieme di esempi di training.
 * Analizza e legge autonomamente la struttura di un file di testo
 * per configurare gli attributi e popolare la matrice dei dati.
 */
public class Data {

    // Matrice Object che contiene il training set (numero esempi X numero attributi)
    private Object[][] data;

    // Cardinalità (numero di righe) del training set
    private int numberOfExamples;

    // Array di oggetti Attribute per rappresentare gli attributi indipendenti (esplicativi)
    private Attribute[] explanatorySet;

    // Oggetto ContinuousAttribute per modellare l'attributo di classe target (numerico)
    private ContinuousAttribute classAttribute;

    /**
     * Costruttore di classe. Esegue il parsing del file configurando
     * gli attributi esplicativi, l'attributo target e la matrice dei dati.
     *
     * @param fileName Nome del file di testo contenente il dataset (es. "servo.dat")
     */
    public Data(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int explanatoryCount = 0;
            int currentExample = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 1. Legge il numero totale di attributi indipendenti
                if (line.startsWith("@schema")) {
                    int totalAttributes = Integer.parseInt(line.split("\\s+")[1]);
                    explanatorySet = new Attribute[totalAttributes];
                }
                // 2. Crea dinamicamente un DiscreteAttribute per ogni riga @desc
                else if (line.startsWith("@desc")) {
                    String[] tokens = line.split("\\s+");
                    String attrName = tokens[1];
                    String[] attrValues = tokens[2].split(",");

                    explanatorySet[explanatoryCount] = new DiscreteAttribute(attrName, explanatoryCount, attrValues);
                    explanatoryCount++;
                }
                // 3. Crea l'attributo continuo di classe target
                else if (line.startsWith("@target")) {
                    String targetName = line.split("\\s+")[1];
                    classAttribute = new ContinuousAttribute(targetName, explanatoryCount);
                }
                // 4. Inizializza la matrice quando incontra il tag @data
                else if (line.startsWith("@data")) {
                    numberOfExamples = Integer.parseInt(line.split("\\s+")[1]);
                    data = new Object[numberOfExamples][explanatoryCount + 1];
                }
                // 5. Popola la matrice con le righe di dati effettive
                else {
                    String[] values = line.split(",");
                    // Popola gli attributi esplicativi indipendenti (String)
                    for (int j = 0; j < explanatorySet.length; j++) {
                        data[currentExample][j] = values[j].trim();
                    }
                    // Popola l'attributo target di classe (convertito in Double)
                    int targetIndex = classAttribute.getIndex();
                    data[currentExample][targetIndex] = Double.valueOf(values[targetIndex].trim());
                    currentExample++;
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file: " + e.getMessage());
        }
    }

    /**
     * Restituisce la cardinalità dell'insieme di esempi (numero di righe).
     *
     * @return Il numero di esempi di training (int)
     */
    public int getNumberOfExamples() {
        return this.numberOfExamples;
    }

    /**
     * Restituisce la cardinalità dell'insieme degli attributi indipendenti.
     *
     * @return Il numero di attributi esplicativi (int)
     */
    public int getNumberOfExplanatoryAttributes() {
        return this.explanatorySet.length;
    }

    /**
     * Restituisce il valore numerico dell'attributo di classe per l'esempio specificato.
     *
     * @param exampleIndex Indice di riga dell'esempio nella matrice data[][]
     * @return Il valore Double dell'attributo target
     */
    public Double getClassValue(int exampleIndex) {
        return (Double) this.data[exampleIndex][this.classAttribute.getIndex()];
    }

    /**
     * Restituisce il valore di un attributo indipendente per uno specifico esempio.
     *
     * @param exampleIndex   Indice di riga dell'esempio nella matrice data[][]
     * @param attributeIndex Indice di colonna dell'attributo indipendente
     * @return L'oggetto Object associato all'attributo richiesto
     */
    public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
        return this.data[exampleIndex][attributeIndex];
    }

    /**
     * Restituisce l'oggetto Attribute esplicativo memorizzato all'indice specificato.
     *
     * @param index Indice nell'array explanatorySet[]
     * @return L'oggetto Attribute corrispondente
     */
    public Attribute getExplanatoryAttribute(int index) {
        return this.explanatorySet[index];
    }

    /**
     * Restituisce l'oggetto ContinuousAttribute associato all'attributo di classe.
     *
     * @return L'attributo di classe target
     */
    public ContinuousAttribute getClassAttribute() {
        return this.classAttribute;
    }

    /**
     * Concatena in una stringa i valori di tutti gli attributi per ogni esempio.
     *
     * @return Una stringa testuale che rappresenta l'intero dataset in memoria
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < numberOfExamples; i++) {
            s.append(i + 1).append(": ");
            for (int j = 0; j < explanatorySet.length; j++) {
                s.append(data[i][j]).append(", ");
            }
            s.append(data[i][classAttribute.getIndex()]).append("\n");
        }
        return s.toString();
    }

    /**
     * Scambia internamente due intere righe (vettori Object[]) della matrice data[][],
     * garantendo che l'associazione tra attributi esplicativi e valore target rimanga coerente.
     *
     * @param i Indice della prima riga da scambiare
     * @param j Indice della seconda riga da scambiare
     */
    private void swap(int i, int j) {
        Object[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    /**
     * Ordina il sottoinsieme di esempi compresi nell'intervallo [beginExampleIndex, endExampleIndex]
     * rispetto allo specifico attributo passato in input.
     *
     * @param attribute         Attributo su cui basare l'ordinamento
     * @param beginExampleIndex Indice iniziale del sottoinsieme
     * @param endExampleIndex   Indice finale del sottoinsieme
     */
    public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
        quicksort(attribute, beginExampleIndex, endExampleIndex);
    }

    /**
     * Struttura ricorsiva di controllo del Quicksort. Identifica il tipo di attributo
     * a runtime per invocare il corretto metodo di partizionamento.
     *
     * @param attribute Attributo su cui ordinare
     * @param inf       Indice inferiore del sottointervallo
     * @param sup       Indice superiore del sottointervallo
     */
    private void quicksort(Attribute attribute, int inf, int sup) {
        if (inf < sup) {
            int pivot;
            // Uso del meccanismo RTTI (instanceof) per smistare il partizionamento polimorfico
            if (attribute instanceof DiscreteAttribute) {
                pivot = partition((DiscreteAttribute) attribute, inf, sup);
            } else {
                pivot = partition((ContinuousAttribute) attribute, inf, sup);
            }
            quicksort(attribute, inf, pivot - 1);
            quicksort(attribute, pivot + 1, sup);
        }
    }

    /**
     * Esegue il partizionamento per attributi DISCRETI.
     * Sfrutta l'ordinamento lessicografico tramite il metodo String.compareTo().
     *
     * @param attribute L'attributo discreto di riferimento
     * @param inf       Indice inferiore
     * @param sup       Indice superiore
     * @return L'indice del pivot posizionato
     */
    private int partition(DiscreteAttribute attribute, int inf, int sup) {
        int attributeIndex = attribute.getIndex();
        String pivotValue = (String) data[inf][attributeIndex];
        int i = inf;
        int j = sup;

        while (i < j) {
            while (i <= sup && ((String) data[i][attributeIndex]).compareTo(pivotValue) <= 0) {
                i++;
            }
            while (j >= inf && ((String) data[j][attributeIndex]).compareTo(pivotValue) > 0) {
                j--;
            }
            if (i < j) {
                swap(i, j);
            }
        }
        swap(inf, j);
        return j;
    }

    /**
     * Esegue il partizionamento per attributi CONTINUI.
     * Sfrutta l'ordinamento numerico crescente tramite il metodo Double.compareTo().
     *
     * @param attribute L'attributo continuo di riferimento
     * @param inf       Indice inferiore
     * @param sup       Indice superiore
     * @return L'indice del pivot posizionato
     */
    private int partition(ContinuousAttribute attribute, int inf, int sup) {
        int attributeIndex = attribute.getIndex();
        Double pivotValue = (Double) data[inf][attributeIndex];
        int i = inf;
        int j = sup;

        while (i < j) {
            while (i <= sup && ((Double) data[i][attributeIndex]).compareTo(pivotValue) <= 0) {
                i++;
            }
            while (j >= inf && ((Double) data[j][attributeIndex]).compareTo(pivotValue) > 0) {
                j--;
            }
            if (i < j) {
                swap(i, j);
            }
        }
        swap(inf, j);
        return j;
    }

    /**
     * Metodo d'esecuzione per testare il funzionamento del caricamento dinamico e del sorting.
     *
     * @param args Argomenti da linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        Data trainingData = new Data("servo.dat");
        System.out.println(trainingData);

        if (trainingData.getNumberOfExplanatoryAttributes() > 0) {
            Attribute attribute = trainingData.getExplanatoryAttribute(0);
            System.out.println("ORDINAMENTO PER ATTRIBUTO: " + attribute.getName());
            trainingData.sort(attribute, 0, trainingData.getNumberOfExamples() - 1);
            System.out.println(trainingData);
        }
    }
}