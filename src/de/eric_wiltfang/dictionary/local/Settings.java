package de.eric_wiltfang.dictionary.local;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author cofl
 *
 */
public class Settings extends HashMap<String, String>{
    public Settings(){
        try {
            File file = new File(Localization.d + "settings.ini");
            if(!file.exists()){
                FileWriter w = new FileWriter(file.getAbsolutePath());
                w.write("#EWDict Global Settings");
                w.close();
            }
            read();
        } catch(Exception e){
            // I, too, like to live dangerously.
        }
    }
    public void read() throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(Localization.d + "settings.ini"));
        String k, l;
        while(null != (l = r.readLine())){
            l = l.trim();
            if(l.equals("") || l.startsWith("#") || !l.contains("=")) continue;
            k = l.substring(0,l.indexOf("=")).trim();
            l = l.substring(l.indexOf("=")+1).trim();
            put(k,l);
        }
        r.close();
    }
    public void write() throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(Localization.d + "settings.ini"));
        FileWriter w = new FileWriter(Localization.d + "settings.ini");
        ArrayList<String> keys = new ArrayList<String>();
        StringBuilder str = new StringBuilder("#EWDict Global Settings\n");
        String k, l;
        while(null != (l = r.readLine())){
            if(!l.contains("=") || l.startsWith("#")) str.append(l).append("\n"); else {
                k = l.substring(0,l.indexOf("=")).trim();
                str.append(k).append(" = ").append(get(k)).append("\n");
                keys.add(k);
            }
        }
        for(String key: keySet()){
            if(keys.contains(key)) continue;
            str.append(key).append(" = ").append(get(key)).append("\n");
        }
        w.write(str.toString());
        r.close();
        w.close();
    }
}
