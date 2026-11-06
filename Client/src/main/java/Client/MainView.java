package Client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
 * Consente l'apprendimento da database MySQL, il caricamento da file serializzato {@code .dmp},
 * la visualizzazione a video delle regole estratte e l'esecuzione interattiva della predizione
 * guidata lungo i rami dell'albero.
 *
 * @author Marco Diana
 * @version 1.0
 */
@Route("")
public class MainView extends VerticalLayout {

    /** Servizio REST client per le chiamate HTTP verso il server backend. */
    private final TreeRestClientService treeRestClientService;

    /** Nome della tabella o del modello attualmente caricato. */
    private String currentTableName;

    /** Sequenza degli indici dei rami scelti finora partendo dalla radice. */
    private final List<Integer> currentPath = new ArrayList<>();

    /** Campo di testo per l'immissione del nome della tabella di database o del file archivio {@code .dmp}. */
    private final TextField tableNameField = new TextField("Tabella / File archivio:");

    /** Pulsante per avviare l'induzione dell'albero dal database MySQL. */
    private final Button learnBtn = new Button("Apprendi da DB", new Icon(VaadinIcon.DATABASE));

    /** Pulsante per avviare il caricamento dell'albero serializzato da file {@code .dmp}. */
    private final Button loadBtn = new Button("Carica da File (.dmp)", new Icon(VaadinIcon.FILE_TEXT));

    /** Area di testo per la visualizzazione delle regole IF-THEN e della struttura dell'albero. */
    private final TextArea rulesArea = new TextArea("Struttura dell'Albero e Regole IF-THEN:");

    /** Contenitore verticale per il rendering dinamico dei passi di predizione guidata. */
    private final VerticalLayout stepContainer = new VerticalLayout();

