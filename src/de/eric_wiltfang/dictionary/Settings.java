package de.eric_wiltfang.dictionary;

import java.util.prefs.Preferences;

public class Settings {
	private Preferences pref;
	private static Settings instance;
	
	public static Preferences getPreferences() {
		return getInstance().pref;
	}
	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}
    private Settings() {
    	pref = Preferences.userRoot().node("/de/eric_wiltfang/dictionary");
    }
}
