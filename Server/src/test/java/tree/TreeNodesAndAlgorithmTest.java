package tree;

import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import server.UnknownValueException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test per nodi, algoritmo induttivo, predizione e serializzazione di RegressionTree")
class TreeNodesAndAlgorithmTest {

    private static Data data;

    @BeforeAll
    static void setupDataset() throws Exception {
        data = new Data("provac");
    }

    @Nested
    @DisplayName("Verifica matematica di Node ed SSE")
    class NodeMathematicsTests {

        @Test
        @DisplayName("Calcolo esatto di SSE su sottoinsieme a target identico (SSE = 0.0)")
        void testExactSSEZeroVariance() {
            DiscreteAttribute attrX = (DiscreteAttribute) data.getExplanatoryAttribute(0);
            data.sort(attrX, 0, data.getNumberOfExamples() - 1);

            Node nodeB = new LeafNode(data, 10, 14);
            assertEquals(10.0, ((LeafNode) nodeB).getPredictedClassValue(), 1e-6);
            assertEquals(0.0, nodeB.getVariance(), 1e-6);
        }

        @Test
        @DisplayName("Calcolo esatto di SSE su sottoinsieme A (10 esempi: cinque 1.0 e cinque 1.5, SSE = 0.625)")
        void testExactSSEKnownSubsetA() {
            DiscreteAttribute attrX = (DiscreteAttribute) data.getExplanatoryAttribute(0);
            data.sort(attrX, 0, data.getNumberOfExamples() - 1);

            Node nodeA = new LeafNode(data, 0, 9);
            assertEquals(1.25, ((LeafNode) nodeA).getPredictedClassValue(), 1e-6);
            assertEquals(0.625, nodeA.getVariance(), 1e-6);
        }

        @Test
        @DisplayName("Proprietà base di LeafNode")
        void testLeafNodeProperties() {
            LeafNode leaf = new LeafNode(data, 0, 4);
            assertEquals(0, leaf.getNumberOfChildren());
            assertTrue(leaf.toString().startsWith("LEAF : class="));
        }
    }

    @Nested
    @DisplayName("Verifica dei nodi di split: DiscreteNode e ContinuousNode")
    class SplitNodesTests {

        @Test
        @DisplayName("DiscreteNode: partizionamento esatto e testCondition")
        void testDiscreteNodePartitioning() {
            DiscreteAttribute attrX = (DiscreteAttribute) data.getExplanatoryAttribute(0);
            DiscreteNode discreteNode = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attrX);

            assertEquals(2, discreteNode.getNumberOfChildren());
            assertEquals(0.625, discreteNode.getVariance(), 1e-6);

            assertEquals(0, discreteNode.testCondition("A"));
            assertEquals(1, discreteNode.testCondition("B"));
            assertEquals(-1, discreteNode.testCondition("Z"));

            String query = discreteNode.formulateQuery();
            assertTrue(query.contains("0:X=A"));
            assertTrue(query.contains("1:X=B"));
            assertTrue(discreteNode.toString().contains("DISCRETE SPLIT : attribute=X"));
        }

        @Test
        @DisplayName("ContinuousNode: ricerca del taglio ottimo e testCondition (<= vs >)")
        void testContinuousNodePartitioning() {
            ContinuousAttribute attrY = (ContinuousAttribute) data.getExplanatoryAttribute(1);
            ContinuousNode continuousNode = new ContinuousNode(data, 0, data.getNumberOfExamples() - 1, attrY);

            assertTrue(continuousNode.getNumberOfChildren() <= 2);
            assertTrue(continuousNode.getVariance() >= 0.0);

            int branchLow = continuousNode.testCondition(0.5);
            int branchHigh = continuousNode.testCondition(20.0);
            assertTrue(branchLow >= 0);
            assertTrue(branchHigh >= 0);
            assertTrue(continuousNode.toString().contains("CONTINUOUS SPLIT : attribute=Y"));
        }

