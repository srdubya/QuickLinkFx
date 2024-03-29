package com.srdubya.QuickLink;

import com.srdubya.QuickLink.Choose.ChooseLinkController;
import com.srdubya.QuickLink.Crypto.Crypto;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
//import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.prefs.Preferences;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main extends Application {

    private static final int DefWidth = 600;
    private static final int DefHeight = 300;

    private final String WindowWidthKey = "Main.Width";
    private final String WindowHeightKey = "Main.Height";

    public static final Preferences appPreferences;

    private static final LinkEntryList masterData;
    private static final FilteredList<LinkEntry> filteredData;
    private static final SortedList<LinkEntry> links;
    private static final ApplicationContext context = new ApplicationContext();
    private static final QuickLinkFile dataFile = new QuickLinkFile();

    static ObservableList<LinkEntry> getLinks() {
        return links;
    }

    static {
        appPreferences = Preferences.userNodeForPackage(Main.class);
        masterData = new LinkEntryList();
        filteredData = new FilteredList<>(masterData, entry -> true);
        links = new SortedList<>(filteredData);
        Security.setProperty("crypto.policy", "unlimited");
//        Security.addProvider(new BouncyCastleProvider());
    }

    private static Control defaultControl;
    static void setDefaultControl(Control control) {
        defaultControl = control;
    }
    private static MainController controller;
    public static void setController(MainController theController) {
        controller = theController;
    }


    @Override
    public void start(Stage primaryStage) throws Exception{
        if(!Crypto.isSystemReady()) {
            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setTitle("QuickLink");
        primaryStage.setScene(new Scene(
                root,
                Main.appPreferences.getDouble(WindowWidthKey, DefWidth),
                Main.appPreferences.getDouble(WindowHeightKey, DefHeight)
        ));
        primaryStage.getIcons().addAll(
                new Image("star64x64.png"),
                new Image("star32x32.png"),
                new Image("star16x16.png"),
                new Image("star48x48.png"),
                new Image("star40x40.png"),
                new Image("star24x24.png"),
                new Image("star128x128.png"),
                new Image("star256x256.png")
        );

        primaryStage.setOnShown((event -> {
            if(event.getEventType() == WindowEvent.WINDOW_SHOWN && defaultControl != null) {
                controller.setStage(primaryStage);
                controller.initCrypto(primaryStage);
                controller.onShown();
                fromFile();
                defaultControl.requestFocus();
                primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> Main.appPreferences.putDouble(WindowWidthKey, newVal.doubleValue()));
                primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> Main.appPreferences.putDouble(WindowHeightKey, newVal.doubleValue()));
                updateFilter("");
            }
        }));
        try {
            primaryStage.show();
            context.addChangeListener(aBoolean -> controller.setIsErrorLabelVisible(aBoolean));
        } catch(NullPointerException e) {
            // do nothing
        }
    }

    @Override
    public void stop() throws Exception {
        controller.onClose();
        super.stop();
    }

    public static void main(String[] args) {
        launch(
                Arrays.stream(args)
                        .filter(arg -> {
                            if (Files.exists(Path.of(arg))) {
                                dataFile.setFilename(arg);
                                return false;
                            }
                            return true;
                        })
                        .toArray(String[] ::new)
        );
    }

    public static void addLinkEntry(LinkEntry entry) {
        masterData.add(entry);
    }

    public static void deleteLinkEntry(LinkEntry entry) {
        masterData.remove(entry);
    }

    public static void updateFilter(String filter) {
        if(filter == null || filter.isEmpty() ) {
            filteredData.setPredicate((entry) -> true);
        } else {
            filteredData.setPredicate((entry) -> entry.contains(filter));
        }
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static void saveToFile() throws IOException {
        if (!context.isError()) {
            dataFile.backup().run(filename -> {
                    try {
                        masterData.toFile(filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }

    public static void saveToFile(File file) throws IOException {
        if (!context.isError()) {
            masterData.toFile(file.getPath());
        }
    }

    private static void fromFile() {
        dataFile.run(filename -> addLinksFromFile(new File(filename)));
    }

    public static void resetEncryptedPasswords() {
        masterData.forEach(linkEntry -> {
            linkEntry.reEncryptPassword();
        });
    }
    public static int addLinksFromFile(File theFile) {
        if (!theFile.exists()) {
            return 0;
        }

        final int[] count = {0};

        LinkEntry.forEach(
                theFile.getPath(),
                entry -> {
                    if (masterData.add(entry, ChooseLinkController::addPotentialDuplicate) ) {
                        count[0]++;
                    }
                }
        );

        for (var potentialDuplicate : ChooseLinkController.getPotentialDuplicates()) {
            count[0] += masterData.add(potentialDuplicate,
                    (linkEntries, linkEntries2) -> ChooseLinkController.chooseLinks(
                            defaultControl.getScene().getWindow(),
                            linkEntries,
                            linkEntries2
                    ));
        }
        ChooseLinkController.clearPotentialDuplicates();

        return count[0];
    }
}
