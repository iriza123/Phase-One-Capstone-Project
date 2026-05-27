package lab3.ui;

import lab1.model.Customer;
import lab3.service.CustomerService;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

// LAB 3 - JavaFX: Login screen with 5-digit PIN only
public class LoginScreen {

    private final Stage stage;
    private final CustomerService customerService = new CustomerService();
    private TextField    phoneField;
    private PasswordField pinField;
    private Label        messageLabel;
    private Button       loginBtn;

    public LoginScreen(Stage stage) { this.stage = stage; }

    public void show() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");

        HBox card = new HBox(0);
        card.setMaxWidth(860);
        card.setMaxHeight(580);
        card.setStyle(
            "-fx-background-color:rgba(255,255,255,0.07);" +
            "-fx-background-radius:20;" +
            "-fx-border-radius:20;" +
            "-fx-border-color:rgba(255,255,255,0.15);" +
            "-fx-border-width:1;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.55),36,0,0,8);");

        card.getChildren().addAll(buildLeft(), buildDivider(), buildRight());
        root.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(root, 860, 580);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Login");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        // Entrance animation
        card.setOpacity(0);
        card.setTranslateY(18);
        ParallelTransition pt = new ParallelTransition(
            fade(card, 0, 1), slide(card, 18, 0));
        pt.play();
    }

    private VBox buildLeft() {
        VBox left = new VBox(16);
        left.setAlignment(Pos.CENTER);
        left.setPrefWidth(360);
        left.setPadding(new Insets(44, 32, 44, 32));
        left.setStyle("-fx-background-color:rgba(0,0,0,0.22);-fx-background-radius:20 0 0 20;");

        ImageView logo = loadLogo(130);
        if (logo != null) {
            // Wrap logo in a gold-bordered circle container
            javafx.scene.layout.StackPane logoContainer = new javafx.scene.layout.StackPane(logo);
            logoContainer.setStyle(
                "-fx-background-color:rgba(212,160,23,0.2);" +
                "-fx-background-radius:70;" +
                "-fx-border-color:#D4A017;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:70;" +
                "-fx-padding:4;");
            logoContainer.setMaxSize(138, 138);
            left.getChildren().add(logoContainer);
        }

        Label name = new Label("IgirePay");
        name.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        Label org = new Label("Igire Rwanda Organization");
        org.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.55);-fx-font-family:'Times New Roman';");

        Label tag = new Label("Your Mobile Money Platform");
        tag.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.35);-fx-font-family:'Times New Roman';");

        VBox features = new VBox(12);
        features.setPadding(new Insets(20, 0, 0, 0));
        features.getChildren().addAll(
            featureRow("Send Money Instantly"),
            featureRow("Deposit and Withdraw"),
            featureRow("Savings Account"),
            featureRow("Transaction History")
        );

        left.getChildren().addAll(name, org, tag, features);
        return left;
    }

    private HBox featureRow(String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("*");
        dot.setStyle("-fx-text-fill:#D4A017;-fx-font-size:16px;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill:rgba(255,255,255,0.6);-fx-font-size:13px;-fx-font-family:'Times New Roman';");
        row.getChildren().addAll(dot, lbl);
        return row;
    }

    private Region buildDivider() {
        Region d = new Region();
        d.setPrefWidth(1);
        d.setStyle("-fx-background-color:rgba(255,255,255,0.12);");
        return d;
    }

    private VBox buildRight() {
        VBox right = new VBox(18);
        right.setAlignment(Pos.CENTER);
        right.setPrefWidth(500);
        right.setPadding(new Insets(44, 44, 44, 44));
        right.setStyle("-fx-background-color:rgba(255,255,255,0.04);-fx-background-radius:0 20 20 0;");

        Label title = new Label("Welcome Back");
        title.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        Label sub = new Label("Sign in with your phone and 5-digit PIN");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.45);-fx-font-family:'Times New Roman';");

        // Phone field
        VBox phoneGroup = new VBox(6);
        Label phoneLabel = new Label("Phone Number");
        phoneLabel.getStyleClass().add("field-label");
        phoneField = new TextField();
        phoneField.getStyleClass().add("text-field-styled");
        phoneField.setPromptText("e.g. 0780000001");
        phoneField.setOnAction(e -> pinField.requestFocus());
        phoneGroup.getChildren().addAll(phoneLabel, phoneField);

        // PIN field - restricted to 5 digits
        VBox pinGroup = new VBox(6);
        Label pinLabel = new Label("5-Digit PIN");
        pinLabel.getStyleClass().add("field-label");
        pinField = new PasswordField();
        pinField.getStyleClass().add("pin-field");
        pinField.setPromptText("Enter 5-digit PIN");
        pinField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) pinField.setText(newVal.replaceAll("[^\\d]", ""));
            if (pinField.getText().length() > 5) pinField.setText(pinField.getText().substring(0, 5));
        });
        pinField.setOnAction(e -> handleLogin());
        pinGroup.getChildren().addAll(pinLabel, pinField);

        // Message label
        messageLabel = new Label();
        messageLabel.setVisible(false);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(380);

        // Login button
        loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        // Register link
        HBox registerRow = new HBox(6);
        registerRow.setAlignment(Pos.CENTER);
        Label noAccount = new Label("No account?");
        noAccount.setStyle("-fx-text-fill:rgba(255,255,255,0.4);-fx-font-size:13px;-fx-font-family:'Times New Roman';");
        Button registerBtn = new Button("Register here");
        registerBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-font-size:13px;" +
            "-fx-cursor:hand;-fx-font-family:'Times New Roman';-fx-underline:true;");
        registerBtn.setOnAction(e -> new RegisterScreen(stage).show());
        registerRow.getChildren().addAll(noAccount, registerBtn);

        right.getChildren().addAll(title, sub, phoneGroup, pinGroup, messageLabel, loginBtn, registerRow);
        return right;
    }

    private void handleLogin() {
        String phone = phoneField.getText().trim();
        String pin   = pinField.getText().trim();
        if (phone.isEmpty()) { showMsg("Enter your phone number.", false); return; }
        if (pin.isEmpty())   { showMsg("Enter your 5-digit PIN.", false); return; }
        if (!pin.matches("\\d{5}")) { showMsg("PIN must be exactly 5 digits.", false); return; }

        loginBtn.setDisable(true);
        loginBtn.setText("Signing in...");

        new Thread(() -> {
            try {
                Customer c = customerService.login(phone, pin);
                SessionManager.login(c);
                javafx.application.Platform.runLater(() -> {
                    if (c.isAdmin()) new AdminDashboardScreen(stage).show();
                    else             new DashboardScreen(stage).show();
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    String msg = ex.getMessage();
                    // Show lock icon if account is now locked
                    if (msg != null && msg.contains("locked")) {
                        showMsg("[LOCKED] " + msg, false);
                        loginBtn.setDisable(true);
                        loginBtn.setText("Account Locked");
                    } else {
                        showMsg(msg, false);
                        loginBtn.setDisable(false);
                        loginBtn.setText("Sign In");
                        pinField.clear();
                        shake(loginBtn);
                    }
                });
            }
        }).start();
    }

    private void showMsg(String msg, boolean ok) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("alert-success", "alert-error");
        messageLabel.getStyleClass().add(ok ? "alert-success" : "alert-error");
        messageLabel.setVisible(true);
    }

    private void shake(javafx.scene.Node n) {
        TranslateTransition t = new TranslateTransition(Duration.millis(55), n);
        t.setByX(7); t.setCycleCount(6); t.setAutoReverse(true);
        t.setOnFinished(e -> n.setTranslateX(0)); t.play();
    }

    private FadeTransition fade(javafx.scene.Node n, double f, double t) {
        FadeTransition ft = new FadeTransition(Duration.millis(550), n);
        ft.setFromValue(f); ft.setToValue(t); return ft;
    }

    private TranslateTransition slide(javafx.scene.Node n, double f, double t) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(550), n);
        tt.setFromY(f); tt.setToY(t);
        tt.setInterpolator(Interpolator.EASE_OUT); return tt;
    }

    // Load the IRO logo image — shown on login left panel, splash, and all top bars
    static ImageView loadLogo(double size) {
        try {
            Image img = new Image(LoginScreen.class.getResourceAsStream("/ui/assets/igirelogo.jpg"));
            if (img == null || img.isError()) return null;
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            // Circular clip so logo appears round like MoMo app icon
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(size / 2, size / 2, size / 2);
            iv.setClip(clip);
            iv.setStyle("-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.5),12,0,0,3);");
            return iv;
        } catch (Exception e) {
            return null;
        }
    }
}
