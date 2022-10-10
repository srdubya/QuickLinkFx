module QuickLinkFx {
    requires java.desktop;
    requires java.prefs;
    requires java.sql;
//    requires fx.gson;
    requires com.google.gson;
    requires jasypt;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
//    requires org.bouncycastle.pkix;
//    requires org.bouncycastle.provider;
    requires org.hildan.fxgson;
    exports com.srdubya.QuickLink;
    exports com.srdubya.QuickLink.Add;
    exports com.srdubya.QuickLink.Choose;
    exports com.srdubya.QuickLink.ConfirmDelete;
    exports com.srdubya.QuickLink.Crypto;
    exports com.srdubya.QuickLink.Export;
    exports com.srdubya.QuickLink.Import;
    opens com.srdubya.QuickLink;
    opens com.srdubya.QuickLink.Add;
    opens com.srdubya.QuickLink.Choose;
    opens com.srdubya.QuickLink.ConfirmDelete;
    opens com.srdubya.QuickLink.Crypto;
    opens com.srdubya.QuickLink.Export;
    opens com.srdubya.QuickLink.Import;
}