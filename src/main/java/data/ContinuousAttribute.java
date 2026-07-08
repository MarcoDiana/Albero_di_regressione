package data;

/**
 * La classe ContinuousAttribute estende la classe Attribute e rappresenta
 * un attributo continuo (numerico/reale), il cui dominio è l'insieme dei numeri reali.
 */
public class ContinuousAttribute extends Attribute {

    /**
     * Costruttore di classe. Inizializza il nome simbolico e l'identificativo
     * numerico dell'attributo richiamando il costruttore della super-classe.
     *
     * @param name  Nome simbolico dell'attributo
     * @param index Identificativo numerico dell'attributo
     */
    public ContinuousAttribute(String name, int index) {
        // Invoca il costruttore della super-classe Attribute
        super(name, index);
    }
}