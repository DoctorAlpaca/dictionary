package de.eric_wiltfang.dictionary;
import java.io.IOException;
import java.util.EnumSet;

/* Exports the dictionary to some other format */
public interface Exporter {
	public enum ExporterSettings {
		ALPHABETICAL
	}
	
	/**
	 *  Allows the format to write out a header
	 *  @param langName The name of the language. 
	 */
	public void start(String langName) throws IOException;
	/**
	 *  Called once for each dictionary entry, in alphabetical order
	 *  @param entry The entry to be added. 
	 */
	public void addEntry(Entry entry) throws IOException;
	/**
	 *  Allows the exporter to clean up 
	 */
	public void finish() throws IOException;
	/**
	 *  Specifies how the exporter wants to be treated.
	 *  @return The flags the Exporter needs to function, or null if none are needed. 
	 */
	public EnumSet<ExporterSettings> getSettings();
}