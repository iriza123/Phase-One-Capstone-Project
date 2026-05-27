package lab3.ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    private final Stage splashStage;
    private ProgressBar progressBar;

    public SplashScreen() {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setAlwaysOnTop(true);
    }

    public void show(Runnable onComplete) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color:#0D2B1A;-fx-background-radius:14;");

        VBox logoBox = new VBox(14);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(50, 40, 20, 40));

        // Load IRO logo
        try {
            Image img = new Image(getClass().getResourceAsStream("/ui/assets/igirelogo.jpg"));
            if (img != null && !img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(120); iv.setFitHeight(120); iv.setPreserveRatio(true);
                // Circular clip
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(60, 60, 60);
                iv.setClip(clip);
                // Gold ring container
                javafx.scene.layout.StackPane logoContainer = new javafx.scene.layout.StackPane(iv);
                logoContainer.setStyle(
                    "-fx-background-color:rgba(212,160,23,0.15);" +
                    "-fx-background-radius:65;" +
                    "-fx-border-color:#D4A017;" +
                    "-fx-border-width:2;" +
                    "-fx-border-radius:65;" +
                    "-fx-padding:4;");
                logoContainer.setMaxSize(128, 128);
                logoBox.getChildren().add(logoContainer);
            }
        } catch (Exception ignored) {}

        Label appName = new Label("IgirePay");
        appName.setStyle("-fx-font-size:46px;-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        Label tagline = new Label("Igire Rwanda Organization - Mobile Money");
        tagline.setStyle("-fx-font-size:14px;-fx-text-fill:rgba(255,255,255,0.45);-fx-font-family:'Times New Roman';");

        logoBox.getChildren().addAll(appName, tagline);

        // Progress bar
        VBox progressBox = new VBox(10);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(28, 60, 40, 60));

        Label statusLabel = new Label("Starting up...");
        statusLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.4);-fx-font-size:12px;-fx-font-family:'Times New Roman';");

        progressBar = new ProgressBar(0);
        progressBar.setStyle("-fx-accent:#D4A017;-fx-background-color:rgba(255,255,255,0.1);" +
            "-fx-background-radius:4;-fx-min-height:4px;-fx-max-height:4px;");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        progressBox.getChildren().addAll(statusLabel, progressBar);

        Label version = new Label("v1.0.0  |  2025 IgirePay");
        version.setStyle("-fx-text-fill:rgba(255,255,255,0.2);-fx-font-size:10px;-fx-font-family:'Times New Roman';");
        version.setPadding(new Insets(0, 0, 14, 0));

        VBox.setVgrow(logoBox, Priority.ALWAYS);
        root.getChildren().addAll(logoBox, progressBox, version);

        // Fade in logo
        logoBox.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), logoBox);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(200));
        fadeIn.play();

        Scene scene = new Scene(root, 500, 360);
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Animate progress bar then open login
        Timeline tl = new Timeline();
        for (int i = 0; i < 5; i++) {
            final double p = (i + 1.0) / 5;
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(400 + i * 500),
                e -> progressBar.setProgress(p)));
        }
        tl.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(ev -> {
                FadeTransition out = new FadeTransition(Duration.millis(400), root);
                out.setFromValue(1); out.setToValue(0);
                out.setOnFinished(evv -> {
                    splashStage.close();
                    Platform.runLater(onComplete);
                });
                out.play();
            });
            pause.play();
        });
        tl.play();
    }
}
