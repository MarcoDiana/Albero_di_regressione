package data;

import java.io.Serializable;

/**
 * La classe astratta Attribute modella un generico attributo discreto o continuo.
 * Implementa l'interfaccia Serializable per consentire la successiva persistenza
 * dell'albero di regressione su file.
 */
public abstract class Attribute implements Serializable {

    // Attributi di classe
    private String name;
    private int index;

    /**
     * Costruttore di classe. Inizializza i valori dei membri name e index.
     *
     * @param name  Nome simbolico dell'attributo
     * @param index Identificativo numerico dell'attributo
     */
    public Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il nome simbolico dell'attributo.
     *
     * @return Il nome dell'attributo (di tipo String)
     */
    public String getName() {
        return this.name;
    }

    /**
     * Restituisce l'identificativo numerico dell'attributo.
     *
     * @return L'indice dell'attributo (di tipo int)
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Ridefinizione del metodo toString() per facilitare la stampa
     * delle informazioni dell'attributo.
     */
    @Override
    public String toString() {
        return name;
    }
}