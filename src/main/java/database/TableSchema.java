package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;




/**
 * Legge e rappresenta lo schema delle colonne di una tabella del database.
 */
public class TableSchema implements Iterable<Column>{
	
	/** Colonne riconosciute, nell'ordine restituito dai metadati JDBC. */
	private List<Column> tableSchema=new ArrayList<Column>();
	
	/**
	 * Carica i nomi e i tipi delle colonne della tabella.
	 *
	 * @param db accesso al database già connesso
	 * @param tableName nome della tabella
	 * @throws SQLException se non è possibile leggere i metadati della tabella
	 */
	public TableSchema(DbAccess db, String tableName) throws SQLException{
		
		HashMap<String,String> mapSQL_JAVATypes=new HashMap<String, String>();
	
		mapSQL_JAVATypes.put("CHAR","string");
		mapSQL_JAVATypes.put("VARCHAR","string");
		mapSQL_JAVATypes.put("LONGVARCHAR","string");
		mapSQL_JAVATypes.put("BIT","string");
		mapSQL_JAVATypes.put("SHORT","number");
		mapSQL_JAVATypes.put("INT","number");
		mapSQL_JAVATypes.put("LONG","number");
		mapSQL_JAVATypes.put("FLOAT","number");
		mapSQL_JAVATypes.put("DOUBLE","number");
		
		
	
		 Connection con=db.getConnection();
		 DatabaseMetaData meta = con.getMetaData();
	     ResultSet res = meta.getColumns(null, null, tableName, null);
		   
	     while (res.next()) {
	         
	         if(mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME")))
	        		 tableSchema.add(new Column(
	        				 res.getString("COLUMN_NAME"),
	        				 mapSQL_JAVATypes.get(res.getString("TYPE_NAME")))
	        				 );
	
	         
	         
	      }
	      res.close();
	
	
	    
	    }
	  
	
		/**
		 * Restituisce il numero di colonne riconosciute nello schema.
		 *
		 * @return numero di colonne
		 */
		public int getNumberOfAttributes(){
			return tableSchema.size();
		}
		
		/**
		 * Restituisce la colonna nella posizione indicata.
		 *
		 * @param index indice della colonna
		 * @return descrizione della colonna
		 */
		public Column getColumn(int index){
			return tableSchema.get(index);
		}


		/**
		 * Restituisce un iteratore sulle colonne dello schema.
		 *
		 * @return iteratore sulle colonne
		 */
		@Override
		public Iterator<Column> iterator() {
			return tableSchema.iterator();
		}

		
	}

		     

