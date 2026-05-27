package lab3.ui;

import lab1.model.Transaction;
import lab3.service.TransactionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

// LAB 3 - JavaFX: Transaction history screen
public class HistoryScreen extends BaseScreen {

    private final TransactionService txSvc = new TransactionService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HistoryScreen(Stage stage) { super(stage); }

    public void show() {
        VBox content = new VBox(16);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Transaction History");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        // Search field
        TextField search = styledField("Search by reference, type, status...");
        search.setMaxWidth(360);

        TableView<Transaction> tv = buildTable();
        VBox.setVgrow(tv, Priority.ALWAYS);

        search.textProperty().addListener((obs, o, n) -> filterTable(tv, n));

        content.getChildren().addAll(title, search, tv);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("History"));
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(20));

        Scene scene = new Scene(root, 1000, 680);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - History");
        stage.show();
        loadTransactions(tv);
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

    private void loadTransactions(TableView<Transaction> tv) {
        try {
            int custId = SessionManager.getUser().getCustomerId();
            List<Transaction> list = txSvc.getByCustomer(custId);
            tv.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            tv.setPlaceholder(new Label("Could not load transactions."));
        }
    }

    private void filterTable(TableView<Transaction> tv, String kw) {
        if (kw == null || kw.isBlank()) { loadTransactions(tv); return; }
        String k = kw.toLowerCase();
        try {
            List<Transaction> all = txSvc.getByCustomer(SessionManager.getUser().getCustomerId());
            tv.setItems(FXCollections.observableArrayList(all.stream().filter(t ->
                (t.getReferenceId()     != null && t.getReferenceId().toLowerCase().contains(k)) ||
                (t.getTransactionType() != null && t.getTransactionType().toLowerCase().contains(k)) ||
                (t.getStatus()          != null && t.getStatus().toLowerCase().contains(k))
            ).toList()));
        } catch (Exception ignored) {}
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }
}
