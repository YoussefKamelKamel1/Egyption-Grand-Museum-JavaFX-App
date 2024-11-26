package com.example.grandmusuemclient;

import java.util.ResourceBundle;

public class ResourceManager {
    private static ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle_en"); // Default to English

    public static void setLanguage(String language) {
        switch (language) {
            case "Arabic":
                bundle = ResourceBundle.getBundle("MessagesBundle_ar");
                break;
            case "Spanish":
                bundle = ResourceBundle.getBundle("MessagesBundle_es");
                break;
            case "French":
                bundle = ResourceBundle.getBundle("MessagesBundle_fr");
                break;
            case "German":
                bundle = ResourceBundle.getBundle("MessagesBundle_de");
                break;
            default:
                bundle = ResourceBundle.getBundle("MessagesBundle_en");
                break;
        }
    }

    public static String getString(String key) {
        return bundle.getString(key);
    }
}
