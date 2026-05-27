package lab3.ui;

import lab1.model.Customer;
import lab3.service.CustomerService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class RegisterScreen {

    private final Stage stage;
    private final CustomerService customerService = new CustomerService();
    private Label messageLabel;

    public RegisterScreen(Stage stage) { this.stage = stage; }

    public void show() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");

        VBox card = new VBox(18);
        card.setMaxWidth(500);
        card.getStyleClass().add("form-card");
        card.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        Label sub = new Label("Join IgirePay - Igire Rwanda Organization");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.45);-fx-font-family:'Times New Roman';");

        // Input fields
        TextField firstNameField = styledField("e.g. Alice");
        TextField lastNameField  = styledField("e.g. Uwimana");
        TextField emailField     = styledField("e.g. alice@email.com");

        // Phone field - exactly 10 digits
        VBox phoneGroup = new VBox(6);
        Label phoneLabel = new Label("Phone");
        phoneLabel.getStyleClass().add("field-label");
        TextField phoneField = styledField("e.g. 0780000001");
        phoneField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) phoneField.setText(n.replaceAll("[^\\d]", ""));
            if (phoneField.getText().length() > 10) phoneField.setText(phoneField.getText().substring(0, 10));
        });
        Label phoneHint = new Label("Phone must be exactly 10 digits");
        phoneHint.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.35);-fx-font-family:'Times New Roman';");
        phoneGroup.getChildren().addAll(phoneLabel, phoneField, phoneHint);

        // PIN field - 5 digits only
        VBox pinGroup = new VBox(6);
        Label pinLabel = new Label("5-Digit PIN");
        pinLabel.getStyleClass().add("field-label");
        PasswordField pinField = new PasswordField();
        pinField.getStyleClass().add("pin-field");
        pinField.setPromptText("Choose a 5-digit PIN");
        pinField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) pinField.setText(n.replaceAll("[^\\d]", ""));
            if (pinField.getText().length() > 5) pinField.setText(pinField.getText().substring(0, 5));
        });
        Label pinHint = new Label("PIN must be exactly 5 digits");
        pinHint.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.35);-fx-font-family:'Times New Roman';");
        pinGroup.getChildren().addAll(pinLabel, pinField, pinHint);

        // Message
        messageLabel = new Label();
        messageLabel.setVisible(false);
        messageLabel.setWrapText(true);

        // Buttons
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("btn-primary");
        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> new LoginScreen(stage).show());
        registerBtn.setOnAction(e -> handleRegister(
            firstNameField.getText(), lastNameField.getText(),
            emailField.getText(), phoneField.getText(), pinField.getText()));
        btnRow.getChildren().addAll(registerBtn, backBtn);

        card.getChildren().addAll(
            title, sub,
            labeledField("First Name", firstNameField),
            labeledField("Last Name",  lastNameField),
            labeledField("Email",      emailField),
            phoneGroup,
            pinGroup, messageLabel, btnRow);

        root.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(root, 620, 660);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Register");
        stage.centerOnScreen();
        stage.show();
    }

    private void handleRegister(String fn, String ln, String email, String phone, String pin) {
        try {
            Customer c = customerService.register(fn, ln, email, phone, pin);
            showMsg("Account created! Welcome, " + c.getFullName() + ". Please login.", true);
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> new LoginScreen(stage).show());
            pause.play();
        } catch (Exception ex) {
            showMsg(ex.getMessage(), false);
        }
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.getStyleClass().add("text-field-styled");
        tf.setPromptText(prompt);
        return tf;
    }

    private VBox labeledField(String label, TextField tf) {
        VBox g = new VBox(6);
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        g.getChildren().addAll(l, tf);
        return g;
    }

    private void showMsg(String msg, boolean ok) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("alert-success", "alert-error");
        messageLabel.getStyleClass().add(ok ? "alert-success" : "alert-error");
        messageLabel.setVisible(true);
    }
}
