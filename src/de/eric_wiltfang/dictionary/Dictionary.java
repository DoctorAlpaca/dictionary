package de.eric_wiltfang.dictionary;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.EnumSet;
import java.util.Vector;

import de.eric_wiltfang.dictionary.DictionaryEvent.DictionaryEventType;
import de.eric_wiltfang.dictionary.Exporter.ExporterSettings;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.util.UUID;

public class Dictionary {
	private DictionarySettings settings;
	private Path workingDirectory;
	private Connection connection;
	
	private Vector<DictionaryListener> listeners; 

	private Dictionary() throws IOException {
		listeners = new Vector<>();
		settings = new DictionarySettings();
		try {
			workingDirectory = Files.createTempDirectory("dict");
		} catch (IOException e) {
			throw new IOException("Couldn't create temporary directory: " + e);
		}
	}
	/** 
	 *	Since temporary files are used for the Dictionary, cleanup() has to be called
	 *  once the Dictionary will not be used anymore, so that no files remain to clutter
	 *  the user's system 
     */
	public void cleanup() throws IOException {
		try {
			connection.close();

			File[] files = workingDirectory.toFile().listFiles();
			for (File f : files) {
				f.delete();
			}

			workingDirectory.toFile().delete();
		} catch(Exception e) {
			throw new IOException("Couldn't clean up: " + e);
		}
	}

	private void connectDatabase() throws IOException {
		try {
			connection = DriverManager.getConnection("jdbc:h2:" + workingDirectory + "/db", "sa", "");
		} catch (SQLException e) {
			throw new IOException("Couldn't connect to Database: " + e);
		}
	}
	private void init() throws IOException, SQLException {
		connectDatabase();
		Statement s = connection.createStatement();
		s.execute("create table entry (" +
		          "    entry_id bigint not null auto_increment," +
		          "    word varchar(255)," +
		          "    definition varchar(10000)," +
		          "    notes varchar(10000)," +
		          "    category varchar(127)," +
		          "    tags array," +
		          "    primary key (entry_id)" +
		          ");");
	}
	/**
	 * Loads dictionary files 
	 */
	private void load(File from) throws IOException {
		try {
			ZipFile zip = new ZipFile(from);
			zip.extractAll(workingDirectory.toString());
			
			InputStreamReader reader = new InputStreamReader(new FileInputStream(workingDirectory + "/settings.json"), Charset.forName("UTF-8"));
			settings.load(reader);
			
			connectDatabase();
		} catch(Exception e) {
			throw new IOException("Coulnd't load file: " + e);
		}
	}
	/**
	 * Saves dictionary files to disk
	 */
	public void save(File target) throws IOException, Exception {
		try {
			connection.close();
		} catch(Exception e) {
			throw new Exception("Coulnd't release database for saving: " + e);
		}
		if (Files.exists(target.toPath())) {
			try {
				Files.delete(target.toPath());
			} catch (IOException e) {
				throw new IOException("Coulnd't save; File " + target + " already exists and can't be deleted: " + e);
			}
		}
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(workingDirectory + "/VERSION"), Charset.forName("UTF-8"));
			writer.write("1.0");
			writer.close();
			
			writer = new OutputStreamWriter(new FileOutputStream(workingDirectory + "/settings.json"), Charset.forName("UTF-8"));
			settings.save(writer);
			writer.close();
			
			ZipFile zip = new ZipFile(target);

			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			File[] files = workingDirectory.toFile().listFiles();

