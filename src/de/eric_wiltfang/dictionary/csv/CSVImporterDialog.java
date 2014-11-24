package de.eric_wiltfang.dictionary.csv;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import de.eric_wiltfang.dictionary.*;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Vector;

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JComboBox;

import org.apache.commons.csv.CSVFormat;

import javax.swing.DefaultComboBoxModel;

public class CSVImporterDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField txtFilename;
	private JTextField txtTags;
	private JTable table;
	private JSpinner spinnerWord;
	private JSpinner spinnerDefinition;
	private JSpinner spinnerCategory;
	private JSpinner spinnerNotes;
	private JButton okButton;
	private JButton cancelButton;
	private JCheckBox chckbxFirstRowContains;
	private JComboBox<String> comboBoxFormat;
	private JComboBox<String> comboBoxCharset;
	
	private File file;
	private CSVImporter importer;
	private Dictionary dic;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CSVImporterDialog dialog = new CSVImporterDialog(Dictionary.createNew(new DictionarySettings()));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CSVImporterDialog(Dictionary dictionary) {
		dic = dictionary;
		
		importer = new CSVImporter();
		
		ChangeListener resetOk = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				okButton.setEnabled(false);
			}
		};
		
		setBounds(100, 100, 699, 421);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(91dlu;min):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		{
			JLabel lblFile = new JLabel(Localization.getInstance().get("csvImportFile"));
			contentPanel.add(lblFile, "2, 2, right, default");
		}
		{
			txtFilename = new JTextField();
			txtFilename.setEditable(false);
			contentPanel.add(txtFilename, "4, 2, 3, 1, fill, default");
			txtFilename.setColumns(10);
		}
		{
			JButton btnBrowse = new JButton("Browse");
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					CostumFileChooser chooser = new CostumFileChooser();
					chooser.setDialogType(JFileChooser.OPEN_DIALOG);
					FileNameExtensionFilter filter = new FileNameExtensionFilter(Localization.getInstance().get("csvFiletypeFilterName"), "csv");
					chooser.setFileFilter(filter);
					
					if (chooser.showDialog(contentPanel, Localization.getInstance().get("buttonSelectImportFile")) == JFileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
						txtFilename.setText(file.toString());
						okButton.setEnabled(false);
					}
				}
			});
			contentPanel.add(btnBrowse, "8, 2");
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, "2, 4, 3, 1");
			panel.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,},
				new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));
			{
				chckbxFirstRowContains = new JCheckBox(Localization.getInstance().get("csvImportLabelRow"));
				chckbxFirstRowContains.addChangeListener(resetOk);
				panel.add(chckbxFirstRowContains, "2, 2, 3, 1");
			}
			{
				JLabel lblWordColumn = new JLabel(Localization.getInstance().get("csvImportWordColumn"));
				panel.add(lblWordColumn, "2, 4, right, default");
			}
			{
				spinnerWord = new JSpinner();
				spinnerWord.addChangeListener(resetOk);
				spinnerWord.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
				panel.add(spinnerWord, "4, 4, fill, default");
			}
			{
				JLabel lblDefinitionColumn = new JLabel(Localization.getInstance().get("csvImportDefinitionColumn"));
				panel.add(lblDefinitionColumn, "2, 6, right, default");
			}
			{
				spinnerDefinition = new JSpinner();
				spinnerDefinition.addChangeListener(resetOk);
				spinnerDefinition.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
				panel.add(spinnerDefinition, "4, 6, fill, default");
			}
			{
				JLabel lblCategoryColumn = new JLabel(Localization.getInstance().get("csvImportCategoryColumn"));
				panel.add(lblCategoryColumn, "2, 8, right, default");
			}
			{
				spinnerCategory = new JSpinner();
				spinnerCategory.addChangeListener(resetOk);
				spinnerCategory.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
				panel.add(spinnerCategory, "4, 8, fill, default");
			}
			{
				JLabel lblTagColumns = new JLabel(Localization.getInstance().get("csvImportTagColumn"));
				panel.add(lblTagColumns, "2, 10, right, default");
			}
			{
				txtTags = new JTextField();
				txtTags.getDocument().addDocumentListener(new DocumentListener() {
					public void removeUpdate(DocumentEvent e) {
						okButton.setEnabled(false);
					}
					public void insertUpdate(DocumentEvent e) {
						okButton.setEnabled(false);
					}
					public void changedUpdate(DocumentEvent e) {
						okButton.setEnabled(false);
					}
				});
				panel.add(txtTags, "4, 10, fill, default");
				txtTags.setColumns(10);
			}
			{
				JLabel lblNotesColumn = new JLabel(Localization.getInstance().get("csvImportNoteColumn"));
				panel.add(lblNotesColumn, "2, 12, right, default");
			}
			{
				spinnerNotes = new JSpinner();
				spinnerNotes.addChangeListener(resetOk);
				spinnerNotes.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
				panel.add(spinnerNotes, "4, 12, fill, default");
			}
			{
				JLabel lblAdvancedOptions = new JLabel(Localization.getInstance().get("advancedOptions"));
				panel.add(lblAdvancedOptions, "2, 14, 3, 1");
			}
			{
				JLabel lblEncoding = new JLabel(Localization.getInstance().get("charset"));
				panel.add(lblEncoding, "2, 16, right, default");
			}
			{
				comboBoxCharset = new JComboBox<>();
				Set<String> charsetNameSet = Charset.availableCharsets().keySet();
				String[] charsetNames = (String[]) charsetNameSet.toArray(new String[charsetNameSet.size()]);;
				comboBoxCharset.setModel(new DefaultComboBoxModel<String>(charsetNames));
				comboBoxCharset.getModel().setSelectedItem("UTF-8");
				comboBoxCharset.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButton.setEnabled(false);;
					}
				});
				panel.add(comboBoxCharset, "4, 16, fill, default");
			}
			{
				JLabel lblCsvFormat = new JLabel(Localization.getInstance().get("csvFormat"));
				panel.add(lblCsvFormat, "2, 18, right, default");
			}
			{
				comboBoxFormat = new JComboBox<>();
				comboBoxFormat.setModel(new DefaultComboBoxModel<String>(new String[] {"DEFAULT", "EXCEL", "MYSQL", "RFC4180", "TDF"}));
				comboBoxFormat.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButton.setEnabled(false);;
					}
				});
				panel.add(comboBoxFormat, "4, 18, fill, default");
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "6, 4, 3, 1, fill, fill");
			{
				table = new JTable();
				table.setFillsViewportHeight(true);
				scrollPane.setViewportView(table);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnPreview = new JButton(Localization.getInstance().get("refreshPreview"));
				btnPreview.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (updatePreview()) {
							okButton.setEnabled(true);
						}
					}
				});
				buttonPane.add(btnPreview);
			}
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							int num = dic.importEntries(importer);
							JOptionPane.showMessageDialog(contentPanel, Localization.getInstance().get("importSuccesfullMessage")+ " " + num + " " + Localization.getInstance().get("pStatImport2"), Localization.getInstance().get("importSuccesfullMessageTitle"), JOptionPane.INFORMATION_MESSAGE);

							setVisible(false);
							dispose();
						} catch (Exception ex) {
							ErrorDialog.showError(Localization.getInstance().get("importErrorMessage"), ex);
						}
					}
				});
				okButton.setToolTipText(Localization.getInstance().get("refreshPreviewHint"));
				okButton.setEnabled(false);
				okButton.setActionCommand(Localization.getInstance().get("ok"));
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton(Localization.getInstance().get("cancel"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand(Localization.getInstance().get("cancel"));
				buttonPane.add(cancelButton);
			}
		}
	}
	
	
	private boolean updatePreview() {
		if (!useSettings()) {
			return false;
		}
		
		// Create preview
		Vector<String[]> tableContent = new Vector<>();
		try {
			for (Entry e : importer.preview(20)) {
				String[] row = {e.getWord(), e.getDefinition(), e.getCategory(), e.getTagsAsString(), e.getNotes()};
				tableContent.add(row);
			}
		} catch (Exception ex) {
			ErrorDialog.showError(Localization.getInstance().get("fileReadExceptionMessage"), ex);
			return false;
		}
		String[] columnNames = {Localization.getInstance().get("wordColumnName"),
				Localization.getInstance().get("definitionColumnName"),
				Localization.getInstance().get("categoryColumnName"),
				Localization.getInstance().get("tagsColumnName"),
				Localization.getInstance().get("notesColumnName")};
		TableModel model = new DefaultTableModel(tableContent.toArray(new String[tableContent.size()][5]), columnNames);
		
		table.setModel(model);
		
		return true;
	}
	/**
	 * Transfers the selected settings to the exporter.
	 * @return true if the settings were valid and were transfered, false otherwise.
	 */
	private boolean useSettings() {
		if (file == null || !file.canRead()) {
			JOptionPane.showMessageDialog(contentPanel, Localization.getInstance().get("messageSelectReadableFile"), Localization.getInstance().get("messageSelectReadableFileTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			importer.file = file;
		}
		importer.wordCol = (Integer) spinnerWord.getValue() - 1;
		importer.definitionCol = (Integer) spinnerDefinition.getValue() - 1;
		importer.notesCol = (Integer) spinnerNotes.getValue() - 1;
		importer.categoryCol = (Integer) spinnerCategory.getValue() - 1;
		
		Vector<Integer> tagCols = new Vector<>();
		for (String s : txtTags.getText().split(",")) {
			s = s.trim();
			if (!s.isEmpty()) {
				try {
					int i = Integer.parseInt(s) - 1;
					if (i >= 0) {
						tagCols.add(i);
					} else {
						JOptionPane.showMessageDialog(contentPanel, Localization.getInstance().get("messageCSVImportTagsInvalid"), Localization.getInstance().get("messageCSVImportTagsInvalidTitle"), JOptionPane.ERROR_MESSAGE);
						return false;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(contentPanel, Localization.getInstance().get("messageCSVImportTagsInvalid"), Localization.getInstance().get("messageCSVImportTagsInvalidTitle"), JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		importer.tagCols = tagCols;
		
		importer.skipFirstRow = chckbxFirstRowContains.isSelected();
		
		CSVFormat format;
		switch ((String) comboBoxFormat.getSelectedItem()) {
		case "DEFAULT":
			format = CSVFormat.DEFAULT;
			break;
		case "EXCEL":
			format = CSVFormat.EXCEL;
			break;
		case "MYSQL":
			format = CSVFormat.MYSQL;
			break;
		case "RFC4180":
			format = CSVFormat.RFC4180;
			break;
		case "TDF":
			format = CSVFormat.TDF;
			break;
		default:
			JOptionPane.showMessageDialog(contentPanel, Localization.getInstance().get("messageCSVInvalidFormat"), Localization.getInstance().get("messageCSVInvalidFormatTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		importer.format = format;
		
		importer.charset = Charset.forName((String) comboBoxCharset.getSelectedItem());
		
		return true;
		
	}

}
