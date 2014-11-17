package de.eric_wiltfang.dictionary;

import java.io.Reader;
import java.io.Writer;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class DictionarySettings {
	private String name;
	
	public DictionarySettings() {
		name = "";
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Loads dictionary settings from a valid JSON representation.
	 * @param from The stream to load from.
	 * @throws JSONException
	 */
	public void load(Reader from) throws JSONException {
		JSONTokener tokener = new JSONTokener(from);
		JSONObject settingsJSON = new JSONObject(tokener);
		setName(settingsJSON.getString("langName"));
	}
	/**
	 * Saves dictionary settings in JSON format.
	 * @param to The reader to stream the JSON to.
	 * @throws JSONException
	 */
	public void save(Writer to) throws JSONException {
		JSONObject settingsJSON = new JSONObject();
		settingsJSON.put("langName", getName());
		settingsJSON.write(to);
	}
}
