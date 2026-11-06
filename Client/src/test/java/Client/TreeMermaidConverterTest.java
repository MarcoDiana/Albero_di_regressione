package Client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test unitari per la classe {@link TreeMermaidConverter}.
 *
 * @author Marco Diana
 * @version 1.0
 */
class TreeMermaidConverterTest {

    private static final String SAMPLE_RULES_PROVAC = """
            ********* TREE **********
            DISCRETE SPLIT : attribute=X ...
            *************************

            ********* RULES **********

            X=A AND Y<=2.0 ==> Class=1.0
            X=A AND Y>2.0 ==> Class=1.5
            X=B ==> Class=10.0

            **************************
            """;

    @Test
    @DisplayName("Dovrebbe analizzare correttamente le 3 regole dell'albero nominale provaC")
    void testParseRulesNominal() {
        List<TreeMermaidConverter.RuleItem> items = TreeMermaidConverter.parseRules(SAMPLE_RULES_PROVAC);
        assertEquals(3, items.size());

        assertEquals(List.of("X=A", "Y<=2.0"), items.get(0).conditions());
        assertEquals("1.0", items.get(0).predictedClass());
        assertEquals("IF X=A AND Y<=2.0 THEN Classe = 1.0", items.get(0).toFormattedString());

        assertEquals(List.of("X=A", "Y>2.0"), items.get(1).conditions());
        assertEquals("1.5", items.get(1).predictedClass());

        assertEquals(List.of("X=B"), items.get(2).conditions());
        assertEquals("10.0", items.get(2).predictedClass());
    }

    @Test
    @DisplayName("Dovrebbe generare un diagramma Mermaid graph TD valido con nodi di split e foglie")
    void testConvertToMermaidNominal() {
        String mermaid = TreeMermaidConverter.convertRulesToMermaid(SAMPLE_RULES_PROVAC);
        assertNotNull(mermaid);
        assertTrue(mermaid.startsWith("graph TD"));
        assertTrue(mermaid.contains("[\"X\"]:::split"));
        assertTrue(mermaid.contains("[\"Y\"]:::split"));
        assertTrue(mermaid.contains("([\"Classe: 1.0\"]):::leaf"));
        assertTrue(mermaid.contains("([\"Classe: 1.5\"]):::leaf"));
        assertTrue(mermaid.contains("([\"Classe: 10.0\"]):::leaf"));
        assertTrue(mermaid.contains("-->|\"= A\"|"));
        assertTrue(mermaid.contains("-->|\"<= 2.0\"|"));
        assertTrue(mermaid.contains("-->|\"> 2.0\"|"));
        assertTrue(mermaid.contains("-->|\"= B\"|"));
        assertTrue(mermaid.contains("classDef split"));
        assertTrue(mermaid.contains("classDef leaf"));
    }

    @Test
    @DisplayName("Dovrebbe gestire correttamente un albero degenere composto da una sola foglia")
    void testSingleLeafTree() {
        String singleLeafRules = """
                ********* RULES **********
                Class=42.5
                **************************
                """;
        List<TreeMermaidConverter.RuleItem> items = TreeMermaidConverter.parseRules(singleLeafRules);
        assertEquals(1, items.size());
        assertTrue(items.get(0).conditions().isEmpty());
        assertEquals("42.5", items.get(0).predictedClass());
        assertEquals("THEN Classe = 42.5", items.get(0).toFormattedString());

        String mermaid = TreeMermaidConverter.convertRulesToMermaid(singleLeafRules);
        assertTrue(mermaid.contains("Classe Predetta: 42.5"));
    }

    @Test
    @DisplayName("Dovrebbe gestire input nulli o vuoti senza sollevare eccezioni")
    void testEmptyOrNullInput() {
        assertTrue(TreeMermaidConverter.parseRules(null).isEmpty());
        assertTrue(TreeMermaidConverter.parseRules("").isEmpty());
        assertTrue(TreeMermaidConverter.extractCleanRules(null).isEmpty());

        String mermaidNull = TreeMermaidConverter.convertRulesToMermaid(null);
        assertTrue(mermaidNull.contains("Nessuna regola disponibile"));
    }

    @Test
    @DisplayName("Dovrebbe estrarre correttamente la lista di regole pulite in formato IF-THEN")
    void testExtractCleanRules() {
        List<String> cleanRules = TreeMermaidConverter.extractCleanRules(SAMPLE_RULES_PROVAC);
        assertEquals(3, cleanRules.size());
        assertEquals("IF X=A AND Y<=2.0 THEN Classe = 1.0", cleanRules.get(0));
        assertEquals("IF X=A AND Y>2.0 THEN Classe = 1.5", cleanRules.get(1));
        assertEquals("IF X=B THEN Classe = 10.0", cleanRules.get(2));
    }
}
