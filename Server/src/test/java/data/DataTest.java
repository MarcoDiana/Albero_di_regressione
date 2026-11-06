package data;

import database.DbAccess;
import database.DatabaseConnectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Test per la classe Data: caricamento MySQL provaC, Quicksort e TrainingDataException")
class DataTest {

    @Nested
    @DisplayName("Test d'integrazione reale su MapDB.provaC")
    class IntegrationWithMapDbTests {

        @Test
        @DisplayName("Caricamento nominale della tabella provaC (15 esempi, 2 esplicativi, 1 target continuo)")
        void testLoadProvaCSuccessfully() throws TrainingDataException {
            Data data = new Data("provac");

            assertEquals(15, data.getNumberOfExamples());
            assertEquals(2, data.getNumberOfExplanatoryAttributes());

            Attribute attr0 = data.getExplanatoryAttribute(0);
            assertTrue(attr0 instanceof DiscreteAttribute);
            assertEquals("X", attr0.getName());
            assertEquals(2, ((DiscreteAttribute) attr0).getNumberOfDistinctValues());

            Attribute attr1 = data.getExplanatoryAttribute(1);
            assertTrue(attr1 instanceof ContinuousAttribute);
            assertEquals("Y", attr1.getName());

            ContinuousAttribute classAttr = data.getClassAttribute();
            assertNotNull(classAttr);
            assertEquals("C", classAttr.getName());
            assertEquals(2, classAttr.getIndex());

            for (int i = 0; i < data.getNumberOfExamples(); i++) {
                assertNotNull(data.getExplanatoryValue(i, 0));
                assertNotNull(data.getExplanatoryValue(i, 1));
                assertNotNull(data.getClassValue(i));
            }

            String str = data.toString();
            assertFalse(str.isEmpty());
            assertEquals(15, str.lines().count());
        }

        @Test
        @DisplayName("Ordinamento Quicksort in-place su attributo discreto X sull'intero dataset")
        void testSortDiscreteAttributeFullRange() throws TrainingDataException {
            Data data = new Data("provac");
            Attribute attrX = data.getExplanatoryAttribute(0);

            data.sort(attrX, 0, data.getNumberOfExamples() - 1);

            boolean seenB = false;
            for (int i = 0; i < data.getNumberOfExamples(); i++) {
                String val = (String) data.getExplanatoryValue(i, 0);
                if (val.equals("B")) {
                    seenB = true;
                } else if (seenB && val.equals("A")) {
                    fail("Trovato valore 'A' dopo un valore 'B': la sequenza non è ordinata!");
                }
            }
        }

        @Test
        @DisplayName("Ordinamento Quicksort in-place su attributo continuo Y sull'intero dataset")
        void testSortContinuousAttributeFullRange() throws TrainingDataException {
            Data data = new Data("provac");
            Attribute attrY = data.getExplanatoryAttribute(1);

            data.sort(attrY, 0, data.getNumberOfExamples() - 1);

            for (int i = 0; i < data.getNumberOfExamples() - 1; i++) {
                Double current = (Double) data.getExplanatoryValue(i, 1);
                Double next = (Double) data.getExplanatoryValue(i + 1, 1);
                assertTrue(current <= next, "Inversione trovata all'indice " + i + ": " + current + " > " + next);
            }
        }

        @Test
        @DisplayName("Ordinamento Quicksort su sotto-range ristretto [begin, end]")
        void testSortSubRange() throws TrainingDataException {
            Data data = new Data("provac");
            Attribute attrY = data.getExplanatoryAttribute(1);

            int begin = 3;
            int end = 8;
            Object preBefore = data.getExplanatoryValue(2, 1);
            Object postAfter = data.getExplanatoryValue(9, 1);

            data.sort(attrY, begin, end);

            assertEquals(preBefore, data.getExplanatoryValue(2, 1));
            assertEquals(postAfter, data.getExplanatoryValue(9, 1));

            for (int i = begin; i < end; i++) {
                Double curr = (Double) data.getExplanatoryValue(i, 1);
                Double nxt = (Double) data.getExplanatoryValue(i + 1, 1);
                assertTrue(curr <= nxt, "Sub-range non ordinato tra indici " + i + " e " + (i + 1));
            }
        }