    /**
     * Costruttore della vista con iniezione del servizio REST.
     *
     * @param treeRestClientService servizio per la comunicazione REST con il backend
     */
    public MainView(TreeRestClientService treeRestClientService) {
        this.treeRestClientService = treeRestClientService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Intestazione
        H2 header = new H2("Albero di Regressione — Web Client");
        Paragraph description = new Paragraph(
                "Interfaccia web interattiva per l'induzione e navigazione dell'albero di regressione da database MySQL.");
        VerticalLayout headerLayout = new VerticalLayout(header, description);
        headerLayout.setPadding(false);
        headerLayout.setSpacing(false);

        // Barra di controllo superiore
        tableNameField.setPlaceholder("Es. provaC");
        tableNameField.setClearButtonVisible(true);
        tableNameField.setWidth("280px");

        learnBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        learnBtn.addClickListener(e -> handleLearn());

        loadBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        loadBtn.addClickListener(e -> handleLoad());

        HorizontalLayout topBar = new HorizontalLayout(tableNameField, learnBtn, loadBtn);
        topBar.setVerticalComponentAlignment(FlexComponent.Alignment.END, tableNameField, learnBtn, loadBtn);
        topBar.setSpacing(true);

        // Area regole a sinistra
        rulesArea.setSizeFull();
        rulesArea.setReadOnly(true);
        rulesArea.setPlaceholder("Le regole e la struttura dell'albero appariranno qui dopo l'apprendimento o il caricamento.");
        rulesArea.getStyle().set("font-family", "Consolas, Menlo, Monaco, monospace");
        rulesArea.getStyle().set("font-size", "13px");

        VerticalLayout leftLayout = new VerticalLayout(rulesArea);
        leftLayout.setWidth("50%");
        leftLayout.setHeightFull();
        leftLayout.setPadding(false);

        // Area predizione a destra
        H3 predictTitle = new H3("Predizione Guidata Passo-Passo");
        stepContainer.setWidthFull();
        stepContainer.setPadding(false);
        renderInitialStepState();

        VerticalLayout rightLayout = new VerticalLayout(predictTitle, stepContainer);
        rightLayout.setWidth("50%");
        rightLayout.setHeightFull();
        rightLayout.setPadding(false);

        // Layout centrale a 2 colonne
        HorizontalLayout contentLayout = new HorizontalLayout(leftLayout, rightLayout);
        contentLayout.setSizeFull();

        add(headerLayout, topBar, contentLayout);
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
            currentTableName = response.tableName();
            currentPath.clear();
            rulesArea.setValue(response.treeRules());
            renderStep(response.rootStep());
            showSuccess("Albero appreso con successo dal database!");
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
            currentTableName = response.tableName();
            currentPath.clear();
            rulesArea.setValue(response.treeRules());
            renderStep(response.rootStep());
            showSuccess("Albero caricato con successo da archivio .dmp!");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Mostra lo stato iniziale del container di predizione prima che un albero sia stato caricato.
     */
    private void renderInitialStepState() {
        stepContainer.removeAll();
        Paragraph info = new Paragraph("Nessun albero attivo. Inserisci il nome della tabella o file e premi 'Apprendi' o 'Carica' per iniziare.");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");
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
     * Esegue il rendering grafico del nodo raggiunto (foglia o split).
     *
     * @param step DTO con le informazioni sul nodo
     */
    private void renderStep(PredictionStepDto step) {
        stepContainer.removeAll();

        if (step == null) {
            renderInitialStepState();
            return;
        }

        if (step.isLeaf()) {
            // Nodo foglia: mostra il valore predetto e i controlli di riavvio
            Span leafBadge = new Span("FOGLIA RAGGIUNTA — VALORE PREDETTO: "
                    + String.format(Locale.US, "%.4f", step.predictedValue()));
            leafBadge.getStyle().set("padding", "12px 18px");
            leafBadge.getStyle().set("font-weight", "bold");
            leafBadge.getStyle().set("font-size", "16px");
            leafBadge.getStyle().set("color", "#155724");
            leafBadge.getStyle().set("background-color", "#d4edda");
            leafBadge.getStyle().set("border", "1px solid #c3e6cb");
            leafBadge.getStyle().set("border-radius", "6px");

            Paragraph pathSummary = new Paragraph("Percorso decisionale completato in " + currentPath.size() + " passi.");
            pathSummary.getStyle().set("color", "var(--lumo-secondary-text-color)");

            HorizontalLayout actions = new HorizontalLayout();
            if (!currentPath.isEmpty()) {
                Button backBtn = new Button("Indietro di un passo", new Icon(VaadinIcon.ARROW_LEFT));
                backBtn.addClickListener(e -> {
                    currentPath.remove(currentPath.size() - 1);
                    doPredictStep();
                });
                actions.add(backBtn);
            }

            Button newPredBtn = new Button("Nuova Predizione", new Icon(VaadinIcon.REFRESH));
            newPredBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            newPredBtn.addClickListener(e -> {
                currentPath.clear();
                doPredictStep();
            });
            actions.add(newPredBtn);

            stepContainer.add(leafBadge, pathSummary, actions);
        } else {
            // Nodo interno di split: mostra attributo, pulsanti dei rami e pulsante indietro
            Span breadcrumb = new Span("Passo corrente: " + (currentPath.size() + 1)
                    + " (Decisioni precedenti: " + currentPath.size() + ")");
            breadcrumb.getStyle().set("font-size", "12px");
            breadcrumb.getStyle().set("color", "var(--lumo-secondary-text-color)");

            H4 attributeHeader = new H4("Attributo di decisione: " + step.attributeName());
            Paragraph guide = new Paragraph("Seleziona il ramo corrispondente al valore del nuovo esempio:");

            VerticalLayout branchList = new VerticalLayout();
            branchList.setPadding(false);
            branchList.setSpacing(true);

            if (step.branchOptions() != null) {
                for (int i = 0; i < step.branchOptions().size(); i++) {
                    final int branchIndex = i;
                    String optionText = step.branchOptions().get(i);
                    Button branchBtn = new Button("Ramo " + i + ": " + step.attributeName() + " " + optionText);
                    branchBtn.setWidthFull();
                    branchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    branchBtn.addClickListener(e -> {
                        currentPath.add(branchIndex);
                        doPredictStep();
                    });
                    branchList.add(branchBtn);
                }
            }

            stepContainer.add(breadcrumb, attributeHeader, guide, branchList);

            if (!currentPath.isEmpty()) {
                Button backBtn = new Button("Indietro di un passo", new Icon(VaadinIcon.ARROW_LEFT));
                backBtn.addClickListener(e -> {
                    currentPath.remove(currentPath.size() - 1);
                    doPredictStep();
                });
                stepContainer.add(backBtn);
            }
        }
    }

    /**
     * Mostra una notifica di successo in verde.
     *
     * @param msg messaggio informativo
     */
    private void showSuccess(String msg) {
        Notification notification = Notification.show(msg, 3500, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Mostra una notifica di errore in rosso.
     *
     * @param msg descrizione dell'errore
     */
    private void showError(String msg) {
        String displayMsg = (msg != null && !msg.isBlank()) ? msg : "Si e' verificato un errore inatteso.";
        Notification notification = Notification.show(displayMsg, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
