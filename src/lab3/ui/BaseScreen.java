package lab3.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// Shared layout helper used by all screens to avoid code repetition
public abstract class BaseScreen {

    protected final Stage stage;
    protected Label messageLabel;

    public BaseScreen(Stage stage) { this.stage = stage; }

    // Top navigation bar with back button and logo
    protected HBox buildTopBar(String title) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:rgba(13,43,26,0.8);" +
            "-fx-border-color:rgba(255,255,255,0.08);-fx-border-width:0 0 1 0;" +
            "-fx-padding:0 20 0 20;-fx-min-height:58px;");

        Button back = new Button("< Back");
        back.setStyle("-fx-background-color:transparent;-fx-text-fill:#D4A017;" +
            "-fx-font-size:13px;-fx-cursor:hand;-fx-font-family:'Times New Roman';");
        back.setOnAction(e -> new DashboardScreen(stage).show());

        ImageView logo = LoginScreen.loadLogo(32);
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        if (logo != null) logoBox.getChildren().add(logo);
        Label appName = new Label("IgirePay");
        appName.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");
        logoBox.getChildren().add(appName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        bar.getChildren().addAll(back, new Label("  "), logoBox, spacer, titleLabel, new Label("  "));
        return bar;
    }

    // Frosted glass form card
    protected VBox buildFormCard() {
        VBox card = new VBox(18);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(560);
        return card;
    }

    // Label + field group
    protected VBox fieldGroup(String label, javafx.scene.Node field) {
        VBox g = new VBox(6);
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        g.getChildren().addAll(l, field);
        return g;
    }

    // Styled text field
    protected TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.getStyleClass().add("text-field-styled");
        tf.setPromptText(prompt);
        return tf;
    }

    // Styled combo box
    protected ComboBox<String> styledCombo(String prompt) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getStyleClass().add("combo-box-styled");
        cb.setPromptText(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    // Initialize message label
    protected Label initMessage() {
        messageLabel = new Label();
        messageLabel.setVisible(false);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(500);
        return messageLabel;
    }

    // Show success or error message
    protected void showMsg(String msg, boolean ok) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("alert-success", "alert-error");
        messageLabel.getStyleClass().add(ok ? "alert-success" : "alert-error");
        messageLabel.setVisible(true);
    }

    // Wrap content in a full-screen root with green gradient background
    protected BorderPane wrapInRoot(HBox topBar, javafx.scene.Node content) {
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        bp.setTop(topBar);
        StackPane center = new StackPane(content);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(28));
        center.setStyle("-fx-background-color:transparent;");
        bp.setCenter(center);
        return bp;
    }
}
