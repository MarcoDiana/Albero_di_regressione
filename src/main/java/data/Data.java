package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Modella l'insieme degli esempi di training.
 * Il training set è memorizzato in una matrice (numero di esempi) x (numero di attributi):
 * le prime colonne contengono gli attributi indipendenti, l'ultima l'attributo di classe.
 *
 * Il formato del file di input è:
 *   @schema n                       numero di attributi indipendenti
 *   @desc nome discrete v1,v2,...   un attributo indipendente per riga
 *   @target nome                    attributo di classe (numerico)
 *   @data m                         numero di esempi, seguito da una riga per esempio
 */
public class Data {

	/** Matrice che contiene il training set (esempi x attributi, classe nell'ultima colonna). */
	private Object[][] data;

	/** Cardinalità del training set. */
	private int numberOfExamples;

	/** Attributi indipendenti (esplicativi) del training set. */
	private Attribute[] explanatorySet;

	/** Attributo di classe (target), di tipo continuo. */
	private ContinuousAttribute classAttribute;

	/**
	 * Costruttore: legge il file e popola schema degli attributi e matrice degli esempi.
	 * Gli attributi indipendenti sono, in questa fase, tutti discreti.
	 *
	 * @param fileName nome del file che contiene il training set
	 * @throws TrainingDataException se il file è inesistente, lo schema è mancante,
	 *                               il training set è vuoto o manca l'attributo target
	 */
	public Data(String fileName) throws TrainingDataException {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(fileName));

			// Lettura dello schema: "@schema n" indica il numero di attributi indipendenti
			if (!sc.hasNextLine())
				throw new TrainingDataException("File vuoto: " + fileName);
			String line = sc.nextLine();
			if (!line.contains("@schema"))
				throw new TrainingDataException("Schema mancante nel file: " + fileName);
			String[] s = line.split(" ");
			explanatorySet = new Attribute[Integer.parseInt(s[1])];

			// Lettura delle dichiarazioni degli attributi, fino al marcatore "@data"
			short iAttribute = 0;
			line = sc.nextLine();
			while (!line.contains("@data")) {
				s = line.split(" ");
				if (s[0].equals("@desc")) {
					// Attributo indipendente: @desc motor discrete A,B,C,D,E
					String[] discreteValues = s[2].split(",");
					explanatorySet[iAttribute] = new DiscreteAttribute(s[1], iAttribute, discreteValues);
				} else if (s[0].equals("@target")) {
					classAttribute = new ContinuousAttribute(s[1], iAttribute);
				}
				iAttribute++;
				line = sc.nextLine();
			}
			if (classAttribute == null)
				throw new TrainingDataException("Attributo target mancante nel file: " + fileName);

			// Numero di esempi: @data 167
			numberOfExamples = Integer.parseInt(line.split(" ")[1]);
			if (numberOfExamples <= 0)
				throw new TrainingDataException("Training set vuoto nel file: " + fileName);

			// Popolamento della matrice: es. E,E,5,4,0.28125095
			data = new Object[numberOfExamples][explanatorySet.length + 1];
			short iRow = 0;
			while (sc.hasNextLine() && iRow < numberOfExamples) {
				line = sc.nextLine();
				s = line.split(",");
				for (short jColumn = 0; jColumn < s.length - 1; jColumn++)
					data[iRow][jColumn] = s[jColumn];
				data[iRow][s.length - 1] = Double.valueOf(s[s.length - 1]);
				iRow++;
			}
		} catch (FileNotFoundException e) {
			throw new TrainingDataException("Impossibile aprire il file del training set: " + fileName, e);
		} catch (java.util.NoSuchElementException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
			throw new TrainingDataException("Formato del training set non valido: " + fileName, e);
		} finally {
			if (sc != null)
				sc.close();
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
	 * Corrisponde alla lunghezza di explanatorySet[] ed è l'indice di colonna
	 * dell'attributo di classe in data[][].
	 *
	 * @return numero di attributi indipendenti
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.length;
	}

	/**
	 * Restituisce il valore dell'attributo di classe per un esempio.
	 * Il valore è memorizzato nell'ultima colonna di data[][].
	 *
	 * @param exampleIndex indice dell'esempio (riga di data[][])
	 * @return valore Double dell'attributo di classe
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data[exampleIndex][explanatorySet.length];
	}

	/**
	 * Restituisce il valore di un attributo indipendente per un dato esempio.
	 * L'indice dell'attributo coincide con l'indice di colonna in data[][].
	 *
	 * @param exampleIndex   indice dell'esempio (riga di data[][])
	 * @param attributeIndex indice dell'attributo indipendente (colonna di data[][])
	 * @return valore dell'attributo (String per gli attributi discreti)
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data[exampleIndex][attributeIndex];
	}

	/**
	 * Restituisce l'attributo indipendente alla posizione indicata.
	 *
	 * @param index posizione in explanatorySet[]
	 * @return attributo indipendente corrispondente
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet[index];
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
			for (int j = 0; j < explanatorySet.length; j++)
				value.append(data[i][j]).append(",");
			value.append(data[i][explanatorySet.length]).append("\n");
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
		Object temp;
		for (int k = 0; k < getNumberOfExplanatoryAttributes() + 1; k++) {
			temp = data[i][k];
			data[i][k] = data[j][k];
			data[j][k] = temp;
		}
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
	 * Ordina il sottoinsieme [inf, sup] con l'algoritmo quicksort, usando come
	 * relazione d'ordine totale "&lt;=" sui valori dell'attributo.
	 * Si ricorre prima sulla partizione più piccola per limitare la profondità
	 * della ricorsione.
	 *
	 * @param attribute attributo rispetto al quale ordinare
	 * @param inf       primo indice del sottoinsieme (incluso)
	 * @param sup       ultimo indice del sottoinsieme (incluso)
	 */
	private void quicksort(Attribute attribute, int inf, int sup) {
		if (sup >= inf) {
			int pos = partition((DiscreteAttribute) attribute, inf, sup);

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
	 * Punto di ingresso di prova: carica servo.dat e ne stampa il contenuto
	 * ordinato rispetto a ciascun attributo indipendente.
	 *
	 * @param args argomenti da riga di comando (non utilizzati)
	 */
	public static void main(String[] args) {
		try {
			Data trainingSet = new Data("servo.dat");
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
