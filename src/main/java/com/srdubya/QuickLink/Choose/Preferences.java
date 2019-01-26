package com.srdubya.QuickLink.Choose;

import com.srdubya.QuickLink.Main;

public class Preferences {

    private final static String COLUMN_WIDTH_FORMAT = "Choose.%s.columnwidth";
    private final static String DIALOG_WIDTH = "Choose.dialogwidth";
    private final static String DIALOG_HEIGHT = "Choose.dialogheight";
    private final static String PASSWORD_COLUMN_VISIBLE = "Choose.pwcolumnvisible";

    static int getColumnWidth(String columnName) {
        return Main.appPreferences.getInt(String.format(COLUMN_WIDTH_FORMAT, columnName), 100);
    }

    static void putColumnWidth(String columnName, int width) {
        Main.appPreferences.putInt(String.format(COLUMN_WIDTH_FORMAT, columnName), width);
    }

    static int getWidth() {
        return Main.appPreferences.getInt(DIALOG_WIDTH, 600);
    }

    static void putWidth(int width) {
        Main.appPreferences.putInt(DIALOG_WIDTH, width);
    }

    static int getHeight() {
        return Main.appPreferences.getInt(DIALOG_HEIGHT, 400);
    }

    static void putHeight(int height) {
        Main.appPreferences.putInt(DIALOG_HEIGHT, height);
    }

    static boolean getPasswordColumnVisible() {
        return Main.appPreferences.getBoolean(PASSWORD_COLUMN_VISIBLE, false);
    }

    static void setPasswordColumnVisible(boolean visible) {
        Main.appPreferences.putBoolean(PASSWORD_COLUMN_VISIBLE, visible);
    }
}
