package data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * La classe DiscreteAttribute estende la classe Attribute e rappresenta
 * un attributo discreto (nominale), caratterizzato da un insieme finito di valori.
 */
public class DiscreteAttribute extends Attribute implements Iterable<String> {

    // Set di oggetti String per mantenere i valori ordinati e senza duplicati
    private Set<String> values;

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
        this.values = new TreeSet<>(Arrays.asList(values));
    }

    /**
     * Restituisce il numero di valori discreti che l'attributo può assumere.
     *
     * @return La cardinalità dell'array values (di tipo int)
     */
    public int getNumberOfDistinctValues() {
        return this.values.size();
    }

    /**
     * Restituisce un iteratore per scorrere i valori del Set.
     * Necessario per l'implementazione dell'interfaccia Iterable<String>.
     *
     * @return Iteratore di stringhe
     */
    @Override
    public Iterator<String> iterator() {
        return this.values.iterator();
    }
}