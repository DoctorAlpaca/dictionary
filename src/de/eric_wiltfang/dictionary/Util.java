package de.eric_wiltfang.dictionary;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Utility functions.
 * 
 * @author Christian LaCourt
 */
public final class Util {
  /**
   * Resources on the classpath.
   */
  private static String LOCAL_PATH = null;
  private static final HashMap<String, Locale> locales = new HashMap<>();
  private static final List<Image> IMAGES = new ArrayList<>();
  
  static {
    /*
     * Load Locales List.
     */
    Locale temp = Locale.forLanguageTag("de");
    locales.put(temp.getDisplayLanguage() + " (de)", temp);
    temp = Locale.forLanguageTag("en");
    locales.put(temp.getDisplayLanguage() + " (en)", temp);
    temp = null;
    File localFolder = new File(getResourcePath());
    final Pattern filterPattern = Pattern.compile("dictionary_(\\S*?)\\.properties");
    FilenameFilter filter = new FilenameFilter(){
      public boolean accept(File dir, String name){
        return filterPattern.matcher(name).matches();
      }
    };
    for(File file: localFolder.listFiles(filter)){
      Matcher m = filterPattern.matcher(file.getName());
      if(m.find()){
        temp = Locale.forLanguageTag(m.group(1).replaceAll("_", "-"));
        locales.put(temp.getDisplayLanguage() + " (" + temp.toLanguageTag() + ")", temp);
      }
    }
    /*
     * Load images.
     */
    try {
      BufferedImage icon = ImageIO.read(ClassLoader.getSystemResourceAsStream("icon.png"));
      IMAGES.add(icon);
      for(int size = 128; size >= 16; size /= 2){
        IMAGES.add(icon.getScaledInstance(size, size, BufferedImage.SCALE_SMOOTH));
      }
    } catch(Exception e){
      // Do nothing. No images.
    }
  }
  
  /**
   * Gets a custom resource bundle from the filesystem.
   * 
   * @param locale The name of the locale to load.
   * @return The loaded resource bundle.
   * @throws IOException If the bundle doesn't exist.
   */
  public static PropertyResourceBundle getCustomResourceBundle(String locale) throws IOException{
    try(FileInputStream in = new FileInputStream(getResourcePath() + "dictionary_" + locale.replaceAll("-", "_") + ".properties")){
      return new PropertyResourceBundle(in);
    }
  }
  
  /**
   * Gets the local file path for settings and custom resources.
   */
  public static String getResourcePath(){
    if(LOCAL_PATH == null)
      LOCAL_PATH = System.getProperty("user.home") + "/.dictionary/";
    return LOCAL_PATH;
  }
  
  /**
   * Loads the settings in from the filesystem, creating them if they don't exist.
   */
  public static Properties loadSettings(){
    Properties defaultProperties = new Properties();
    Properties properties = new Properties();
    try(InputStream in = ClassLoader.getSystemResourceAsStream("settings.properties")){
      defaultProperties.load(in);
    }catch(IOException e){
      DictionaryMainWindow.getInstance().die("Failed to load default settings: " + e.getMessage());
    }
    File localSettingsFile = new File(getResourcePath() + "settings.properties");
    try{
      if(!Files.exists(localSettingsFile.toPath())){
        DictionaryMainWindow.getLogger().fine("Creating local settings directory.");
        createLocalFS(defaultProperties);
      }
    }catch(IOException e){
      DictionaryMainWindow.getLogger().severe("Could not create local settings directory.");
      return defaultProperties;
    }
    try(FileInputStream in = new FileInputStream(localSettingsFile)){
      properties.load(in);
    }catch(IOException e){
      DictionaryMainWindow.getLogger().warning("Error while loading local settings file.");
      return defaultProperties;
    }
    return properties;
  }
  
  public static String[] getLanguages(){
    String[] out = locales.keySet().toArray(new String[0]);
    Arrays.sort(out);
    return out;
  }
  
  public static Locale getLocale(String text){
    return locales.getOrDefault(text, Locale.ENGLISH);
  }
  
  public static List<Image> getImages(){
    return IMAGES;
  }
  
  /**
   * Creates the local folder for settings and custom resources. Also saves the default properties if properties isn't null.
   * 
   * @param properties Properties to save if not null.
   */
  private static void createLocalFS(Properties properties) throws IOException{
    Path path = new File(getResourcePath()).toPath();
    if(!Files.isDirectory(path))
      Files.createDirectory(path);
    path = new File(getResourcePath() + "settings.properties").toPath();
    if(!Files.isRegularFile(path)){
      BufferedWriter out = Files.newBufferedWriter(path);
      properties.store(out, null);
      out.flush();
      out.close();
    }
  }
  
  public static boolean saveSettings(Properties settings){
    Path localSettingsFile = new File(getResourcePath() + "settings.properties").toPath();
    try{
      if(!Files.exists(localSettingsFile)){
        createLocalFS(settings);
      } else {
        BufferedWriter out = Files.newBufferedWriter(localSettingsFile);
        settings.store(out, null);
        out.flush();
        out.close();
      }
    }catch(IOException e){
      return false;
    }
    return true;
  }
}
