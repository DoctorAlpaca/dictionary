package de.eric_wiltfang.dictionary;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * https://www.reddit.com/r/javahelp/comments/2lypn4/trying_to_use_javafxstagefilechooser_in_a_swing/
 */
public final class CFileChooser {
  static{
    Platform.setImplicitExit(false);
  }
  
  private final static Object LOCK = new Object();
  
  private File selectedFile = null;
  private boolean wait = true;
  
  private static boolean isJavaFXStillUsable(){
    try{
      @SuppressWarnings("unused")
      // Initializes the Toolkit required by JavaFX, as stated in the docs of Platform.runLater()
      final JFXPanel dummyForToolkitInitialization = new JFXPanel();
    }catch(IllegalStateException e){
      return false;
    }
    return true;
  }
  
  public boolean open(final Component master, final String title, final File defaultFile, final String extGroupName, final String... exts){
    if(!isJavaFXStillUsable()){
      CustomFileChooser fallback = new CustomFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter(extGroupName, exts);
      fallback.setFileFilter(filter);
      fallback.setDialogType(JFileChooser.OPEN_DIALOG);
      if(title != null){
        fallback.setDialogTitle(title);
      }
      if(defaultFile != null){
        fallback.setCurrentDirectory(defaultFile);
        fallback.setSelectedFile(defaultFile);
      }
      boolean is = fallback.showOpenDialog(master) == CustomFileChooser.APPROVE_OPTION;
      if(is)
        selectedFile = fallback.getSelectedFile();
      return is;
    }
    synchronized(LOCK){
      Platform.runLater(new Runnable(){
        public void run(){
          synchronized(LOCK){
            FileChooser fileChooser = new FileChooser();
            for(int i = 0; i < exts.length; i++)
              exts[i] = "*." + exts[i];
            ExtensionFilter filter = new ExtensionFilter(extGroupName, exts);
            fileChooser.getExtensionFilters().add(filter);
            fileChooser.setSelectedExtensionFilter(filter);
            if(title != null){
              fileChooser.setTitle(title);
            }
            if(defaultFile != null){
              fileChooser.setInitialDirectory(new File(defaultFile.getParent()));
              fileChooser.setInitialFileName(defaultFile.getName());
            }
            selectedFile = fileChooser.showOpenDialog(null);
            
            wait = false;
            LOCK.notifyAll();
          }
        }
      });
      do{
        try{
          LOCK.wait();
        }catch(InterruptedException e){
          DictionaryMainWindow.getLogger().severe(e.getMessage());
        }
      }while(wait);
    }
    return selectedFile != null;
  }
  
  public boolean save(final Component master, final File file, final String extGroupName, final String... exts){
    if(!isJavaFXStillUsable()){
      CustomFileChooser fallback = new CustomFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter(extGroupName, exts);
      fallback.setFileFilter(filter);
      fallback.setDialogType(JFileChooser.SAVE_DIALOG);
      boolean is = fallback.showOpenDialog(master) == CustomFileChooser.APPROVE_OPTION;
      if(is)
        selectedFile = fallback.getSelectedFile();
      return is;
    }
    synchronized(LOCK){
      Platform.runLater(new Runnable(){
        public void run(){
          synchronized(LOCK){
            FileChooser fileChooser = new FileChooser();
            for(int i = 0; i < exts.length; i++)
              exts[i] = "*." + exts[i];
            ExtensionFilter filter = new ExtensionFilter(extGroupName, exts);
            fileChooser.getExtensionFilters().add(filter);
            fileChooser.setSelectedExtensionFilter(filter);
            if(file != null){
              fileChooser.setInitialFileName(file.getName());
            }
            selectedFile = fileChooser.showSaveDialog(null);
            
            wait = false;
            LOCK.notifyAll();
          }
        }
      });
      do{
        try{
          LOCK.wait();
        }catch(InterruptedException e){
          DictionaryMainWindow.getLogger().severe(e.getMessage());
        }
      }while(wait);
    }
    return selectedFile != null;
  }

  public File getSelectedFile(){
    return selectedFile;
  }
}
