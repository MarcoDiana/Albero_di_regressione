package Client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe di utilità per la conversione e il parsing delle regole testuali dell'albero
 * di regressione in rappresentazioni visuali (diagramma vettoriale Mermaid e lista strutturata).
 *
 * <p>Analizza l'output prodotto da {@code RegressionTree.getRules()} e sintetizza
 * un grafo di decisione conforme alla sintassi Mermaid {@code graph TD}, assegnando
 * nodi ellittici per le foglie e rettangolari per i bivi di split, con etichette esplicative
 * su ogni arco orientato.</p>
 *
 * @author Marco Diana
 * @version 1.0
 */
public final class TreeMermaidConverter {

    /** Pattern per estrarre la linea della regola con prefisso di condizioni e valore target. */
    private static final Pattern RULE_LINE_PATTERN = Pattern.compile("^(.*?)\\s*==>\\s*Class=(.+)$");

    /** Pattern per identificare l'operatore di confronto all'interno di una condizione singola. */
    private static final Pattern CONDITION_OPERATOR_PATTERN = Pattern.compile("^(.*?)(<=|>=|!=|<|>|=)(.*)$");

    /**
     * Costruttore privato per prevenire l'istanziazione di questa classe di utilità.
     */
    private TreeMermaidConverter() {
        // utility class
    }

    /**
     * Rappresentazione immutabile di una singola regola decisionale estratta.
     *
     * @param conditions     lista delle condizioni atomiche (es. {@code ["X = A", "Y <= 2.0"]})
     * @param predictedClass valore continuo predetto dalla foglia terminale
     */
    public record RuleItem(List<String> conditions, String predictedClass) {
        /**
         * Restituisce la formulazione in linguaggio naturale IF-THEN della regola.
         *
         * @return stringa formattata (es. {@code "IF X = A AND Y <= 2.0 THEN Classe = 1.0"})
         */
        public String toFormattedString() {
            if (conditions.isEmpty()) {
                return "THEN Classe = " + predictedClass;
            }
            return "IF " + String.join(" AND ", conditions) + " THEN Classe = " + predictedClass;
        }
    }

    /**
     * Converte la trascrizione testuale delle regole in un diagramma Mermaid {@code graph TD}.
     *
     * @param treeRulesOutput output testuale completo restituito dal server
     * @return diagramma Mermaid valido pronto per il rendering SVG
     */
    public static String convertRulesToMermaid(String treeRulesOutput) {
        List<RuleItem> rules = parseRules(treeRulesOutput);
        if (rules.isEmpty()) {
            return "graph TD\n    Empty[\"Nessuna regola disponibile\"]\n";
        }

        // Caso limite: albero degenere con sola foglia
        if (rules.size() == 1 && rules.get(0).conditions().isEmpty()) {
            String val = rules.get(0).predictedClass();
            return "graph TD\n    L0([\"Classe Predetta: " + escapeMermaid(val) + "\"]):::leaf\n"
                    + "    classDef leaf fill:#d4edda,stroke:#28a745,stroke-width:2px,color:#155724;\n";
        }

        // Costruzione del prefix tree (Trie dei nodi decisionali)
        TrieNode rootTrie = new TrieNode("");
        int leafCounter = 0;

        for (RuleItem rule : rules) {
            TrieNode current = rootTrie;
            for (String cond : rule.conditions()) {
                ParsedCondition parsed = parseCondition(cond);
                current.attributeName = parsed.attribute();
                String branchLabel = parsed.branchLabel();
                current = current.children.computeIfAbsent(branchLabel, k -> new TrieNode(""));
            }
            current.isLeaf = true;
            current.predictedValue = rule.predictedClass();
        }

        // Generazione del grafo Mermaid
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");

        NodeIdGenerator idGen = new NodeIdGenerator();
        generateMermaidGraph(rootTrie, idGen, sb);

        // Aggiunta stili CSS professionali
        sb.append("\n    classDef split fill:#ebf8ff,stroke:#3182ce,stroke-width:2px,color:#2b6cb0,font-weight:bold;\n");
        sb.append("    classDef leaf fill:#d4edda,stroke:#28a745,stroke-width:2px,color:#155724,font-weight:bold;\n");

        return sb.toString();
    }

    /**
     * Estrae le sole linee delle regole formattate come stringhe leggibili.
     *
     * @param treeRulesOutput output testuale completo restituito dal server
     * @return elenco di regole formattate con prefisso IF-THEN
     */
    public static List<String> extractCleanRules(String treeRulesOutput) {
        List<RuleItem> items = parseRules(treeRulesOutput);
        List<String> list = new ArrayList<>(items.size());
        for (RuleItem item : items) {
            list.add(item.toFormattedString());
        }
        return list;
    }

