package lab3.ui;

import lab1.model.Account;
import lab3.service.AccountService;
import lab3.service.TransactionService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class SavingsScreen extends BaseScreen {

    private final AccountService     accSvc = new AccountService();
    private final TransactionService txSvc  = new TransactionService();

    public SavingsScreen(Stage stage) { super(stage); }

    public void show() {
        VBox card = buildFormCard();
        card.setMaxWidth(660);

        Label title = new Label("Savings Account");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        card.getChildren().addAll(title, buildRulesBox(), buildAccountsSection(), initMessage());

        BorderPane root = wrapInRoot(buildTopBar("Savings"), card);
        Scene scene = new Scene(root, 740, 560);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Savings");
        stage.show();
    }

    private VBox buildRulesBox() {
        VBox box = new VBox(8);
        box.setStyle(
            "-fx-background-color:rgba(212,160,23,0.1);-fx-background-radius:10;-fx-padding:14;" +
            "-fx-border-color:rgba(212,160,23,0.3);-fx-border-radius:10;-fx-border-width:1;");
        Label t = new Label("Savings Account Rules:");
        t.setStyle("-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");
        box.getChildren().addAll(t,
            rule("Money saved must come from your Wallet balance"),
            rule("1.5% fee on every withdrawal"),
            rule("Maximum 500,000 RWF per withdrawal"),
            rule("Maximum 5 withdrawals per month"));
        return box;
    }

    private VBox buildAccountsSection() {
        VBox section = new VBox(14);
        try {
            int custId = SessionManager.getUser().getCustomerId();
            List<Account> all     = accSvc.getByCustomer(custId);
            Optional<Account> savingsOpt = all.stream()
                .filter(a -> "SAVINGS".equals(a.getAccountType()) && "ACTIVE".equals(a.getStatus()))
                .findFirst();
            Optional<Account> walletOpt  = all.stream()
                .filter(a -> "WALLET".equals(a.getAccountType()) && "ACTIVE".equals(a.getStatus()))
                .findFirst();

            if (savingsOpt.isEmpty()) {
                Label none = new Label("You don't have a savings account yet.");
                none.setStyle("-fx-text-fill:rgba(255,255,255,0.55);-fx-font-family:'Times New Roman';");
                Button createBtn = new Button("Open Savings Account");
                createBtn.getStyleClass().add("btn-success");
                createBtn.setOnAction(e -> {
                    try {
                        Account a = accSvc.createSavings(custId);
                        showMsg("Savings account created: " + a.getAccountNumber(), true);
                        new SavingsScreen(stage).show();
                    } catch (Exception ex) { showMsg(ex.getMessage(), false); }
                });
                section.getChildren().addAll(none, createBtn);
            } else {
                section.getChildren().add(buildAccountCard(savingsOpt.get(), walletOpt.orElse(null)));
            }
        } catch (Exception e) {
            section.getChildren().add(new Label("Could not load accounts."));
        }
        return section;
    }

    private VBox buildAccountCard(Account savings, Account wallet) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color:rgba(255,255,255,0.08);-fx-background-radius:12;-fx-padding:18;" +
            "-fx-border-color:rgba(39,174,96,0.4);-fx-border-radius:12;-fx-border-width:1;");

        Label num  = new Label("Account:  " + savings.getAccountNumber());
        num.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-family:'Times New Roman';");

        Label bal  = new Label(String.format("Balance:  %,.2f RWF", savings.getBalance()));
        bal.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        Label stat = new Label("Status:   " + savings.getStatus());
        stat.setStyle("-fx-text-fill:#2ECC71;-fx-font-family:'Times New Roman';");

        card.getChildren().addAll(num, bal, stat);

        if (wallet != null) {
            Label walletBal = new Label("Wallet balance: " + String.format("%,.2f RWF", wallet.getBalance()));
            walletBal.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.45);-fx-font-family:'Times New Roman';");

            TextField amtField = new TextField();
            amtField.getStyleClass().add("text-field-styled");
            amtField.setPromptText("Amount to move from Wallet to Savings (RWF)");

            Button saveBtn = new Button("Move to Savings");
            saveBtn.getStyleClass().add("btn-success");
            saveBtn.setMaxWidth(Double.MAX_VALUE);
            saveBtn.setOnAction(e -> {
                String amt = amtField.getText().trim();
                if (amt.isEmpty()) { showMsg("Enter an amount.", false); return; }
                try {
                    BigDecimal amount = new BigDecimal(amt.replace(",", ""));
                    txSvc.transferToSavings(wallet.getAccountId(), savings.getAccountId(), amount, "Wallet to Savings");
                    showMsg("Moved " + String.format("%,.2f RWF", amount) + " to savings.", true);
                    amtField.clear();
                    new SavingsScreen(stage).show();
                } catch (Exception ex) { showMsg(ex.getMessage(), false); }
            });

            Button withdrawBtn = new Button("Withdraw from Savings");
            withdrawBtn.getStyleClass().add("btn-primary");
            withdrawBtn.setMaxWidth(Double.MAX_VALUE);
            withdrawBtn.setOnAction(e -> new WithdrawScreen(stage).show());

            card.getChildren().addAll(walletBal, fieldGroup("Save from Wallet", amtField), saveBtn, withdrawBtn);
        } else {
            Label noWallet = new Label("No active wallet found. Create a wallet to save money.");
            noWallet.setStyle("-fx-text-fill:#E74C3C;-fx-font-family:'Times New Roman';");
            Button withdrawBtn = new Button("Withdraw from Savings");
            withdrawBtn.getStyleClass().add("btn-primary");
            withdrawBtn.setMaxWidth(Double.MAX_VALUE);
            withdrawBtn.setOnAction(e -> new WithdrawScreen(stage).show());
            card.getChildren().addAll(noWallet, withdrawBtn);
        }

        return card;
    }

    private Label rule(String text) {
        Label l = new Label("  * " + text);
        l.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:13px;-fx-font-family:'Times New Roman';");
        return l;
    }
}
