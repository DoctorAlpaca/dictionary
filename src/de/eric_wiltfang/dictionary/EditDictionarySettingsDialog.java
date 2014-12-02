package de.eric_wiltfang.dictionary;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditDictionarySettingsDialog extends JDialog {
	private static final long serialVersionUID = 7084120296023918658L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField nameTextField;
	private boolean confirmed;

	/**
	 * Create the dialog.
	 */
	public EditDictionarySettingsDialog() {
		DictionarySettings settings = new DictionarySettings();
		showDialog(settings);
	}
	public EditDictionarySettingsDialog(DictionarySettings settings) {
		showDialog(settings);
	}
	
	private void showDialog(DictionarySettings settings) {
		setModal(true);
		setResizable(false);
		setTitle(Util.get("createDictionaryWindowTitle"));
		setBounds(100, 100, 400, 115);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblLanguageName = new JLabel(Util.get("languageSettingsName") + " ");
			contentPanel.add(lblLanguageName, "2, 2, right, default");
		}
		{
			nameTextField = new JTextField();
			contentPanel.add(nameTextField, "4, 2, fill, default");
			nameTextField.setColumns(10);
			nameTextField.setText(settings.getName());
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Util.get("ok"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						confirmed = true;
						setVisible(false);
						dispose();
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
						confirmed = false;
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand(Util.get("cancel"));
				buttonPane.add(cancelButton);
			}
		}
	}
	/**
	 * @return Whether or not the user confirmed the changes.
	 */
	public boolean getConfirmed() {
		return confirmed;
	}
	public DictionarySettings getSettings() {
		if (!confirmed) {
			return null;
		} else {
			DictionarySettings settings = new DictionarySettings();
			settings.setName(nameTextField.getText());
			return settings;
		}
	}

}
