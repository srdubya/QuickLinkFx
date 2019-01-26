package com.srdubya.QuickLink.Export;

import com.srdubya.QuickLink.Crypto.Crypto;
import com.srdubya.QuickLink.Crypto.CryptoInitController;
import com.srdubya.QuickLink.Main;
import com.srdubya.QuickLink.MainController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ExportController implements Initializable {

    private static final String ExportLastFolder = "ExportLastFolder";

    @FXML private PasswordField firstPasswordField;
    @FXML private PasswordField secondPasswordField;
    @FXML private TextField commentTextField;

    @FXML private Button okBtn;

    private Stage dialogStage;
    private File targetFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        firstPasswordField.textProperty().addListener((observable, oldValue, newValue) -> okBtn.setDisable(isPasswordIssueFound()));
        secondPasswordField.textProperty().addListener((observable, oldValue, newValue) -> okBtn.setDisable(isPasswordIssueFound()));
    }

    private boolean isPasswordIssueFound() {
        String passwordIssue = getPasswordProblemDescription(firstPasswordField.getText(), secondPasswordField.getText());
        commentTextField.setText(passwordIssue);
        return passwordIssue.length() > 0;
    }

    private String getPasswordProblemDescription(final String firstPassword, final String secondPassword) {
        if(firstPassword.length() < CryptoInitController.MIN_PASSWORD_LEN) {
            return "Minimum password length of " + Integer.toString(CryptoInitController.MIN_PASSWORD_LEN) + " not met";
        }
        if(firstPassword.equals(secondPassword)) {
            return "";
        }
        return "Passwords do not match";
    }

    public static void showDialog(Window owner) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/Export.fxml"));
        AnchorPane page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Enter your password");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        ExportController controller = loader.getController();
        controller.dialogStage = dialogStage;

        dialogStage.setOnShown(event -> controller.onShown());
        dialogStage.showAndWait();
    }

    private static File getTargetFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(Main.appPreferences.get(
                ExportLastFolder,
                System.getProperty("user.home")
        )));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setTitle("Choose a file to save to");
        return fileChooser.showSaveDialog(owner);
    }

    private void onShown() {
        targetFile = getTargetFile(dialogStage.getOwner());
        if(targetFile == null) {
            dialogStage.close();
            return;
        }

        firstPasswordField.requestFocus();
    }

    @FXML private void onCancelBtnClicked() {
        dialogStage.close();
    }

    @FXML private void onOKBtnClicked() {
        commentTextField.setText("Saving...");
        try {
            Main.appPreferences.put(ExportLastFolder, targetFile.getParent());
            Crypto.initialize(firstPasswordField.getText());
            Main.saveToFile(targetFile);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            Crypto.initialize(Crypto.decryptPreference(Main.appPreferences.get(MainController.AppPasswordKey, "")));
            dialogStage.close();
        }
    }
}
