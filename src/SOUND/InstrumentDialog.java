package SOUND;


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import Utils.Utils;



@SuppressWarnings("serial")
public class InstrumentDialog extends JDialog {
	
	CEditor ce;
	Dimension screenSize;
	JComboBox jc[];
	
	public InstrumentDialog(CEditor ce) {
		super((JFrame) ce, ce.def.text[89], true);
		this.ce = ce;
		this.setResizable(false);
		
		if (ce.mid.sb == null)  {
			 new Utils().doMessagePane( ce.def.text[90]);
			 doQuit();
		}
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add("Center", getSComponents());
		JPanel bot = new JPanel();
		JButton close = new JButton(ce.def.text[78]);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveInstr();
				InstrumentDialog.this.ce.State = true;
				doQuit();
			}
		});
		bot.add(close);
		pane.add("South", bot);
		this.getContentPane().add(pane);
		pack();
		ce.def.setNewLocation(ce, this);
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
		this.setVisible(true);
	}
	public void saveInstr() {
		for (int n = 0; n < this.jc.length; n++) {
			int sel = this.jc[n].getSelectedIndex();
			this.ce.def.setInstrumentForChannel(n, sel);
		}
	}
	public void doQuit() {
    	this.setVisible(false);
    }
	private JToolBar getSComponents() {
		Vector<String> elements = new Vector<String>();
		this.ce.mid.open(ce.def);
		for (int n = 0; n < this.ce.mid.instruments.length; n++) 
			elements.addElement(this.ce.mid.instruments[n].getName()); // instrName[n]
			
		this.jc = new JComboBox[ce.def.voiceMax+ce.def.melodyChannels];
		JToolBar pa = new JToolBar();
		pa.setFloatable(false);
		//b.add(Box.createVerticalStrut(130));
		pa.setOrientation(JToolBar.VERTICAL);
		for (int n = 0; n < this.jc.length; n++) {
			this.jc[n] = new JComboBox(elements);
			this.jc[n].setSelectedIndex(this.ce.def.getInstrumentForChannel(n));
			JPanel pl = new JPanel();
			pl.add(new JLabel(""+(n+1)));
			pl.add(this.jc[n]);
			pa.add(pl);
			pa.add(Box.createVerticalStrut(10));
		}
		return pa;
	}
}