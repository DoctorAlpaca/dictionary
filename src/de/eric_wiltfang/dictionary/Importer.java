package de.eric_wiltfang.dictionary;

import java.io.IOException;

public interface Importer {
	/**
	 * Called once before the importing process starts.
	 */
	public void initialize() throws IOException;
	/**
	 * Delivers the next entry.
	 * @return An entry.
	 */
	public Entry nextEntry() throws IOException;
}
