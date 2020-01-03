package com.srdubya.QuickLink.Add;

import com.srdubya.QuickLink.Crypto.Password;
import com.srdubya.QuickLink.LinkEntry;
import com.srdubya.QuickLink.Main;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class AddController implements Initializable {

    private static final String LastPathDirectoryKey = "Add.Last.Path.Directory";
    private static final String LastPathFileKey = "Add.Last.Path.File";
    private static final String LastAppKey = "Add.Last.App";
    private static final String LastPasswordLengthKey = "Add.Last.Password.Length";

    private boolean dialogResult;

    @FXML private Button findAppBtn;
    @FXML private Button findFolderBtn;
    @FXML private Button findFileBtn;
    @FXML private Button okBtn;
    @FXML private TextField nameTextField;
    @FXML private TextField loginTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField appTextField;
    @FXML private TextField pathTextField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField passwordLengthTextField;
    @FXML private Button passwordGenerateBtn;
    @FXML private CheckBox exitUponUseCB;

    private Stage dialogStage;
    private static AddController controller;
    private SimpleBooleanProperty viewingPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewingPassword = new SimpleBooleanProperty(false);
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        passwordTextField.managedProperty().bind(viewingPassword);
        passwordTextField.visibleProperty().bind(viewingPassword);

        passwordField.managedProperty().bind(viewingPassword.not());
        passwordField.visibleProperty().bind(viewingPassword.not());

        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        nameTextField.textProperty().addListener(((obs, oldVal, newVal) -> textChangedListener()));
        pathTextField.textProperty().addListener(((obs, oldVal, newVal) -> textChangedListener()));
        appTextField.textProperty().addListener(((obs, oldVal, newVal) -> textChangedListener()));

        findFolderBtn.setTooltip(new Tooltip("Pick a folder..."));
        findFileBtn.setTooltip(new Tooltip("Pick a file..."));

        passwordLengthTextField.textProperty().addListener((observable, oldValue, newValue) -> numberChangedListener(newValue));
    }

    private void numberChangedListener(String newValue) {
        try {
            Integer.parseInt(newValue);
            passwordGenerateBtn.setDisable(false);
        } catch (NumberFormatException ex) {
            passwordGenerateBtn.setDisable(true);
        }
    }

    private void textChangedListener() {
        okBtn.setDisable(!isEntryMakeable());
    }

    public interface NewLinkEntryHandler {
        void operation(LinkEntry element) throws IOException;
    }

    private NewLinkEntryHandler newLinkEntryHandler;

    private static AddController makeController(Window owner, NewLinkEntryHandler handler) throws IOException {
        if(handler == null) {
            throw new IllegalArgumentException("Argument 'handler' cannot be null");
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/Add.fxml"));
        AnchorPane page = loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setOnShown(event -> {
            controller.passwordLengthTextField.setText(
                    Integer.toString(Main.appPreferences.getInt(LastPasswordLengthKey,8))
            );
            controller.passwordLengthTextField.setPrefWidth(controller.passwordGenerateBtn.getWidth());
        });

        AddController controller = loader.getController();
        controller.dialogStage = dialogStage;
        controller.newLinkEntryHandler = handler;

        controller.dialogResult = false;

        return controller;
    }

    public static void showAddDialog(Window owner, NewLinkEntryHandler handler ) throws IOException {
        controller = makeController(owner, handler);
        controller.dialogStage.setTitle("Add Link");
        controller.dialogResult = false;
        controller.dialogStage.showAndWait();
    }

    public static boolean showEditDialog(LinkEntry entry, Window owner, NewLinkEntryHandler handler) throws IOException {
        controller = makeController(owner, handler);
        controller.dialogStage.setTitle("Edit Link");
        controller.nameTextField.setText(entry.getName());
        controller.loginTextField.setText(entry.getLogin());
        controller.emailTextField.setText(entry.getEmail());
        controller.appTextField.setText(entry.getApp());
        controller.pathTextField.setText(entry.getPath());
        controller.passwordField.setText(entry.getPassword());
        controller.exitUponUseCB.setSelected(entry.isClose());
        controller.dialogStage.showAndWait();
        return controller.dialogResult;
    }

    @FXML private void onChoosePathDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File initialDirectory = new File(Main.appPreferences.get(LastPathDirectoryKey, System.getProperty("user.home")));
        if(!initialDirectory.isDirectory()) {
            initialDirectory = new File(System.getProperty("user.home"));
        }
        directoryChooser.setInitialDirectory(initialDirectory);
        directoryChooser.setTitle("Choose Path as Directory");

        File selectedDirectory = directoryChooser.showDialog(dialogStage);

        if(selectedDirectory != null && selectedDirectory.isDirectory()) {
            pathTextField.setText(selectedDirectory.getPath());
        }
    }

    @FXML private void onChoosePathFile() {
        FileChooser fileChooser = new FileChooser();

        File lastPathFile = new File(Main.appPreferences.get(LastPathFileKey, ""));
        if(lastPathFile.exists() && Files.isExecutable(lastPathFile.toPath())) {
            fileChooser.setInitialDirectory(lastPathFile.getParentFile());
        } else {
            fileChooser.setInitialDirectory(new File(Main.appPreferences.get(LastPathDirectoryKey, System.getProperty("user.home"))));
        }
        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        if(selectedFile != null && selectedFile.exists() && Files.isExecutable(selectedFile.toPath())) {
            pathTextField.setText(selectedFile.getPath());
        }
    }

    @FXML private void onChooseAppFile() {
        FileChooser fileChooser = new FileChooser();

        File lastApp = new File(Main.appPreferences.get(LastAppKey, ""));
        if(lastApp.exists() && Files.isExecutable(lastApp.toPath())) {
            fileChooser.setInitialDirectory(lastApp.getParentFile());
        }

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if(selectedFile != null && selectedFile.exists() && Files.isExecutable(selectedFile.toPath())) {
            appTextField.setText(selectedFile.getPath());
        }
    }

    @FXML private void onCloseBtnClicked() {
        dialogResult = false;
        dialogStage.close();
    }

    @FXML private void onOKBtnClicked() {
        if(isEntryMakeable()) {
            dialogResult = true;

            LinkEntry tmp = new LinkEntry();
            tmp.setName(nameTextField.getText());
            tmp.setLogin(loginTextField.getText());
            tmp.setEmail(emailTextField.getText());
            tmp.setApp(appTextField.getText());
            tmp.setPath(pathTextField.getText());
            tmp.setPassword(passwordField.getText());
            tmp.setClose(exitUponUseCB.isSelected());

            try {
                newLinkEntryHandler.operation(tmp);

                File lastPath = new File(pathTextField.getText());
                if (lastPath.exists()) {
                    Main.appPreferences.put(LastPathDirectoryKey, lastPath.getParentFile().getPath());
                    if (lastPath.isFile()) {
                        Main.appPreferences.put(LastPathFileKey, lastPath.getPath());
                    }
                }
                File lastApp = new File(appTextField.getText());
                if (lastApp.exists() && Files.isExecutable(lastApp.toPath())) {
                    Main.appPreferences.put(LastAppKey, lastApp.getPath());
                }

                int pwLength = Integer.parseInt(passwordLengthTextField.getText());
                Main.appPreferences.putInt(LastPasswordLengthKey, pwLength);
            } catch (NumberFormatException | IOException ex) {
                ex.printStackTrace();
            }
        }
        dialogStage.close();
    }

    private boolean isEntryMakeable() {
        return nameTextField.getText().length() > 0
                && pathTextField.getText().length() > 0
                && isRunnable(appTextField.getText());
    }

    private boolean isRunnable(String text) {
        if(text.length() == 0) {
            return true;
        }
        File file = new File(text);
        return file.isFile() && Files.isExecutable(file.toPath());
    }

    @FXML private void onViewBtnClicked() {
        viewingPassword.setValue(!viewingPassword.getValue());
    }

    @FXML private void onGeneratePasswordClicked() {
        try {
            int length = Integer.parseInt(this.passwordLengthTextField.getText());
            passwordTextField.setText(Password.getPassword(length));
        } catch (NumberFormatException ex) {
            return;
        }
    }

}
