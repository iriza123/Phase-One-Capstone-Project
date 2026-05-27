package lab3.ui;

import lab1.model.Account;
import lab3.service.AccountService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

// LAB 3 - Admin: View all accounts and delete inactive ones
public class AdminAccountsScreen extends BaseScreen {

    private final AccountService accSvc = new AccountService();
    private TableView<Account> table;

    public AdminAccountsScreen(Stage stage) { super(stage); }

    @Override
    protected HBox buildTopBar(String title) {
        HBox bar = super.buildTopBar(title);
        ((Button) bar.getChildren().get(0)).setOnAction(e -> new AdminDashboardScreen(stage).show());
        return bar;
    }

    public void show() {
        VBox content = new VBox(16);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("All Accounts");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        HBox statsRow = new HBox(16);
        int total = 0; double balance = 0;
        try { total   = accSvc.count(); }                      catch (Exception ignored) {}
        try { balance = accSvc.totalBalance().doubleValue(); } catch (Exception ignored) {}
        statsRow.getChildren().addAll(
            miniCard("Total Active Accounts", String.valueOf(total),              "#27AE60"),
            miniCard("Total Balance",         String.format("%,.2f RWF", balance),"#D4A017")
        );

        Button deleteBtn = new Button("Delete All Inactive Accounts");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> deleteInactive());

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(title, statsRow, deleteBtn, initMessage(), table);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("Admin - Accounts"));
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(20));

        Scene scene = new Scene(root, 1000, 680);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - All Accounts");
        stage.show();
        loadAccounts();
    }

    private VBox miniCard(String label, String value, String accent) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:rgba(255,255,255,0.09);-fx-background-radius:12;-fx-border-color:" + accent + ";-fx-border-width:0 0 0 4;-fx-padding:16;-fx-min-width:200;");
        Label l = new Label(label); l.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.5);-fx-font-family:'Times New Roman';");
        Label v = new Label(value); v.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");
        card.getChildren().addAll(l, v);
        return card;
    }

    private TableView<Account> buildTable() {
        TableView<Account> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("ID",             a -> String.valueOf(a.getAccountId())),
            col("Account Number", a -> a.getAccountNumber()),
            col("Type",           a -> a.getAccountType()),
            col("Balance (RWF)",  a -> String.format("%,.2f", a.getBalance())),
            col("Status",         a -> a.getStatus()),
            col("Created",        a -> a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate().toString() : "")
        );
        TableColumn<Account, Void> actCol = new TableColumn<>("Action");
        actCol.setMaxWidth(100);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            { btn.getStyleClass().add("btn-danger");
              btn.setOnAction(e -> {
                  Account a = getTableView().getItems().get(getIndex());
                  if (!"INACTIVE".equals(a.getStatus())) { showMsg("Only inactive accounts can be deleted.", false); return; }
                  try { accSvc.deactivate(a.getAccountId()); loadAccounts(); showMsg("Account deleted.", true); }
                  catch (Exception ex) { showMsg(ex.getMessage(), false); }
              });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Account a = getTableView().getItems().get(getIndex());
                setGraphic("INACTIVE".equals(a.getStatus()) ? btn : null);
            }
        });
        tv.getColumns().add(actCol);
        tv.setPlaceholder(new Label("No accounts found."));
        return tv;
    }

    private void loadAccounts() {
        try { table.setItems(FXCollections.observableArrayList(accSvc.getAll())); }
        catch (Exception e) { showMsg("Error: " + e.getMessage(), false); }
    }

    private void deleteInactive() {
        try {
            List<Account> list = accSvc.getAll();
            int deleted = 0;
            for (Account a : list) {
                if ("INACTIVE".equals(a.getStatus())) {
                    try { accSvc.deactivate(a.getAccountId()); deleted++; } catch (Exception ignored) {}
                }
            }
            loadAccounts();
            showMsg(deleted > 0 ? "Deleted " + deleted + " inactive account(s)." : "No inactive accounts found.", deleted > 0);
        } catch (Exception e) { showMsg("Error: " + e.getMessage(), false); }
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }
}