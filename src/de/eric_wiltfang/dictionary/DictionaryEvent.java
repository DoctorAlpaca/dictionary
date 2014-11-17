package de.eric_wiltfang.dictionary;

import java.util.EventObject;

/**
 * An Event that gets send when the Dictionary is modified.
 */
public class DictionaryEvent extends EventObject {
	public enum DictionaryEventType {
		OTHER,
		NEW,
		UPDATE,
		DELETE
	}

	private static final long serialVersionUID = -3121911907465948134L;
	private DictionaryEventType type;
	private long id;

	public DictionaryEvent(Object source, DictionaryEventType type, long targetID) {
		super(source);
		this.type = type;
		id = targetID;
	}
	
	/**
	 * @return What happened to the Entry that was modified;
	 */
	public DictionaryEventType getType() {
		return type;
	}
	/**
	 * @return The id of the Entry that was modified.
	 */
	public long getTargetID() {
		return id;
	}
}
