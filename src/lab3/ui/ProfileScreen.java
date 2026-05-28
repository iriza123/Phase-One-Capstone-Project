package lab3.ui;

import lab1.model.Account;
import lab1.model.Customer;
import lab3.service.AccountService;
import lab3.service.CustomerService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class ProfileScreen extends BaseScreen {

    private final CustomerService custSvc = new CustomerService();
    private final AccountService  accSvc  = new AccountService();

    public ProfileScreen(Stage stage) { super(stage); }

    public void show() {
        VBox card = buildFormCard();
        card.setMaxWidth(600);

        Customer user = SessionManager.getUser();

        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        // ── Customer info ─────────────────────────────────────────────
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-background-color:rgba(255,255,255,0.07);-fx-background-radius:10;-fx-padding:16;");
        infoBox.getChildren().addAll(
            infoRow("Name",  user.getFullName()),
            infoRow("Phone", user.getPhone()),
            infoRow("Email", user.getEmail()),
            infoRow("Role",  user.getRole())
        );

        // ── My Accounts ───────────────────────────────────────────────
        Label accTitle = new Label("My Accounts");
        accTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        VBox accountsList = new VBox(8);
        try {
            List<Account> accounts = accSvc.getByCustomer(user.getCustomerId());
            if (accounts.isEmpty()) {
                Label none = new Label("No accounts found.");
                none.setStyle("-fx-text-fill:rgba(255,255,255,0.4);-fx-font-family:'Times New Roman';");
                accountsList.getChildren().add(none);
            } else {
                for (Account a : accounts) {
                    HBox row = new HBox(16);
                    row.setStyle("-fx-background-color:rgba(255,255,255,0.06);-fx-background-radius:8;-fx-padding:12;");
                    Label info = new Label(
                        a.getAccountType() + "  |  " + a.getAccountNumber() +
                        "  |  " + String.format("%,.2f RWF", a.getBalance()) +
                        "  |  " + a.getStatus());
                    info.setStyle("-fx-text-fill:white;-fx-font-family:'Times New Roman';-fx-font-size:13px;");
                    row.getChildren().add(info);
                    accountsList.getChildren().add(row);
                }
            }
        } catch (Exception e) {
            accountsList.getChildren().add(new Label("Could not load accounts."));
        }

        // ── Change PIN ────────────────────────────────────────────────
        Label pinTitle = new Label("Change PIN");
        pinTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        PasswordField oldPin = new PasswordField();
        oldPin.getStyleClass().add("pin-field");
        oldPin.setPromptText("Current 5-digit PIN");
        limitPin(oldPin);

        PasswordField newPin = new PasswordField();
        newPin.getStyleClass().add("pin-field");
        newPin.setPromptText("New 5-digit PIN");
        limitPin(newPin);

        Button changePinBtn = new Button("Change PIN");
        changePinBtn.getStyleClass().add("btn-secondary");
        changePinBtn.setOnAction(e -> {
            try {
                custSvc.changePin(user.getCustomerId(), oldPin.getText(), newPin.getText());
                showMsg("PIN changed successfully.", true);
                oldPin.clear(); newPin.clear();
            } catch (Exception ex) { showMsg(ex.getMessage(), false); }
        });

        card.getChildren().addAll(
            title, infoBox,
            accTitle, accountsList,
            new Separator(), pinTitle,
            fieldGroup("Current PIN", oldPin),
            fieldGroup("New PIN", newPin),
            changePinBtn, initMessage());

        BorderPane root = wrapInRoot(buildTopBar("Profile"), card);
        Scene scene = new Scene(root, 720, 600);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Profile");
        stage.show();
    }

    // Restrict PasswordField to 5 digits only
    private void limitPin(PasswordField pf) {
        pf.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) pf.setText(n.replaceAll("[^\\d]", ""));
            if (pf.getText().length() > 5) pf.setText(pf.getText().substring(0, 5));
        });
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(12);
        Label l = new Label(label + ":");
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:rgba(255,255,255,0.55);-fx-min-width:80;-fx-font-family:'Times New Roman';");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:white;-fx-font-family:'Times New Roman';");
        row.getChildren().addAll(l, v);
        return row;
    }
}