        @Test
        @DisplayName("Sort con range limite: singolo elemento (begin == end) e vuoto (begin > end)")
        void testSortBoundaryRanges() throws TrainingDataException {
            Data data = new Data("provac");
            Attribute attrY = data.getExplanatoryAttribute(1);

            assertDoesNotThrow(() -> data.sort(attrY, 4, 4));
            assertDoesNotThrow(() -> data.sort(attrY, 5, 3));
        }
    }

    @Nested
    @DisplayName("Test di gestione eccezioni TrainingDataException")
    class TrainingDataExceptionTests {

        @Test
        @DisplayName("Errore con nome tabella nullo, vuoto o di soli spazi")
        void testInvalidTableName() {
            TrainingDataException exNull = assertThrows(TrainingDataException.class, () -> new Data(null));
            assertTrue(exNull.getMessage().contains("non può essere vuoto"));

            TrainingDataException exEmpty = assertThrows(TrainingDataException.class, () -> new Data(""));
            assertTrue(exEmpty.getMessage().contains("non può essere vuoto"));

            TrainingDataException exSpaces = assertThrows(TrainingDataException.class, () -> new Data("   "));
            assertTrue(exSpaces.getMessage().contains("non può essere vuoto"));
        }

        @Test
        @DisplayName("Errore con tabella inesistente nel database")
        void testNonExistentTable() {
            assertThrows(TrainingDataException.class, () -> new Data("tabella_inesistente_xyz_12345"));
        }

        @Test
        @DisplayName("Errore quando la tabella ha meno di due colonne (< 2)")
        void testTableWithLessThanTwoColumns() throws Exception {
            try (MockedConstruction<DbAccess> mocked = Mockito.mockConstruction(DbAccess.class, (mockDb, context) -> {
                Connection mockConn = mock(Connection.class);
                Statement mockStmt = mock(Statement.class);
                ResultSet mockRs = mock(ResultSet.class);
                ResultSetMetaData mockMeta = mock(ResultSetMetaData.class);

                when(mockDb.getConnection()).thenReturn(mockConn);
                when(mockConn.createStatement()).thenReturn(mockStmt);
                when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
                when(mockRs.getMetaData()).thenReturn(mockMeta);
                when(mockMeta.getColumnCount()).thenReturn(1);
            })) {
                TrainingDataException ex = assertThrows(TrainingDataException.class, () -> new Data("one_col_table"));
                assertTrue(ex.getMessage().contains("deve avere almeno due colonne"));
            }
        }

        @Test
        @DisplayName("Errore quando l'ultima colonna (target) non è numerica")
        void testLastColumnNotNumeric() throws Exception {
            try (MockedConstruction<DbAccess> mocked = Mockito.mockConstruction(DbAccess.class, (mockDb, context) -> {
                Connection mockConn = mock(Connection.class);
                Statement mockStmt = mock(Statement.class);
                ResultSet mockRs = mock(ResultSet.class);
                ResultSetMetaData mockMeta = mock(ResultSetMetaData.class);

                when(mockDb.getConnection()).thenReturn(mockConn);
                when(mockConn.createStatement()).thenReturn(mockStmt);
                when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
                when(mockRs.getMetaData()).thenReturn(mockMeta);
                when(mockMeta.getColumnCount()).thenReturn(2);
                when(mockMeta.getColumnType(1)).thenReturn(Types.VARCHAR);
                when(mockMeta.getColumnName(1)).thenReturn("feat");
                when(mockMeta.getColumnType(2)).thenReturn(Types.VARCHAR);
                when(mockMeta.getColumnName(2)).thenReturn("target_str");
            })) {
                TrainingDataException ex = assertThrows(TrainingDataException.class, () -> new Data("str_target_table"));
                assertTrue(ex.getMessage().contains("deve essere numerica"));
            }
        }

