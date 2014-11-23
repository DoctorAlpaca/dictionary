package de.eric_wiltfang.dictionary.local;

import de.eric_wiltfang.dictionary.Dictionary;
import de.eric_wiltfang.dictionary.DictionaryMainWindow;

import javax.swing.JFrame;
import java.awt.*;
import java.io.File;

/**
 * @author cofl
 *
 */
public class LocalizationHelper {
    public static String[] argsHolder;
    public static File dict = null;
    public static Dictionary dic = null;
    public static Point location = null;

    public static void restart(JFrame window){
        location = window.getLocation();
        window.dispose();
        DictionaryMainWindow.main(argsHolder);
    }
}
