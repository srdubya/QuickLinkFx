package com.srdubya.QuickLink.Crypto;

import com.srdubya.QuickLink.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CryptoInitController implements Initializable {

    public static final int MIN_PASSWORD_LEN = 8;

    @FXML private Button okBtn;
    @FXML private TextField passwordTextField;

    private Stage dialogStage;
    private static CryptoInitController self;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordTextField.textProperty().addListener((obs, oldVal, newVal) -> handlePasswordChanges(newVal));
    }

    public static String getPassword(Window owner) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("Crypto/CryptoInit.fxml"));
        AnchorPane page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Set Password");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setScene(new Scene(page));
        dialogStage.setOnShown((event -> self.passwordTextField.requestFocus()));

        CryptoInitController controller = loader.getController();
        controller.dialogStage = dialogStage;
        self = controller;

        dialogStage.showAndWait();

        return self.passwordTextField.textProperty().getValue().trim();
    }

    @FXML private void handleCancelBtnClick() {
        passwordTextField.textProperty().setValue("");
        dialogStage.close();
    }

    @FXML private void handleOKBtnClick() {
        dialogStage.close();
    }

    @FXML private void handlePasswordChanges(String newVal) {
        okBtn.setDisable(newVal.length() < MIN_PASSWORD_LEN);
    }
}
