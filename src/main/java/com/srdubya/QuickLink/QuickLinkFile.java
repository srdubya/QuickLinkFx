package com.srdubya.QuickLink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class QuickLinkFile {

    private static QuickLinkFile self;
    private String filename;

    private QuickLinkFile(String filename) {
        this.filename = filename;
        if (self != null) {
            throw new IllegalStateException("Singleton is already initialized");
        }
        self = this;
    }

    public QuickLinkFile() {
        this(null);
    }

    private static String getHomeFolder() {
        return System.getProperty("user.home");
    }

    private static String getBackupFolder() throws IOException {
        var backupFolder = Path.of(getHomeFolder(), ".quicklink");
        if (!Files.isDirectory(backupFolder)) {
            Files.createDirectory(backupFolder);
        }
        return backupFolder.toString();
    }

    public void setFilename(String s) {
        filename = s;
    }

    public String getFilename() {
        if (filename == null) {
            filename = Path.of(getHomeFolder(), ".quicklink.json").toString();
        }

        return filename;
    }

    public QuickLinkFile backup() throws IOException {
        var backupFilename = Path.of(
                getBackupFolder()
                ,Path.of(getFilename()).getFileName().toString() + "." + getTimestamp()
        );
        var backupFile = backupFilename.toFile();
        var file = new File(getFilename());
        if (file.exists()) {
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return this;
    }

    public QuickLinkFile run(Consumer<String> consumer) {
        consumer.accept(getFilename());
        return this;
    }

    private Object getTimestamp() {
        var now = LocalDateTime.now();
        return String.format(
                "%4d.%02d.%02d.%02d.%02d.%02d.%03d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond(),
                now.getNano() / (1000 * 1000)   // milliseconds
                );
    }
}
