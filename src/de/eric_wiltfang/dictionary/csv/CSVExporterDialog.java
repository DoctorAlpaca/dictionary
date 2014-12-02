package de.eric_wiltfang.dictionary.csv;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import de.eric_wiltfang.dictionary.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
			chckbxAddColumnHeaders = new JCheckBox(Util.get("csvExportAddHeader"));
			contentPanel.add(chckbxAddColumnHeaders, "2, 2");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Util.get("export"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CostumFileChooser chooser = new CostumFileChooser();
						FileNameExtensionFilter filter = new FileNameExtensionFilter(Util.get("csvFiletypeFilterName"), "csv");
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
								ErrorDialog.showError(Util.get("exportErrorMessage"), ex);
							}
						} else {
							return;
						}
					}
				});
				okButton.setActionCommand(Util.get("ok"));
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(Util.get("cancel"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand(Util.get("cancel"));
				buttonPane.add(cancelButton);
			}
		}
	}

	public boolean getExported() {
		return exported;
	}
}
