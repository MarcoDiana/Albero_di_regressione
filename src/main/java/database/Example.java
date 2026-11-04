package database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Rappresenta una tupla di valori letta da una tabella del database.
 */
public class Example implements Comparable<Example>, Iterable<Object>{
	/** Valori della tupla nell'ordine delle colonne della tabella. */
	private List<Object> example=new ArrayList<Object>();

	/**
	 * Aggiunge un valore alla fine della tupla.
	 *
	 * @param o valore da aggiungere
	 */
	public void add(Object o){
		example.add(o);
	}
	
	/**
	 * Restituisce il valore nella posizione indicata.
	 *
	 * @param i indice del valore nella tupla
	 * @return valore corrispondente
	 */
	public Object get(int i){
		return example.get(i);
	}

	/**
	 * Confronta questa tupla con un'altra confrontandone i valori in ordine.
	 *
	 * @param ex tupla da confrontare
	 * @return un valore negativo, zero o positivo secondo l'ordine lessicografico
	 */
	public int compareTo(Example ex) {
		
		int i=0;
		for(Object o:ex.example){
			if(!o.equals(this.example.get(i)))
				return ((Comparable)o).compareTo(example.get(i));
			i++;
		}
		return 0;
	}

	/**
	 * Restituisce i valori della tupla separati da spazi.
	 *
	 * @return rappresentazione testuale della tupla
	 */
	public String toString(){
		String str="";
		for(Object o:example)
			str+=o.toString()+ " ";
		return str;
	}

	/**
	 * Restituisce un iteratore sui valori della tupla.
	 *
	 * @return iteratore sui valori della tupla
	 */
	@Override
	public Iterator<Object> iterator() {
		return example.iterator();
	}
	
}