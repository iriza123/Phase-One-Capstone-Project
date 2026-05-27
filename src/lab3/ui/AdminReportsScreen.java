package lab3.ui;

import lab1.model.Transaction;
import lab3.service.TransactionService;
import lab3.util.CSVExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// LAB 3 - Admin: Reports - export all transactions, daily summary
public class AdminReportsScreen extends BaseScreen {

    private final TransactionService txSvc = new TransactionService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private TableView<Transaction> table;
    private Label summaryLabel;
    private DatePicker fromPicker, toPicker;

    public AdminReportsScreen(Stage stage) { super(stage); }

    @Override
    protected HBox buildTopBar(String title) {
        HBox bar = super.buildTopBar(title);
        ((Button) bar.getChildren().get(0)).setOnAction(e -> new AdminDashboardScreen(stage).show());
        return bar;
    }

    public void show() {
        VBox content = new VBox(18);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Reports & Export");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        // Filter + export card
        VBox filterCard = new VBox(14);
        filterCard.getStyleClass().add("form-card");
        filterCard.setMaxWidth(1000);

        HBox filterRow = new HBox(14);
        filterRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox fromGroup = new VBox(5);
        Label fromLbl = new Label("From Date"); fromLbl.getStyleClass().add("field-label");
        fromPicker = new DatePicker(LocalDate.now().minusMonths(1));
        fromPicker.getStyleClass().add("date-picker"); fromPicker.setPrefWidth(160);
        fromGroup.getChildren().addAll(fromLbl, fromPicker);

        VBox toGroup = new VBox(5);
        Label toLbl = new Label("To Date"); toLbl.getStyleClass().add("field-label");
        toPicker = new DatePicker(LocalDate.now());
        toPicker.getStyleClass().add("date-picker"); toPicker.setPrefWidth(160);
        toGroup.getChildren().addAll(toLbl, toPicker);

        Button filterBtn  = new Button("Filter");       filterBtn.getStyleClass().add("btn-primary");
        Button allBtn     = new Button("Show All");     allBtn.getStyleClass().add("btn-secondary");
        Button exportBtn  = new Button("Export CSV");   exportBtn.getStyleClass().add("btn-success");
        Button dailyBtn   = new Button("Daily Summary CSV"); dailyBtn.getStyleClass().add("btn-secondary");

        filterBtn.setOnAction(e -> applyFilter());
        allBtn.setOnAction(e -> loadAll());
        exportBtn.setOnAction(e -> exportCSV());
        dailyBtn.setOnAction(e -> exportDailySummary());

        VBox btnGroup = new VBox(5);
        Label sp = new Label(" "); sp.getStyleClass().add("field-label");
        HBox btnRow = new HBox(10);
        btnRow.getChildren().addAll(filterBtn, allBtn, exportBtn, dailyBtn);
        btnGroup.getChildren().addAll(sp, btnRow);
        HBox.setHgrow(btnGroup, Priority.ALWAYS);

        filterRow.getChildren().addAll(fromGroup, toGroup, btnGroup);

        summaryLabel = new Label("Loading...");
        summaryLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        filterCard.getChildren().addAll(filterRow, summaryLabel);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, filterCard, table);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("Admin - Reports"));
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(20));

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Admin Reports");
        stage.show();
        loadAll();
    }

    private void loadAll() {
        try {
            List<Transaction> list = txSvc.getAll();
            table.setItems(FXCollections.observableArrayList(list));
            updateSummary(list);
        } catch (Exception e) { summaryLabel.setText("Error: " + e.getMessage()); }
    }

    private void applyFilter() {
        LocalDate from = fromPicker.getValue();
        LocalDate to   = toPicker.getValue();
        if (from == null || to == null || from.isAfter(to)) {
            summaryLabel.setText("Select a valid date range."); return;
        }
        try {
            List<Transaction> list = txSvc.getByDateRange(from, to);
            table.setItems(FXCollections.observableArrayList(list));
            updateSummary(list);
        } catch (Exception e) { summaryLabel.setText("Error: " + e.getMessage()); }
    }

    private void updateSummary(List<Transaction> list) {
        double total = list.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();
        double fees  = list.stream().mapToDouble(t -> t.getFee() != null ? t.getFee().doubleValue() : 0).sum();
        summaryLabel.setText(String.format("%d transactions  |  Total: %,.2f RWF  |  Fees: %,.2f RWF",
            list.size(), total, fees));
    }

    private void exportCSV() {
        if (table.getItems().isEmpty()) { summaryLabel.setText("No data to export."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Report");
        fc.setInitialFileName("igirepay_all_transactions_" + LocalDate.now() + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                CSVExporter.exportTransactions(table.getItems(), file.getAbsolutePath());
                summaryLabel.setText("Exported " + table.getItems().size() + " rows to: " + file.getName());
            } catch (Exception e) { summaryLabel.setText("Export failed: " + e.getMessage()); }
        }
    }

    private void exportDailySummary() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Daily Summary");
        fc.setInitialFileName("daily_summary_" + LocalDate.now() + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                List<Transaction> todayTx = txSvc.getByDateRange(LocalDate.now(), LocalDate.now());
                CSVExporter.exportDailySummary(todayTx, file.getAbsolutePath(), LocalDate.now().toString());
                summaryLabel.setText("Daily summary exported: " + file.getName());
            } catch (Exception e) { summaryLabel.setText("Export failed: " + e.getMessage()); }
        }
    }

    private TableView<Transaction> buildTable() {
        TableView<Transaction> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("Reference",    t -> t.getReferenceId()),
            col("Customer",     t -> t.getCustomerName() != null ? t.getCustomerName() : "--"),
            col("Type",         t -> t.getTransactionType()),
            col("From",         t -> t.getFromAccountNumber() != null ? t.getFromAccountNumber() : "--"),
            col("To",           t -> t.getToAccountNumber()   != null ? t.getToAccountNumber()   : "--"),
            col("Amount (RWF)", t -> String.format("%,.2f", t.getAmount())),
            col("Fee (RWF)",    t -> t.getFee() != null ? String.format("%,.2f", t.getFee()) : "0.00"),
            col("Status",       t -> t.getStatus()),
            col("Date",         t -> t.getCreatedAt() != null ? t.getCreatedAt().format(FMT) : "")
        );
        tv.setPlaceholder(new Label("No transactions found."));
        return tv;
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }
}