    /**
     * Esegue il parsing analitico delle regole dall'output testuale.
     *
     * @param treeRulesOutput output testuale completo
     * @return lista strutturata di oggetti {@link RuleItem}
     */
    public static List<RuleItem> parseRules(String treeRulesOutput) {
        List<RuleItem> results = new ArrayList<>();
        if (treeRulesOutput == null || treeRulesOutput.isBlank()) {
            return results;
        }

        String[] lines = treeRulesOutput.split("\\r?\\n");
        boolean inRulesSection = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.equals("********* RULES **********")) {
                inRulesSection = true;
                continue;
            }
            if (inRulesSection && trimmed.equals("**************************")) {
                break;
            }
            if (inRulesSection && !trimmed.isEmpty()) {
                if (trimmed.startsWith("Class=")) {
                    String classValuePart = trimmed.substring("Class=".length()).trim();
                    results.add(new RuleItem(List.of(), classValuePart));
                } else {
                    Matcher matcher = RULE_LINE_PATTERN.matcher(trimmed);
                    if (matcher.matches()) {
                        String conditionsPart = matcher.group(1).trim();
                        String classValuePart = matcher.group(2).trim();

                        List<String> conditions = new ArrayList<>();
                        if (!conditionsPart.isEmpty()) {
                            String[] tokens = conditionsPart.split("\\s+AND\\s+");
                            for (String token : tokens) {
                                String t = token.trim();
                                if (!t.isEmpty()) {
                                    conditions.add(t);
                                }
                            }
                        }
                        results.add(new RuleItem(conditions, classValuePart));
                    }
                }
            }
        }
        return results;
    }

    /**
     * Record interno per memorizzare un attributo e la sua etichetta di bivio.
     */
    private record ParsedCondition(String attribute, String branchLabel) {}

    /**
     * Scompone una condizione atomica in nome attributo ed etichetta di ramo.
     */
    private static ParsedCondition parseCondition(String cond) {
        Matcher m = CONDITION_OPERATOR_PATTERN.matcher(cond);
        if (m.matches()) {
            String attr = m.group(1).trim();
            String op = m.group(2).trim();
            String val = m.group(3).trim();
            return new ParsedCondition(attr, op + " " + val);
        }
        return new ParsedCondition(cond, cond);
    }

    /**
     * Generatore sequenziale ricorsivo per la sintassi Mermaid.
     */
    private static void generateMermaidGraph(TrieNode node, NodeIdGenerator idGen, StringBuilder sb) {
        String currentId = idGen.getOrCreateId(node);
        if (node.isLeaf) {
            sb.append("    ").append(currentId).append("([\"Classe: ")
                    .append(escapeMermaid(node.predictedValue)).append("\"]):::leaf\n");
            return;
        }

        String label = node.attributeName != null && !node.attributeName.isEmpty() ? node.attributeName : "Split";
        sb.append("    ").append(currentId).append("[\"").append(escapeMermaid(label)).append("\"]:::split\n");

        for (Map.Entry<String, TrieNode> entry : node.children.entrySet()) {
            String edgeLabel = entry.getKey();
            TrieNode childNode = entry.getValue();
            String childId = idGen.getOrCreateId(childNode);
            sb.append("    ").append(currentId).append(" -->|\"").append(escapeMermaid(edgeLabel)).append("\"| ").append(childId).append("\n");
            generateMermaidGraph(childNode, idGen, sb);
        }
    }

    private static String escapeMermaid(String text) {
        if (text == null) return "";
        return text.replace("\"", "'");
    }

    /**
     * Nodo ausiliario per la costruzione del grafo prefisso.
     */
    private static class TrieNode {
        String attributeName;
        boolean isLeaf = false;
        String predictedValue;
        final Map<String, TrieNode> children = new LinkedHashMap<>();

        TrieNode(String attributeName) {
            this.attributeName = attributeName;
        }
    }

    /**
     * Gestore per l'assegnazione di identificatori univoci ai nodi del grafo.
     */
    private static class NodeIdGenerator {
        private int splitCount = 0;
        private int leafCount = 0;
        private final Map<TrieNode, String> nodeMap = new LinkedHashMap<>();

        String getOrCreateId(TrieNode node) {
            return nodeMap.computeIfAbsent(node, n -> {
                if (n.isLeaf) {
                    return "L" + (leafCount++);
                } else {
                    return "N" + (splitCount++);
                }
            });
        }
    }
}
