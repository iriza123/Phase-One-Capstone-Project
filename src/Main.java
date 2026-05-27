import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lab2.db.DatabaseConnection;
import lab3.ui.LoginScreen;
import lab3.ui.SplashScreen;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseConnection.initSchema();
        new SplashScreen().show(() -> new LoginScreen(primaryStage).show());
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
