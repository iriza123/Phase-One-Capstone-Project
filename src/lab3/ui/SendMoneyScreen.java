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

public class SendMoneyScreen extends BaseScreen {

    private final AccountService     accSvc = new AccountService();
    private final TransactionService txSvc  = new TransactionService();
    private ComboBox<String> fromCombo;
    private TextField toField, amountField, descField;
    private Label balanceLabel;

    public SendMoneyScreen(Stage stage) { super(stage); }

    public void show() {
        VBox card = buildFormCard();

        Label title = new Label("Send Money");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
        Label sub = new Label("Transfer funds to another IgirePay account");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.45);-fx-font-family:'Times New Roman';");

        fromCombo    = styledCombo("Select your wallet account...");
        fromCombo.setOnAction(e -> updateBalance());
        balanceLabel = new Label("Balance: --");
        balanceLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        toField     = styledField("Recipient account number");
        amountField = styledField("Amount in RWF");
        descField   = styledField("Description (optional)");

        Button sendBtn = new Button("Send Money");
        sendBtn.getStyleClass().add("btn-primary");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setOnAction(e -> handleSend());

        card.getChildren().addAll(title, sub,
            fieldGroup("From Account", fromCombo), balanceLabel,
            fieldGroup("To Account Number", toField),
            fieldGroup("Amount (RWF)", amountField),
            fieldGroup("Description", descField),
            initMessage(), sendBtn);

        BorderPane root = wrapInRoot(buildTopBar("Send Money"), card);
        Scene scene = new Scene(root, 700, 540);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Send Money");
        stage.show();
        loadAccounts();
    }

    private void loadAccounts() {
        try {
            List<Account> list = accSvc.getByCustomer(SessionManager.getUser().getCustomerId());
            for (Account a : list)
                if ("WALLET".equals(a.getAccountType()) && "ACTIVE".equals(a.getStatus()))
                    fromCombo.getItems().add(a.getAccountNumber() + " | " +
                        String.format("%,.2f RWF", a.getBalance()));
        } catch (Exception e) { showMsg("Could not load accounts.", false); }
    }

    private void updateBalance() {
        String sel = fromCombo.getValue();
        if (sel == null) return;
        try {
            Account a = accSvc.getByNumber(sel.split(" \\| ")[0]);
            balanceLabel.setText("Balance: " + String.format("%,.2f RWF", a.getBalance()));
        } catch (Exception ignored) {}
    }

    private void handleSend() {
        String sel = fromCombo.getValue();
        if (sel == null) { showMsg("Select your account.", false); return; }
        String toNum  = toField.getText().trim();
        String amtStr = amountField.getText().trim();
        if (toNum.isEmpty())  { showMsg("Enter recipient account number.", false); return; }
        if (amtStr.isEmpty()) { showMsg("Enter amount.", false); return; }
        try {
            Account from = accSvc.getByNumber(sel.split(" \\| ")[0]);
            BigDecimal amount = new BigDecimal(amtStr.replace(",", ""));
            Transaction tx = txSvc.sendMoney(from.getAccountId(), toNum, amount, descField.getText());
            showMsg("Sent " + String.format("%,.2f RWF", amount) + " | Ref: " + tx.getReferenceId(), true);
            fromCombo.getItems().clear(); loadAccounts(); updateBalance();
            toField.clear(); amountField.clear(); descField.clear();
        } catch (Exception e) { showMsg(e.getMessage(), false); }
    }
}
