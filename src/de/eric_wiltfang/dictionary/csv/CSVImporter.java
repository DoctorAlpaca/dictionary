package de.eric_wiltfang.dictionary.csv;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import de.eric_wiltfang.dictionary.Entry;
import de.eric_wiltfang.dictionary.Importer;

public class CSVImporter implements Importer {
	public File file;
	public Charset charset;
	public CSVFormat format;
	public boolean skipFirstRow;
	public int wordCol;
	public int definitionCol;
	public int notesCol;
	public int categoryCol;
	public Vector<Integer> tagCols;
	
	private Iterator<CSVRecord> content;
	
	public CSVImporter() {
		
	}

	/**
	 * Yields up to a number of entries from the source.
	 * @param number Maximum number of entries.
	 */
	public Vector<Entry> preview(int number) throws IOException {
		Vector<Entry> entries = new Vector<>();
		initialize();
		
		for (int i = 0; i < number; i++) {
			Entry entry = nextEntry();
			if (entry == null) {
				break;
			}
			entries.add(entry);
		}
		
		return entries;
	}
	
	public void initialize() throws IOException {
		content = CSVParser.parse(file, charset, format).iterator();
		if (skipFirstRow) {
			if (content.hasNext()) {
				content.next();
			}
		}
	}
	public Entry nextEntry() {
		if (!content.hasNext()) {
			return null;
		}
		CSVRecord rec = content.next();
		
		Entry entry = new Entry();
		if (rec.size() > wordCol && wordCol != -1) {
			entry.setWord(rec.get(wordCol));
		}
		if (rec.size() > categoryCol && categoryCol != -1) {
			entry.setCategory(rec.get(categoryCol));
		}
		if (rec.size() > notesCol && notesCol != -1) {
			entry.setNotes(rec.get(notesCol));
		}
		if (rec.size() > definitionCol && definitionCol != -1) {
			entry.setDefinition(rec.get(definitionCol));
		}
		
		Vector<String> tags = new Vector<>(); 
		for (int i : tagCols) {
			if (rec.size() > i) {
				tags.add(rec.get(i));
			}
		}
		entry.setTags(tags);
		
		// Don't import empty entries
		if (entry.isEmpty()) {
			return nextEntry();
		}
		
		return entry;
	}
}
