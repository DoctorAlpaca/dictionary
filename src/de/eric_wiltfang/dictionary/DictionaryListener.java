package de.eric_wiltfang.dictionary;

import java.util.EventListener;

public interface DictionaryListener extends EventListener {
	public void recieveEvent(DictionaryEvent event);
}
