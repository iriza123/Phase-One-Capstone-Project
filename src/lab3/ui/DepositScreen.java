package lab3.ui;

import lab1.model.Account;
import lab1.model.Transaction;
import lab3.service.AccountService;
import lab3.service.TransactionService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class DepositScreen extends BaseScreen {

    private final AccountService     accSvc = new AccountService();
    private final TransactionService txSvc  = new TransactionService();
    private ComboBox<String> accountCombo;
    private TextField amountField, descField;
    private Label balanceLabel;

    public DepositScreen(Stage stage) { super(stage); }

    public void show() {
        VBox card = buildFormCard();

        Label title = new Label("Deposit Funds");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        accountCombo = styledCombo("Select account...");
        accountCombo.setOnAction(e -> updateBalance());
        balanceLabel = new Label("Balance: --");
        balanceLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        amountField = styledField("Amount in RWF");
        descField   = styledField("Description (optional)");

        Button btn = new Button("Deposit");
        btn.getStyleClass().add("btn-primary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> handleDeposit());

        card.getChildren().addAll(title,
            fieldGroup("Account", accountCombo), balanceLabel,
            fieldGroup("Amount (RWF)", amountField),
            fieldGroup("Description", descField),
            initMessage(), btn);

        BorderPane root = wrapInRoot(buildTopBar("Deposit"), card);
        Scene scene = new Scene(root, 700, 520);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Deposit");
        stage.show();
        loadAccounts();
    }

    private void loadAccounts() {
        try {
            List<Account> list = accSvc.getByCustomer(SessionManager.getUser().getCustomerId());
            for (Account a : list)
                if ("ACTIVE".equals(a.getStatus()))
                    accountCombo.getItems().add(a.getAccountNumber() + " [" + a.getAccountType() + "]");
        } catch (Exception e) { showMsg("Could not load accounts.", false); }
    }

    private void updateBalance() {
        String sel = accountCombo.getValue();
        if (sel == null) return;
        try {
            Account a = accSvc.getByNumber(sel.split(" \\[")[0]);
            balanceLabel.setText("Balance: " + String.format("%,.2f RWF", a.getBalance()));
        } catch (Exception ignored) {}
    }

    private void handleDeposit() {
        String sel = accountCombo.getValue();
        if (sel == null) { showMsg("Select an account.", false); return; }
        String amtStr = amountField.getText().trim();
        if (amtStr.isEmpty()) { showMsg("Enter amount.", false); return; }
        try {
            Account a = accSvc.getByNumber(sel.split(" \\[")[0]);
            BigDecimal amount = new BigDecimal(amtStr.replace(",", ""));
            Transaction tx = txSvc.deposit(a.getAccountId(), amount, descField.getText());
            showMsg("Deposited " + String.format("%,.2f RWF", amount) + " | Ref: " + tx.getReferenceId(), true);
            accountCombo.getItems().clear(); loadAccounts(); updateBalance();
            amountField.clear(); descField.clear();
        } catch (Exception e) { showMsg(e.getMessage(), false); }
    }
}
