package lab3.ui;

import lab3.service.AccountService;
import lab3.service.CustomerService;
import lab3.service.TransactionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// LAB 3 - JavaFX: ADMIN dashboard
public class AdminDashboardScreen {

    private final Stage stage;
    private final CustomerService    custSvc = new CustomerService();
    private final AccountService     accSvc  = new AccountService();
    private final TransactionService txSvc   = new TransactionService();

    public AdminDashboardScreen(Stage stage) { this.stage = stage; }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar());
        root.setCenter(buildContent());

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Admin Dashboard");
        stage.centerOnScreen();
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:rgba(13,43,26,0.85);" +
            "-fx-border-color:rgba(255,255,255,0.08);-fx-border-width:0 0 1 0;" +
            "-fx-padding:0 20 0 20;-fx-min-height:58px;");

        ImageView logo = LoginScreen.loadLogo(34);
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        if (logo != null) logoBox.getChildren().add(logo);
        Label appName = new Label("IgirePay Admin");
        appName.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");
        logoBox.getChildren().add(appName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String name = SessionManager.getUser() != null ? SessionManager.getUser().getFullName() : "Admin";
        Label userLabel = new Label("Admin: " + name);
        userLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setOnAction(e -> { SessionManager.logout(); new LoginScreen(stage).show(); });

        bar.getChildren().addAll(logoBox, spacer, userLabel, new Label("  "), logoutBtn);
        return bar;
    }

    private ScrollPane buildContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Admin Overview");
        title.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        content.getChildren().addAll(title, buildStats(), buildActions());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
        return scroll;
    }

    private HBox buildStats() {
        HBox row = new HBox(16);
        int customers = 0, accounts = 0, txToday = 0;
        double balance = 0, volume = 0;
        try { customers = custSvc.count(); }    catch (Exception ignored) {}
        try { accounts  = accSvc.count(); }     catch (Exception ignored) {}
        try { balance   = accSvc.totalBalance().doubleValue(); } catch (Exception ignored) {}
        try { txToday   = txSvc.countToday(); } catch (Exception ignored) {}
        try { volume    = txSvc.sumToday(); }   catch (Exception ignored) {}

        row.getChildren().addAll(
            statCard("Customers",    String.valueOf(customers),             "#D4A017"),
            statCard("Accounts",     String.valueOf(accounts),              "#27AE60"),
            statCard("Total Balance",String.format("%,.0f RWF", balance),  "#2980B9"),
            statCard("Tx Today",     String.valueOf(txToday),               "#8E44AD"),
            statCard("Volume Today", String.format("%,.0f RWF", volume),   "#C0392B")
        );
        for (javafx.scene.Node n : row.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        return row;
    }

    private VBox statCard(String label, String value, String accent) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:rgba(255,255,255,0.09);-fx-background-radius:12;" +
            "-fx-border-radius:12;-fx-border-color:" + accent + ";-fx-border-width:0 0 0 4;-fx-padding:18;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.5);-fx-font-family:'Times New Roman';");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
        card.getChildren().addAll(l, v);
        return card;
    }

    private HBox buildActions() {
        HBox row = new HBox(14);
        row.getChildren().addAll(
            adminTile("Manage\nCustomers",  "#556B2F", () -> new AdminScreen(stage).show()),
            adminTile("All\nTransactions",  "#2980B9", () -> new AdminHistoryScreen(stage).show()),
            adminTile("All\nAccounts",      "#D4A017", () -> new AdminAccountsScreen(stage).show()),
            adminTile("Reports\n& Export",  "#8E44AD", () -> new AdminReportsScreen(stage).show())
        );
        return row;
    }

    private VBox adminTile(String label, String color, Runnable action) {
        VBox tile = new VBox();
        tile.setAlignment(Pos.CENTER);
        tile.setPrefSize(180, 90);
        tile.setStyle("-fx-background-color:" + color + ";-fx-background-radius:12;" +
            "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;" +
            "-fx-font-family:'Times New Roman';-fx-text-alignment:center;");
        lbl.setWrapText(true);
        tile.getChildren().add(lbl);
        tile.setOnMouseClicked(e -> action.run());
        return tile;
    }
}
