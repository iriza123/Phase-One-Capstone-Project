package lab3.ui;

import lab1.model.Account;
import lab1.model.Loan;
import lab3.service.AccountService;
import lab3.service.LoanService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LoanScreen extends BaseScreen {

    private final LoanService    loanSvc = new LoanService();
    private final AccountService accSvc  = new AccountService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TableView<Loan> loanTable;
    private ComboBox<Account> accountBox;
    private TextField amountField;
    private TextArea  reasonField;
    private Label     statusLabel;

    public LoanScreen(Stage stage) { super(stage); }

    @Override
    protected HBox buildTopBar(String title) {
        HBox bar = super.buildTopBar(title);
        ((Button) bar.getChildren().get(0)).setOnAction(e -> new DashboardScreen(stage).show());
        return bar;
    }

    public void show() {
        VBox content = new VBox(20);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Loan Requests");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        content.getChildren().addAll(title, buildRequestForm(), buildLoansTable());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("Loans"));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
        scroll.setPadding(new Insets(20));
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Loans");
        stage.show();

        loadAccounts();
        loadLoans();
    }

    private VBox buildRequestForm() {
        VBox card = new VBox(14);
        card.setStyle(
            "-fx-background-color:rgba(255,255,255,0.08);-fx-background-radius:14;" +
            "-fx-border-radius:14;-fx-border-color:rgba(255,255,255,0.12);" +
            "-fx-border-width:1;-fx-padding:20;");

        Label heading = new Label("Request a Loan");
        heading.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        Label accLabel = new Label("Credit to account:");
        accLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.8);-fx-font-family:'Times New Roman';");
        accountBox = new ComboBox<>();
        accountBox.setPromptText("Select account");
        accountBox.setMaxWidth(320);
        accountBox.setStyle(
            "-fx-background-color:rgba(255,255,255,0.1);-fx-text-fill:white;" +
            "-fx-prompt-text-fill:rgba(255,255,255,0.4);-fx-border-color:rgba(255,255,255,0.2);" +
            "-fx-border-radius:6;-fx-background-radius:6;-fx-font-family:'Times New Roman';");

        amountField = styledField("Amount (RWF)");
        amountField.setMaxWidth(320);

        Label reasonLabel = new Label("Reason:");
        reasonLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.8);-fx-font-family:'Times New Roman';");
        reasonField = new TextArea();
        reasonField.setPromptText("Why do you need this loan?");
        reasonField.setPrefRowCount(3);
        reasonField.setMaxWidth(460);
        reasonField.setStyle(
            "-fx-background-color:rgba(255,255,255,0.1);-fx-text-fill:white;" +
            "-fx-prompt-text-fill:rgba(255,255,255,0.4);-fx-border-color:rgba(255,255,255,0.2);" +
            "-fx-border-radius:6;-fx-background-radius:6;-fx-font-family:'Times New Roman';");

        Button submitBtn = new Button("Submit Loan Request");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setOnAction(e -> handleSubmit());

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-family:'Times New Roman';-fx-font-size:13px;");
        statusLabel.setWrapText(true);

        card.getChildren().addAll(heading, accLabel, accountBox, amountField, reasonLabel, reasonField, submitBtn, statusLabel);
        return card;
    }

    private VBox buildLoansTable() {
        VBox box = new VBox(10);

        Label heading = new Label("My Loan History");
        heading.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        loanTable = new TableView<>();
        loanTable.getStyleClass().add("table-view");
        loanTable.setPrefHeight(260);
        loanTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loanTable.getColumns().addAll(
            col("ID",          l -> String.valueOf(l.getLoanId())),
            col("Amount (RWF)",l -> String.format("%,.2f", l.getAmount())),
            col("Status",      l -> l.getStatus()),
            col("Reason",      l -> l.getReason()),
            col("Notes",       l -> l.getNotes() != null ? l.getNotes() : ""),
            col("Requested",   l -> l.getRequestedAt() != null ? l.getRequestedAt().format(FMT) : "")
        );
        loanTable.setPlaceholder(new Label("No loan requests yet."));

        box.getChildren().addAll(heading, loanTable);
        return box;
    }

    private void loadAccounts() {
        try {
            int custId = SessionManager.getUser().getCustomerId();
            List<Account> accounts = accSvc.getByCustomer(custId);
            accountBox.setItems(FXCollections.observableArrayList(accounts));
            accountBox.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Account a) {
                    return a == null ? "" : a.getAccountType() + " #" + a.getAccountNumber() +
                        " (" + String.format("%,.0f", a.getBalance()) + " RWF)";
                }
                public Account fromString(String s) { return null; }
            });
        } catch (Exception e) {
            setStatus("Could not load accounts.", false);
        }
    }

    private void loadLoans() {
        try {
            int custId = SessionManager.getUser().getCustomerId();
            loanTable.setItems(FXCollections.observableArrayList(loanSvc.getByCustomer(custId)));
        } catch (Exception e) {
            loanTable.setPlaceholder(new Label("Could not load loans."));
        }
    }

    private void handleSubmit() {
        Account selected = accountBox.getValue();
        String amtText = amountField.getText().trim();
        String reason  = reasonField.getText().trim();

        if (selected == null) { setStatus("Please select an account.", false); return; }
        if (amtText.isEmpty()) { setStatus("Please enter an amount.", false); return; }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText.replace(",", ""));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            setStatus("Enter a valid amount.", false);
            return;
        }

        try {
            int custId = SessionManager.getUser().getCustomerId();
            loanSvc.requestLoan(custId, selected.getAccountId(), amount, reason);
            setStatus("Loan request submitted! Awaiting admin approval.", true);
            amountField.clear();
            reasonField.clear();
            accountBox.setValue(null);
            loadLoans();
        } catch (Exception ex) {
            setStatus(ex.getMessage(), false);
        }
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill:" + (ok ? "#27AE60" : "#E74C3C") +
            ";-fx-font-family:'Times New Roman';-fx-font-size:13px;");
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }
}
