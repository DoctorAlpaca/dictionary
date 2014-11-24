package de.eric_wiltfang.dictionary;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.eric_wiltfang.dictionary.local.Localization;

public class CostumFileChooser extends JFileChooser {
	private static final long serialVersionUID = -4713194705787991578L;

	@Override
	public void approveSelection(){
	    File f = getSelectedFile();
	    if(f.exists() && getDialogType() == SAVE_DIALOG){
	        int result = JOptionPane.showConfirmDialog(this, Localization.getInstance().get("dConfOverwrite"), Localization.getInstance().get("dConfOverS"), JOptionPane.YES_NO_CANCEL_OPTION);
			switch(result){
			case JOptionPane.YES_OPTION:
				super.approveSelection();
				return;
			case JOptionPane.NO_OPTION:
				return;
			case JOptionPane.CLOSED_OPTION:
				return;
			case JOptionPane.CANCEL_OPTION:
				cancelSelection();
				return;
			}
	    }
	    super.approveSelection();
	}
}
