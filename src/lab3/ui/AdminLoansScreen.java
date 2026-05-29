package lab3.ui;

import lab1.model.Loan;
import lab3.service.LoanService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminLoansScreen extends BaseScreen {

    private final LoanService loanSvc = new LoanService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TableView<Loan> table;
    private ComboBox<String> filterBox;

    public AdminLoansScreen(Stage stage) { super(stage); }

    @Override
    protected HBox buildTopBar(String title) {
        HBox bar = super.buildTopBar(title);
        ((Button) bar.getChildren().get(0)).setOnAction(e -> new AdminDashboardScreen(stage).show());
        return bar;
    }

    public void show() {
        VBox content = new VBox(16);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Loan Requests");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        HBox filterRow = new HBox(12);
        filterRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.8);-fx-font-family:'Times New Roman';");
        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("ALL", "PENDING", "APPROVED", "REJECTED");
        filterBox.setValue("PENDING");
        filterBox.setStyle(
            "-fx-background-color:rgba(255,255,255,0.1);-fx-text-fill:white;" +
            "-fx-border-color:rgba(255,255,255,0.2);-fx-border-radius:6;-fx-background-radius:6;" +
            "-fx-font-family:'Times New Roman';");
        filterBox.setOnAction(e -> loadLoans());
        filterRow.getChildren().addAll(filterLabel, filterBox);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, filterRow, table);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("Admin - Loans"));
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(20));

        Scene scene = new Scene(root, 1050, 700);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Loan Requests");
        stage.show();
        loadLoans();
    }

    private TableView<Loan> buildTable() {
        TableView<Loan> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tv.getColumns().addAll(
            col("ID",           l -> String.valueOf(l.getLoanId())),
            col("Customer ID",  l -> String.valueOf(l.getCustomerId())),
            col("Account ID",   l -> String.valueOf(l.getAccountId())),
            col("Amount (RWF)", l -> String.format("%,.2f", l.getAmount())),
            col("Status",       l -> l.getStatus()),
            col("Reason",       l -> l.getReason() != null ? l.getReason() : ""),
            col("Requested",    l -> l.getRequestedAt() != null ? l.getRequestedAt().format(FMT) : "")
        );

        TableColumn<Loan, Void> actCol = new TableColumn<>("Actions");
        actCol.setMinWidth(180);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn  = new Button("Reject");
            {
                approveBtn.setStyle(
                    "-fx-background-color:rgba(39,174,96,0.75);-fx-text-fill:white;" +
                    "-fx-font-size:12px;-fx-font-family:'Times New Roman';" +
                    "-fx-padding:6 12;-fx-background-radius:6;-fx-cursor:hand;");
                rejectBtn.getStyleClass().add("btn-danger");

                approveBtn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    try {
                        loanSvc.approveLoan(loan.getLoanId());
                        loadLoans();
                    } catch (Exception ex) {
                        showAlert("Approve Failed", ex.getMessage());
                    }
                });

                rejectBtn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    TextInputDialog dlg = new TextInputDialog();
                    dlg.setTitle("Reject Loan");
                    dlg.setHeaderText("Rejection Notes");
                    dlg.setContentText("Reason for rejection (optional):");
                    dlg.showAndWait().ifPresent(notes -> {
                        try {
                            loanSvc.rejectLoan(loan.getLoanId(), notes);
                            loadLoans();
                        } catch (Exception ex) {
                            showAlert("Reject Failed", ex.getMessage());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Loan loan = getTableView().getItems().get(getIndex());
                if ("PENDING".equals(loan.getStatus())) {
                    HBox box = new HBox(8, approveBtn, rejectBtn);
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });
        tv.getColumns().add(actCol);
        tv.setPlaceholder(new Label("No loan requests found."));
        return tv;
    }

    private void loadLoans() {
        try {
            String filter = filterBox.getValue();
            List<Loan> loans = "ALL".equals(filter) ? loanSvc.getAll() : loanSvc.getPending();
            if (!"ALL".equals(filter) && !"PENDING".equals(filter)) {
                // getAll filtered by status
                loans = loanSvc.getAll().stream()
                    .filter(l -> filter.equals(l.getStatus()))
                    .toList();
            }
            table.setItems(FXCollections.observableArrayList(loans));
        } catch (Exception e) {
            table.setPlaceholder(new Label("Could not load loans: " + e.getMessage()));
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }
}
