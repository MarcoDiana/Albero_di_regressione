package data;

/**
 * La classe DiscreteAttribute estende la classe Attribute e rappresenta
 * un attributo discreto (nominale), caratterizzato da un insieme finito di valori.
 */
public class DiscreteAttribute extends Attribute {

    // Array di oggetti String, uno per ciascun valore discreto che l'attributo può assumere
    private String[] values;

    /**
     * Costruttore di classe. Inizializza il nome, l'indice e l'insieme dei valori discreti.
     *
     * @param name   Nome simbolico dell'attributo
     * @param index  Identificativo numerico dell'attributo
     * @param values Array di stringhe contenente i valori discreti dell'attributo
     */
    public DiscreteAttribute(String name, int index, String[] values) {
        // Invoca il costruttore della super-classe Attribute
        super(name, index);
        this.values = values;
    }

    /**
     * Restituisce il numero di valori discreti che l'attributo può assumere.
     *
     * @return La cardinalità dell'array values (di tipo int)
     */
    public int getNumberOfDistinctValues() {
        return this.values.length;
    }

    /**
     * Restituisce lo specifico valore discreto presente all'indice specificato.
     *
     * @param i Indice dell'elemento da restituire rispetto all'array values
     * @return La stringa corrispondente al valore discreto in posizione i
     */
    public String getValue(int i) {
        return this.values[i];
    }
}