        @Test
        @DisplayName("ContinuousNode: caso limite con attributo costante (1 sola partizione)")
        void testContinuousNodeConstantValue() {
            ContinuousAttribute attrY = (ContinuousAttribute) data.getExplanatoryAttribute(1);
            data.sort(attrY, 0, data.getNumberOfExamples() - 1);

            int start = -1;
            int end = -1;
            for (int i = 0; i < data.getNumberOfExamples() - 1; i++) {
                if (data.getExplanatoryValue(i, 1).equals(data.getExplanatoryValue(i + 1, 1))) {
                    start = i;
                    end = i + 1;
                    break;
                }
            }
            assertTrue(start >= 0);

            ContinuousNode constNode = new ContinuousNode(data, start, end, attrY);
            assertEquals(1, constNode.getNumberOfChildren());
            assertEquals("<=", constNode.getSplitInfo(0).getComparator());
        }
    }

    @Nested
    @DisplayName("Verifica di RegressionTree: induzione, print, predictClass e serializzazione")
    class RegressionTreeFullTests {

        @Test
        @DisplayName("Induzione completa di RegressionTree su provaC e print rules/tree")
        void testTreeInductionAndPrints() {
            RegressionTree tree = new RegressionTree(data);
            assertNotNull(tree);

            String treeStr = tree.toString();
            assertFalse(treeStr.isEmpty());
            assertTrue(treeStr.contains("SPLIT : attribute="));
            assertTrue(treeStr.contains("LEAF : class="));

            assertDoesNotThrow(tree::printTree);
            assertDoesNotThrow(tree::printRules);
        }

        @Test
        @DisplayName("Predizione interattiva predictClass con flussi simulati in memoria")
        void testPredictClassSuccessful() throws Exception {
            RegressionTree tree = new RegressionTree(data);

            ByteArrayOutputStream clientBytes = new ByteArrayOutputStream();
            try (ObjectOutputStream clientOut = new ObjectOutputStream(clientBytes)) {
                clientOut.writeObject("1");
                clientOut.flush();
            }

            ByteArrayInputStream serverInBytes = new ByteArrayInputStream(clientBytes.toByteArray());
            ObjectInputStream serverIn = new ObjectInputStream(serverInBytes);
            ByteArrayOutputStream serverOutBytes = new ByteArrayOutputStream();
            ObjectOutputStream serverOut = new ObjectOutputStream(serverOutBytes);

            Double prediction = tree.predictClass(serverOut, serverIn);
            assertEquals(10.0, prediction, 1e-6);

            ByteArrayInputStream receivedBytes = new ByteArrayInputStream(serverOutBytes.toByteArray());
            ObjectInputStream receivedIn = new ObjectInputStream(receivedBytes);
            assertEquals("QUERY", receivedIn.readObject());
            String queryText = (String) receivedIn.readObject();
            assertTrue(queryText.contains("X=B"));
        }

        @Test
        @DisplayName("Sollevamento di UnknownValueException per indice di ramo non valido")
        void testPredictClassUnknownValueException() throws Exception {
            RegressionTree tree = new RegressionTree(data);

            ByteArrayOutputStream clientBytes = new ByteArrayOutputStream();
            try (ObjectOutputStream clientOut = new ObjectOutputStream(clientBytes)) {
                clientOut.writeObject("99");
                clientOut.flush();
            }

            ByteArrayInputStream serverInBytes = new ByteArrayInputStream(clientBytes.toByteArray());
            ObjectInputStream serverIn = new ObjectInputStream(serverInBytes);
            ByteArrayOutputStream serverOutBytes = new ByteArrayOutputStream();
            ObjectOutputStream serverOut = new ObjectOutputStream(serverOutBytes);

            assertThrows(UnknownValueException.class, () -> tree.predictClass(serverOut, serverIn));
        }

        @Test
        @DisplayName("Round-trip completo di serializzazione salva/carica su file .dmp in @TempDir")
        void testSerializationRoundTrip(@TempDir Path tempDir) throws Exception {
            RegressionTree originalTree = new RegressionTree(data);
            Path dumpFile = tempDir.resolve("test_tree.dmp");

            originalTree.salva(dumpFile.toString());
            assertTrue(dumpFile.toFile().exists());
            assertTrue(dumpFile.toFile().length() > 0);

            RegressionTree loadedTree = RegressionTree.carica(dumpFile.toString());
            assertNotNull(loadedTree);

            ByteArrayOutputStream clientBytes = new ByteArrayOutputStream();
            try (ObjectOutputStream clientOut = new ObjectOutputStream(clientBytes)) {
                clientOut.writeObject("1");
                clientOut.flush();
            }

            ObjectInputStream serverIn = new ObjectInputStream(new ByteArrayInputStream(clientBytes.toByteArray()));
            ObjectOutputStream serverOut = new ObjectOutputStream(new ByteArrayOutputStream());

            Double loadedPrediction = loadedTree.predictClass(serverOut, serverIn);
            assertEquals(10.0, loadedPrediction, 1e-6);

            assertThrows(FileNotFoundException.class, () -> RegressionTree.carica(tempDir.resolve("not_found.dmp").toString()));
        }
    }
}
