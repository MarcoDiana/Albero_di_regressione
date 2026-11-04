package database;

/**
 * Descrive una colonna di una tabella e indica se contiene valori numerici.
 */
public class Column{
	/** Nome della colonna nel database. */
	private String name;

	/** Categoria del tipo di colonna ({@code "number"} o {@code "string"}). */
	private String type;

	/**
	 * Crea la descrizione di una colonna.
	 *
	 * @param name nome della colonna
	 * @param type categoria del tipo di colonna
	 */
	Column(String name,String type){
		this.name=name;
		this.type=type;
	}

	/**
	 * Restituisce il nome della colonna.
	 *
	 * @return nome della colonna
	 */
	public String getColumnName(){
		return name;
	}

	/**
	 * Indica se la colonna è numerica.
	 *
	 * @return {@code true} se la colonna è numerica
	 */
	public boolean isNumber(){
		return type.equals("number");
	}

	/**
	 * Restituisce il nome della colonna e la categoria del suo tipo.
	 *
	 * @return descrizione nel formato {@code nome:tipo}
	 */
	public String toString(){
		return name+":"+type;
	}
}