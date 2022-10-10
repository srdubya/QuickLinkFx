package com.srdubya.QuickLink;

import com.srdubya.QuickLink.Add.AddController;
import com.srdubya.QuickLink.ConfirmDelete.ConfirmDeleteController;
import com.srdubya.QuickLink.Export.ExportController;
import com.srdubya.QuickLink.Import.ImportController;
import com.srdubya.QuickLink.Crypto.Crypto;
import com.srdubya.QuickLink.Crypto.CryptoInitController;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ResourceBundle;


public final class MainController implements Initializable {

    private static final String MainSortColumnNameKey = "Main.Sort.ColumnName";
    public static final String AppPasswordKey = "AppPassword";

    private Stage stage;

    @FXML private TableView<LinkEntry> tableView;
    @FXML private TableColumn<LinkEntry, String> nameColumn;
    @FXML private TableColumn<LinkEntry, String> loginColumn;
    @FXML private TableColumn<LinkEntry, String> emailColumn;
    @FXML private TableColumn<LinkEntry, String> appColumn;
    @FXML private TableColumn<LinkEntry, String> pathColumn;
    @FXML private TableColumn<LinkEntry, Boolean> closeColumn;
    @FXML private TableColumn<LinkEntry, String> passwordColumn;

    @FXML private Button menuBtn;
    @FXML private ContextMenu mainContextMenu;

    @FXML private TextField searchTextField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label isErrorLabel;
    @FXML private AnchorPane topAnchorPane;

