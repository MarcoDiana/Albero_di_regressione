package database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test per il package database (Column, Example, DbAccess, TableSchema, TableData)")
class DatabasePackageTest {

    @Nested
    @DisplayName("Test per Column")
    class ColumnTests {

        @Test
        @DisplayName("Proprietà e metodi di Column con tipo string e number")
        void testColumnProperties() {
            Column colStr = new Column("name", "string");
            assertEquals("name", colStr.getColumnName());
            assertFalse(colStr.isNumber());
            assertEquals("name:string", colStr.toString());

            Column colNum = new Column("age", "number");
            assertEquals("age", colNum.getColumnName());
            assertTrue(colNum.isNumber());
            assertEquals("age:number", colNum.toString());
        }
    }

    @Nested
    @DisplayName("Test per Example")
    class ExampleTests {

        @Test
        @DisplayName("Inserimento, accesso per indice e iterazione su Example")
        void testExampleAddGetIterator() {
            Example ex = new Example();
            ex.add("A");
            ex.add(2.5);
            ex.add(10.0);

            assertEquals("A", ex.get(0));
            assertEquals(2.5, ex.get(1));
            assertEquals(10.0, ex.get(2));

            List<Object> items = new ArrayList<>();
            for (Object o : ex) {
                items.add(o);
            }
            assertEquals(List.of("A", 2.5, 10.0), items);
            assertEquals("A 2.5 10.0 ", ex.toString());
        }

        @Test
        @DisplayName("Confronto compareTo tra tuple Example")
        void testExampleCompareTo() {
            Example ex1 = new Example();
            ex1.add("A");
            ex1.add(1.0);

            Example ex2 = new Example();
            ex2.add("A");
            ex2.add(1.0);

            assertEquals(0, ex1.compareTo(ex2));

            Example ex3 = new Example();
            ex3.add("B");
            ex3.add(1.0);

            assertTrue(ex1.compareTo(ex3) > 0);
            assertTrue(ex3.compareTo(ex1) < 0);
        }
    }

    @Nested
    @DisplayName("Test per DbAccess e integrazione reale con MySQL MapDB")
    class DbAccessTests {

        @Test
        @DisplayName("Connessione e chiusura regolare a MapDB")
        void testInitAndCloseConnection() throws DatabaseConnectionException, SQLException {
            DbAccess db = new DbAccess();
            db.initConnection();
            Connection conn = db.getConnection();
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            assertSame(conn, db.getConn());

            db.closeConnection();
            assertTrue(conn.isClosed());

            assertDoesNotThrow(db::closeConnection);
        }
    }

    @Nested
    @DisplayName("Test per TableSchema e TableData su MapDB.provaC")
    class TableSchemaAndDataTests {

        @Test
        @DisplayName("Lettura dello schema della tabella provac")
        void testTableSchemaReading() throws Exception {
            DbAccess db = new DbAccess();
            db.initConnection();
            try {
                TableSchema schema = new TableSchema(db, "provac");
                assertEquals(3, schema.getNumberOfAttributes());

                Column colX = schema.getColumn(0);
                assertEquals("X", colX.getColumnName());
                assertFalse(colX.isNumber());

                Column colY = schema.getColumn(1);
                assertEquals("Y", colY.getColumnName());
                assertTrue(colY.isNumber());

                Column colC = schema.getColumn(2);
                assertEquals("C", colC.getColumnName());
                assertTrue(colC.isNumber());

                List<String> colNames = new ArrayList<>();
                for (Column c : schema) {
                    colNames.add(c.getColumnName());
                }
                assertEquals(List.of("X", "Y", "C"), colNames);
            } finally {
                db.closeConnection();
            }
        }

        @Test
        @DisplayName("Estrazione tuple con TableData.getTransazioni")
        void testTableDataGetTransazioni() throws Exception {
            DbAccess db = new DbAccess();
            db.initConnection();
            try {
                TableData tableData = new TableData(db);
                List<Example> trans = tableData.getTransazioni("provac");
                assertNotNull(trans);
                assertEquals(15, trans.size());

                Example first = trans.get(0);
                assertTrue(first.get(0) instanceof String);
                assertTrue(first.get(1) instanceof Double);
                assertTrue(first.get(2) instanceof Double);
            } finally {
                db.closeConnection();
            }
        }

        @Test
        @DisplayName("Estrazione valori distinti con TableData.getDistinctColumnValues")
        void testGetDistinctColumnValues() throws Exception {
            DbAccess db = new DbAccess();
            db.initConnection();
            try {
                TableData tableData = new TableData(db);
                TableSchema schema = new TableSchema(db, "provac");

                Set<Object> distinctX = tableData.getDistinctColumnValues("provac", schema.getColumn(0));
                assertEquals(Set.of("A", "B"), distinctX);

                Set<Object> distinctY = tableData.getDistinctColumnValues("provac", schema.getColumn(1));
                assertEquals(7, distinctY.size());
            } finally {
                db.closeConnection();
            }
        }

        @Test
        @DisplayName("Sollevamento di EmptySetException ed eccezioni su tabella inesistente")
        void testTableDataExceptions() throws Exception {
            DbAccess db = new DbAccess();
            db.initConnection();
            try {
                TableData tableData = new TableData(db);
                assertThrows(SQLException.class, () -> tableData.getTransazioni("tabella_inesistente_xyz"));
            } finally {
                db.closeConnection();
            }
        }
    }
}
