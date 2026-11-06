package Client;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import dto.PredictionStepDto;
import dto.TreeResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vista principale dell'interfaccia web Vaadin Flow per l'Albero di Regressione.
 *
 * <p>Fornisce un'esperienza utente moderna e reattiva per:
 * <ul>
 *   <li>L'apprendimento supervisionato da database relazionale MySQL;</li>
 *   <li>Il caricamento da archivio serializzato binario {@code .dmp};</li>
 *   <li>La visualizzazione a schede dell'albero: diagramma vettoriale Mermaid, regole IF-THEN stilizzate e testo raw;</li>
 *   <li>La predizione guidata interattiva con percorso breadcrumb dinamico e card di risultato ad alta visibilità.</li>
 * </ul>
 * </p>
 *
 * @author Marco Diana
 * @version 1.0
 */
@Route("")
public class MainView extends VerticalLayout {

    /** Identificatore DOM del contenitore per il rendering SVG del diagramma Mermaid. */
    private static final String DIAGRAM_CONTAINER_ID = "tree-diagram-display";

    /** Servizio REST client per le chiamate HTTP verso il server backend. */
    private final TreeRestClientService treeRestClientService;

    /** Nome della tabella o del modello attualmente caricato. */
    private String currentTableName;

    /** Trascrizione testuale dell'ultimo albero caricato. */
    private String currentTreeRules;

    /** Sequenza degli indici dei rami scelti finora partendo dalla radice. */
    private final List<Integer> currentPath = new ArrayList<>();

    /** Storico descrittivo dei rami scelti per il rendering dei breadcrumb interattivi. */
    private final List<String> pathDescriptions = new ArrayList<>();

    /** Campo di testo per l'immissione del nome della tabella di database o del file archivio {@code .dmp}. */
    private final TextField tableNameField = new TextField("Tabella / File archivio:");

    /** Pulsante per avviare l'induzione dell'albero dal database MySQL. */
    private final Button learnBtn = new Button("Apprendi da DB", new Icon(VaadinIcon.DATABASE));

    /** Pulsante per avviare il caricamento dell'albero serializzato da file {@code .dmp}. */
    private final Button loadBtn = new Button("Carica da File (.dmp)", new Icon(VaadinIcon.FILE_TEXT));

    /** Contenitore a schede per le molteplici rappresentazioni dell'albero. */
    private final TabSheet treeTabSheet = new TabSheet();

    /** Contenitore per il rendering del diagramma vettoriale Mermaid. */
    private final Div diagramContainer = new Div();

    /** Contenitore verticale per l'elenco stilizzato delle regole IF-THEN a card. */
    private final VerticalLayout rulesListContainer = new VerticalLayout();

    /** Area di testo per la consultazione della trascrizione raw dell'albero e delle regole. */
    private final TextArea rawRulesArea = new TextArea();

    /** Contenitore verticale per il rendering dinamico dei passi di predizione guidata. */
    private final VerticalLayout stepContainer = new VerticalLayout();

    /**
     * Costruttore della vista con iniezione del servizio REST e composizione dell'interfaccia.
     *
     * @param treeRestClientService servizio per la comunicazione REST con il backend
     */
    public MainView(TreeRestClientService treeRestClientService) {
        this.treeRestClientService = treeRestClientService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        // 1. Intestazione in Card moderna con badge
        ComponentCard headerCard = createHeaderCard();

        // 2. Barra di controllo e input
        ComponentCard topBarCard = createTopBarCard();

        // 3. Colonna sinistra: Visualizzazione dell'albero (Diagramma, Regole a card, Raw)
        ComponentCard leftCard = createLeftTreeCard();

        // 4. Colonna destra: Pannello di predizione interattivo
        ComponentCard rightCard = createRightPredictCard();

        // Layout centrale a 2 colonne
        HorizontalLayout contentLayout = new HorizontalLayout(leftCard, rightCard);
        contentLayout.setSizeFull();
        contentLayout.setSpacing(true);

        add(headerCard, topBarCard, contentLayout);
    }

