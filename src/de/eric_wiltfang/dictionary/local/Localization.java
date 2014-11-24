package de.eric_wiltfang.dictionary.local;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

public class Localization {
	/**
	 * This bundle contains all Strings in the users default language 
	 */
	private ResourceBundle bundle;
	/**
	 * This bundle contains all the String in English, in case a String is missing in the translation 
	 */
	private ResourceBundle fallbackBundle;
	private static Localization instance;
	
	public static Localization getInstance() {
		if (instance == null) {
			instance = new Localization();
		}
		return instance;
	}
	
	private Localization() {
		try {
			bundle = ResourceBundle.getBundle("dictionary");
			fallbackBundle = ResourceBundle.getBundle("dictionary", Locale.ENGLISH);
		} catch (MissingResourceException ex) {
			JOptionPane.showMessageDialog(null, "Fatal error", "Error while initializing: " + ex.getMessage(), JOptionPane.OK_OPTION);
			System.exit(-1);
		}
	}
	
	public String get(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ex) {
			try {
				return fallbackBundle.getString(key);
			} catch (MissingResourceException ex2) {
				// We're really in trouble now...
				return "Missing String for \"" + key + "\"";
			}
		}
	}
}
