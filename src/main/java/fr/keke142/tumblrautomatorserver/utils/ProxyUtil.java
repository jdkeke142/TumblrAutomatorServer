package fr.keke142.tumblrautomatorserver.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyUtil {

    public static void setProxy(String ip, String port, String user, String password) {
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyHost", ip);
        System.setProperty("http.proxyPort", port);
        System.setProperty("http.proxyUser", user);
        System.setProperty("http.proxyPassword", password);

        System.setProperty("https.proxyHost", ip);
        System.setProperty("https.proxyPort", port);
        System.setProperty("https.proxyUser", user);
        System.setProperty("https.proxyPassword", password);

        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }
}
