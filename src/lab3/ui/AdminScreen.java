package lab3.ui;

import lab1.model.Customer;
import lab3.service.AccountService;
import lab3.service.CustomerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class AdminScreen extends BaseScreen {

    private final CustomerService custSvc = new CustomerService();
    private final AccountService  accSvc  = new AccountService();
    private TableView<Customer> table;

    public AdminScreen(Stage stage) { super(stage); }

    @Override
    protected HBox buildTopBar(String title) {
        HBox bar = super.buildTopBar(title);
        ((Button) bar.getChildren().get(0)).setOnAction(e -> new AdminDashboardScreen(stage).show());
        return bar;
    }

    public void show() {
        VBox content = new VBox(16);
        content.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Customer Management");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;-fx-font-family:'Times New Roman';");

        TextField search = styledField("Search by name, phone, email...");
        search.setMaxWidth(360);
        search.textProperty().addListener((obs, o, n) -> filterTable(n));

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, search, table);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#0D2B1A,#1B4332,#2D5A27);");
        root.setTop(buildTopBar("Admin - Customers"));
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(20));

        Scene scene = new Scene(root, 1000, 680);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("IgirePay - Admin");
        stage.show();
        loadCustomers();
    }

    private TableView<Customer> buildTable() {
        TableView<Customer> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tv.getColumns().addAll(
            col("ID",     c -> String.valueOf(c.getCustomerId())),
            col("Name",   c -> c.getFullName()),
            col("Phone",  c -> c.getPhone()),
            col("Email",  c -> c.getEmail()),
            col("Role",   c -> c.getRole()),
            col("Status", c -> c.getStatus())
        );

        // Deactivate + Unlock action column
        TableColumn<Customer, Void> actCol = new TableColumn<>("Actions");
        actCol.setMinWidth(160);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button deactivateBtn = new Button("Deactivate");
            private final Button unlockBtn     = new Button("Unlock");
            {
                deactivateBtn.getStyleClass().add("btn-danger");
                unlockBtn.setStyle(
                    "-fx-background-color:rgba(39,174,96,0.75);-fx-text-fill:white;" +
                    "-fx-font-size:12px;-fx-font-family:'Times New Roman';" +
                    "-fx-padding:6 12;-fx-background-radius:6;-fx-cursor:hand;");

                deactivateBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    try {
                        custSvc.deactivate(c.getCustomerId());
                        accSvc.inactivateByCustomerId(c.getCustomerId());
                        loadCustomers();
                    } catch (Exception ex) { System.err.println(ex.getMessage()); }
                });

                unlockBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    try { custSvc.unlockAccount(c.getCustomerId()); loadCustomers(); }
                    catch (Exception ex) { System.err.println(ex.getMessage()); }
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Customer c = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);
                // Show Unlock only for locked accounts
                if ("LOCKED".equals(c.getStatus())) box.getChildren().add(unlockBtn);
                else box.getChildren().add(deactivateBtn);
                setGraphic(box);
            }
        });
        tv.getColumns().add(actCol);
        tv.setPlaceholder(new Label("No customers found."));
        return tv;
    }

    private void loadCustomers() {
        try {
            List<Customer> list = custSvc.getAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }

    private void filterTable(String kw) {
        if (kw == null || kw.isBlank()) { loadCustomers(); return; }
        try {
            List<Customer> list = custSvc.search(kw);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ignored) {}
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }
}