			for (File f : files) {
				zip.addFile(f, parameters);
			}
		} catch (Exception e) {
			throw new IOException("Coulnd't save: " + e);
		}
		connectDatabase();
	}

	/**
	 * Creates a new dictionary 
	 */
	public static Dictionary createNew(DictionarySettings settings) throws IOException, SQLException {
		Dictionary dic = new Dictionary();
		dic.settings = settings;
		dic.init();
		return dic;
	}
	/** 
	 * Creates a dictionary with contents from a file 
	 */
	public static Dictionary createFromFile(File file) throws IOException, SQLException {
		Dictionary dic = new Dictionary();
		dic.load(file);
		return dic;
	}

	public Entry getEntry(long id) throws IllegalArgumentException, SQLException {
		return new Entry(id, connection);
	}
	public void insertEntry(Entry entry) throws SQLException {
		boolean fresh = entry.insertSelf(connection);

		DictionaryEventType evtype = fresh ? DictionaryEventType.NEW : DictionaryEventType.UPDATE;
		DictionaryEvent e = new DictionaryEvent(this, evtype, entry.getId());
		broadcast(e);
	}
	public void deleteEntry(Entry entry) throws SQLException {
		deleteEntry(entry.getId());
	}
	public void deleteEntry(long entryID) throws SQLException {
		PreparedStatement s = connection.prepareStatement("DELETE FROM entry WHERE entry_id = ?");
		s.setLong(1, entryID);
		s.execute();
		broadcast(new DictionaryEvent(this, DictionaryEventType.DELETE, entryID));
	}

	/**
	 *  Exports the dictionary via an exporter
	 */
	public void export(Exporter ex) throws SQLException, IOException {
		ex.start(settings.getName());
		
		String query = "SELECT entry_id FROM entry";
		EnumSet<ExporterSettings> expSetting = ex.getSettings();
		if (expSetting == null) {
			expSetting = EnumSet.noneOf(ExporterSettings.class);
		}
		if (expSetting.contains(ExporterSettings.ALPHABETICAL)) {
			query = query + " ORDER BY LOWER(word)";
		}
		query = query + ";";

		ResultSet res = connection.createStatement().executeQuery(query);
		while (res.next()) {
			ex.addEntry(getEntry(res.getLong("entry_id")));
		}

		ex.finish();
	}
	/**
	 * Imports entries from an Importer. 
	 * @return 
	 */
	public int importEntries(Importer im) throws IOException, SQLException {
		int num = 0;
		im.initialize();
		Entry entry;
		while ((entry = im.nextEntry()) != null) {
			entry.insertSelf(connection);
			num++;
		}
		broadcast(new DictionaryEvent(this, DictionaryEventType.OTHER, -1));
		return num;
	}
	
	public String getName() {
		return settings.getName();
	}
	public void setSettings(DictionarySettings settings) {
		this.settings = settings;
	}
	public DictionarySettings getSettings() {
		return settings;
	}
	
	/**
	 * Searches for entries that contain the specified key.
	 * @param key The key to search for.
	 * @return A vector of ids for matching entries.
	 */
	public Vector<Long> searchID(String key) throws SQLException {
		PreparedStatement s = connection.prepareStatement(
			"SELECT entry_id" +
			"    FROM entry WHERE word like '%'||?||'%' OR definition like '%'||?||'%';");
		s.setString(1,  key);
		s.setString(2,  key);
		ResultSet result = s.executeQuery();
		
		Vector<Long> ids = new Vector<Long>();
		
		while (result.next()) {
			ids.add(result.getLong("entry_id"));
		}
		
		return ids;
	}
	public Vector<Long> getAllIDs() throws SQLException {
		ResultSet result = connection.createStatement().executeQuery("SELECT entry_id FROM entry");
		
		Vector<Long> ids = new Vector<Long>();
		
		while (result.next()) {
			ids.add(result.getLong("entry_id"));
		}
		
		return ids;
	}
	public Vector<Entry> searchEntry(String key) throws SQLException {
		Vector<Long> ids = searchID(key);
		Vector<Entry> entries = new Vector<Entry>(ids.size());
		
		for (int i = 0; i < ids.size(); i++) {
			entries.add(getEntry(ids.get(i)));
		}
		
		return entries;
	}
	
	public void addDictionaryListener(DictionaryListener l) {
		listeners.add(l);
	}
	public void removeDictionaryListener(DictionaryListener l) {
		listeners.remove(l);
	}
	private void broadcast(DictionaryEvent e) {
		for (DictionaryListener l : listeners) {
			l.recieveEvent(e);
		}
	}
}