package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;



/**
 * Recupera le tuple e i valori delle colonne di una tabella del database.
 */
public class TableData {

	/** Accesso al database usato per eseguire le query. */
	private DbAccess db;
	

	
	/**
	 * Crea un lettore di dati associato all'accesso al database indicato.
	 *
	 * @param db accesso al database già connesso
	 */
	public TableData(DbAccess db) {
		this.db=db;
	}

	/**
	 * Legge tutte le tuple della tabella, convertendo le colonne numeriche in
	 * {@link Double} e quelle testuali in {@link String}.
	 *
	 * @param table nome della tabella
	 * @return tuple lette, nell'ordine delle colonne dello schema
	 * @throws SQLException se la tabella o i suoi dati non possono essere letti
	 * @throws EmptySetException se la tabella non contiene tuple
	 */
	public List<Example> getTransazioni(String table) throws SQLException, EmptySetException{
		LinkedList<Example> transSet = new LinkedList<Example>();
		Statement statement;
		TableSchema tSchema=new TableSchema(db,table);
		
		
		String query="select ";
		
		for(int i=0;i<tSchema.getNumberOfAttributes();i++){
			Column c=tSchema.getColumn(i);
			if(i>0)
				query+=",";
			query += c.getColumnName();
		}
		if(tSchema.getNumberOfAttributes()==0)
			throw new SQLException();
		query += (" FROM "+table);
		
		statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);
		boolean empty=true;
		while (rs.next()) {
			empty=false;
			Example currentTuple=new Example();
			for(int i=0;i<tSchema.getNumberOfAttributes();i++)
				if(tSchema.getColumn(i).isNumber())
					currentTuple.add(rs.getDouble(i+1));
				else
					currentTuple.add(rs.getString(i+1));
			transSet.add(currentTuple);
		}
		rs.close();
		statement.close();
		if(empty) throw new EmptySetException("La tabella non contiene tuple");
		
		
		return transSet;

	}

	/**
	 * Legge i valori distinti della colonna, ordinandoli secondo il database.
	 *
	 * @param table nome della tabella
	 * @param column colonna di cui leggere i valori
	 * @return insieme ordinato dei valori distinti
	 * @throws SQLException se la query non può essere eseguita
	 */
	public Set<Object> getDistinctColumnValues(String table, Column column) throws SQLException{
		Set<Object> distinctValues = new TreeSet<Object>();
		Statement statement;
		String query="select distinct "+column.getColumnName()+" FROM "+table+" ORDER BY "+column.getColumnName();

		statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);

		while (rs.next()) {
			if(column.isNumber())
				distinctValues.add(rs.getDouble(1));
			else
				distinctValues.add(rs.getString(1));
		}
		rs.close();
		statement.close();

		return distinctValues;
	}

	

}
