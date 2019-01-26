package com.srdubya.QuickLink.Import;

import com.srdubya.QuickLink.Crypto.Crypto;
import com.srdubya.QuickLink.Crypto.CryptoInitController;
import com.srdubya.QuickLink.LinkEntry;
import com.srdubya.QuickLink.Main;
import com.srdubya.QuickLink.MainController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ImportController implements Initializable {

    private static final String LastFolder = "Import.Folder";
    private static final String LastFilename = "Import.Path";
    private static final String LastExtensionIndex = "Import.ExtensionIndex";

    private static final FileChooser.ExtensionFilter[] ChooserExtensions = {
        new FileChooser.ExtensionFilter("Text Files", "*.txt"),
        new FileChooser.ExtensionFilter("XML Files", "*.xml"),
        new FileChooser.ExtensionFilter("JSON Files", "*.json"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    };

    @FXML private Button importBtn;
    @FXML private TextField filenameTextField;
    @FXML private PasswordField passwordField;

    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public interface NewLinkEntryHandler {
        void operation(LinkEntry element);
    }

    private NewLinkEntryHandler newLinkEntryHandler;

    public static void showDialog(Window owner, NewLinkEntryHandler handler ) throws IOException {
        if(handler == null) {
            throw new IllegalArgumentException("Argument 'handler' cannot be null");
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/Import.fxml"));
        AnchorPane page = loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Import Links");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        ImportController controller = loader.getController();
        controller.dialogStage = dialogStage;
        controller.filenameTextField.setText(Main.appPreferences.get(LastFilename, ""));
        controller.setImportEnabled();
        controller.newLinkEntryHandler = handler;

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
    }

    @FXML private void onFindFileRequest() {
        File initialDirectory = new File(Main.appPreferences.get(LastFolder, System.getProperty("user.home")));
        if (!initialDirectory.exists() || !initialDirectory.isDirectory()) {
            initialDirectory = new File(System.getProperty("user.home"));
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(initialDirectory);
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(ChooserExtensions);
        fileChooser.setSelectedExtensionFilter(ChooserExtensions[Main.appPreferences.getInt(LastExtensionIndex, 0)]);

        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedFile != null && selectedFile.isFile()) {
            filenameTextField.setText(selectedFile.getPath());
            Main.appPreferences.putInt(LastExtensionIndex, getChooserIndex(fileChooser.getSelectedExtensionFilter()));
            Main.appPreferences.put(LastFolder, selectedFile.getParentFile().getPath());
            setImportEnabled();
        }
    }

    private int getChooserIndex(FileChooser.ExtensionFilter selectedExtensionFilter) {
        for(int i = 0; i < ChooserExtensions.length; i++) {
            if(ChooserExtensions[i] == selectedExtensionFilter) {
                return i;
            }
        }
        return 0;
    }

    private void setImportEnabled() {
        importBtn.setDisable(!new File(filenameTextField.getText()).isFile());
    }

    private String getExtension(String name) {
        int index = name.lastIndexOf('.');
        if(index > 0) {
            return name.substring(index + 1).toLowerCase();
        } else {
            return "";
        }
    }

    @FXML private void onImportRequest() throws ParserConfigurationException, SAXException, IOException {
        Main.appPreferences.put(LastFilename, filenameTextField.getText());

        File file = new File(filenameTextField.getText());
        String extension = getExtension(file.getName());

        boolean isCryptoReInited = false;

        try {
            if (passwordField.getText().length() >= CryptoInitController.MIN_PASSWORD_LEN) {
                Crypto.initialize(passwordField.getText());
                isCryptoReInited = true;
            }
            if (extension.equals("xml")) {
                importXmlFile(file);
            } else if (extension.endsWith("json")) {
                int count = importJsonFile(file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Complete");
                alert.setHeaderText(String.format("Imported %d links", count));
                alert.setContentText(null);
                alert.showAndWait();
            }
        } finally {
            if(isCryptoReInited) {
                Crypto.initialize(Crypto.decryptPreference(Main.appPreferences.get(MainController.AppPasswordKey, "")));
            }
            dialogStage.close();
        }
    }

    private int importJsonFile(File file) {
        return Main.addLinksFromFile(file);
    }

    private void importXmlFile(File file) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        LinkEntrySAXHandler saxHandler = new LinkEntrySAXHandler(newLinkEntryHandler);
        saxParser.parse(file, saxHandler);
    }
}
