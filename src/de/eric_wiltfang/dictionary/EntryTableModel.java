package de.eric_wiltfang.dictionary;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.SQLException;
import java.util.Vector;


public class EntryTableModel implements TableModel, DictionaryListener {
	private Dictionary dic;
	private Vector<TableModelListener> listeners;
	private Vector<Long> ids;
	private String key;
	
	public EntryTableModel() {
		listeners = new Vector<>();
		ids = new Vector<>();
		key = "";
	}
	
	public void setDictionary(Dictionary newDic) throws SQLException {
		if (dic != null) {
			dic.removeDictionaryListener(this);
		}
		if (newDic == null) {
			ids.clear();
			dic = null;
		} else {
			dic = newDic;
			dic.addDictionaryListener(this);
			update();
		}
	}
	public void searchFor(String key) throws SQLException {
		if (dic != null) {
			this.key = key;
			if (key.isEmpty()) {
				ids = dic.getAllIDs();
			} else {
				ids = dic.searchID(key);
			}
			for (TableModelListener l : listeners) {
				l.tableChanged(new TableModelEvent(this));
			}
		}
	}
	public void update() throws SQLException {
		searchFor(key);
	}
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case 0: 
		case 1:
		case 2:
		case 3: return String.class;
		default:
			return null;
		}
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0: return Util.get("wordColumnName");
		case 1: return Util.get("definitionColumnName");
		case 2: return Util.get("categoryColumnName");
		case 3: return Util.get("notesColumnName");
		default:
			return null;
		}
	}

	@Override
	public int getRowCount() {
		return ids.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Entry entry;
		try {
			entry = dic.getEntry(ids.get(row));
		} catch (Exception ex) {
			return Util.get("error") + " " + ex;
		}
		switch(col) {
		case 0:
			return escapeString(entry.getWord());
		case 1:
			return escapeString(entry.getDefinition());
		case 2:
			return escapeString(entry.getCategory());
		case 3:
			return escapeString(entry.getTagsAsString());
		default:
			return null;
		}
	}
	
	/**
	 * Escapes the given string so it can be displayed in the table without surprises.
	 * @param s The string to escape
	 * @return The escaped string
	 */
	private String escapeString(String s) {
		return "<html><body>" + StringEscapeUtils.escapeHtml4(s) + "</body></hmtl>";
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		
	}

	@Override
	public void recieveEvent(DictionaryEvent event) {
		try {
			update();
		} catch (Exception e) {
			// I too like to live dangerous.
		}
	}
	
	public long getID(int row) {
		return ids.get(row);
	}
}
