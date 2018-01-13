package com.srdubya.QuickLink;

import com.google.gson.stream.JsonReader;
import com.srdubya.QuickLink.Crypto.Crypto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.FileReader;

public class LinkEntry {

    public void copyFrom(LinkEntry link) {
        setName(link.getName());
        setLogin(link.getLogin());
        setEmail(link.getEmail());
        setApp(link.getApp());
        setPath(link.getPath());
        setPassword(link.getPassword());
        setClose(link.isClose());
    }

    private static class LinkEntryBuilder {

        String name;
        String login;
        String email;
        String app;
        String path;
        String password;
        boolean close;

        LinkEntryBuilder() {
        }

        LinkEntry create() {
            LinkEntry ret = new LinkEntry();

            ret.setName(name);
            ret.setLogin(login);
            ret.setEmail(email);
            ret.setApp(app);
            ret.setPath(path);
            ret.setPassword(password);
            ret.setClose(close);

            return ret;
        }

        boolean isReady() {
            return path != null;
        }

        void set(JsonReader reader) {
            try {
                name = "";
                login = "";
                email = "";
                app = "";
                path = null;
                password = "";
                close = false;
                while (reader.hasNext()) {
                    String fieldName = reader.nextName();
                    switch (fieldName) {
                        case "name":
                            name = reader.nextString();
                            break;
                        case "login":
                            login = reader.nextString();
                            break;
                        case "email":
                            email = reader.nextString();
                            break;
                        case "app":
                            app = reader.nextString();
                            break;
                        case "path":
                            path = reader.nextString();
                            break;
                        case "encryptedPassword":
                            password = Crypto.Decrypt(reader.nextString());
                            break;
                        case "close":
                            close = reader.nextBoolean();
                            break;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty login = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    private final SimpleStringProperty app = new SimpleStringProperty();
    private final SimpleStringProperty path = new SimpleStringProperty();
    private transient SimpleStringProperty password = null;
    private final SimpleBooleanProperty close = new SimpleBooleanProperty();
    private String encryptedPassword = null;

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getLogin() {
        return login.get();
    }

    public SimpleStringProperty loginProperty() {
        return login;
    }

    public void setLogin(String login) {
        this.login.set(login);
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getApp() {
        return app.get();
    }

    public SimpleStringProperty appProperty() {
        return app;
    }

    public void setApp(String app) {
        this.app.set(app);
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    private void initializePassword() {
        if(password == null) {
            password = new SimpleStringProperty();
            password.set(encryptedPassword == null ? "" : Crypto.Decrypt(encryptedPassword));
        }
    }

    public String getPassword() {
        initializePassword();
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        initializePassword();
        return password;
    }

    public void setPassword(String password) {
        initializePassword();
        this.password.set(password);
        this.encryptedPassword = Crypto.Encrypt(password);
    }

    public boolean isClose() {
        return close.get();
    }

    public SimpleBooleanProperty closeProperty() {
        return close;
    }

    public void setClose(boolean close) {
        this.close.set(close);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LinkEntry) {
            LinkEntry that = (LinkEntry) obj;
            if(this.name.getValue().equals(that.name.getValue())) {
                if (this.login.getValue().equals(that.login.getValue())) {
                    if (this.email.getValue().equals(that.email.getValue())) {
                        if (this.app.getValue().equals(that.app.getValue())) {
                            if (this.path.getValue().equals(that.path.getValue())) {
                                if (this.password.getValue().equals(that.password.getValue())) {
                                    return this.close.getValue().equals(that.close.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean contains(String filter) {
        String f = filter.toLowerCase();
        return name.getValue().toLowerCase().contains(f)
                || path.getValue().toLowerCase().contains(f);
    }

    public interface LinkEntryHandler {
        void add(LinkEntry entry);
    }

    public static void forEach(String filename, LinkEntryHandler adder) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filename));
            LinkEntryBuilder builder = new LinkEntryBuilder();
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                builder.set(reader);
                if (builder.isReady()) {
                    adder.add(builder.create());
                }
                reader.endObject();
            }
            reader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