    /**
     * Costruisce la card dell'intestazione con icona, titolo e badge informativo.
     */
    private ComponentCard createHeaderCard() {
        Icon appIcon = new Icon(VaadinIcon.CLUSTER);
        appIcon.setSize("32px");
        appIcon.setColor("#3182ce");

        H2 title = new H2("Albero di Regressione");
        title.getStyle().set("margin", "0");
        title.getStyle().set("color", "#1a365d");
        title.getStyle().set("font-size", "22px");

        Span subtitle = new Span("Esplorazione visiva, induzione supervisionata e predizione passo-passo.");
        subtitle.getStyle().set("color", "#718096");
        subtitle.getStyle().set("font-size", "13px");

        VerticalLayout titleLayout = new VerticalLayout(title, subtitle);
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        HorizontalLayout leftHeader = new HorizontalLayout(appIcon, titleLayout);
        leftHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        leftHeader.setSpacing(true);

        // Badge di connessione backend
        Span backendBadge = new Span("REST API :8080 Active");
        backendBadge.getStyle().set("background-color", "#ebf8ff");
        backendBadge.getStyle().set("color", "#2b6cb0");
        backendBadge.getStyle().set("font-size", "12px");
        backendBadge.getStyle().set("font-weight", "600");
        backendBadge.getStyle().set("padding", "6px 12px");
        backendBadge.getStyle().set("border-radius", "16px");
        backendBadge.getStyle().set("border", "1px solid #bee3f8");

        HorizontalLayout headerContent = new HorizontalLayout(leftHeader, backendBadge);
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);

