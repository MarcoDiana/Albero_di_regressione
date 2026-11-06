package data;

import database.Example;
import database.DbAccess;
import database.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Modella l'insieme degli esempi di training.
 * Il training set contiene una lista di esempi, ciascuno con i valori degli attributi
 * indipendenti seguiti dal valore dell'attributo di classe.
 *
 * Lo schema e gli esempi vengono letti dalle colonne e dalle righe di una tabella
 * del database. L'ultima colonna è l'attributo di classe e deve essere numerica.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class Data {

	/** Esempi del training set, ciascuno con i valori ordinati come le colonne della tabella. */
	private List<Example> data = new ArrayList<Example>();

	/** Cardinalità del training set. */
	private int numberOfExamples;

	/** Attributi indipendenti (esplicativi) del training set. */
	private List<Attribute> explanatorySet = new LinkedList<Attribute>();

	/** Attributo di classe (target), di tipo continuo. */
	private ContinuousAttribute classAttribute;

	/**
	 * Carica lo schema e gli esempi di addestramento dalla tabella indicata.
	 * Le colonne numeriche sono rappresentate da attributi continui; le altre
	 * colonne indipendenti sono rappresentate da attributi discreti.
	 *
	 * @param tableName nome della tabella che contiene il training set
	 * @throws TrainingDataException se la connessione fallisce, la tabella non esiste,
	 *                               ha meno di due colonne o nessuna riga, oppure
	 *                               l'ultima colonna non è numerica
	 */
	public Data(String tableName) throws TrainingDataException {
		if (tableName == null || tableName.trim().isEmpty())
			throw new TrainingDataException("Il nome della tabella non può essere vuoto");

		DbAccess db = new DbAccess();
		try {
			db.initConnection();
			String query = "SELECT * FROM `" + tableName.replace("`", "``") + "`";
			try (Connection connection = db.getConnection();
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(query)) {
				ResultSetMetaData metadata = resultSet.getMetaData();
				int columnCount = metadata.getColumnCount();
				if (columnCount < 2)
					throw new TrainingDataException("La tabella " + tableName + " deve avere almeno due colonne");

				int classIndex = columnCount - 1;
				boolean[] numericColumns = new boolean[columnCount];
				String[] columnNames = new String[columnCount];
				List<Set<String>> discreteDomains = new ArrayList<Set<String>>(columnCount);
				for (int column = 0; column < columnCount; column++) {
					numericColumns[column] = isNumericType(metadata.getColumnType(column + 1));
					columnNames[column] = metadata.getColumnName(column + 1);
					discreteDomains.add(numericColumns[column] ? null : new TreeSet<String>());
				}
				if (!numericColumns[classIndex])
					throw new TrainingDataException("L'ultima colonna della tabella " + tableName + " deve essere numerica");

				while (resultSet.next()) {
					Example example = new Example();
					for (int column = 0; column < columnCount; column++) {
						Object value = resultSet.getObject(column + 1);
						if (value == null)
							throw new TrainingDataException("Valore nullo nella colonna "
									+ columnNames[column] + " della tabella " + tableName);

						if (numericColumns[column]) {
							if (!(value instanceof Number))
								throw new TrainingDataException("Valore non numerico nella colonna "
										+ columnNames[column] + " della tabella " + tableName);
							example.add(((Number) value).doubleValue());
						} else {
							String discreteValue = value.toString();
							example.add(discreteValue);
							if (column < classIndex)
								discreteDomains.get(column).add(discreteValue);
						}
					}
					data.add(example);
				}
				if (data.isEmpty())
					throw new TrainingDataException("La tabella " + tableName + " non contiene tuple");

				for (int column = 0; column < classIndex; column++) {
					if (numericColumns[column])
						explanatorySet.add(new ContinuousAttribute(columnNames[column], column));
					else
						explanatorySet.add(new DiscreteAttribute(columnNames[column], column,
								discreteDomains.get(column).toArray(new String[0])));
				}
				classAttribute = new ContinuousAttribute(columnNames[classIndex], classIndex);
				numberOfExamples = data.size();
			}
		} catch (DatabaseConnectionException | SQLException e) {
			throw new TrainingDataException("Impossibile caricare il training set dalla tabella "
					+ tableName, e);
		}
	}

	/**
	 * Verifica se un tipo SQL rappresenta un valore numerico supportato.
	 *
	 * @param sqlType tipo JDBC restituito dai metadati della colonna
	 * @return {@code true} se il tipo è numerico, {@code false} altrimenti
	 */
	private boolean isNumericType(int sqlType) {
		switch (sqlType) {
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
		case Types.NUMERIC:
		case Types.DECIMAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Restituisce il numero di esempi nel training set.
	 *
	 * @return cardinalità del training set
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero di attributi indipendenti (esplicativi).
	 * Corrisponde alla cardinalità di explanatorySet ed è la posizione
	 * dell'attributo di classe in ciascun esempio.
	 *
	 * @return numero di attributi indipendenti
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore dell'attributo di classe per un esempio.
	 * Il valore è memorizzato dopo gli attributi indipendenti in ciascun esempio.
	 *
	 * @param exampleIndex indice dell'esempio
	 * @return valore Double dell'attributo di classe
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	/**
	 * Restituisce il valore di un attributo indipendente per un dato esempio.
	 * L'indice dell'attributo coincide con la posizione del valore nell'esempio.
	 *
	 * @param exampleIndex   indice dell'esempio
	 * @param attributeIndex indice dell'attributo indipendente
	 * @return valore dell'attributo (String se discreto, Double se continuo)
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce l'attributo indipendente alla posizione indicata.
	 *
	 * @param index posizione in explanatorySet
	 * @return attributo indipendente corrispondente
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'attributo di classe (target).
	 *
	 * @return attributo di classe, di tipo continuo
	 */
	public ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	/**
	 * Rappresentazione testuale del training set: una riga per esempio,
	 * con i valori separati da virgola e la classe come ultimo valore.
	 *
	 * @return stringa che contiene tutti gli esempi
	 */
	@Override
	public String toString() {
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < numberOfExamples; i++) {
			for (int j = 0; j < explanatorySet.size(); j++)
				value.append(data.get(i).get(j)).append(",");
			value.append(data.get(i).get(explanatorySet.size())).append("\n");
		}
		return value.toString();
	}

	/**
	 * Ordina gli esempi del sottoinsieme [beginExampleIndex, endExampleIndex]
	 * rispetto ai valori dell'attributo indicato.
	 *
	 * @param attribute         attributo rispetto al quale ordinare
	 * @param beginExampleIndex primo indice del sottoinsieme (incluso)
	 * @param endExampleIndex   ultimo indice del sottoinsieme (incluso)
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	/**
	 * Scambia l'esempio i con l'esempio j, colonna per colonna
	 * (attributi indipendenti e attributo di classe).
	 *
	 * @param i indice del primo esempio
	 * @param j indice del secondo esempio
	 */
	private void swap(int i, int j) {
		Example temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);
	}

	/**
	 * Partiziona il sottoinsieme [inf, sup] rispetto al pivot (elemento centrale)
	 * e restituisce la posizione finale del pivot.
	 *
	 * @param attribute attributo discreto rispetto al quale si partiziona
	 * @param inf       primo indice del sottoinsieme (incluso)
	 * @param sup       ultimo indice del sottoinsieme (incluso)
	 * @return posizione del pivot dopo il partizionamento
	 */
	private int partition(DiscreteAttribute attribute, int inf, int sup) {
		int i = inf;
		int j = sup;
		int med = (inf + sup) / 2;
		String x = (String) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {
			while (i <= sup && ((String) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0)
				i++;
			while (((String) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0)
				j--;
			if (i < j)
				swap(i, j);
			else
				break;
		}
		swap(inf, j);
		return j;
	}

	/**
	 * Partiziona il sottoinsieme [inf, sup] rispetto al pivot (elemento centrale)
	 * e restituisce la posizione finale del pivot.
	 *
	 * @param attribute attributo continuo rispetto al quale si partiziona
	 * @param inf       primo indice del sottoinsieme (incluso)
	 * @param sup       ultimo indice del sottoinsieme (incluso)
	 * @return posizione del pivot dopo il partizionamento
	 */
	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i = inf;
		int j = sup;
		int med = (inf + sup) / 2;
		Double x = (Double) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {
			while (i <= sup && ((Double) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0)
				i++;
			while (((Double) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0)
				j--;
			if (i < j)
				swap(i, j);
			else
				break;
		}
		swap(inf, j);
		return j;
	}

	/**
	 * Ordina il sottoinsieme [inf, sup] con l'algoritmo quicksort, usando come
	 * relazione d'ordine totale "&lt;=" sui valori dell'attributo.
	 * Il tipo dell'attributo (discreto o continuo) è riconosciuto a runtime (RTTI)
	 * per scegliere il partizionamento adeguato.
	 * Si ricorre prima sulla partizione più piccola per limitare la profondità
	 * della ricorsione.
	 *
	 * @param attribute attributo rispetto al quale ordinare
	 * @param inf       primo indice del sottoinsieme (incluso)
	 * @param sup       ultimo indice del sottoinsieme (incluso)
	 */
	private void quicksort(Attribute attribute, int inf, int sup) {
		if (sup >= inf) {
			int pos;
			if (attribute instanceof DiscreteAttribute)
				pos = partition((DiscreteAttribute) attribute, inf, sup);
			else
				pos = partition((ContinuousAttribute) attribute, inf, sup);

			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			} else {
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}
		}
	}

	/**
	 * Punto di ingresso di prova: carica la tabella servo e ne stampa il contenuto
	 * ordinato rispetto a ciascun attributo indipendente.
	 *
	 * @param args argomenti da riga di comando (non utilizzati)
	 */
	public static void main(String[] args) {
		try {
			Data trainingSet = new Data("servo");
			System.out.println(trainingSet);

			for (int jColumn = 0; jColumn < trainingSet.getNumberOfExplanatoryAttributes(); jColumn++) {
				System.out.println("ORDER BY " + trainingSet.getExplanatoryAttribute(jColumn));
				trainingSet.quicksort(trainingSet.getExplanatoryAttribute(jColumn), 0,
						trainingSet.getNumberOfExamples() - 1);
				System.out.println(trainingSet);
			}
		} catch (TrainingDataException e) {
			System.out.println(e);
		}
	}
}
