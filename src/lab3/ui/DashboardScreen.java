package lab3.ui;

import lab1.model.Account;
import lab1.model.Transaction;
import lab3.service.AccountService;
import lab3.service.TransactionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardScreen {

    private final Stage stage;
    private final AccountService     accSvc = new AccountService();
    private final TransactionService txSvc  = new TransactionService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public DashboardScreen(Stage stage) { this.stage = stage; }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar());
        root.setCenter(buildContent());

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Dashboard");
        stage.setMinWidth(900);
        stage.centerOnScreen();
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:rgba(13,43,26,0.8);" +
            "-fx-border-color:rgba(255,255,255,0.08);-fx-border-width:0 0 1 0;" +
            "-fx-padding:0 20 0 20;-fx-min-height:58px;");

        ImageView logo = LoginScreen.loadLogo(36);
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        if (logo != null) logoBox.getChildren().add(logo);
        Label appName = new Label("IgirePay");
        appName.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#D4A017;-fx-font-family:'Times New Roman';");
        logoBox.getChildren().add(appName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String name = SessionManager.getUser() != null ? SessionManager.getUser().getFullName() : "";
        Label userLabel = new Label("Hello, " + name);
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

        content.getChildren().add(buildBalanceCards());

        Label actTitle = new Label("What would you like to do?");
        actTitle.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
        content.getChildren().addAll(actTitle, buildActionGrid());

        Label txTitle = new Label("Recent Transactions");
        txTitle.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
        content.getChildren().addAll(txTitle, buildRecentTransactions());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
        return scroll;
    }

    private HBox buildBalanceCards() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        try {
            int custId = SessionManager.getUser().getCustomerId();
            List<Account> accounts = accSvc.getByCustomer(custId);
            if (accounts.isEmpty()) {
                Label noAcc = new Label("No accounts yet. Go to Profile to create one.");
                noAcc.setStyle("-fx-text-fill:rgba(255,255,255,0.5);-fx-font-family:'Times New Roman';");
                row.getChildren().add(noAcc);
            } else {
                for (Account a : accounts) {
                    VBox card = new VBox(8);
                    String color = "WALLET".equals(a.getAccountType()) ? "#D4A017" : "#27AE60";
                    card.setStyle(
                        "-fx-background-color:rgba(255,255,255,0.1);" +
                        "-fx-background-radius:14;-fx-border-radius:14;" +
                        "-fx-border-color:" + color + ";-fx-border-width:0 0 0 4;" +
                        "-fx-padding:18;-fx-min-width:220;");
                    Label type = new Label(a.getAccountType() + " ACCOUNT");
                    type.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.5);-fx-font-family:'Times New Roman';");
                    Label num = new Label(a.getAccountNumber());
                    num.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.7);-fx-font-family:'Times New Roman';");
                    Label bal = new Label(String.format("%,.2f RWF", a.getBalance()));
                    bal.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
                    card.getChildren().addAll(type, num, bal);
                    row.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            row.getChildren().add(new Label("Could not load accounts."));
        }
        return row;
    }

    private GridPane buildActionGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        Object[][] actions = {
            {"SEND MONEY", "#D4A017", "Send Money"},
            {"DEPOSIT",    "#27AE60", "Deposit"},
            {"WITHDRAW",   "#C0392B", "Withdraw"},
            {"SAVINGS",    "#2980B9", "Savings"},
            {"HISTORY",    "#8E44AD", "History"},
            {"REPORTS",    "#6D4C41", "Reports"},
            {"PROFILE",    "#556B2F", "Profile"}
        };
        for (int i = 0; i < actions.length; i++) {
            grid.add(momoTile((String)actions[i][0], (String)actions[i][1], (String)actions[i][2]),
                     i % 3, i / 3);
        }
        return grid;
    }

    private VBox momoTile(String label, String color, String target) {
        VBox tile = new VBox();
        tile.setAlignment(Pos.CENTER);
        tile.setPrefSize(160, 80);
        tile.setStyle("-fx-background-color:" + color + ";-fx-background-radius:12;" +
            "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
            "-fx-font-family:'Times New Roman';-fx-text-alignment:center;");
        lbl.setWrapText(true);
        lbl.setMaxWidth(140);
        tile.getChildren().add(lbl);
        tile.setOnMouseClicked(e -> navigate(target));
        tile.setOnMouseEntered(e -> tile.setStyle(
            "-fx-background-color:derive(" + color + ",20%);-fx-background-radius:12;" +
            "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.45),12,0,0,3);"));
        tile.setOnMouseExited(e -> tile.setStyle(
            "-fx-background-color:" + color + ";-fx-background-radius:12;" +
            "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);"));
        return tile;
    }

    private TableView<Transaction> buildRecentTransactions() {
        TableView<Transaction> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setMaxHeight(220);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("Reference",    t -> t.getReferenceId()),
            col("Type",         t -> t.getTransactionType()),
            col("Amount (RWF)", t -> String.format("%,.2f", t.getAmount())),
            col("Date",         t -> t.getCreatedAt() != null ? t.getCreatedAt().format(FMT) : ""),
            col("Status",       t -> t.getStatus())
        );
        try {
            int custId = SessionManager.getUser().getCustomerId();
            List<Transaction> txList = txSvc.getByCustomer(custId);
            tv.setItems(FXCollections.observableArrayList(
                txList.size() > 10 ? txList.subList(0, 10) : txList));
        } catch (Exception e) {
            tv.setPlaceholder(new Label("No transactions yet."));
        }
        tv.setPlaceholder(new Label("No transactions yet."));
        return tv;
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }

    private void navigate(String target) {
        switch (target) {
            case "Send Money" -> new SendMoneyScreen(stage).show();
            case "Deposit"    -> new DepositScreen(stage).show();
            case "Withdraw"   -> new WithdrawScreen(stage).show();
            case "Savings"    -> new SavingsScreen(stage).show();
            case "History"    -> new HistoryScreen(stage).show();
            case "Reports"    -> new ReportsScreen(stage).show();
            case "Profile"    -> new ProfileScreen(stage).show();
        }
    }
}
