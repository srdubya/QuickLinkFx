package com.srdubya.QuickLink.Crypto;

import javafx.scene.control.Alert;
import org.jasypt.util.text.StrongTextEncryptor;

import javax.crypto.Cipher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Crypto {

    private static final String INIT_REQD = "You must first initialize the Crypto class";

    private static final String basePassword;
    private static Crypto instance;

    private StrongTextEncryptor textEncryptor;

    static {
        basePassword = getUserID();
        instance = null;
    }

    private static String getUserID() {
        try {
            final String os = System.getProperty("os.name").toLowerCase();
            String className = null;
            List<String> methodNames = new ArrayList<>();
            if (os.contains("windows")) {
                className = "com.sun.security.auth.module.NTSystem";
                methodNames.add("getUserSID");
                methodNames.add("getDomainSID");
            } else if (os.contains("linux") || os.contains("mac os")) {
                className = "com.sun.security.auth.module.UnixSystem";
                methodNames.add("getUid");
                methodNames.add("getGid");
            } else if (os.contains("solaris") || os.contains("sunos")) {
                className = "com.sun.security.auth.module.SolarisSystem";
                methodNames.add("getUid");
                methodNames.add("getGid");
            }
            if (className != null) {
                StringBuilder sb = new StringBuilder();
                Class<?> c = Class.forName(className);
                for(String methodName : methodNames) {
                    Method method = c.getDeclaredMethod(methodName);
                    Object o = c.newInstance();
                    sb.append(method.invoke(o).toString());
                }
                return sb.toString();
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Integer.toString(System.getProperty("user.home").hashCode()) + getMacAddress();
    }

    private static String getMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return "unreal-MAC-address";
    }

    public static void initialize(String password) {
        if(instance == null) {
            synchronized (basePassword) {
                if(instance == null) {
                    instance = new Crypto(password);
                }
            }
        }
    }

    public static String decryptPreference(String encryptedText) {
        StrongTextEncryptor textEncryptor = buildTextEncryptor(basePassword);
        return textEncryptor.decrypt(encryptedText);
    }

    public static String encryptPreference(String clearText) {
        StrongTextEncryptor textEncryptor = buildTextEncryptor(basePassword);
        return textEncryptor.encrypt(clearText);
    }

    public static String Decrypt(String encryptedText) throws UnsupportedOperationException {
        if(instance == null) {
            throw new UnsupportedOperationException(INIT_REQD);
        }
        return instance.textEncryptor.decrypt(encryptedText);
    }

    public static String Encrypt(String clearText) throws UnsupportedOperationException {
        if(instance == null) {
            throw new UnsupportedOperationException(INIT_REQD);
        }
        return instance.textEncryptor.encrypt(clearText);
    }

    private static StrongTextEncryptor buildTextEncryptor(String password) {
        StrongTextEncryptor tmp = new StrongTextEncryptor();
        tmp.setPassword(password);
        return tmp;
    }

    private Crypto(String password) {
        textEncryptor = buildTextEncryptor(password);
    }

    public static boolean isSystemReady() {
        try {
            int len = Cipher.getMaxAllowedKeyLength("AES");
            if(len >= 256) {
                return true;
            }
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setContentText("Strong encryption has not been installed.  Search the internet for 'Java JCE' to get the missing JRE policy.");
            dialog.showAndWait();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
}
