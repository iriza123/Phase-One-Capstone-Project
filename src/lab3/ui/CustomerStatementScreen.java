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

// LAB 3 - Exercise 3.3: Customer personal transaction statement
public class CustomerStatementScreen extends BaseScreen {

    private final TransactionService txSvc = new TransactionService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private TableView<Transaction> table;
    private Label summaryLabel;

    public CustomerStatementScreen(Stage stage) { super(stage); }

    public void show() {
        VBox content = new VBox(20);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("My Account Statement");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        // Customer info
        VBox infoCard = new VBox(8);
        infoCard.getStyleClass().add("form-card");
        infoCard.setMaxWidth(900);
        String name  = SessionManager.getUser() != null ? SessionManager.getUser().getFullName() : "";
        String phone = SessionManager.getUser() != null ? SessionManager.getUser().getPhone() : "";
        Label nameLabel  = new Label("Customer: " + name);
        Label phoneLabel = new Label("Phone: " + phone);
        Label dateLabel  = new Label("Statement Date: " + LocalDate.now());
        nameLabel.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-family:'Times New Roman';");
        phoneLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.7);-fx-font-size:13px;-fx-font-family:'Times New Roman';");
        dateLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.7);-fx-font-size:13px;-fx-font-family:'Times New Roman';");

        summaryLabel = new Label();
        summaryLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        HBox btnRow = new HBox(12);
        Button exportBtn = new Button("Export Statement CSV");
        exportBtn.getStyleClass().add("btn-success");
        exportBtn.setOnAction(e -> exportStatement());
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadStatement());
        btnRow.getChildren().addAll(exportBtn, refreshBtn);

        infoCard.getChildren().addAll(nameLabel, phoneLabel, dateLabel, summaryLabel, btnRow);

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, infoCard, table);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("My Statement"));
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(20));

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Statement");
        stage.show();
        loadStatement();
    }

    private void loadStatement() {
        try {
            int custId = SessionManager.getUser().getCustomerId();
            List<Transaction> list = txSvc.getByCustomer(custId);
            table.setItems(FXCollections.observableArrayList(list));
            double total = list.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();
            double fees  = list.stream().mapToDouble(t -> t.getFee() != null ? t.getFee().doubleValue() : 0).sum();
            summaryLabel.setText(String.format("%d transactions  |  Total: %,.2f RWF  |  Fees paid: %,.2f RWF",
                list.size(), total, fees));
        } catch (Exception e) {
            summaryLabel.setText("Error loading statement: " + e.getMessage());
        }
    }

    private void exportStatement() {
        if (table.getItems().isEmpty()) { summaryLabel.setText("No transactions to export."); return; }
        String safeName = SessionManager.getUser().getFullName().replace(" ", "_");
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Statement");
        fc.setInitialFileName("statement_" + safeName + "_" + LocalDate.now() + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                CSVExporter.exportTransactions(table.getItems(), file.getAbsolutePath());
                summaryLabel.setText("Statement exported: " + file.getName());
            } catch (Exception e) { summaryLabel.setText("Export failed: " + e.getMessage()); }
        }
    }

    private TableView<Transaction> buildTable() {
        TableView<Transaction> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("Reference",    t -> t.getReferenceId()),
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
