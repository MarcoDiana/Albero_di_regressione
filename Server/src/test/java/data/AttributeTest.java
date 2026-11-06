package data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test per la gerarchia Attribute (DiscreteAttribute e ContinuousAttribute)")
class AttributeTest {

    @Nested
    @DisplayName("Test per ContinuousAttribute")
    class ContinuousAttributeTests {

        @Test
        @DisplayName("Inizializzazione corretta di nome e indice per ContinuousAttribute")
        void testContinuousAttributeInitialization() {
            ContinuousAttribute ca = new ContinuousAttribute("target", 2);
            assertEquals("target", ca.getName());
            assertEquals(2, ca.getIndex());
            assertEquals("target", ca.toString());
        }

        @Test
        @DisplayName("Serializzazione e deserializzazione di ContinuousAttribute")
        void testContinuousAttributeSerialization() throws Exception {
            ContinuousAttribute original = new ContinuousAttribute("humidity", 5);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }

            ContinuousAttribute deserialized;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                deserialized = (ContinuousAttribute) ois.readObject();
            }

            assertNotNull(deserialized);
            assertEquals(original.getName(), deserialized.getName());
            assertEquals(original.getIndex(), deserialized.getIndex());
        }
    }

    @Nested
    @DisplayName("Test per DiscreteAttribute")
    class DiscreteAttributeTests {

        @Test
        @DisplayName("Deduplicazione e ordinamento lessicografico dei valori discreti")
        void testDiscreteAttributeDeduplicationAndSorting() {
            String[] rawValues = {"Sunny", "Rain", "Sunny", "Overcast", "Rain"};
            DiscreteAttribute da = new DiscreteAttribute("outlook", 0, rawValues);

            assertEquals("outlook", da.getName());
            assertEquals(0, da.getIndex());
            assertEquals("outlook", da.toString());
            assertEquals(3, da.getNumberOfDistinctValues());

            List<String> valuesInOrder = new ArrayList<>();
            for (String val : da) {
                valuesInOrder.add(val);
            }

            assertEquals(List.of("Overcast", "Rain", "Sunny"), valuesInOrder);
        }

        @Test
        @DisplayName("Comportamento con dominio vuoto o a singolo valore")
        void testEmptyAndSingleValueDomains() {
            DiscreteAttribute emptyDa = new DiscreteAttribute("empty", 1, new String[0]);
            assertEquals(0, emptyDa.getNumberOfDistinctValues());
            assertFalse(emptyDa.iterator().hasNext());

            DiscreteAttribute singleDa = new DiscreteAttribute("single", 2, new String[]{"Constant"});
            assertEquals(1, singleDa.getNumberOfDistinctValues());
            Iterator<String> it = singleDa.iterator();
            assertTrue(it.hasNext());
            assertEquals("Constant", it.next());
            assertFalse(it.hasNext());
        }

        @Test
        @DisplayName("Serializzazione e deserializzazione di DiscreteAttribute")
        void testDiscreteAttributeSerialization() throws Exception {
            DiscreteAttribute original = new DiscreteAttribute("wind", 3, new String[]{"Weak", "Strong"});
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }

            DiscreteAttribute deserialized;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                deserialized = (DiscreteAttribute) ois.readObject();
            }

            assertNotNull(deserialized);
            assertEquals(original.getName(), deserialized.getName());
            assertEquals(original.getIndex(), deserialized.getIndex());
            assertEquals(original.getNumberOfDistinctValues(), deserialized.getNumberOfDistinctValues());

            List<String> origVals = new ArrayList<>();
            original.forEach(origVals::add);
            List<String> desVals = new ArrayList<>();
            deserialized.forEach(desVals::add);
            assertEquals(origVals, desVals);
        }
    }

    @Test
    @DisplayName("Polimorfismo di inclusione tramite riferimento Attribute")
    void testPolymorphism() {
        Attribute attr1 = new ContinuousAttribute("cont", 0);
        Attribute attr2 = new DiscreteAttribute("disc", 1, new String[]{"X", "Y"});

        assertTrue(attr1 instanceof ContinuousAttribute);
        assertTrue(attr2 instanceof DiscreteAttribute);
        assertEquals("cont", attr1.toString());
        assertEquals("disc", attr2.toString());
    }
}
