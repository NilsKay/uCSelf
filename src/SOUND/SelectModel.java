package SOUND;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class SelectModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 17809783L;
	Object[] columnNames;
	Vector<OneNote> data;
	
	final int NOTE = 0;
	final int OCTAVE = 1;
	final int FREQ = 2;
	
	public SelectModel( Object[] columnNames, Vector<OneNote> input) {
		this.columnNames = columnNames;
		this.data = input; //new Vector<OneNote>();
	}
	
	/**
     * 
     * @param ima
     */
	public void setDataModel(Vector<OneNote> ima) {
		this.data = ima;
		//this.gui.jdftable.updateTable();
    }
    
    public int getColumnCount() {
    	return columnNames.length;
    }
    public int getRowCount() {
    	return data.size(); // 
    }
    
    public String getColumnName(int column) {
    	return columnNames[column].toString();
    }
    // Tabel data has changed in some way:-------------------
    public void updateTableRow(int index) {
    	
    	fireTableRowsUpdated(index, index);
    }
    
    public void updateTable() {
    	fireTableDataChanged();
    }
    public void sort() {
    	Collections.sort(this.data, new Comparator<OneNote>() {
 		   public int compare(OneNote o1, OneNote o2){
 			   
 			   return Double.compare(o1.freq, o2.freq);
 		   }
 		});
    }
    //-------------------------------------------------------
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		Object o = getValueAt(0, c);
		//System.out.println("got Object at column "+c+" ="+o);
		if (o == null) o = new String("null");
		return o.getClass();
    }
    /**
     * This Method fills the table with values, addressed by row, column
     *"Nr.", "Job", "Plate", "Circ.[mm]", "Status"};
     * @param row the row of the table
     * @param col the column
     * @return an Object representing the Value!
     */
    public Object getValueAt(int row, int col) {
		Object o = new String(" ");
		
		if (data == null || data.size() <= 0 || row >= this.getRowCount()) 
		    return o;
		
		OneNote ob = this.data.elementAt(row);
		
		if (ob == null) return o;
		switch (col) {
		
		case NOTE:	
			o = ob.note;
		    break;
		case OCTAVE:	
		    o = (ob.Octave > 0)?ob.Octave:"";
		    break;
		case FREQ:	
		    o = (int) ob.freq;
		    break;
		}
		return o;
    }
}