        @Test
        @DisplayName("Errore in presenza di valori NULL nelle tuple")
        void testNullValuesInTuple() throws Exception {
            try (MockedConstruction<DbAccess> mocked = Mockito.mockConstruction(DbAccess.class, (mockDb, context) -> {
                Connection mockConn = mock(Connection.class);
                Statement mockStmt = mock(Statement.class);
                ResultSet mockRs = mock(ResultSet.class);
                ResultSetMetaData mockMeta = mock(ResultSetMetaData.class);

                when(mockDb.getConnection()).thenReturn(mockConn);
                when(mockConn.createStatement()).thenReturn(mockStmt);
                when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
                when(mockRs.getMetaData()).thenReturn(mockMeta);
                when(mockMeta.getColumnCount()).thenReturn(2);
                when(mockMeta.getColumnType(1)).thenReturn(Types.VARCHAR);
                when(mockMeta.getColumnName(1)).thenReturn("feat");
                when(mockMeta.getColumnType(2)).thenReturn(Types.DOUBLE);
                when(mockMeta.getColumnName(2)).thenReturn("target");

                when(mockRs.next()).thenReturn(true);
                when(mockRs.getObject(1)).thenReturn(null);
            })) {
                TrainingDataException ex = assertThrows(TrainingDataException.class, () -> new Data("null_val_table"));
                assertTrue(ex.getMessage().contains("Valore nullo nella colonna"));
            }
        }

        @Test
        @DisplayName("Errore in presenza di valore non numerico in colonna numerica")
        void testNonNumericValueInNumericColumn() throws Exception {
            try (MockedConstruction<DbAccess> mocked = Mockito.mockConstruction(DbAccess.class, (mockDb, context) -> {
                Connection mockConn = mock(Connection.class);
                Statement mockStmt = mock(Statement.class);
                ResultSet mockRs = mock(ResultSet.class);
                ResultSetMetaData mockMeta = mock(ResultSetMetaData.class);

                when(mockDb.getConnection()).thenReturn(mockConn);
                when(mockConn.createStatement()).thenReturn(mockStmt);
                when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
                when(mockRs.getMetaData()).thenReturn(mockMeta);
                when(mockMeta.getColumnCount()).thenReturn(2);
                when(mockMeta.getColumnType(1)).thenReturn(Types.VARCHAR);
                when(mockMeta.getColumnName(1)).thenReturn("feat");
                when(mockMeta.getColumnType(2)).thenReturn(Types.DOUBLE);
                when(mockMeta.getColumnName(2)).thenReturn("target");

                when(mockRs.next()).thenReturn(true);
                when(mockRs.getObject(1)).thenReturn("valid_str");
                when(mockRs.getObject(2)).thenReturn("not_a_number_object");
            })) {
                TrainingDataException ex = assertThrows(TrainingDataException.class, () -> new Data("bad_number_table"));
                assertTrue(ex.getMessage().contains("Valore non numerico"));
            }
        }

        @Test
        @DisplayName("Errore quando la tabella non contiene alcuna tupla")
        void testEmptyTable() throws Exception {
            try (MockedConstruction<DbAccess> mocked = Mockito.mockConstruction(DbAccess.class, (mockDb, context) -> {
                Connection mockConn = mock(Connection.class);
                Statement mockStmt = mock(Statement.class);
                ResultSet mockRs = mock(ResultSet.class);
                ResultSetMetaData mockMeta = mock(ResultSetMetaData.class);

                when(mockDb.getConnection()).thenReturn(mockConn);
                when(mockConn.createStatement()).thenReturn(mockStmt);
                when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
                when(mockRs.getMetaData()).thenReturn(mockMeta);
                when(mockMeta.getColumnCount()).thenReturn(2);
                when(mockMeta.getColumnType(1)).thenReturn(Types.VARCHAR);
                when(mockMeta.getColumnName(1)).thenReturn("feat");
                when(mockMeta.getColumnType(2)).thenReturn(Types.DOUBLE);
                when(mockMeta.getColumnName(2)).thenReturn("target");

                when(mockRs.next()).thenReturn(false);
            })) {
                TrainingDataException ex = assertThrows(TrainingDataException.class, () -> new Data("empty_table"));
                assertTrue(ex.getMessage().contains("non contiene tuple"));
            }
        }
    }
}
