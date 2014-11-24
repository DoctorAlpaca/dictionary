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

import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class CSVExporterDialog extends JDialog {
	private static final long serialVersionUID = -9145444416183683605L;
	
	private final JPanel contentPanel = new JPanel();
	private boolean exported;
	private Dictionary dic;
	JCheckBox chckbxAddColumnHeaders;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CSVExporterDialog dialog = new CSVExporterDialog(Dictionary.createNew(new DictionarySettings()));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CSVExporterDialog(Dictionary dictionary) {
		dic = dictionary;
		
		exported = false;
		
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			chckbxAddColumnHeaders = new JCheckBox(Localization.getInstance().get("csvExportAddHeader"));
			contentPanel.add(chckbxAddColumnHeaders, "2, 2");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Localization.getInstance().get("export"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CostumFileChooser chooser = new CostumFileChooser();
						FileNameExtensionFilter filter = new FileNameExtensionFilter(Localization.getInstance().get("csvFiletypeFilterName"), "csv");
						chooser.setFileFilter(filter);
						if (!dic.getName().isEmpty()) {
							chooser.setSelectedFile(new File(dic.getName() + ".csv"));
						}
						
						if (chooser.showSaveDialog(contentPanel) == CostumFileChooser.APPROVE_OPTION) {
							File f = chooser.getSelectedFile();
							try {
								CSVExporter exp = new CSVExporter(f);
								exp.printHeader = chckbxAddColumnHeaders.isSelected();
								dic.export(exp);

								setVisible(false);
								dispose();
							} catch (Exception ex) {
								ErrorDialog.showError(Localization.getInstance().get("exportErrorMessage"), ex);
							}
						} else {
							return;
						}
					}
				});
				okButton.setActionCommand(Localization.getInstance().get("ok"));
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(Localization.getInstance().get("cancel"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand(Localization.getInstance().get("cancel"));
				buttonPane.add(cancelButton);
			}
		}
	}

	public boolean getExported() {
		return exported;
	}
}
