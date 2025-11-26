package SOUND.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import SOUND.CEditor;
import SOUND.OneNote;
import SOUND.SelectTable;
import SOUND.SelectionListener;
import SOUND.models.SelectModel;
import Utils.Utils;


@SuppressWarnings("serial")
public class SelectDialog extends JDialog {
	
	//MyGlobalKeys mgk; 
	public CEditor ce;
	Dimension screenSize;
	SelectModel model1, model2;
	SelectTable table1, table2;
	String colnames[] = {
		"Note ", "Oktave ", "Frequenz [Hz]"
	};
	
	JButton add, remove, close;
	
	public SelectDialog(CEditor ce, Vector<OneNote> allNotes, Vector<OneNote> selNotes) {
		super((JFrame) ce, ce.def.text[73], true);
		this.ce = ce;
		ImageIcon im2 = new Utils().getIcon((Component) this, "pfeil_gross_links_on.gif");
		ImageIcon im1 = new Utils().getIcon((Component) this, "pfeil_gross_rechts_on.gif");
		//this.mgk = new MyGlobalKeys(this);
		this.setResizable(false);
		if (!ce.def.tonal)
			allNotes = new Vector<OneNote>(); // Frequencys, not Notes !
		
		if (selNotes == null)
			selNotes = new Vector<OneNote>();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				AWTEvent event = (AWTEvent) ev;
				if (event.getID() == Event.WINDOW_DESTROY) {
					//dispose();
					doQuit();
				}	
			}
		});
		if (im1 != null)
			this.add = new JButton(im1); //ce.def.text[75]);
		else
			this.add = new JButton("Add");
		this.add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get selection
				int[] sel = table1.getSelectedRows();
				
				if (sel.length > 0) {
					SelectModel m =  (SelectModel) table2.getModel();
					SelectModel m1 =  (SelectModel) table1.getModel();
					Vector<OneNote> selN = new Vector<OneNote>();
					for (int n = 0; n < sel.length; n++) {
						selN.addElement(m1.data.elementAt(sel[n]));
					}
					// Now remove the selected Notes from the left side:
					for (int n = 0; n < selN.size(); n++) {
						m1.data.remove(selN.elementAt(n));
					}
					m.data.addAll(selN);
					model1.sort();
					model1.updateTable();
					model2.sort();
					model2.updateTable();
				}
			}
		});
		if (im1 != null)
			this.remove = new JButton(im2); //ce.def.text[76]);
		else
			this.remove = new JButton(ce.def.text[76]);
		this.remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get selection
				int[] sel = table2.getSelectedRows();
				
				if (sel.length > 0) {
					SelectModel m =  (SelectModel) table1.getModel();
					SelectModel m2 =  (SelectModel) table2.getModel();
					Vector<OneNote> selN = new Vector<OneNote>();
					for (int n = 0; n < sel.length; n++) {
						selN.addElement(m2.data.elementAt(sel[n]));
					}
					// Now remove the selected Notes from the right side:
					for (int n = 0; n < selN.size(); n++) {
						m2.data.remove(selN.elementAt(n));
					}
					m.data.addAll(selN);
					model1.sort();
					model2.sort();
					model1.updateTable();
					model2.updateTable();
					
				}
			}
		});
		this.add.setToolTipText(ce.def.text[83]);
		this.remove.setToolTipText(ce.def.text[84]);
		JToolBar b = new JToolBar();
		b.setFloatable(false);
		b.add(Box.createVerticalStrut(130));
		b.setOrientation(JToolBar.VERTICAL);
		b.add(this.add);
		b.add(Box.createVerticalStrut(10));
		b.add(this.remove);
		setButtonSizeForToolbar(this.add, new Dimension(40, 30));
		setButtonSizeForToolbar(this.remove, new Dimension(40, 30));
		//---------------
		this.model1 = new SelectModel(colnames, allNotes);
		this.table1 = new SelectTable(model1);
		JScrollPane tp1 = new JScrollPane(this.table1);
		
		this.model2 = new SelectModel(colnames, selNotes);
		this.table2 = new SelectTable(model2);
		this.table2.addMouseListener(new SelectionListener(this, table2));
		this.table2.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				processKey(e, false);
			}
		});
		JScrollPane tp2 = new JScrollPane(this.table2);
		tp2.addMouseListener(new SelectionListener(this, table2));
		
		this.close = new JButton(ce.def.text[78]);
		this.close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectDialog.this.ce.def.selectedNotes = SelectDialog.this.model2.data;
				SelectDialog.this.ce.State = true;
				doQuit();
			}
		});
		tp1.setBorder(BorderFactory.createTitledBorder(ce.def.text[81]));
		tp2.setBorder(BorderFactory.createTitledBorder(ce.def.text[82]));
		//---------------------
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		if (ce.def.tonal) {
			pane.add("West", tp1);
			pane.add("Center", b);
		}
		pane.add("East", tp2);
		JPanel ps =  new JPanel();
		ps.add(close);
		pane.add("South",ps);
		this.getContentPane().add(pane);
		pack();
		this.table1.setEnabled(ce.def.tonal);
		this.add.setEnabled(ce.def.tonal);
		this.remove.setEnabled(ce.def.tonal);
		ce.def.setNewLocation(ce, this);

		this.setVisible(true);
	}
	public void processKey(KeyEvent e, boolean pressed) {
		int keyCode = e.getKeyCode();
		System.out.println("processKey = "+keyCode);
		if (keyCode == KeyEvent.VK_DELETE ) {
    		
			int[] sel = table2.getSelectedRows();
			if (sel.length > 0) {
				Vector<OneNote> selN = new Vector<OneNote>();
				for (int n = 0; n < sel.length; n++) {
					selN.addElement(model2.data.elementAt(sel[n]));
				}
				// Now remove the selected Notes from the right side:
				for (int n = 0; n < selN.size(); n++) {
					model2.data.remove(selN.elementAt(n));
				}
				if (ce.def.tonal) {
					model1.data.addAll(selN);
				}
				model2.sort();
				model2.updateTable();
				model1.sort();
				model1.updateTable();
				
			}
		}
    }
	public static void setButtonSizeForToolbar( AbstractButton button, Dimension dim) {
		Dimension pref = button.getPreferredSize();
		
		if (dim.height != -1 && pref.height != dim.height) {
			pref.height = dim.height;
		}
		if (dim.width != -1 && pref.width < dim.width) {
			pref.width = dim.width;
		}
		
		button.setPreferredSize(pref);
		button.setMinimumSize(pref);
		button.setMaximumSize(pref);
    }
	 
	public void doQuit() {
    	this.setVisible(false);
    }
	
}