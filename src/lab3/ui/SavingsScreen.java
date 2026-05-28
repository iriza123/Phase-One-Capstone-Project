package lab3.ui;

import lab1.model.Account;
import lab3.service.AccountService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class SavingsScreen extends BaseScreen {

    private final AccountService accSvc = new AccountService();

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
            rule("1.5% fee on every withdrawal"),
            rule("Maximum 500,000 RWF per withdrawal"),
            rule("Maximum 5 withdrawals per month"),
            rule("No transfers — use Wallet for transfers"));
        return box;
    }

    private VBox buildAccountsSection() {
        VBox section = new VBox(14);
        try {
            List<Account> savings = accSvc.getByCustomer(SessionManager.getUser().getCustomerId())
                .stream().filter(a -> "SAVINGS".equals(a.getAccountType())).toList();

            if (savings.isEmpty()) {
                Label none = new Label("You don't have a savings account yet.");
                none.setStyle("-fx-text-fill:rgba(255,255,255,0.55);-fx-font-family:'Times New Roman';");

                Button createBtn = new Button("Open Savings Account");
                createBtn.getStyleClass().add("btn-success");
                createBtn.setOnAction(e -> {
                    try {
                        Account a = accSvc.createSavings(SessionManager.getUser().getCustomerId());
                        showMsg("Savings account created: " + a.getAccountNumber(), true);
                        new SavingsScreen(stage).show();
                    } catch (Exception ex) { showMsg(ex.getMessage(), false); }
                });
                section.getChildren().addAll(none, createBtn);
            } else {
                for (Account a : savings) {
                    section.getChildren().add(buildAccountCard(a));
                }
            }
        } catch (Exception e) {
            section.getChildren().add(new Label("Could not load accounts."));
        }
        return section;
    }

    private VBox buildAccountCard(Account a) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color:rgba(255,255,255,0.08);-fx-background-radius:12;-fx-padding:18;" +
            "-fx-border-color:rgba(39,174,96,0.4);-fx-border-radius:12;-fx-border-width:1;");

        Label num  = new Label("Account:  " + a.getAccountNumber());
        num.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-family:'Times New Roman';");

        Label bal  = new Label(String.format("Balance:  %,.2f RWF", a.getBalance()));
        bal.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        Label stat = new Label("Status:   " + a.getStatus());
        stat.setStyle("-fx-text-fill:" + ("ACTIVE".equals(a.getStatus()) ? "#2ECC71" : "#E74C3C") +
            ";-fx-font-family:'Times New Roman';");

        Label hint = new Label("To save money, deposit to this account. To access funds, withdraw.");
        hint.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.4);-fx-font-family:'Times New Roman';");
        hint.setWrapText(true);

        HBox btns = new HBox(12);
        Button depositBtn  = new Button("Deposit to Savings");
        Button withdrawBtn = new Button("Withdraw from Savings");
        depositBtn.getStyleClass().add("btn-success");
        withdrawBtn.getStyleClass().add("btn-primary");
        depositBtn.setOnAction(e  -> new DepositScreen(stage).show());
        withdrawBtn.setOnAction(e -> new WithdrawScreen(stage).show());
        btns.getChildren().addAll(depositBtn, withdrawBtn);

        card.getChildren().addAll(num, bal, stat, hint, btns);
        return card;
    }

    private Label rule(String text) {
        Label l = new Label("  * " + text);
        l.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:13px;-fx-font-family:'Times New Roman';");
        return l;
    }
}
