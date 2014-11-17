package de.eric_wiltfang.dictionary;

import java.util.Vector;

import javax.swing.JFrame;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Font;
import java.awt.Toolkit;

public class EditEntryWindow {	
	private JFrame frame;
	private JTextField wordTextField;
	private JTextField catTextField;
	private JTextField tagsTextField;
	private JTextArea notesTextArea;
	private JTextArea defTextArea;
	
	private Entry entry;
	private Dictionary dic;
	private boolean newEntryOnSave;
	private JButton btnCancel;
	private JButton btnSave;
	
	private static int x, y, width, height;

	/**
	 * Create the application.
	 * @wbp.parser.constructor
	 */
	public EditEntryWindow(Dictionary dic) {
		initialize();
		this.dic = dic;
		entry = new Entry();
	}
	public EditEntryWindow(Dictionary dic, Entry e) {
		initialize();
		this.dic = dic;
		setEntry(e);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// Used for saving on pressing Ctrl-Enter and cancelling on escape
		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ev) {
				if ((ev.getKeyCode() == KeyEvent.VK_ENTER) && ((ev.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)) {
					saveEntry();
				} else if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.setVisible(false);
					frame.dispose();
				}
			}
		};
		
		
		frame = new JFrame();
		frame.addKeyListener(keyAdapter);
		frame.setBounds(100, 100, 450, 300);
		if (width != 0 && height != 0) {
			frame.setBounds(x, y, width, height);
		} else {
			x = 100; y = 100; width = 450; height = 300;
		}
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,}));
		
		JLabel lblWord = new JLabel("Word");
		lblWord.setHorizontalAlignment(SwingConstants.RIGHT);
		frame.getContentPane().add(lblWord, "2, 2, right, default");
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent ev) {
				x = frame.getX();
				y = frame.getY();
			}
			@Override
			public void componentResized(ComponentEvent ev) {
				width = frame.getWidth();
				height = frame.getHeight();
			}
		});
		
		wordTextField = new JTextField();
		frame.getContentPane().add(wordTextField, "4, 2, 3, 1, fill, default");
		wordTextField.setColumns(10);
		wordTextField.addKeyListener(keyAdapter);
		
		JLabel lblDefinition = new JLabel("Definition");
		lblDefinition.setHorizontalAlignment(SwingConstants.RIGHT);
		frame.getContentPane().add(lblDefinition, "2, 4");
		
		JScrollPane defScrollPane = new JScrollPane();
		frame.getContentPane().add(defScrollPane, "4, 4, 3, 1, fill, fill");
		
		defTextArea = new JTextArea();
		defTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		defScrollPane.setViewportView(defTextArea);
		defTextArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					if (e.getModifiers() > 0) {
						wordTextField.grabFocus();
					} else {
						notesTextArea.grabFocus();
					}
					e.consume();
				}
			}
		});
		defTextArea.addKeyListener(keyAdapter);
		
		JLabel lblNotes = new JLabel("Notes");
		lblNotes.setHorizontalAlignment(SwingConstants.RIGHT);
		frame.getContentPane().add(lblNotes, "2, 6");
		
		JScrollPane notesScrollPane = new JScrollPane();
		frame.getContentPane().add(notesScrollPane, "4, 6, 3, 1, fill, fill");
		
		notesTextArea = new JTextArea();
		notesTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		notesScrollPane.setViewportView(notesTextArea);
		notesTextArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					if (e.getModifiers() > 0) {
						defTextArea.grabFocus();
					} else {
						catTextField.grabFocus();
					}
					e.consume();
				}
			}
		});
		notesTextArea.addKeyListener(keyAdapter);
		
		JLabel lblCategory = new JLabel("Category");
		lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
		frame.getContentPane().add(lblCategory, "2, 8, right, default");
		
		catTextField = new JTextField();
		frame.getContentPane().add(catTextField, "4, 8, 3, 1, fill, default");
		catTextField.setColumns(10);
		catTextField.addKeyListener(keyAdapter);
		
		JLabel lblTags = new JLabel("Tags");
		lblTags.setHorizontalAlignment(SwingConstants.RIGHT);
		frame.getContentPane().add(lblTags, "2, 10, right, default");
		
		tagsTextField = new JTextField();
		frame.getContentPane().add(tagsTextField, "4, 10, 3, 1, fill, default");
		tagsTextField.setColumns(10);
		tagsTextField.addKeyListener(keyAdapter);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveEntry();
			}
		});
		frame.getContentPane().add(btnSave, "4, 12");
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// TODO Maybe ask for saving?
				frame.setVisible(false);
				frame.dispose();
			}
		});
		frame.getContentPane().add(btnCancel, "6, 12");
	}

	/**
	 * Sets the Entry object and displays the data in the dialog.
	 */
	public void setEntry(Entry e) {
		entry = e;
		
		catTextField.setText(entry.getCategory());
		defTextArea.setText(entry.getDefinition());
		notesTextArea.setText(entry.getNotes());
		wordTextField.setText(entry.getWord());
		tagsTextField.setText(entry.getTagsAsString());
	}
	/**
	 * Updates the Entry object with the entered data. 
	 */
	private void updateEntry() {
		entry.setCategory(catTextField.getText());
		entry.setDefinition(defTextArea.getText());
		entry.setNotes(notesTextArea.getText());
		entry.setWord(wordTextField.getText());
		
		Vector<String> tags = new Vector<>();
		for (String s : tagsTextField.getText().split(",")) {
			tags.add(s.trim());
		}
		entry.setTags(tags);
	}
	private void saveEntry() {
		try {
			updateEntry();
			dic.insertEntry(entry);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "Error while saving: " + ex, "Error while saving", JOptionPane.ERROR_MESSAGE);
		}
		
		if (newEntryOnSave) {
			EditEntryWindow window = new EditEntryWindow(dic);
			window.setNewEntryOnSave(true);
			window.setVisible(true);
		}
		
		frame.setVisible(false);
		frame.dispose();
	}
	
	public void setVisible(boolean show) {
		frame.setVisible(show);
	}
	
	public void setNewEntryOnSave(boolean b) {
		newEntryOnSave = b;
		if (b) {
			btnSave.setText("Save & enter next");
			btnCancel.setText("Stop entering new words");
		} else {
			btnSave.setText("Save");
			btnCancel.setText("Cancel");
		}
	}
}
