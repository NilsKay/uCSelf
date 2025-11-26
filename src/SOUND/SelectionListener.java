package SOUND;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import SOUND.models.SelectModel;
import SOUND.ui.SelectDialog;
import Utils.Utils;


public class SelectionListener extends MouseAdapter {
	JTable tableView;
	public int button_now; 
	public int selectedRow, selectedColumn;
	SelectDialog sd;
	boolean tonal = false;
	
	public SelectionListener(SelectDialog sd, JTable tableView) {
		this.tableView = tableView;
	 	this.sd = sd;
	 	this.tonal = sd.ce.def.tonal;
	}
	
	@SuppressWarnings("unused")
	public void mousePressed(MouseEvent e) { //mouseClicked(MouseEvent e) {
	    button_now = e.getModifiers();
	    if (this.tonal)
	    	return;
	    SelectModel model = (SelectModel) tableView.getModel();
	    TableColumnModel columnModel = tableView.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
		int column = tableView.convertColumnIndexToModel(viewColumn);
		int selRow = tableView.rowAtPoint(e.getPoint());
		boolean enabled = model.data.size() > 0;
		
		int right_Button_Mask = MouseEvent.BUTTON3_MASK;
	    if ((button_now & right_Button_Mask ) > 0) {
	    	//&& md.mStatus == md.IDLE) {
			
			//------- Here react to clicks !
			if (e.getClickCount() >= 1) {
				Utils ut = new Utils(); 
			    //System.out.println("Right mousePressed clicked in JTable ! column="+column+" selRow="+selRow);
				String in = ut.doInputPane(
						sd.ce.def.text[77]+"   ("+sd.ce.def.text[19]+sd.ce.def.min_freq+", "+
								sd.ce.def.text[18]+sd.ce.def.max_freq+")");
				if (in != null && in.length() > 0) {
					int i = in.indexOf(';');
					if (i < 0) {
						// user entered no semicolons
						i = in.indexOf(',');
						if (i >= 0) // user entered , instead ?
							in = in.replaceAll(",", ";");
							
					}
					String[] sep = ut.getSeparatedValues(in, ';');
					if (sep != null) {
						Vector<OneNote> res = new Vector<OneNote>();
						for (int n = 0; n < sep.length; n++) {
							OneNote on = new OneNote(ut.getDouble(sep[n], 0), 0, -1);
							if (on.freq >= sd.ce.def.min_freq &&
							on.freq <= sd.ce.def.max_freq)
								res.addElement(on);
						}
						if (res.size() > 0) {
							model.data.addAll(res);
							model.sort();
							model.updateTable();
						}
					}
				}
			
			}
	    }
	    else  { // left
	    	if (e.getClickCount() > 1) { // double click
	    		
	    	}
	    	else { //single click
	    		/*this.selectedRow = selRow;
	    		this.selectedColumn = column;
	    		int[] sel = tableView.getSelectedRows();
	    		if (sel.length > 0) {
					Vector<OneNote> selN = new Vector<OneNote>();
					for (int n = 0; n < sel.length; n++) {
						selN.addElement(model.data.elementAt(sel[n]));
					}
					// Now remove the selected Notes from the right side:
					for (int n = 0; n < selN.size(); n++) {
						model.data.remove(selN.elementAt(n));
					}
					model.sort();
					model.updateTable();
					
				}
	    		*/
	    	}
	    	
	    }
	}
} // end of class ListMouseListener