package de.eric_wiltfang.dictionary;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * @author cofl
 */
public class Util {
    private static class Settings extends Properties {
        public Settings() throws IOException {
            super();
            if(!new File(getPath("settings.properties")).exists()){
                setProperty("useLast","false");
                setProperty("language", fromLocale(Locale.getDefault()));
                setProperty("lastFile","");
                save();
            }
            load(new InputStreamReader(new FileInputStream(getPath("settings.properties"))));
            if(!containsKey("useLast")) setProperty("useLast","true");
            if(!containsKey("lastFile")) setProperty("lastFile","");
            if(!containsKey("language")) setProperty("language",fromLocale(Locale.getDefault()));
        }

        public void save() throws IOException {
            store(new OutputStreamWriter(new FileOutputStream(getPath("settings.properties")),"UTF-8"), "EWDictionary Settings");
        }

        public void reload() throws IOException {
            load(new InputStreamReader(new FileInputStream(getPath("settings.properties")), "UTF-8"));
        }
    }

    private static Settings preferences;
    private static ResourceBundle bundle;
    private static Locale local;
    private static String basePath;

    public static void initialize(){
        try {
            boolean isCustom = false;
            String OS = System.getProperty("os.name").toLowerCase();
            basePath = (OS.contains("win")?System.getenv("AppData"):System.getProperty("user.home"))
                    + (OS.contains("mac")?"/Library/Application Support":"") + "/.ewdictionary";
            File check = new File(getPath("/"));
            if(!check.exists()) if(!check.mkdir()) throw new IOException("Failed to create primary directory at: "+check.getAbsolutePath());
            check = new File(getPath("/assets/"));
            if(!check.exists()) if(!check.mkdir()) throw new IOException("Failed to create assets directory at: "+check.getAbsolutePath());

            preferences = new Settings();

            local = toLocale(getProperty("language"));

            File[] files = new File(getPath("/assets/resources/")).listFiles(new FileFilter() {@Override public boolean accept(File file){
                return file.getName().endsWith(".properties") && file.getName().startsWith("dictionary_");
            }});
            if(null != files) for(File f: files) if(f.getName().equals("dictionary_"+getProperty("language")+".properties")) isCustom = true;

            //Yes this works
            try {
                try {
                    if(isCustom)
                        throw new Exception("this is a convenience");
                    bundle = new PropertyResourceBundle(new FileInputStream(getPath("/assets/dictionary_" + getProperty("language") + ".properties")));
                } catch(Exception e){
                    bundle = ResourceBundle.getBundle("dictionary", local);
                }
            }  catch (MissingResourceException ex) {
                JOptionPane.showMessageDialog(null, "Fatal error", "Error while initializing: " + ex.getMessage(), JOptionPane.OK_OPTION);
                System.exit(-1);
            }
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Returns the path within the .ewdictionary directory
     * @param relative
     * @return
     */
    public static String getPath(String relative){
        if(relative.startsWith("/")) return basePath + relative;
        return basePath + "/" + relative;
    }

    /**
     * For settings
     * @param key
     * @return
     */
    public static String getProperty(String key){
        return preferences.getProperty(key);
    }

    public static String getProperty(String key, String def){
        if(!preferences.containsKey(key)) return def;
        return getProperty(key);
    }

    /**
     * For settings
     * @param key
     * @return
     */
    public static boolean getBoolean(String key){
        if(preferences.getProperty(key).equalsIgnoreCase("true")) return true;
        return false;
    }

    public static boolean getBoolean(String key, boolean def){
        if(!preferences.containsKey(key)) return def;
        return getBoolean(key);
    }

    /**
     * For settings
     * @param key
     * @param value
     */
    public static void setProperty(String key, String value){
        preferences.setProperty(key, value);
        try {
            preferences.save();
        } catch(Exception e){
            try {
                preferences.save();
            } catch(Exception e1){
                //wellp...
            }
        }
    }

    /**
     * For settings
     * @param key
     * @param bool
     */
    public static void setProperty(String key, boolean bool){
        preferences.setProperty(key,bool?"true":"false");
        try {
            preferences.save();
        } catch(Exception e){
            try {
                preferences.save();
            } catch(Exception e1){
                //wellp...
            }
        }
    }

    /**
     * For localization
     * @param key
     * @return the localized key
     */
    public static String get(String key){
        return bundle.getString(key);
    }

    /**
     * Gets a string for storing from a Locale
     * @param locale
     * @return
     */
    public static String fromLocale(Locale locale){
        return locale.toLanguageTag();
    }

    /**
     * Gets a locale from a string generated by fromLocale
     * @param property
     * @return
     */
    public static Locale toLocale(String property){
        Locale local = Locale.forLanguageTag(property);
        if(null == local) return Locale.ENGLISH;
        return local;
    }
}
