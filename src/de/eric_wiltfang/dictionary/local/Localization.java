package de.eric_wiltfang.dictionary.local;

import de.eric_wiltfang.dictionary.DictionaryMainWindow;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author cofl
 *
 */
public class Localization extends java.util.HashMap<String, String>{

    public Localization(){
        String OS = System.getProperty("os.name").toLowerCase(),d;
        if(OS.contains("win")) d = System.getenv("AppData") + "/.ewdictionary/"; else {
            d = System.getProperty("user.home");
            if(OS.contains("mac")) d += "/Library/Application Support/";
            d += "/.ewdictionary/";
        }
        try {
            File cf = new File(d);
            if(!cf.exists()) if(!cf.mkdir()) throw new Exception("");
            cf = new File(d + "assets/");
            if(!cf.exists()) if(!cf.mkdir()) throw new Exception("");
            cf = new File(d + "lang.string");
            if(!cf.exists()){
                FileWriter w = new FileWriter(cf.getAbsolutePath());
                w.write("English");
                w.close();
            }
            URL jarl = DictionaryMainWindow.class.getProtectionDomain().getCodeSource().getLocation();
            if(jarl.toString().startsWith("file:")){
                File dir = new File(jarl.toString().substring(5)+"assets/");
                File[] files = dir.listFiles();
                assert files != null;
                for(File file : files) if(file.isFile() && file.getAbsolutePath().endsWith(".lang")){
                    if(new File(d + "assets/" + file.getName()).exists()) continue;
                    BufferedReader r = new BufferedReader(new FileReader(file));
                    FileWriter w = new FileWriter(d + "assets/" + file.getName());
                    w.write("");
                    String line;
                    while(null != (line = r.readLine())){
                        w.append(line).append("\n");
                    }
                    r.close();
                    w.close();
                }
            } else {
                ZipInputStream jar = new ZipInputStream(jarl.openStream());
                ZipEntry en;
                while(null != (en = jar.getNextEntry())){
                    if(!en.getName().startsWith("assets"))
                        continue;
                    String line, f = d + "assets/" + en.getName().substring(en.getName().lastIndexOf("/") + 1);
                    if(new File(f).exists())
                        continue;
                    BufferedReader r = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(en.getName())));
                    FileWriter w = new FileWriter(f);
                    while(null != (line = r.readLine())){
                        w.append(line).append("\n");
                    }
                    r.close();
                    w.close();
                }
            }
            BufferedReader r = new BufferedReader(new FileReader(d + "lang.string"));
            String line = r.readLine();
            if(line == null) line = "English"; else line = line.trim();
            r.close();
            cf = new File(d + "assets/" + line + ".lang");
            if(!cf.exists()){
                cf = new File(d + "assets/English.lang");
                if(!cf.exists()) throw new Exception("");
            }

            r = new BufferedReader(new FileReader(cf.getAbsolutePath()));
            String k, l;
            while(null != (l = r.readLine())){
                l = l.trim();
                if(l.startsWith("#") || !l.contains("=") || l.equals("")) continue;
                k = l.substring(0,l.indexOf("=")).trim();
                l = l.substring(l.indexOf("=")+1).trim();
                put(k,l);
            }
            r.close();
        } catch(Exception e){
            System.out.println("Could not properly initialize localisation. Emergency Mode Activated");
            clear();
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("assets/English.lang")));
                String k, l;
                while(null != (l = r.readLine())){
                    l = l.trim();
                    if(l.startsWith("#") || !l.contains("=")) continue;
                    k = l.substring(0,l.indexOf("=")).trim();
                    l = l.substring(l.indexOf("=")+1).trim();
                    put(k,l);
                }
                r.close();
            } catch(Exception fe){
                JOptionPane.showMessageDialog(null, "Fatal error: Could not load localization by any means.", "Dictionary Editor", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        }
    }
}
