package lab3.ui;

import lab1.model.Account;
import lab3.service.AccountService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * LAB 3 - Savings Screen
 * Shows the customer's savings accounts and their rules.
 * To open a savings account, go to Profile.
 */
public class SavingsScreen extends BaseScreen {

    private final AccountService accSvc = new AccountService();

    public SavingsScreen(Stage stage) { super(stage); }

    public void show() {
        VBox card = buildFormCard();
        card.setMaxWidth(600);

        Label title = new Label("Savings Account");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        // Savings rules info box
        VBox infoBox = new VBox(8);
        infoBox.setStyle(
            "-fx-background-color:rgba(212,160,23,0.1);-fx-background-radius:10;" +
            "-fx-padding:14;-fx-border-color:rgba(212,160,23,0.3);-fx-border-radius:10;-fx-border-width:1;");
        Label rulesTitle = new Label("Savings Account Rules:");
        rulesTitle.setStyle("-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");
        infoBox.getChildren().addAll(rulesTitle,
            rule("1.5% fee on every withdrawal"),
            rule("Maximum 500,000 RWF per withdrawal"),
            rule("Maximum 5 withdrawals per month"),
            rule("No transfers — use Wallet for transfers"));

        // List savings accounts
        Label accTitle = new Label("Your Savings Accounts");
        accTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        VBox accountsList = new VBox(10);
        try {
            List<Account> accounts = accSvc.getByCustomer(SessionManager.getUser().getCustomerId());
            boolean hasSavings = false;
            for (Account a : accounts) {
                if ("SAVINGS".equals(a.getAccountType())) {
                    hasSavings = true;
                    HBox row = new HBox(16);
                    row.setStyle("-fx-background-color:rgba(255,255,255,0.07);-fx-background-radius:10;-fx-padding:14;");
                    VBox info = new VBox(4);
                    Label num = new Label("Account: " + a.getAccountNumber());
                    num.setStyle("-fx-text-fill:rgba(255,255,255,0.7);-fx-font-family:'Times New Roman';");
                    Label bal = new Label("Balance: " + String.format("%,.2f RWF", a.getBalance()));
                    bal.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
                    Label status = new Label("Status: " + a.getStatus());
                    status.setStyle("-fx-text-fill:" + ("ACTIVE".equals(a.getStatus()) ? "#2ECC71" : "#E74C3C") +
                        ";-fx-font-family:'Times New Roman';");
                    info.getChildren().addAll(num, bal, status);
                    row.getChildren().add(info);
                    accountsList.getChildren().add(row);
                }
            }
            if (!hasSavings) {
                Label none = new Label("No savings account yet. Go to Profile to open one.");
                none.setStyle("-fx-text-fill:rgba(255,255,255,0.4);-fx-font-family:'Times New Roman';");
                accountsList.getChildren().add(none);
            }
        } catch (Exception e) {
            accountsList.getChildren().add(new Label("Could not load accounts."));
        }

        card.getChildren().addAll(title, infoBox, accTitle, accountsList);

        BorderPane root = wrapInRoot(buildTopBar("Savings"), card);
        Scene scene = new Scene(root, 700, 620);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Savings");
        stage.show();
    }

    private Label rule(String text) {
        Label l = new Label("  * " + text);
        l.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:13px;-fx-font-family:'Times New Roman';");
        return l;
    }
}