    @FXML private ContextMenu tableContextMenu;
    @FXML private MenuItem tableCopyMenuItem;
    @FXML private MenuItem tableEditMenuItem;
    @FXML private MenuItem tableDeleteMenuItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        appColumn.setCellValueFactory(new PropertyValueFactory<>("app"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        closeColumn.setCellValueFactory(new PropertyValueFactory<>("close"));

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setOnContextMenuRequested((event) -> {
            ObservableList<LinkEntry> selectedEntries = tableView.getSelectionModel().getSelectedItems();
            tableCopyMenuItem.setDisable(selectedEntries.size() != 1  ||
                    selectedEntries.get(0).getPassword().length() == 0);
            tableEditMenuItem.setDisable(selectedEntries.size() != 1);
            tableDeleteMenuItem.setDisable(selectedEntries.size() == 0);
            tableContextMenu.show(tableView.getScene().getWindow(), event.getScreenX(), event.getScreenY());
        });
        tableView.setRowFactory((tableView) -> {
            TableRow<LinkEntry> row = new TableRow<>();
            row.setOnMouseClicked((event) -> {
                if(row.isEmpty()) {
                    return;
                }
                if(event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
                    launch(row.getItem());
                }
            });
            return row;
        });
        tableView.setItems(Main.getLinks());
        ((SortedList<LinkEntry>) tableView.getItems()).comparatorProperty().bind(tableView.comparatorProperty());

        Main.setDefaultControl(searchTextField);
        Main.setController(this);

        String sortColumnName = Main.appPreferences.get(MainSortColumnNameKey, "Name");
        tableView.getColumns().forEach((column) -> {
            column.setPrefWidth(Main.appPreferences.getInt("Column.Width." + column.getText(), 100));
            if(column.getText().equals(sortColumnName)) {
                tableView.getSortOrder().add(column);
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    void setIsErrorLabelVisible(boolean visible) {
        isErrorLabel.setVisible(visible);
    }

    private void launch(LinkEntry item) {
        try {
            clip(item.getPassword());

            String path = item.getPath();
            if (path.length() == 0) {
                return;
            }

            File app = new File(item.getApp());
            if (app.exists() && Files.isExecutable(app.toPath())) {
                new ProcessBuilder(app.getPath(), path).start();
            } else if (path.toLowerCase().startsWith("http")) {
                Desktop.getDesktop().browse(new URI(path));
            } else if (path.toLowerCase().startsWith("www.")) {
                Desktop.getDesktop().browse(new URI("http://" + path));
            } else {
                File file = new File(path);
                if (!file.exists()) {
                    return;
                }
                Desktop.getDesktop().open(file);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(item.isClose()) {
                stage.close();
            }
        }
    }

    private void clip(String password) {
        if(password == null || password.length() == 0) {
            return;
        }
        StringSelection selection = new StringSelection(password);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    @FXML private void onCopyPassword() {
        ObservableList<LinkEntry> selectedItems = tableView.getSelectionModel().getSelectedItems();
        if(selectedItems.size() == 1) {
            String password = selectedItems.get(0).getPassword();
            clip(password);
        }
    }

    @FXML private void onEditEntry() {
        ObservableList<LinkEntry> selectedItems = tableView.getSelectionModel().getSelectedItems();
        if(selectedItems.size() == 1) {
            try {
                if(AddController.showEditDialog(
                        selectedItems.get(0),
                        tableView.getScene().getWindow(),
                        (link) -> selectedItems.get(0).copyFrom(link))
                        ) {
                    Main.saveToFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void onDeleteEntry() {
        ArrayList<LinkEntry> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems().size());
        selectedItems.addAll(tableView.getSelectionModel().getSelectedItems());
        selectedItems.forEach((item) -> {
            try {
                ConfirmDeleteController.showDialog(item, tableView.getScene().getWindow(), (link) -> {
                    Main.deleteLinkEntry(link);
                    Main.saveToFile();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void onShown() {
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> Main.updateFilter(newVal));
        tableView.getColumns().forEach((column) -> column.widthProperty().addListener((observable, oldValue, newValue) -> {
            Main.appPreferences.putInt("Column.Width." + column.getText(), newValue.intValue());
        }));
        tableView.setOnKeyReleased(event -> {
            if (event.getCode().equals(KeyCode.DELETE)) {
                onDeleteEntry();
                event.consume();
            }
        });
    }

    public void onClose() {
        if (tableView.getSortOrder().size() > 0) {
            TableColumn<LinkEntry,?> sortColumn = (tableView.getSortOrder()).get(0);
            Main.appPreferences.put(MainSortColumnNameKey, sortColumn.getText());
        }
    }

    @FXML private void onMenuBtnClicked() {
        mainContextMenu.show(menuBtn, Side.BOTTOM, 0.0D, 0.0D);
    }

    @FXML private void onAddMenuItemClicked() {
        try {
            AddController.showAddDialog(menuBtn.getScene().getWindow(), (entry) -> {
                Main.addLinkEntry(entry);
                Main.saveToFile();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void onImportMenuItemClicked() {
        try {
            ImportController.showDialog(menuBtn.getScene().getWindow(), Main::addLinkEntry);
            Main.saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void onSaveAsMenuItemClicked() {
        try {
            ExportController.showDialog(menuBtn.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void onShowPasswordChanged() {
        passwordColumn.setVisible(showPasswordCheckBox.isSelected());
    }

    @FXML private void onExitMenuItemClicked() {
        stage.close();
    }
    
    @FXML private void onResetPassword() { resetPassword(stage); }

    private void resetPassword(Stage stage) {
        String password;
        try {
            password = CryptoInitController.getPassword(topAnchorPane.getScene().getWindow());
            if (CryptoInitController.isPasswordAcceptable(password)) {
                Main.getContext().setError(false);
                password = Crypto.encryptPreference(password);
                Main.appPreferences.put(AppPasswordKey, password);
                Crypto.initialize(Crypto.decryptPreference(password));
                Main.resetEncryptedPasswords();
                Main.saveToFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initCrypto(Stage primaryStage) {
        boolean loop = true;
        String password = Main.appPreferences.get(AppPasswordKey, "");
        while (loop) {
            try {
                if (password.equals("")) {
                    password = CryptoInitController.getPassword(topAnchorPane.getScene().getWindow());
                    if (!CryptoInitController.isPasswordAcceptable(password)) {
                        primaryStage.close();   // because user must have cancelled the password dialog
                        return;
                    }
                    password = Crypto.encryptPreference(password);
                    Main.appPreferences.put(AppPasswordKey, password);
                }
                Crypto.initialize(Crypto.decryptPreference(password));
                loop = false;
            } catch (Exception e) {
                e.printStackTrace();
                password = "";
            }
        }
    }
}
