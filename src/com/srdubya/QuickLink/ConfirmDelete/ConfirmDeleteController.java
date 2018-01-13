package com.srdubya.QuickLink.ConfirmDelete;

import com.srdubya.QuickLink.LinkEntry;
import com.srdubya.QuickLink.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmDeleteController implements Initializable {

    @FXML private TextField nameTextField;
    @FXML private TextField pathTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public interface DeleteLinkHandler {
        void deleteLink(LinkEntry link);
    }

    private LinkEntry targetEntry;
    private Stage dialogStage;
    private DeleteLinkHandler deleteHandler;

    public static void showDialog(LinkEntry targetEntry, Window owner, DeleteLinkHandler handler ) throws IOException {
        if(handler == null) {
            throw new IllegalArgumentException("Argument 'handler' cannot be null");
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("ConfirmDelete/ConfirmDelete.fxml"));
        AnchorPane page = loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add Link");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        ConfirmDeleteController controller = loader.getController();
        controller.targetEntry = targetEntry;
        controller.dialogStage = dialogStage;
        controller.deleteHandler = handler;
        controller.nameTextField.setText(targetEntry.getName());
        controller.pathTextField.setText(targetEntry.getPath());

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
    }

    @FXML private void onConfirmBtnClicked() {
        deleteHandler.deleteLink(targetEntry);
        dialogStage.close();
    }

    @FXML private void onCancelBtnClicked() {
        dialogStage.close();
    }
}
