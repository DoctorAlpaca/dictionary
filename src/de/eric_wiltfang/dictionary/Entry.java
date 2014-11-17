package de.eric_wiltfang.dictionary;
import java.sql.*;
import java.util.Vector;

public class Entry {
	private long id;
	private String word;
	private String definition;
	private String notes;
	private String category;
	private Vector<String> tags;

	public Entry() {
		id = 0;
		word = "";
		definition = "";
		notes = "";
		category = "";
		tags = new Vector<String>();
	}
	public Entry(long id, Connection connection) throws IllegalArgumentException, SQLException {
		PreparedStatement s = connection.prepareStatement(
			"SELECT word, definition, notes, category, tags" +
			"    FROM entry WHERE entry_id = ?;");
		s.setLong(1, id);
		ResultSet result = s.executeQuery();
		if (!result.next()) {
			throw new IllegalArgumentException("Invalid entry id: " + id);
		}
		this.id = id;
		word = result.getString("word");
		definition = result.getString("definition");
		notes = result.getString("notes");
		category = result.getString("category");
		ResultSet tagResult = result.getArray("tags").getResultSet();
		tags = new Vector<String>();
		while (tagResult.next()) {
			tags.add(tagResult.getString(2));
		}
	}
	/**
	 * Inserts the Entry into an database.
	 * @param connection The connection to use
	 * @return true if the entry was freshly inserted, false if it overwrote an older entry.
	 * @throws SQLException
	 */
	public boolean insertSelf(Connection connection) throws SQLException {
		if (id <= 0) {
			PreparedStatement s = connection.prepareStatement(
				"INSERT INTO entry (word, definition, notes, category, tags) " +
				"VALUES (?, ?, ?, ?, ?);",
				Statement.RETURN_GENERATED_KEYS);
			s.setString(1, word);
			s.setString(2, definition);
			s.setString(3, notes);
			s.setString(4, category);
			s.setObject(5, (Object[]) tags.toArray());
			s.execute();

			ResultSet keys = s.getGeneratedKeys();
			keys.next();
			id = keys.getLong(1);
			return true;
		} else {
			PreparedStatement s = connection.prepareStatement(
				"UPDATE entry " +
				"SET word = ?, " +
				"    definition = ?, " +
				"    notes = ?, " +
				"    category = ?, " +
				"    tags = ? " +
				"WHERE entry_id = ? ");
			s.setString(1, word);
			s.setString(2, definition);
			s.setString(3, notes);
			s.setString(4, category);
			s.setObject(5, (Object[]) tags.toArray());
			s.setLong(6, id);
			s.execute();
			return false;
		}
	}

	public long getId() {
	    return this.id;
	}

	public String getWord() {
	    return this.word;
	}
	public void setWord(String word) {
	    this.word = word;
	}

	public String getDefinition() {
	    return this.definition;
	}
	public void setDefinition(String definition) {
	    this.definition = definition;
	}

	public String getNotes() {
	    return this.notes;
	}
	public void setNotes(String notes) {
	    this.notes = notes;
	}

	public String getCategory() {
	    return this.category;
	}
	public void setCategory(String category) {
	    this.category = category;
	}

	public Vector<String> getTags() {
		return this.tags;
	}
	public void setTags(Vector<String> tags) {
		for (int i = 0; i < tags.size(); i++) {
			tags.set(i, tags.get(i).trim());
			if (tags.get(i).isEmpty()) {
				tags.remove(i);
				i--;
			}
		}
		this.tags = tags;
	}
	public String getTagsAsString() {
		if (tags.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tags.size() - 1; i++) {
				String tag = tags.get(i);
				sb.append(tag);
				sb.append(", ");
			}
			sb.append(tags.lastElement());
			return sb.toString();
		} else {
			return "";
		}
	}
	
	public String toString() {
		return "Entry:{Word:" + word + ";Definition:" + definition + "}";
	}
	
	/**
	 * Checks whether the entry contains any information.
	 * @return true if the entry contains no information.
	 */
	public boolean isEmpty() {
		return word.isEmpty() && definition.isEmpty() && notes.isEmpty() && category.isEmpty() && tags.isEmpty();
	}
}