        ComponentCard card = new ComponentCard();
        card.setWidthFull();
        card.add(headerContent);
        return card;
    }

    /**
     * Costruisce la card contenente la form per l'immissione della tabella o file.
     */
    private ComponentCard createTopBarCard() {
        tableNameField.setPlaceholder("Es. provaC");
        tableNameField.setClearButtonVisible(true);
        tableNameField.setWidth("320px");

        learnBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        learnBtn.getStyle().set("font-weight", "600");
        learnBtn.addClickListener(e -> handleLearn());

        loadBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        loadBtn.getStyle().set("font-weight", "600");
        loadBtn.addClickListener(e -> handleLoad());

        HorizontalLayout formLayout = new HorizontalLayout(tableNameField, learnBtn, loadBtn);
        formLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, tableNameField, learnBtn, loadBtn);
        formLayout.setSpacing(true);

        ComponentCard card = new ComponentCard();
        card.setWidthFull();
        card.add(formLayout);
        return card;
    }

    /**
     * Costruisce la card di sinistra per la visualizzazione multilivello dell'albero.
     */
    private ComponentCard createLeftTreeCard() {
        H3 sectionTitle = new H3("Struttura dell'Albero di Decisione");
        sectionTitle.getStyle().set("margin-top", "0");
        sectionTitle.getStyle().set("margin-bottom", "10px");
        sectionTitle.getStyle().set("color", "#2d3748");
        sectionTitle.getStyle().set("font-size", "16px");

        treeTabSheet.setSizeFull();

        // Tab 1: Diagramma ad albero Mermaid
        diagramContainer.setId(DIAGRAM_CONTAINER_ID);
        diagramContainer.setSizeFull();
        diagramContainer.getStyle().set("overflow", "auto");
        diagramContainer.getStyle().set("display", "flex");
        diagramContainer.getStyle().set("justify-content", "center");
        diagramContainer.getStyle().set("align-items", "center");
        diagramContainer.getStyle().set("background-color", "#ffffff");
        diagramContainer.getStyle().set("border-radius", "8px");
        diagramContainer.getStyle().set("padding", "15px");
        renderInitialDiagramPlaceholder();

        // Tab 2: Regole IF-THEN a card
        rulesListContainer.setSizeFull();
        rulesListContainer.setPadding(true);
        rulesListContainer.setSpacing(true);
        rulesListContainer.getStyle().set("overflow-y", "auto");
        renderInitialRulesPlaceholder();

        // Tab 3: Testo raw originale
        rawRulesArea.setSizeFull();
        rawRulesArea.setReadOnly(true);
        rawRulesArea.setPlaceholder("La trascrizione testuale grezza apparirà qui dopo l'acquisizione.");
        rawRulesArea.getStyle().set("font-family", "Consolas, Menlo, Monaco, monospace");
        rawRulesArea.getStyle().set("font-size", "12px");

        Tab tabDiagram = new Tab(new Icon(VaadinIcon.CLUSTER), new Span("Diagramma ad Albero"));
        Tab tabRules = new Tab(new Icon(VaadinIcon.LIST_UL), new Span("Regole IF-THEN"));
        Tab tabRaw = new Tab(new Icon(VaadinIcon.CODE), new Span("Testo Raw"));

        treeTabSheet.add(tabDiagram, diagramContainer);
        treeTabSheet.add(tabRules, rulesListContainer);
        treeTabSheet.add(tabRaw, rawRulesArea);

        // Quando l'utente torna sul tab del diagramma, ri-renderizza per garantire dimensioni corrette
        treeTabSheet.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == tabDiagram && currentTreeRules != null) {
                renderMermaidDiagram();
            }
        });

        ComponentCard card = new ComponentCard();
        card.setWidth("55%");
        card.setHeightFull();
        card.add(sectionTitle, treeTabSheet);
        return card;
    }

    /**
     * Costruisce la card di destra per il pannello di predizione interattivo.
     */
    private ComponentCard createRightPredictCard() {
        H3 predictTitle = new H3("Predizione Guidata Passo-Passo");
        predictTitle.getStyle().set("margin-top", "0");
        predictTitle.getStyle().set("margin-bottom", "10px");
        predictTitle.getStyle().set("color", "#2d3748");
        predictTitle.getStyle().set("font-size", "16px");

        stepContainer.setWidthFull();
        stepContainer.setPadding(false);
        renderInitialStepState();

        ComponentCard card = new ComponentCard();
        card.setWidth("45%");
        card.setHeightFull();
        card.getStyle().set("overflow-y", "auto");
        card.add(predictTitle, stepContainer);
        return card;
    }

    /**
     * Mostra il placeholder iniziale nel tab del diagramma.
     */
    private void renderInitialDiagramPlaceholder() {
        diagramContainer.removeAll();
        Paragraph msg = new Paragraph("Nessun modello caricato. Premi 'Apprendi da DB' o 'Carica da File' per visualizzare il diagramma.");
        msg.getStyle().set("color", "#a0aec0");
        msg.getStyle().set("text-align", "center");
        diagramContainer.add(msg);
    }

    /**
     * Mostra il placeholder iniziale nel tab delle regole IF-THEN.
     */
    private void renderInitialRulesPlaceholder() {
        rulesListContainer.removeAll();
        Paragraph msg = new Paragraph("Nessuna regola attiva. Le regole IF-THEN compariranno qui dopo l'acquisizione.");
        msg.getStyle().set("color", "#a0aec0");
        rulesListContainer.add(msg);
    }

    /**
     * Gestisce il click sul pulsante "Apprendi da DB".
     */
    private void handleLearn() {
        String tableName = tableNameField.getValue();
        if (tableName == null || tableName.trim().isEmpty()) {
            showError("Inserire il nome della tabella di training.");
            return;
        }
        try {
            TreeResponseDto response = treeRestClientService.learnTree(tableName.trim());
            onTreeModelLoaded(response);
            showSuccess("Albero di regressione appreso con successo dal database!");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Gestisce il click sul pulsante "Carica da File (.dmp)".
     */
    private void handleLoad() {
        String fileName = tableNameField.getValue();
        if (fileName == null || fileName.trim().isEmpty()) {
            showError("Inserire il nome del file archivio .dmp da caricare.");
            return;
        }
        try {
            TreeResponseDto response = treeRestClientService.loadTree(fileName.trim());
            onTreeModelLoaded(response);
            showSuccess("Albero caricato con successo dall'archivio serializzato!");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Azione comune eseguita al caricamento di un nuovo albero (aggiorna tutte le viste).
     */
    private void onTreeModelLoaded(TreeResponseDto response) {
        currentTableName = response.tableName();
        currentTreeRules = response.treeRules();
        currentPath.clear();
        pathDescriptions.clear();

        // 1. Aggiorna tab testo raw
        rawRulesArea.setValue(currentTreeRules);

        // 2. Renderizza diagramma Mermaid
        renderMermaidDiagram();

        // 3. Renderizza regole IF-THEN a card
        renderRulesCardList();

        // 4. Renderizza il primo passo di predizione
        renderStep(response.rootStep());
    }

    /**
     * Invoca il client JS per il rendering del diagramma Mermaid.
     */
    private void renderMermaidDiagram() {
        if (currentTreeRules == null) return;
        String mermaidCode = TreeMermaidConverter.convertRulesToMermaid(currentTreeRules);
        UI.getCurrent().getPage().executeJs(
                "if (window.TreeViewer) {" +
                "    window.TreeViewer.render($0, $1);" +
                "} else {" +
                "    const s = document.createElement('script');" +
                "    s.src = '/tree-viewer.js';" +
                "    s.onload = () => { if (window.TreeViewer) window.TreeViewer.render($0, $1); };" +
                "    document.head.appendChild(s);" +
                "}",
                DIAGRAM_CONTAINER_ID, mermaidCode
        );
    }

    /**
     * Popola il tab con le regole IF-THEN stilizzate come card individuali.
     */
    private void renderRulesCardList() {
        rulesListContainer.removeAll();
        List<TreeMermaidConverter.RuleItem> items = TreeMermaidConverter.parseRules(currentTreeRules);

        if (items.isEmpty()) {
            renderInitialRulesPlaceholder();
            return;
        }

        int ruleIndex = 1;
        for (TreeMermaidConverter.RuleItem item : items) {
            HorizontalLayout ruleCard = new HorizontalLayout();
            ruleCard.setWidthFull();
            ruleCard.setAlignItems(FlexComponent.Alignment.CENTER);
            ruleCard.getStyle().set("background-color", "#ffffff");
            ruleCard.getStyle().set("border", "1px solid #e2e8f0");
            ruleCard.getStyle().set("border-radius", "8px");
            ruleCard.getStyle().set("padding", "10px 14px");
            ruleCard.getStyle().set("box-shadow", "0 1px 2px rgba(0,0,0,0.05)");

            Span numBadge = new Span("#" + ruleIndex++);
            numBadge.getStyle().set("background-color", "#edf2f7");
            numBadge.getStyle().set("color", "#4a5568");
            numBadge.getStyle().set("font-weight", "bold");
            numBadge.getStyle().set("font-size", "11px");
            numBadge.getStyle().set("padding", "3px 8px");
            numBadge.getStyle().set("border-radius", "12px");

            // Condizioni in badge blu
            HorizontalLayout condsLayout = new HorizontalLayout();
            condsLayout.setSpacing(true);
            condsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            if (item.conditions().isEmpty()) {
                Span rootPill = new Span("Nessuna condizione (Foglia radice)");
                rootPill.getStyle().set("color", "#718096");
                rootPill.getStyle().set("font-size", "12px");
                condsLayout.add(rootPill);
            } else {
                for (int i = 0; i < item.conditions().size(); i++) {
                    if (i > 0) {
                        Span andSpan = new Span("AND");
                        andSpan.getStyle().set("font-size", "10px");
                        andSpan.getStyle().set("font-weight", "bold");
                        andSpan.getStyle().set("color", "#a0aec0");
                        condsLayout.add(andSpan);
                    }
                    Span condPill = new Span(item.conditions().get(i));
                    condPill.getStyle().set("background-color", "#ebf8ff");
                    condPill.getStyle().set("color", "#2b6cb0");
                    condPill.getStyle().set("font-size", "12px");
                    condPill.getStyle().set("font-weight", "600");
                    condPill.getStyle().set("padding", "4px 8px");
                    condPill.getStyle().set("border-radius", "6px");
                    condPill.getStyle().set("border", "1px solid #bee3f8");
                    condsLayout.add(condPill);
                }
            }

            Span arrow = new Span("→");
            arrow.getStyle().set("font-size", "16px");
            arrow.getStyle().set("font-weight", "bold");
            arrow.getStyle().set("color", "#718096");

            // Target in badge verde
            Span classPill = new Span("Classe: " + item.predictedClass());
            classPill.getStyle().set("background-color", "#d4edda");
            classPill.getStyle().set("color", "#155724");
            classPill.getStyle().set("font-size", "12px");
            classPill.getStyle().set("font-weight", "bold");
            classPill.getStyle().set("padding", "4px 10px");
            classPill.getStyle().set("border-radius", "6px");
            classPill.getStyle().set("border", "1px solid #c3e6cb");

            ruleCard.add(numBadge, condsLayout, arrow, classPill);
            rulesListContainer.add(ruleCard);
        }
    }

    /**
     * Mostra lo stato iniziale del container di predizione prima che un albero sia stato caricato.
     */
    private void renderInitialStepState() {
        stepContainer.removeAll();
        Paragraph info = new Paragraph("Nessun albero attivo. Inserisci il nome della tabella o file e premi 'Apprendi' o 'Carica' per iniziare.");
        info.getStyle().set("color", "#a0aec0");
        info.getStyle().set("font-size", "13px");
        stepContainer.add(info);
    }

    /**
     * Esegue una richiesta di predizione al server per il percorso corrente e aggiorna la UI.
     */
    private void doPredictStep() {
        try {
            PredictionStepDto step = treeRestClientService.predictStep(currentTableName, currentPath);
            renderStep(step);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Esegue il rendering grafico del nodo raggiunto (foglia o split) con breadcrumb interattivo.
     *
     * @param step DTO con le informazioni sul nodo
     */
    private void renderStep(PredictionStepDto step) {
        stepContainer.removeAll();

        if (step == null) {
            renderInitialStepState();
            return;
        }

        // 1. Breadcrumb Stepper visuale
        HorizontalLayout breadcrumbLayout = createBreadcrumbLayout();
        stepContainer.add(breadcrumbLayout);

        if (step.isLeaf()) {
            // Nodo foglia: card in evidenza ad alto impatto
            ComponentCard leafCard = createLeafCard(step);
            stepContainer.add(leafCard);
        } else {
            // Nodo interno di split: mostra attributo e pulsanti dei rami con icone
            ComponentCard splitCard = createSplitCard(step);
            stepContainer.add(splitCard);
        }
    }

    /**
     * Costruisce la barra breadcrumb interattiva con pill cliccabili per tornare indietro.
     */
    private HorizontalLayout createBreadcrumbLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("padding-bottom", "10px");
        layout.getStyle().set("border-bottom", "1px solid #edf2f7");
        layout.getStyle().set("margin-bottom", "12px");
        layout.getStyle().set("overflow-x", "auto");

        // Radice
        Button rootBtn = new Button("Radice", new Icon(VaadinIcon.HOME));
        rootBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        rootBtn.addClickListener(e -> {
            if (!currentPath.isEmpty()) {
                currentPath.clear();
                pathDescriptions.clear();
                doPredictStep();
            }
        });
        layout.add(rootBtn);

        // Passi intermedi
        for (int i = 0; i < currentPath.size(); i++) {
            final int stepTargetIndex = i;
            Span sep = new Span("›");
            sep.getStyle().set("color", "#a0aec0");
            sep.getStyle().set("font-weight", "bold");

            String label = (i < pathDescriptions.size()) ? pathDescriptions.get(i) : ("P" + (i + 1));
            Button stepBtn = new Button(label);
            stepBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            if (i == currentPath.size() - 1) {
                stepBtn.getStyle().set("font-weight", "bold");
                stepBtn.getStyle().set("color", "#2b6cb0");
            } else {
                stepBtn.addClickListener(e -> {
                    while (currentPath.size() > stepTargetIndex + 1) {
                        currentPath.remove(currentPath.size() - 1);
                        if (!pathDescriptions.isEmpty()) {
                            pathDescriptions.remove(pathDescriptions.size() - 1);
                        }
                    }
                    doPredictStep();
                });
            }
            layout.add(sep, stepBtn);
        }

        return layout;
    }

    /**
     * Costruisce la card di risultato per il nodo foglia.
     */
    private ComponentCard createLeafCard(PredictionStepDto step) {
        ComponentCard card = new ComponentCard();
        card.setWidthFull();
        card.getStyle().set("background-color", "#f0fff4");
        card.getStyle().set("border", "2px solid #68d391");
        card.getStyle().set("padding", "20px");

        Icon checkIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
        checkIcon.setSize("36px");
        checkIcon.setColor("#38a169");

        H3 leafTitle = new H3("Foglia Raggiunta — Predizione Conclusa");
        leafTitle.getStyle().set("margin", "0");
        leafTitle.getStyle().set("color", "#22543d");
        leafTitle.getStyle().set("font-size", "18px");

        HorizontalLayout titleRow = new HorizontalLayout(checkIcon, leafTitle);
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);

        // Valore predetto a grandi cifre
        String formattedVal = String.format(Locale.US, "%.4f", step.predictedValue());
        Span valueDisplay = new Span(formattedVal);
        valueDisplay.getStyle().set("font-size", "36px");
        valueDisplay.getStyle().set("font-weight", "800");
        valueDisplay.getStyle().set("color", "#276749");
        valueDisplay.getStyle().set("margin", "12px 0");
        valueDisplay.getStyle().set("display", "block");

        // Statistiche percorso
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setSpacing(true);

        Span badgeDecisions = new Span("Passi: " + currentPath.size());
        styleStatBadge(badgeDecisions, "#ebf8ff", "#2b6cb0", "#bee3f8");

        Span badgeModel = new Span("Modello: " + currentTableName);
        styleStatBadge(badgeModel, "#edf2f7", "#4a5568", "#cbd5e0");

        statsLayout.add(badgeDecisions, badgeModel);

        // Pulsanti di azione
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "16px");

        if (!currentPath.isEmpty()) {
            Button backBtn = new Button("Indietro di un passo", new Icon(VaadinIcon.ARROW_LEFT));
            backBtn.addClickListener(e -> {
                currentPath.remove(currentPath.size() - 1);
                if (!pathDescriptions.isEmpty()) {
                    pathDescriptions.remove(pathDescriptions.size() - 1);
                }
                doPredictStep();
            });
            actions.add(backBtn);
        }

        Button newPredBtn = new Button("Nuova Predizione", new Icon(VaadinIcon.REFRESH));
        newPredBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newPredBtn.addClickListener(e -> {
            currentPath.clear();
            pathDescriptions.clear();
            doPredictStep();
        });
        actions.add(newPredBtn);

        card.add(titleRow, valueDisplay, statsLayout, actions);
        return card;
    }

    private void styleStatBadge(Span span, String bg, String color, String border) {
        span.getStyle().set("background-color", bg);
        span.getStyle().set("color", color);
        span.getStyle().set("border", "1px solid " + border);
        span.getStyle().set("font-size", "12px");
        span.getStyle().set("font-weight", "600");
        span.getStyle().set("padding", "3px 9px");
        span.getStyle().set("border-radius", "12px");
    }

    /**
     * Costruisce la card per la selezione del bivio decisionale (SplitNode).
     */
    private ComponentCard createSplitCard(PredictionStepDto step) {
        ComponentCard card = new ComponentCard();
        card.setWidthFull();

        Span stepBadge = new Span("Bivio Decisionale #" + (currentPath.size() + 1));
        stepBadge.getStyle().set("background-color", "#ebf8ff");
        stepBadge.getStyle().set("color", "#2b6cb0");
        stepBadge.getStyle().set("font-weight", "bold");
        stepBadge.getStyle().set("font-size", "12px");
        stepBadge.getStyle().set("padding", "3px 8px");
        stepBadge.getStyle().set("border-radius", "6px");

        H4 attributeHeader = new H4("Attributo di Valutazione: " + step.attributeName());
        attributeHeader.getStyle().set("margin-top", "8px");
        attributeHeader.getStyle().set("margin-bottom", "4px");
        attributeHeader.getStyle().set("color", "#2d3748");

        Paragraph guide = new Paragraph("Seleziona il ramo che corrisponde al valore dell'esempio da stimare:");
        guide.getStyle().set("color", "#718096");
        guide.getStyle().set("font-size", "13px");
        guide.getStyle().set("margin-bottom", "12px");

        VerticalLayout branchList = new VerticalLayout();
        branchList.setPadding(false);
        branchList.setSpacing(true);

        if (step.branchOptions() != null) {
            for (int i = 0; i < step.branchOptions().size(); i++) {
                final int branchIndex = i;
                String optionText = step.branchOptions().get(i);
                String fullBranchLabel = step.attributeName() + " " + optionText;

                Button branchBtn = new Button(fullBranchLabel, new Icon(VaadinIcon.ARROW_RIGHT));
                branchBtn.setWidthFull();
                branchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                branchBtn.getStyle().set("font-weight", "600");
                branchBtn.getStyle().set("justify-content", "space-between");

                branchBtn.addClickListener(e -> {
                    currentPath.add(branchIndex);
                    pathDescriptions.add(fullBranchLabel);
                    doPredictStep();
                });
                branchList.add(branchBtn);
            }
        }

        HorizontalLayout navActions = new HorizontalLayout();
        navActions.getStyle().set("margin-top", "12px");
        if (!currentPath.isEmpty()) {
            Button backBtn = new Button("Indietro di un passo", new Icon(VaadinIcon.ARROW_LEFT));
            backBtn.addClickListener(e -> {
                currentPath.remove(currentPath.size() - 1);
                if (!pathDescriptions.isEmpty()) {
                    pathDescriptions.remove(pathDescriptions.size() - 1);
                }
                doPredictStep();
            });
            navActions.add(backBtn);
        }

        card.add(stepBadge, attributeHeader, guide, branchList, navActions);
        return card;
    }

    /**
     * Mostra una notifica di successo in verde.
     */
    private void showSuccess(String msg) {
        Notification notification = Notification.show(msg, 3500, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Mostra una notifica di errore in rosso.
     */
    private void showError(String msg) {
        String displayMsg = (msg != null && !msg.isBlank()) ? msg : "Si e' verificato un errore inatteso.";
        Notification notification = Notification.show(displayMsg, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Classe helper interna per creare container a card con stile moderno e ombreggiature uniformi.
     */
    private static class ComponentCard extends VerticalLayout {
        ComponentCard() {
            setPadding(true);
            setSpacing(true);
            getStyle().set("background-color", "#ffffff");
            getStyle().set("border", "1px solid #e2e8f0");
            getStyle().set("border-radius", "12px");
            getStyle().set("box-shadow", "0 1px 3px 0 rgba(0, 0, 0, 0.07), 0 1px 2px 0 rgba(0, 0, 0, 0.04)");
        }
    }
}
