package de.eric_wiltfang.dictionary.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.EnumSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import de.eric_wiltfang.dictionary.Entry;
import de.eric_wiltfang.dictionary.Exporter;

public class CSVExporter implements Exporter {
	private CSVPrinter exporter;
	public boolean printHeader;
	
	public CSVExporter(File target) throws IOException {
		Appendable a = new OutputStreamWriter(new FileOutputStream(target), Charset.forName("UTF-8"));
		exporter = new CSVPrinter(a, CSVFormat.DEFAULT);
	}

	@Override
	public void start(String langName) throws IOException {
		if (printHeader) {
			exporter.printRecord("Word", "Definition", "Category", "Tags", "Notes");
		}
	}

	@Override
	public void addEntry(Entry entry) throws IOException {
		exporter.printRecord(entry.getWord(), entry.getDefinition(),
				entry.getCategory(), entry.getTagsAsString(), entry.getNotes());
	}

	public void finish() throws IOException {
		exporter.flush();
		exporter.close();
	}

	public EnumSet<ExporterSettings> getSettings() {
		return null;
	}
}
