package utility;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test per la classe Keyboard con simulazione di System.in")
class KeyboardTest {

    private static InputStream originalSystemIn;

    @BeforeAll
    static void captureOriginalIn() {
        originalSystemIn = System.in;
        Keyboard.setPrintErrors(false);
    }

    @AfterAll
    static void restoreOriginalIn() {
        System.setIn(originalSystemIn);
        Keyboard.setPrintErrors(true);
    }

    private void provideInput(String data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        System.setIn(bais);
        try {
            Field inField = Keyboard.class.getDeclaredField("in");
            inField.setAccessible(true);
            inField.set(null, new BufferedReader(new InputStreamReader(System.in)));

            Field readerField = Keyboard.class.getDeclaredField("reader");
            readerField.setAccessible(true);
            readerField.set(null, null);

            Field tokenField = Keyboard.class.getDeclaredField("current_token");
            tokenField.setAccessible(true);
            tokenField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile reimpostare lo stream di Keyboard", e);
        }
        Keyboard.resetErrorCount(0);
    }

    @Test
    @DisplayName("Lettura di stringhe con readString e readWord")
    void testReadStringAndWord() {
        provideInput("Hello World\nSecondLine\n");
        assertEquals("Hello World", Keyboard.readString());

        provideInput("WordOne WordTwo\n");
        assertEquals("WordOne", Keyboard.readWord());
        assertEquals("WordTwo", Keyboard.readWord());
    }

    @Test
    @DisplayName("Lettura di valori booleani con readBoolean e gestione errori")
    void testReadBoolean() {
        provideInput("true\nFalse\nTRUE\nnot_a_bool\n");
        assertTrue(Keyboard.readBoolean());
        assertFalse(Keyboard.readBoolean());
        assertTrue(Keyboard.readBoolean());

        assertFalse(Keyboard.readBoolean());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    @DisplayName("Lettura di caratteri con readChar")
    void testReadChar() {
        provideInput("ABC\n");
        assertEquals('A', Keyboard.readChar());
        assertEquals('B', Keyboard.readChar());
        assertEquals('C', Keyboard.readChar());
    }

    @Test
    @DisplayName("Lettura di interi con readInt e gestione valori non validi")
    void testReadInt() {
        provideInput("42\n-100\n0\ninvalid_int\n");
        assertEquals(42, Keyboard.readInt());
        assertEquals(-100, Keyboard.readInt());
        assertEquals(0, Keyboard.readInt());

        assertEquals(Integer.MIN_VALUE, Keyboard.readInt());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    @DisplayName("Lettura di interi long con readLong")
    void testReadLong() {
        provideInput("12345678901\ninvalid_long\n");
        assertEquals(12345678901L, Keyboard.readLong());
        assertEquals(Long.MIN_VALUE, Keyboard.readLong());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    @DisplayName("Lettura di numeri floating point con readFloat e readDouble")
    void testReadFloatAndDouble() {
        provideInput("3.14\ninvalid_float\n2.71828\ninvalid_double\n");
        assertEquals(3.14f, Keyboard.readFloat(), 1e-4);
        assertTrue(Float.isNaN(Keyboard.readFloat()));

        assertEquals(2.71828, Keyboard.readDouble(), 1e-5);
        assertTrue(Double.isNaN(Keyboard.readDouble()));
        assertEquals(2, Keyboard.getErrorCount());
    }

    @Test
    @DisplayName("Controllo fine riga con endOfLine e gestione errori")
    void testEndOfLineAndErrorControl() {
        provideInput("SingleWord\n");
        Keyboard.readWord();
        assertTrue(Keyboard.endOfLine());

        Keyboard.resetErrorCount(10);
        assertEquals(0, Keyboard.getErrorCount());

        Keyboard.setPrintErrors(false);
        assertFalse(Keyboard.getPrintErrors());
        Keyboard.setPrintErrors(true);
        assertTrue(Keyboard.getPrintErrors());
        Keyboard.setPrintErrors(false);
    }
}
