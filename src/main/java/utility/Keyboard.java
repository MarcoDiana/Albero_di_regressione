package utility;

import java.io.*;
import java.util.*;

/**
 * La classe Keyboard facilita l'acquisizione di input da Standard Input (tastiera)
 * astraendo i dettagli relativi al parsing, alle conversioni di tipo
 * e alla gestione delle eccezioni di I/O.
 */
public class Keyboard {

    private static boolean printErrors = true;
    private static int errorCount = 0;
    private static String current_token = null;
    private static StringTokenizer reader;
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Restituisce il conteggio degli errori correnti avvenuti durante le letture.
     *
     * @return Il numero di errori riscontrati (int)
     */
    public static int getErrorCount() {
        return errorCount;
    }

    /**
     * Resetta il conteggio degli errori correnti azzerandolo.
     *
     * @param count Il parametro è ignorato nell'implementazione, il conteggio viene azzerato in ogni caso.
     */
    public static void resetErrorCount(int count) {
        errorCount = 0;
    }

    /**
     * Verifica se gli errori di input vengono attualmente stampati
     * sullo standard output.
     *
     * @return true se la stampa errori è abilitata, false altrimenti
     */
    public static boolean getPrintErrors() {
        return printErrors;
    }

    /**
     * Imposta un flag booleano per decidere se gli errori di input
     * debbano essere stampati o meno sullo standard output.
     *
     * @param flag true per stampare gli errori, false per nasconderli
     */
    public static void setPrintErrors(boolean flag) {
        printErrors = flag;
    }

    /**
     * Incrementa il conteggio degli errori e stampa il messaggio testuale
     * qualora il flag di stampa (printErrors) sia abilitato.
     *
     * @param str Il messaggio di errore da stampare
     */
    private static void error(String str) {
        errorCount++;
        if (printErrors)
            System.out.println(str);
    }

    /**
     * Recupera il prossimo token di input assumendo che possa trovarsi
     * anche sulle successive righe di input.
     *
     * @return Il token testuale letto
     */
    private static String getNextToken() {
        return getNextToken(true);
    }

    /**
     * Recupera il prossimo token di input, che potrebbe essere già stato letto
     * e mantenuto in memoria (bufferizzato).
     *
     * @param skip Booleano per indicare se procedere sulle righe successive
     * @return Il token testuale letto
     */
    private static String getNextToken(boolean skip) {
        String token;

        if (current_token == null)
            token = getNextInputToken(skip);
        else {
            token = current_token;
            current_token = null;
        }

        return token;
    }

    /**
     * Recupera il prossimo token dall'input; questo può provenire dalla
     * riga corrente o da una riga successiva. Il parametro determina
     * se le righe successive devono essere ispezionate.
     *
     * @param skip Se true analizza anche le linee successive
     * @return La stringa acquisita, o null in caso di eccezione
     */
    private static String getNextInputToken(boolean skip) {
        final String delimiters = " \t\n\r\f";
        String token = null;

        try {
            if (reader == null)
                reader = new StringTokenizer(in.readLine(), delimiters, true);

            while (token == null || ((delimiters.indexOf(token) >= 0) && skip)) {
                while (!reader.hasMoreTokens())
                    reader = new StringTokenizer(in.readLine(), delimiters, true);

                token = reader.nextToken();
            }
        } catch (Exception exception) {
            token = null;
        }

        return token;
    }

    /**
     * Ritorna true se non ci sono ulteriori token da leggere
     * sull'attuale riga di input.
     *
     * @return true se fine riga è raggiunta, false altrimenti
     */
    public static boolean endOfLine() {
        return !reader.hasMoreTokens();
    }

    /**
     * Legge e restituisce un'intera stringa testuale acquisita
     * dallo standard input fino alla fine della riga.
     *
     * @return La stringa letta, o null se l'acquisizione fallisce
     */
    public static String readString() {
        String str;

        try {
            str = getNextToken(false);
            while (!endOfLine()) {
                str = str + getNextToken(false);
            }
        } catch (Exception exception) {
            error("Error reading String data, null value returned.");
            str = null;
        }
        return str;
    }

    /**
     * Legge e restituisce una sottostringa delimitata da spazi (una singola parola)
     * acquisita dallo standard input.
     *
     * @return Il token di tipo parola, o null se l'acquisizione fallisce
     */
    public static String readWord() {
        String token;
        try {
            token = getNextToken();
        } catch (Exception exception) {
            error("Error reading String data, null value returned.");
            token = null;
        }
        return token;
    }

    /**
     * Legge e restituisce un valore booleano. Accetta "true" o "false"
     * (ignorando il case) e restituisce false in caso di input non riconosciuto.
     *
     * @return Il boolean convertito
     */
    public static boolean readBoolean() {
        String token = getNextToken();
        boolean bool;
        try {
            if (token.toLowerCase().equals("true"))
                bool = true;
            else if (token.toLowerCase().equals("false"))
                bool = false;
            else {
                error("Error reading boolean data, false value returned.");
                bool = false;
            }
        } catch (Exception exception) {
            error("Error reading boolean data, false value returned.");
            bool = false;
        }
        return bool;
    }

    /**
     * Legge e restituisce il primo carattere della stringa immessa
     * sullo standard input.
     *
     * @return Il carattere letto, o Character.MIN_VALUE in caso di errore
     */
    public static char readChar() {
        String token = getNextToken(false);
        char value;
        try {
            if (token.length() > 1) {
                current_token = token.substring(1, token.length());
            } else
                current_token = null;
            value = token.charAt(0);
        } catch (Exception exception) {
            error("Error reading char data, MIN_VALUE value returned.");
            value = Character.MIN_VALUE;
        }

        return value;
    }

    /**
     * Legge e restituisce un numero intero (int) dallo standard input.
     * In caso di immissione non compatibile con un intero, stampa errore e
     * restituisce il valore convenzionale Integer.MIN_VALUE.
     *
     * @return Il valore int acquisito
     */
    public static int readInt() {
        String token = getNextToken();
        int value;
        try {
            value = Integer.parseInt(token);
        } catch (Exception exception) {
            error("Error reading int data, MIN_VALUE value returned.");
            value = Integer.MIN_VALUE;
        }
        return value;
    }

    /**
     * Legge e restituisce un intero a 64-bit (long) dallo standard input.
     *
     * @return Il valore long acquisito, o Long.MIN_VALUE in caso di errore
     */
    public static long readLong() {
        String token = getNextToken();
        long value;
        try {
            value = Long.parseLong(token);
        } catch (Exception exception) {
            error("Error reading long data, MIN_VALUE value returned.");
            value = Long.MIN_VALUE;
        }
        return value;
    }

    /**
     * Legge e restituisce un numero decimale a singola precisione (float)
     * dallo standard input.
     *
     * @return Il valore float, o Float.NaN se il parsing fallisce
     */
    public static float readFloat() {
        String token = getNextToken();
        float value;
        try {
            // "new Float(token)" deprecato in Java recenti, ma mantenuto per aderenza alla logica originale
            value = (new Float(token)).floatValue();
        } catch (Exception exception) {
            error("Error reading float data, NaN value returned.");
            value = Float.NaN;
        }
        return value;
    }

    /**
     * Legge e restituisce un numero decimale a doppia precisione (double)
     * dallo standard input.
     *
     * @return Il valore double, o Double.NaN se il parsing fallisce
     */
    public static double readDouble() {
        String token = getNextToken();
        double value;
        try {
            value = (new Double(token)).doubleValue();
        } catch (Exception exception) {
            error("Error reading double data, NaN value returned.");
            value = Double.NaN;
        }
        return value;
    }
}