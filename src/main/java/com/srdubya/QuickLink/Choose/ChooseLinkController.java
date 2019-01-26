package com.srdubya.QuickLink.Choose;

import com.srdubya.QuickLink.LinkEntry;
import com.srdubya.QuickLink.Main;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ChooseLinkController implements Initializable {

    @FXML private AnchorPane topPane;
    @FXML private Label messageLabel;
    @FXML private TableView<LinkEntry> tableView;
    @FXML private TableColumn<LinkEntry, String> nameColumn;
    @FXML private TableColumn<LinkEntry, String> loginColumn;
    @FXML private TableColumn<LinkEntry, String> emailColumn;
    @FXML private TableColumn<LinkEntry, String> appColumn;
    @FXML private TableColumn<LinkEntry, String> pathColumn;
    @FXML private TableColumn<LinkEntry, Boolean> closeColumn;
    @FXML private TableColumn<LinkEntry, String> passwordColumn;
    @FXML private CheckBox showPasswordsCB;
    @FXML private Button okBtn;
    @FXML private Button cancelBtn;

    static ChooseLinkController self;
    Stage stage;
    boolean isCanceled;
    List<LinkEntry> returnValue;
    final static Map<String,List<LinkEntry>> pendingDuplicates = new HashMap<>();

    static {
        Preferences.setPasswordColumnVisible(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        appColumn.setCellValueFactory(new PropertyValueFactory<>("app"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        closeColumn.setCellValueFactory(new PropertyValueFactory<>("close"));
        addIsChosenColumn();

        topPane.setPrefWidth(Preferences.getWidth());
        topPane.setPrefHeight(Preferences.getHeight());
        topPane.widthProperty().addListener((obv, oldValue, newValue) -> Preferences.putWidth(newValue.intValue()));
        topPane.heightProperty().addListener((obv, oldValue, newValue) -> Preferences.putHeight(newValue.intValue()));

        for (var col : tableView.getColumns()) {
            final var colName = col.getText();
            col.setPrefWidth(Preferences.getColumnWidth(colName));
            col.widthProperty().addListener(
                    (obv, oldValue, newValue) -> Preferences.putColumnWidth(colName, newValue.intValue())
            );
        }
        passwordColumn.setVisible(Preferences.getPasswordColumnVisible());

        tableView.setRowFactory((tableView) -> {
            TableRow<LinkEntry> row = new TableRow<>();
            row.setOnMouseClicked((event) -> {
                onRowClicked(row, event);
            });
            return row;
        });

    }

    public static void addPotentialDuplicate(final LinkEntry linkEntry) {
        var name = linkEntry.getName();
        if (!pendingDuplicates.containsKey(name)) {
            pendingDuplicates.put(name, new LinkedList<>());
        }
        pendingDuplicates.get(name).add(linkEntry);
    }


    public static Collection<List<LinkEntry>> getPotentialDuplicates() {
        return pendingDuplicates.values();
    }

    public static void clearPotentialDuplicates() {
        pendingDuplicates.clear();
    }


    private void onRowClicked(TableRow<LinkEntry> row, MouseEvent event) {
        if (row.isEmpty()) {
            return;
        }
        if (event.getClickCount() == 1 && event.getButton().equals(MouseButton.PRIMARY)) {
            var linkEntry = row.getItem();
            linkEntry.setIsChosen(!linkEntry.isChosen());
        }
    }

    public static List<LinkEntry> chooseLinks(
            final Window owner,
            final List<LinkEntry> currentLinks,
            final List<LinkEntry> newLinks
    ) {
        try {
            FXMLLoader loader = new FXMLLoader();
            var fxml = Main.class.getResource("/ChooseLink.fxml");
            loader.setLocation(fxml);
            AnchorPane page = loader.load();

            self = loader.getController();
            self.returnValue = new LinkedList<>();

            self.stage = new Stage();
            self.stage.setTitle("Choose Links");
            self.stage.initModality(Modality.WINDOW_MODAL);
            self.stage.initOwner(owner);
            self.stage.setScene(new Scene(page));
            self.stage.setOnShown(event -> self.onShown(currentLinks, newLinks));

            self.stage.showAndWait();

            if (self.isCanceled) {
                return null;
            }
            return self.returnValue;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void onShowPasswordsChanges() {
        var passwordColumn = tableView.getColumns().filtered(
                linkEntryTableColumn -> linkEntryTableColumn.getText().equalsIgnoreCase("password")
        ).get(0);
        passwordColumn.setVisible(showPasswordsCB.isSelected());
        Preferences.setPasswordColumnVisible(true);
    }

    @FXML
    public void onCancelBtn() {
        isCanceled = true;
        stage.close();
    }

    @FXML
    public void onOKBtn() {
        isCanceled = false;
        for (var row : tableView.getItems()) {
            if (row.isChosen()) {
                returnValue.add(row);
            }
        }
        stage.close();
    }

    private void onShown(final List<LinkEntry> currentLinks, final List<LinkEntry> newLinks) {
        for (var linkEntry : currentLinks) {
            linkEntry.setIsChosen(true);
            tableView.getItems().add(linkEntry);
        }
        for (var newLink : newLinks) {
            newLink.setIsChosen(false);
            tableView.getItems().add(newLink);
        }
    }

    private void addIsChosenColumn() {
        var checkedColumn = new TableColumn<LinkEntry, CheckBoxTableCell<LinkEntry,LinkEntry>>("");
        checkedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(index -> {
            var linkEntry = tableView.getItems().get(index);
            return linkEntry.isChosenProperty();
        }));
//        checkedColumn.setCellFactory(linkEntryBooleanTableColumn -> new CheckBoxTableCell());
//        checkedColumn.setCellValueFactory(data -> {
//            var linkEntry = data.getValue();
//            var x = data.getTableColumn().
//        });

        tableView.getColumns().add(0, checkedColumn);
    }
}
