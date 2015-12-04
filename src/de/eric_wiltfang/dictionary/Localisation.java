package de.eric_wiltfang.dictionary;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Localisation.
 * 
 * @author Christian LaCourt
 */
public class Localisation {
  /**
   * Read JavaDocs. Automatically falls back.
   */
  private static ResourceBundle strings;
  private final static String BUNDLE_NAME = "dictionary";
  private Locale locale;
  
  Localisation(Properties settings){
    if(settings.containsKey("locale")){
      locale = Locale.forLanguageTag(settings.getProperty("locale"));
    }else{
      locale = Locale.getDefault();
    }
    try{
      strings = Util.getCustomResourceBundle(settings.getProperty("locale"));
    }catch(MissingResourceException | IOException e){
      try{
        strings = ResourceBundle.getBundle(BUNDLE_NAME, locale);
      }catch(Exception e2){
        die(e2);
      }
    }
  }
  
  public String get(String key){
    try{
      return strings.getString(key);
    }catch(MissingResourceException e){
      DictionaryMainWindow.getLogger().warning("Failed to find localisation key: " + key);
      return "???";
    }
  }
  
  private static void die(Exception e){
    DictionaryMainWindow.getInstance().die("Failed to load language file or fallbacks: " + e.getMessage());
  }
  
  public Locale getLocale(){
    return strings.getLocale();
  }
}
