package de.eric_wiltfang.dictionary;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JSeparator;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ErrorDialog extends JDialog {
	private static final long serialVersionUID = -955724717365117867L;
	
	private final JPanel contentPanel = new JPanel();
	private String errorText;

	/**
	 * Test driver.
	 */
	public static void main(String[] args) {
		try {
			ErrorDialog dialog = new ErrorDialog("Test error", new Exception("Test exception"));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * @param message The message to display to the user.
	 * @param ex The exception to add to the message.
	 */
	public ErrorDialog(String message, Exception ex) {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		{
			JLabel lblErrorMessage = new JLabel(message);
			contentPanel.add(lblErrorMessage, "2, 2");
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator, "1, 4, 2, 1");
		}
		{
			JLabel lblAdditionalInformation = new JLabel(DictionaryMainWindow.getInstance().getLocalisedString("additionalInformation"));
			contentPanel.add(lblAdditionalInformation, "2, 6");
		}
		{	
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "2, 8, fill, fill");
			{
				StringBuilder errorTextBuilder = new StringBuilder();
				errorTextBuilder.append(DictionaryMainWindow.getInstance().getLocalisedString("errorMessage")+"\n");
				errorTextBuilder.append(message + "\n\n");
				errorTextBuilder.append(DictionaryMainWindow.getInstance().getLocalisedString("exceptionMessage")+"\n");
				errorTextBuilder.append(ex.getMessage() + "\n\n");
				
				StringWriter stackTraceWriter = new StringWriter();
				ex.printStackTrace(new PrintWriter(stackTraceWriter));
				errorTextBuilder.append(DictionaryMainWindow.getInstance().getLocalisedString("stackTrace") + "\n");
				errorTextBuilder.append(stackTraceWriter.toString());
				errorText = errorTextBuilder.toString();
				
				JTextArea textArea = new JTextArea(errorText);
				textArea.setEditable(false);
				scrollPane.setViewportView(textArea);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(DictionaryMainWindow.getInstance().getLocalisedString("ok"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				{
					JButton btnCopyInformation = new JButton(DictionaryMainWindow.getInstance().getLocalisedString("copyErrorInformation"));
					btnCopyInformation.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errorText), new ClipboardOwner() {
								@Override
								public void lostOwnership(Clipboard clipboard, Transferable contents) {
									// Not important for us.
								}
							});
						}
					});
					buttonPane.add(btnCopyInformation);
				}
				okButton.setActionCommand(DictionaryMainWindow.getInstance().getLocalisedString("ok"));
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	public static void showError(String message, Exception ex) {
		ErrorDialog dialog = new ErrorDialog(message, ex);
		dialog.setVisible(true);
	}
}
