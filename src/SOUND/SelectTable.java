package SOUND;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;


@SuppressWarnings("serial")
public class SelectTable extends JTable {
	
	public final Object[] longValues = {
			"Note", "6753", 
			"67567.8843"};
	//ListMouseListener lma;
	
	public SelectTable(SelectModel model) {
		super(model);
		this.getTableHeader().setReorderingAllowed( false ); // so the user connot re-order our table header !
		
		this.setPreferredScrollableViewportSize(new Dimension(250, 300));
		//------------ Selection model
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//-------- Cell-Renderer -----------
		this.setDefaultRenderer(String.class, new MyTableCellRenderer(model));
		this.setDefaultRenderer(Integer.class, new MyTableCellRenderer(model));
		this.setDefaultRenderer(Double.class, new MyTableCellRenderer(model));
		//------------- Set up column sizes.
		initColumnSizes(model); // horizontal dir !
		
		//------------- add a Mouselistener
		this.setColumnSelectionAllowed(false); 
		
		/*this.lma = new ListMouseListener(this.parnt, table);
		table.addMouseListener(lma); 
		*/
		if (model.data != null && model.data.size() > 0) setSelectedRow(0);
		
	}
	public void setSelectedRow(int row) {
		this.getSelectionModel().setSelectionInterval(row, row);
	}
	/**
	 * To find out if the user has modified this table-column sizes
	 * @param model
	 * @return
	 */
	 public int[] getColumnSizes(SelectModel model) {
		 int[] sizes = new int[model.columnNames.length];
		 for (int i = 0; i < model.columnNames.length; i++) {
			 TableColumn column = this.getColumnModel().getColumn(i);
			 sizes[i] = column.getWidth();
		 }
		 return sizes;
	 }
	 /*
	     * This method picks good column sizes.
	     * If all column heads are wider than the column's cells' 
	     * contents, then you can just use column.sizeWidthToFit().
	     */
	    @SuppressWarnings("unused")
		public void initColumnSizes(SelectModel model) {
	        TableColumn column = null;
	        Component comp = null;
	        int headerWidth = 0;
	        int cellWidth = 0;
	        Object[] longValues = this.longValues;
	        for (int i = 0; i < model.columnNames.length; i++) {
	            column = this.getColumnModel().getColumn(i);
	            comp = this.getDefaultRenderer(model.getColumnClass(i)).
	                             getTableCellRendererComponent(
	                                 this, longValues[i],
	                                 false, false, 0, i);
	            ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
/*
	            cellWidth = comp.getPreferredSize().width;
	            
	            
	            
	            if (i == JDFTableModel.EXPAND) {
	            	//column.setPreferredWidth(5);
	            	column.setMinWidth(parnt.c_sizes[JDFTableModel.EXPAND]);
	            	column.setMaxWidth(parnt.c_sizes[JDFTableModel.EXPAND]);
	            }
	            */
	            
	        }
	    } 
	    
	    class MyTableCellRenderer extends DefaultTableCellRenderer{
	    	/**
		 * 
		 */
	    	Color tbg = Color.white;
	        Color fg = Color.black;
	        private static final long serialVersionUID = 113542L;
			SelectModel model;
	    	
	    	public MyTableCellRenderer(SelectModel model) {
	    		this.model = model;
	    	}
			public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected,
		                                                    boolean hasFocus,int row, int column){
				JLabel label =(JLabel)super.getTableCellRendererComponent( table,value,isSelected,
										       hasFocus,row,column);
			    
			   // value = model.getValueAt(row, column);
			 
			    if (isSelected) {
					label.setForeground(table.getSelectionForeground());
					label.setBackground(table.getSelectionBackground());
			    }
			    else {
			    	label.setForeground(fg);
			    	int rest = row % 2;
			    	Color col = rest == 0?tbg:new Color(0xe0e0e0); 
			    	label.setBackground(col);
			    }
			    return label;  
			}
	    }   
}