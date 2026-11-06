package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import utility.Keyboard;

/**
 * Avvia il client e gestisce la comunicazione con il server per apprendere,
 * caricare e interrogare un albero di regressione.
 *
 * @author Marco Diana
 * @version 1.0
 */
public class Main {

	/**
	 * Costruttore predefinito del client CLI su socket TCP.
	 */
	public Main() {
	}

	/**
	 * Risolve l'indirizzo del server, apre la connessione e guida l'utente
	 * nelle fasi di apprendimento o caricamento e nelle successive predizioni.
	 *
	 * @param args argomenti da riga di comando: indirizzo host del server e porta
	 */
	public static void main(String[] args) {
		String host = args.length > 0 ? args[0] : "localhost";
		int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

		InetAddress addr;
		try {
			// Verifica che il nome host specificato sia risolvibile.
			addr = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			System.out.println(e.toString());
			return;
		}
		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			// Crea la connessione e i flussi serializzati usati dal protocollo client-server.
			socket = new Socket(addr, port);
			System.out.println(socket);		
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
		} catch (IOException e) {
			System.out.println(e.toString());
			return;
		}

		String answer = "";
		int decision = 0;
		// Richiede una scelta valida prima di iniziare la comunicazione applicativa.
		do {
			System.out.println("Learn Regression Tree from data [1]");
			System.out.println("Load Regression Tree from archive [2]");
			decision = Keyboard.readInt();
		} while (!(decision == 1) && !(decision == 2));
		
		String tableName = "";
		System.out.println("File name:");
		tableName = Keyboard.readString();
		try {
			// Invia al server la richiesta di apprendimento oppure di caricamento dell'albero.
			if (decision == 1) {
				System.out.println("Starting data acquisition phase!");
				
				// Il codice 0 avvia l'acquisizione dei dati; il nome identifica la sorgente.
				out.writeObject(0);
				out.writeObject(tableName);
				answer = in.readObject().toString();
				if (!answer.equals("OK")) {
					System.out.println(answer);
					return;
				}
				
				System.out.println("Starting learning phase!");
				out.writeObject(1);
			} else {
				out.writeObject(2);
				out.writeObject(tableName);
			}
			
			answer = in.readObject().toString();
			if (!answer.equals("OK")) {
				System.out.println(answer);
				return;
			}
			
			// Continua a inviare richieste di predizione finché l'utente lo desidera.
			char risp = 'y';
			
			do {
				out.writeObject(3);
				
				System.out.println("Starting prediction phase!");
				answer = in.readObject().toString();
				
				// Il server richiede progressivamente le scelte necessarie per attraversare l'albero.
				while (answer.equals("QUERY")) {
					// Mostra la domanda del server e gli invia la risposta dell'utente.
					answer = in.readObject().toString();
					System.out.println(answer);
					int path = Keyboard.readInt();
					out.writeObject(path);
					answer = in.readObject().toString();
				}
			
				if (answer.equals("OK")) {
					// Legge e mostra la classe predetta dal server.
					answer = in.readObject().toString();
					System.out.println("Predicted class:" + answer);
				} else {
					// Mostra l'eventuale errore restituito dal server.
					System.out.println(answer);
				}
			
				System.out.println("Would you repeat ? (y/n)");
				risp = Keyboard.readChar();
					
			} while (Character.toUpperCase(risp) == 'Y');
			
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.toString());
		}
	}
}
