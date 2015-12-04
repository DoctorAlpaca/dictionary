package de.eric_wiltfang.dictionary;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import de.eric_wiltfang.dictionary.csv.CSVExporterDialog;
import de.eric_wiltfang.dictionary.csv.CSVImporterDialog;
import de.eric_wiltfang.dictionary.html.HTMLExporter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

public class DictionaryMainWindow implements Runnable {
  private static Logger LOGGER = Logger.getLogger("Dictionary");
  private static Properties SETTINGS;
  private static Localisation LOCALISATION;
  private static DictionaryMainWindow INSTANCE;
  
  private JFrame frmDictionaryEditor;
  private JTextField searchField;
  private JTable table;
  private EntryTableModel model;
  private JLabel lblStatuslabel;
  
  private Dictionary dic;
  private File defaultFile;
  private boolean changed;
  
  private Vector<JComponent> needDictionary;
  private JLabel lblEntries;
  private JProgressBar progressBar;
  
  /**
   * Launch the application.
   */
  public static void main(String[] args){
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("TextArea.font", UIManager.get("Table.font"));
    }catch(Exception ex){
      // No biggie, default style works too.
    }
    SETTINGS = Util.loadSettings();
    LOCALISATION = new Localisation(SETTINGS);
    INSTANCE = new DictionaryMainWindow();
    EventQueue.invokeLater(INSTANCE);
  }
  
  @Override
  public void run(){
    if(LOCALISATION == null)
      LOCALISATION = new Localisation(SETTINGS);
    INSTANCE.initialize();
  }
  
  /**
   * Initialize the contents of the frame.
   */
  private void initialize(){
    frmDictionaryEditor = new JFrame();
    frmDictionaryEditor.setIconImages(Util.getImages());
    needDictionary = new Vector<>();
    // former initialize() below
    frmDictionaryEditor.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e) {
        quit();
      }
    });
    frmDictionaryEditor.setTitle(getLocalisedString("mainWindowTitle"));
    frmDictionaryEditor.setBounds(100, 100, 535, 468);
    frmDictionaryEditor.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    
    JMenuBar menuBar = new JMenuBar();
    frmDictionaryEditor.setJMenuBar(menuBar);
    
    JMenu mnFile = new JMenu(getLocalisedString("menuItemFile"));
    menuBar.add(mnFile);
    
    JMenuItem mntmNew = new JMenuItem(getLocalisedString("menuItemFileNew"));
    mntmNew.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        if(!askForSave()){
          return;
        }
        
        EditDictionarySettingsDialog dialog = new EditDictionarySettingsDialog();
        dialog.setVisible(true);
        if(dialog.getConfirmed()){
          try{
            Dictionary dic = Dictionary.createNew(dialog.getSettings());
            setDictionary(dic);
            setStatus(getLocalisedString("statusTextDictionaryCreated"), false);
            
            changed = true;
          }catch(Exception ex){
            showError(getLocalisedString("dictionaryCreationError"), ex);
          }
          frmDictionaryEditor.setTitle(getLocalisedString("mainWindowTitle") + ": " + dic.getName());
          defaultFile = null;
        }
      }
    });
    mnFile.add(mntmNew);
    
    JMenuItem mntmOpen = new JMenuItem(getLocalisedString("menuItemFileOpen"));
    mntmOpen.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        if(!askForSave()){
          return;
        }
        CFileChooser chooser = new CFileChooser();
        if(chooser.open(frmDictionaryEditor, null, defaultFile, getLocalisedString("dictionaryFiletypeFilterName"), "dict")){
          try{
            setStatus(getLocalisedString("statusTextLoading"), true);
            defaultFile = chooser.getSelectedFile();
            dic = Dictionary.createFromFile(defaultFile);
            frmDictionaryEditor.setTitle(getLocalisedString("mainWindowTitle") + ": " + dic.getName());
            setDictionary(dic);
            changed = false;
            setStatus(getLocalisedString("statusTextFileLoaded"), false);
            SETTINGS.setProperty("lastFile", defaultFile.getAbsolutePath().toString());
            Util.saveSettings(SETTINGS);
          }catch(Exception ex){
            showError(getLocalisedString("dictionaryLoadError"), ex);
          }
        }
      }
    });
    mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
    mnFile.add(mntmOpen);
    
    JMenuItem mntmSave = new JMenuItem(getLocalisedString("menuItemFileSave"));
    mntmSave.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        save();
      }
    });
    mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    mnFile.add(mntmSave);
    needDictionary.add(mntmSave);
    
    JMenuItem mntmSaveAs = new JMenuItem(getLocalisedString("menuItemFileSaveAs"));
    mntmSaveAs.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        File temp = defaultFile;
        defaultFile = null;
        save();
        if(defaultFile == null){
          defaultFile = temp;
        }
      }
    });
    mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    mnFile.add(mntmSaveAs);
    needDictionary.add(mntmSaveAs);
    
    JSeparator separator_1 = new JSeparator();
    mnFile.add(separator_1);
    
    JMenu mnImport = new JMenu(getLocalisedString("menuItemFileImport"));
    mnFile.add(mnImport);
    
    JMenuItem mntmCSVImport = new JMenuItem(getLocalisedString("menuItemExportCSV"));
    mntmCSVImport.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ev){
        CSVImporterDialog importer = new CSVImporterDialog(dic);
        importer.setVisible(true);
      }
    });
    mnImport.add(mntmCSVImport);
    needDictionary.add(mntmCSVImport);
    
    JMenu mnExportAs = new JMenu(getLocalisedString("menuItemFileExport"));
    mnFile.add(mnExportAs);
    needDictionary.add(mnExportAs);
    
    JMenuItem mntmCSVExport = new JMenuItem(getLocalisedString("menuItemExportCSV"));
    mntmCSVExport.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0){
        CSVExporterDialog dialog = new CSVExporterDialog(dic);
        dialog.setVisible(true);
      }
    });
    mnExportAs.add(mntmCSVExport);
    needDictionary.add(mntmCSVExport);
    
    JMenuItem mntmWebDictionaryhtml = new JMenuItem(getLocalisedString("menuItemExportHTML"));
    mntmWebDictionaryhtml.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0){
        CFileChooser chooser = new CFileChooser();
        File defaultFile = null;
        if(!dic.getName().isEmpty()){
          defaultFile = new File(dic.getName() + ".html");
        }
        if(chooser.save(frmDictionaryEditor, defaultFile, getLocalisedString("htmlFiletypeFilterName"), "html", "htm")){
          File f = chooser.getSelectedFile();
          try{
            HTMLExporter exp = new HTMLExporter(f);
            dic.export(exp);
          }catch(Exception ex){
            ErrorDialog.showError(getLocalisedString("dictionaryExportError"), ex);
          }
        }
      }
    });
    mnExportAs.add(mntmWebDictionaryhtml);
    needDictionary.add(mntmWebDictionaryhtml);
    
    JMenuItem mntmQuit = new JMenuItem(getLocalisedString("menuItemFileQuit"));
    mntmQuit.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0){
        quit();
      }
    });
    
    final JCheckBoxMenuItem mntmUseLast = new JCheckBoxMenuItem(getLocalisedString("useLastDictionaryOption"));
    mntmUseLast.addItemListener(new ItemListener(){
      @Override
      public void itemStateChanged(ItemEvent e){
        if(mntmUseLast.isSelected()){
          SETTINGS.setProperty("useLast", "true");
        }else{
          SETTINGS.setProperty("useLast", "false");
        }
        Util.saveSettings(SETTINGS);
      }
    });
    
    JSeparator separator_4 = new JSeparator();
    mnFile.add(separator_4);
    mntmUseLast.setSelected(Boolean.parseBoolean(SETTINGS.getProperty("useLast", "false")));
    mnFile.add(mntmUseLast);
    
    JMenu mnLanguage = new JMenu(getLocalisedString("menuLanguage"));
    ButtonGroup mnLanguageGroup = new ButtonGroup();
    String[] languages = Util.getLanguages();
    Locale currentLocale = LOCALISATION.getLocale();
    if(currentLocale.toLanguageTag().equals("und"))
      currentLocale = Locale.ENGLISH;
    String currentLanguage = currentLocale.getDisplayLanguage() + " (" + currentLocale.toLanguageTag() + ")";
    ActionListener languageListener = new ActionListener(){
      public void actionPerformed(ActionEvent e){
        JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();
        if(button.isSelected()){
          SETTINGS.setProperty("locale", Util.getLocale(button.getText()).toLanguageTag());
          finish();
          frmDictionaryEditor = null;
          LOCALISATION = null;
          EventQueue.invokeLater(DictionaryMainWindow.this);
        }
      }
    };
    for(int i = 0; i < languages.length; i++){
      JRadioButtonMenuItem mntmRad = new JRadioButtonMenuItem(languages[i]);
      mnLanguageGroup.add(mntmRad);
      mnLanguage.add(mntmRad);
      mntmRad.addActionListener(languageListener);
      if(languages[i].equalsIgnoreCase(currentLanguage))
        mnLanguageGroup.setSelected(mntmRad.getModel(), true);
    }
    
    mnFile.add(mnLanguage);
    
    JSeparator separator = new JSeparator();
    mnFile.add(separator);
    mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
    mnFile.add(mntmQuit);
    
    JMenu mnEdit = new JMenu(getLocalisedString("menuItemEdit"));
    menuBar.add(mnEdit);
    
    JMenuItem mntmDeleteSelected = new JMenuItem(getLocalisedString("deleteEntrySelected"));
    mntmDeleteSelected.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0){
        deleteSelected();
      }
    });
    
    JMenuItem mntmNewEntry = new JMenuItem(getLocalisedString("newEntry"));
    mntmNewEntry.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        EditEntryWindow window = new EditEntryWindow(dic);
        window.setVisible(true);
      }
    });
    mntmNewEntry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
    mnEdit.add(mntmNewEntry);
    needDictionary.add(mntmNewEntry);
    
    JMenuItem mntmMultipleNewEntries = new JMenuItem(getLocalisedString("newEntryMultiple"));
    mntmMultipleNewEntries.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        EditEntryWindow window = new EditEntryWindow(dic);
        window.setNewEntryOnSave(true);
        window.setVisible(true);
      }
    });
    mntmMultipleNewEntries.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    mnEdit.add(mntmMultipleNewEntries);
    needDictionary.add(mntmMultipleNewEntries);
    
    JSeparator separator_3 = new JSeparator();
    mnEdit.add(separator_3);
    
    JMenuItem mntmEditSelected = new JMenuItem(getLocalisedString("editEntrySelected"));
    mntmEditSelected.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        editSelected();
      }
    });
    mntmEditSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    mnEdit.add(mntmEditSelected);
    needDictionary.add(mntmEditSelected);
    mntmDeleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    mnEdit.add(mntmDeleteSelected);
    needDictionary.add(mntmDeleteSelected);
    
    JSeparator separator_2 = new JSeparator();
    mnEdit.add(separator_2);
    
    JMenuItem mntmEditDictionarySettings = new JMenuItem(getLocalisedString("editDictionarySettings"));
    mntmEditDictionarySettings.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        EditDictionarySettingsDialog edsd = new EditDictionarySettingsDialog(dic.getSettings());
        edsd.setVisible(true);
        if(edsd.getConfirmed()){
          dic.setSettings(edsd.getSettings());
        }
        
        frmDictionaryEditor.setTitle(getLocalisedString("mainWindowTitle") + ": " + dic.getName());
        changed = true;
      }
    });
    mnEdit.add(mntmEditDictionarySettings);
    needDictionary.add(mntmEditDictionarySettings);
    
    frmDictionaryEditor.getContentPane().setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:default:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    
    JLabel lblSearch = new JLabel(getLocalisedString("buttonSearch"));
    frmDictionaryEditor.getContentPane().add(lblSearch, "2, 2, right, default");
    
    searchField = new JTextField();
    searchField.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        String key = searchField.getText();
        if(dic != null){
          try{
            model.searchFor(key);
          }catch(SQLException ex){
            showError(getLocalisedString("internalError"), ex);
          }
        }
      }
    });
    frmDictionaryEditor.getContentPane().add(searchField, "4, 2, fill, default");
    searchField.setColumns(10);
    needDictionary.add(searchField);
    
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    frmDictionaryEditor.getContentPane().add(scrollPane, "2, 4, 3, 1, fill, fill");
    
    model = new EntryTableModel();
    table = new JTable();
    table.addKeyListener(new KeyAdapter(){
      @Override
      public void keyPressed(KeyEvent ev){
        if(ev.getKeyCode() == KeyEvent.VK_ENTER){
          editSelected();
        }
      }
    });
    table.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent e){
        if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
          Point p = e.getPoint();
          if(table.isRowSelected(table.rowAtPoint(p))){
            editSelected();
          }
        }
      }
    });
    table.setFillsViewportHeight(true);
    table.setEnabled(false);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setModel(model);
    table.getColumnModel().getColumn(0).setPreferredWidth(100);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);
    table.getColumnModel().getColumn(3).setPreferredWidth(100);
    scrollPane.setViewportView(table);
    needDictionary.add(table);
    
    JPanel panel = new JPanel();
    frmDictionaryEditor.getContentPane().add(panel, "1, 6, 4, 1, default, fill");
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("43px:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("right:97px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("right:min"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] { RowSpec.decode("14px"), }));
    
    lblStatuslabel = new JLabel(getLocalisedString("statusTextDefault"));
    panel.add(lblStatuslabel, "2, 1, left, top");
    
    progressBar = new JProgressBar();
    panel.add(progressBar, "4, 1, left, center");
    progressBar.setVisible(false);
    
    lblEntries = new JLabel("0 " + getLocalisedString("entryCount"));
    panel.add(lblEntries, "6, 1, right, center");
    model.addTableModelListener(new TableModelListener(){
      public void tableChanged(TableModelEvent e){
        lblEntries.setText(((EntryTableModel) e.getSource()).getRowCount() + " " + getLocalisedString("entryCount"));
      }
    });
    
    // former initialize() lies above
    
    table.setModel(model);
    
    JPopupMenu popupMenu = new JPopupMenu();
    addPopup(table, popupMenu);
    
    JMenuItem mntmNewEntryRightClick = new JMenuItem(getLocalisedString("newEntry"));
    mntmNewEntryRightClick.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        EditEntryWindow window = new EditEntryWindow(dic);
        window.setVisible(true);
      }
    });
    popupMenu.add(mntmNewEntryRightClick);
    needDictionary.add(mntmNewEntryRightClick);
    
    JMenuItem mntmEditSelectedRightClick = new JMenuItem(getLocalisedString("editEntrySelected"));
    mntmEditSelectedRightClick.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0){
        editSelected();
      }
    });
    popupMenu.add(mntmEditSelectedRightClick);
    needDictionary.add(mntmEditSelectedRightClick);
    
    JMenuItem mntnDeleteRightClick = new JMenuItem(getLocalisedString("deleteEntrySelected"));
    mntnDeleteRightClick.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0){
        deleteSelected();
      }
    });
    popupMenu.add(mntnDeleteRightClick);
    needDictionary.add(mntnDeleteRightClick);
    
    setComponentEnabled(false);
    setStatus(getLocalisedString("statusTextWelcome"), false);
    
    frmDictionaryEditor.pack();
    frmDictionaryEditor.setVisible(true);
    
    if(Boolean.parseBoolean(SETTINGS.getProperty("useLast", "false"))){
      try{
        setStatus(getLocalisedString("statusTextLoading"), true);
        File lastDic = new File(SETTINGS.getProperty("lastFile", null));
        if(lastDic.exists()){
          Dictionary dic = Dictionary.createFromFile(lastDic);
          frmDictionaryEditor.setTitle(getLocalisedString("mainWindowTitle") + ": " + dic.getName());
          defaultFile = lastDic;
          setDictionary(dic);
          changed = false;
          setStatus(getLocalisedString("statusTextFileLoaded"), false);
        }
      }catch(Exception e){
        showError(getLocalisedString("dictionaryLoadError"), e);
      }
    }
  }
  
  /**
   * Gets a localised string.
   * 
   * @param string The Localisation.Key of the string.
   */
  public String getLocalisedString(String string){
    if(LOCALISATION == null)
      LOCALISATION = new Localisation(SETTINGS);
    return LOCALISATION.get(string);
  }
  
  /**
   * Enables/disables all components that need a loaded dictionary to work.
   */
  private void setComponentEnabled(boolean b){
    for(JComponent c: needDictionary){
      c.setEnabled(b);
    }
  }
  /**
   * Sets the dictionary the program edits.
   * 
   * @param dic The dictionary to edit.
   */
  private void setDictionary(Dictionary dic){
    this.dic = dic;
    
    dic.addDictionaryListener(new DictionaryListener(){
      public void recieveEvent(DictionaryEvent event){
        changed = true;
      }
    });
    dic.addDictionaryListener(new DictionaryListener(){
      public void recieveEvent(DictionaryEvent event){
        switch(event.getType()){
          case OTHER:
          case UPDATE:
            setStatus(getLocalisedString("statusTextDictionaryEdited"), false);
            break;
          case DELETE:
            setStatus(getLocalisedString("statusTextEntryDeleted"), false);
            break;
          case NEW:
            setStatus(getLocalisedString("statusTextEntryAdded"), false);
            break;
        }
      }
    });
    
    setComponentEnabled(true);
    
    connectTableModel();
  }
  /**
   * Sets up the table model with the current dictionary.
   */
  private void connectTableModel(){
    try{
      model.setDictionary(dic);
    }catch(Exception ex){
      showError(getLocalisedString("internalError"), ex);
    }
    try{
      model.searchFor(searchField.getText());
    }catch(Exception ex){
      showError(getLocalisedString("internalError"), ex);
    }
  }
  
  private void showError(String message, Exception ex){
    ErrorDialog.showError(message, ex);
  }
  private Vector<Long> getSelectedIDs(){
    int[] rows = table.getSelectedRows();
    Vector<Long> ids = new Vector<>(rows.length);
    for(int row: rows){
      ids.add(model.getID(row));
    }
    return ids;
  }
  private void deleteSelected(){
    if(JOptionPane.showConfirmDialog(frmDictionaryEditor, getLocalisedString("messageConfirmEntryDelete")) != JOptionPane.OK_OPTION){
      return;
    }
    try{
      for(Long id: getSelectedIDs()){
        dic.deleteEntry(id);
      }
    }catch(Exception ex){
      showError(getLocalisedString("deletionError"), ex);
    }
  }
  private void editSelected(){
    if(table.getSelectedRowCount() > 10){
      if(JOptionPane.showConfirmDialog(frmDictionaryEditor, getLocalisedString("messageConfirmEntryEdit") + " " + table.getSelectedRowCount() + " " + getLocalisedString("dConfMult2"), getLocalisedString("messageConfirmEntryEditTitle"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
        return;
      }
    }
    try{
      for(Long id: getSelectedIDs()){
        EditEntryWindow editWindow = new EditEntryWindow(dic, dic.getEntry(id));
        editWindow.setVisible(true);
      }
    }catch(Exception ex){
      showError(getLocalisedString("internalError"), ex);
    }
    
  }
  
  private void quit(int code){
    finish();
    System.exit(code);
  }
  
  public void quit(){
    quit(0);
  }
  
  public void finish(){
    if(!askForSave()){
      return;
    }
    
    if(dic != null){
      try{
        dic.cleanup();
      }catch(IOException ex){
        showError(getLocalisedString("cleanupError"), ex);
      }
    }
    Util.saveSettings(SETTINGS);
    frmDictionaryEditor.dispose();
  }
  
  /**
   * Asks the user if they want to save if there was a change.
   * 
   * @return false if the action was cancelled, true if the file was saved or the user declined saving (or there was nothing to save).
   */
  private boolean askForSave(){
    if(changed){
      int answer = JOptionPane.showConfirmDialog(frmDictionaryEditor, getLocalisedString("messageSaveBeforeQuit"), getLocalisedString("messageSaveBeforeQuitTitle"), JOptionPane.YES_NO_CANCEL_OPTION);
      
      switch(answer){
        case JOptionPane.YES_OPTION:
          boolean saved = save();
          return saved;
        case JOptionPane.NO_OPTION:
          return true;
        case JOptionPane.CANCEL_OPTION:
          return false;
      }
    }
    return true;
  }
  /**
   * Shows a dialog to save the dictionary.
   * 
   * @return true if the dictionary was saved, false otherwise.
   */
  private boolean save(){
    if(dic == null){
      return false;
    }
    File f;
    if(defaultFile == null){
      CFileChooser chooser = new CFileChooser();
      if(chooser.save(frmDictionaryEditor, new File(dic.getName() + ".dict"), getLocalisedString("dictionaryFiletypeFilterName"), "dict")){
        f = chooser.getSelectedFile();
        LOGGER.fine("Saving to: " + f);
      }else{
        return false;
      }
    }else{
      f = defaultFile;
    }
    try{
      setStatus("Saving", true);
      dic.save(f);
      defaultFile = f;
      setStatus(getLocalisedString("statusTextFileSaved"), false);
      
      changed = false;
      return true;
    }catch(Exception ex){
      showError(getLocalisedString("dictionarySaveError"), ex);
      return false;
    }
  }
  
  private void setStatus(String message, boolean busy){
    lblStatuslabel.setText(message + (busy?"...":""));
    // progressBar.setIndeterminate(busy);
  }
  
  private static void addPopup(Component component, final JPopupMenu popup){
    component.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent e){
        if(e.isPopupTrigger()){
          showMenu(e);
        }
      }
      public void mouseReleased(MouseEvent e){
        if(e.isPopupTrigger()){
          showMenu(e);
        }
      }
      private void showMenu(MouseEvent e){
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }
  
  /**
   * Gets the instance of the program.
   */
  public static DictionaryMainWindow getInstance(){
    return INSTANCE;
  }
  /**
   * Fatal Error w/o localised msg.
   * 
   * @param msg Message to display.
   */
  public void die(String msg){
    die("Fatal error", msg);
  }
  private void die(String title, String msg){
    JOptionPane.showMessageDialog(frmDictionaryEditor, msg, title, JOptionPane.ERROR_MESSAGE);
    quit(1);
  }
  /**
   * Gets the logger for Dictionary.
   */
  public static Logger getLogger(){
    return LOGGER;
  }
}
