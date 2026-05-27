package lab3.ui;

import lab1.model.Account;
import lab1.model.SavingsAccount;
import lab1.model.Transaction;
import lab3.service.AccountService;
import lab3.service.TransactionService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class WithdrawScreen extends BaseScreen {

    private final AccountService     accSvc = new AccountService();
    private final TransactionService txSvc  = new TransactionService();
    private ComboBox<String> accountCombo;
    private TextField amountField, descField;
    private Label balanceLabel, feeLabel;

    public WithdrawScreen(Stage stage) { super(stage); }

    public void show() {
        VBox card = buildFormCard();

        Label title = new Label("Withdraw Funds");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        accountCombo = styledCombo("Select account...");
        accountCombo.setOnAction(e -> updateBalance());
        balanceLabel = new Label("Balance: --");
        balanceLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        amountField = styledField("Amount in RWF");
        amountField.textProperty().addListener((obs, o, n) -> updateFee(n));
        descField = styledField("Description (optional)");

        feeLabel = new Label("Fee: 0.00 RWF");
        feeLabel.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.5);-fx-font-family:'Times New Roman';");

        Button btn = new Button("Withdraw");
        btn.getStyleClass().add("btn-primary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> handleWithdraw());

        card.getChildren().addAll(title,
            fieldGroup("Account", accountCombo), balanceLabel,
            fieldGroup("Amount (RWF)", amountField), feeLabel,
            fieldGroup("Description", descField),
            initMessage(), btn);

        BorderPane root = wrapInRoot(buildTopBar("Withdraw"), card);
        Scene scene = new Scene(root, 700, 580);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Withdraw");
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

    private void updateFee(String amtStr) {
        String sel = accountCombo.getValue();
        if (sel == null || amtStr.isBlank()) { feeLabel.setText("Fee: 0.00 RWF"); return; }
        try {
            Account a = accSvc.getByNumber(sel.split(" \\[")[0]);
            BigDecimal amt = new BigDecimal(amtStr.replace(",", ""));
            if (a instanceof SavingsAccount sa) {
                BigDecimal fee = sa.calculateFee(amt);
                feeLabel.setText("Fee (1.5%): " + String.format("%,.2f RWF", fee) +
                    "  |  Total: " + String.format("%,.2f RWF", amt.add(fee)));
            } else {
                feeLabel.setText("Fee: 0.00 RWF (Wallet - no fee)");
            }
        } catch (Exception ignored) { feeLabel.setText("Fee: --"); }
    }

    private void handleWithdraw() {
        String sel = accountCombo.getValue();
        if (sel == null) { showMsg("Select an account.", false); return; }
        String amtStr = amountField.getText().trim();
        if (amtStr.isEmpty()) { showMsg("Enter amount.", false); return; }
        try {
            Account a = accSvc.getByNumber(sel.split(" \\[")[0]);
            BigDecimal amount = new BigDecimal(amtStr.replace(",", ""));
            Transaction tx = txSvc.withdraw(a.getAccountId(), amount, descField.getText());
            showMsg("Withdrew " + String.format("%,.2f RWF", amount) + " | Ref: " + tx.getReferenceId(), true);
            accountCombo.getItems().clear(); loadAccounts(); updateBalance();
            amountField.clear(); descField.clear(); feeLabel.setText("Fee: 0.00 RWF");
        } catch (Exception e) { showMsg(e.getMessage(), false); }
    }
}
