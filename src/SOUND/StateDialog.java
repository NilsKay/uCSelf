package SOUND;


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import Utils.Converter;


@SuppressWarnings("serial")
public class StateDialog extends JDialog {
	
	CEditor ce;
	Dimension screenSize;
	
	
	StateModel model;
	StateUi ui;
	boolean busy;
	
	
	public StateDialog(CEditor ce) {
		super((JFrame) ce, ce.def.text[109], true);
		this.ce = ce;
		this.model = new StateModel();
		this.ui = new StateUi();
		this.setResizable(false);
		
		model.close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.save();
				StateDialog.this.ce.State = true;
				doQuit();
			}
		});
		model.quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//model.save();
				//StateDialog.this.ce.State = true;
				doQuit();
			}
		});
		model.addIntListeners(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (busy ) return;
				Object o = e.getSource();
				busy = true;
				if (o == model.ptfs[model.RANGE]) {
					model.r = Converter.getInt(model.ptfs[model.RANGE].getText(), -1);
					if (model.r < 0)
						model.r = 0;
					//StateDialog.this.ce.def.range = model.r;
					model.ptfs[model.RANGE].setText(""+model.r);
				}
				else if (o == model.ptfs[model.TRIGGER]) {
					model.t = Converter.getInt(model.ptfs[model.TRIGGER].getText(), -1);
					if (model.t < 1)
						model.t = 1;
					model.ptfs[model.TRIGGER].setText(""+model.t);
				}
				else if (o == model.ptfs[model.CASCADECOUNT]) {
					model.cc = Converter.getInt(model.ptfs[model.CASCADECOUNT].getText(), -1);
					if (model.cc < 1)
						model.cc = 1;
					model.ptfs[model.CASCADECOUNT].setText(""+model.cc);
				}
				else if (o == model.ptfs[model.STEPUP]) {
					model.cu = Converter.getInt(model.ptfs[model.STEPUP].getText(), -1);
					if (model.cu < 1)
						model.cu = 1;
					model.ptfs[model.STEPUP].setText(""+model.cu);
				}
				else if (o == model.ptfs[model.STEPUP]) {
					model.cd = Converter.getInt(model.ptfs[model.STEPDOWN].getText(), -1);
					if (model.cd < 1)
						model.cd = 1;
					model.ptfs[model.STEPDOWN].setText(""+model.cd);
				}
				else if (o == model.ptfs[model.LOOPDEPTH]) {
					model.loopDepth = Converter.getInt(model.ptfs[model.LOOPDEPTH].getText(), -1);
					if (model.loopDepth < 1)
						model.loopDepth = 1;
					model.ptfs[model.LOOPDEPTH].setText(""+model.loopDepth);
				}
				else if (o == model.ptfs[model.LOOPREPEAT]) {
					model.loopRepeat = Converter.getInt(model.ptfs[model.LOOPREPEAT].getText(), -1);
					if (model.loopRepeat < 1)
						model.loopRepeat = 1;
					model.ptfs[model.LOOPREPEAT].setText(""+model.loopRepeat);
				}
				else if (o == model.ptfs[model.MINTEMPO]) {
					model.minTempo = Converter.getInt(model.ptfs[model.MINTEMPO].getText(), -1);
					if (model.minTempo < 10)
						model.minTempo = 10;
					model.ptfs[model.MINTEMPO].setText(""+model.minTempo);
				}
				else if (o == model.ptfs[model.MAXTEMPO]) {
					model.maxTempo = Converter.getInt(model.ptfs[model.MAXTEMPO].getText(), -1);
					if (model.maxTempo > 120)
						model.maxTempo = 120;
					model.ptfs[model.MAXTEMPO].setText(""+model.maxTempo);
				}
				else if (o == model.ptfs[model.STEP]) {
					model.speedStep = Converter.getInt(model.ptfs[model.STEP].getText(), -1);
					if (model.speedStep < 10)
						model.speedStep = 10;
					if (model.speedStep > 180)
						model.speedStep = 180;
					model.ptfs[model.STEP].setText(""+model.speedStep);
				}
				busy = false;
			}
		});
		this.getContentPane().add(ui);
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
	
	public void doQuit() {
    	this.setVisible(false);
    }
	
	
	class StateModel {
		//Checkbox
		public int USECASCADE = 0;
		public int USELOOP = 1;
		public int USESPEED = 2;
		public int PERMUTATION = 3;
		public int cMax = 4;
		
		// Int input:
		public int RANGE = 0;
		public int TRIGGER = 1;
		public int CASCADECOUNT = 2;
		public int STEPUP = 3;
		public int STEPDOWN = 4;
		public int LOOPDEPTH = 5;
		public int LOOPREPEAT = 6;
		public int MINTEMPO = 7;
		public int MAXTEMPO = 8;
		public int STEP = 9;
		public int Max = 10;
		
		JCheckBox cb[];
		JLabel la[];
		JTextField ptfs[];
		JButton close;
		JButton quit;
		int r,t, cc, cu, cd;
		int loopDepth, loopRepeat, minTempo, maxTempo,speedStep;
		
		public StateModel() {
			close = new JButton(ce.def.text[78]);
			quit = new JButton(ce.def.text[87]); // cancel
			this.cb = new JCheckBox[cMax];
			cb[USECASCADE] = new JCheckBox(ce.def.text[106]);
			cb[USECASCADE].setSelected(ce.def.useCascade);
			cb[USELOOP] = new JCheckBox(ce.def.text[107]);
			cb[USELOOP].setSelected(ce.def.useLoop);
			cb[USESPEED] = new JCheckBox(ce.def.text[108]);
			cb[USESPEED].setSelected(ce.def.useSpeed);
			cb[PERMUTATION] = new JCheckBox(ce.def.text[123]);
			cb[PERMUTATION].setSelected(ce.def.permutation);
			cb[PERMUTATION].setToolTipText(ce.def.text[124]);
			
			la = new JLabel[Max];
			ptfs = new JTextField[Max];
			
			int index = RANGE;
			la[index] = new JLabel(ce.def.text[101], JLabel.LEFT);
			this.r = ce.def.range;
			ptfs[index] = new JTextField(""+ce.def.range, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[110]);
			index = TRIGGER;
			la[index] = new JLabel(ce.def.text[102], JLabel.LEFT);
			this.t = ce.def.trigger;
			ptfs[index] = new JTextField(""+ce.def.trigger, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[111]);
			
			index = CASCADECOUNT;
			la[index] = new JLabel(ce.def.text[105], JLabel.LEFT);
			this.cc = ce.def.cascadeCount;
			ptfs[index] = new JTextField(""+cc, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[112]);
			index = STEPUP;
			la[index] = new JLabel(ce.def.text[103], JLabel.LEFT);
			this.cu = ce.def.stepUp;
			ptfs[index] = new JTextField(""+cu, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[113]);
			index = STEPDOWN;
			la[index] = new JLabel(ce.def.text[104], JLabel.LEFT);
			this.cd = ce.def.stepDown;
			ptfs[index] = new JTextField(""+cd, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[114]);
			index = LOOPDEPTH;
			la[index] = new JLabel(ce.def.text[115], JLabel.LEFT);
			this.loopDepth = ce.def.loopDepth;
			ptfs[index] = new JTextField(""+loopDepth, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[117]);
			index = LOOPREPEAT;
			la[index] = new JLabel(ce.def.text[116], JLabel.LEFT);
			this.loopRepeat = ce.def.loopRepeat;
			ptfs[index] = new JTextField(""+loopRepeat, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[118]);
			
			index = MINTEMPO;
			la[index] = new JLabel(ce.def.text[119], JLabel.LEFT);
			this.minTempo = ce.def.minTempo;
			ptfs[index] = new JTextField(""+minTempo, ce.digits);
			//ptfs[index].setToolTipText(ce.def.text[118]);
			
			index = MAXTEMPO;
			la[index] = new JLabel(ce.def.text[120], JLabel.LEFT);
			this.maxTempo = ce.def.maxTempo;
			ptfs[index] = new JTextField(""+maxTempo, ce.digits);
			
			index = STEP;
			la[index] = new JLabel(ce.def.text[121], JLabel.LEFT);
			this.speedStep = ce.def.speedStep;
			ptfs[index] = new JTextField(""+speedStep, ce.digits);
			ptfs[index].setToolTipText(ce.def.text[122]);
			
		}
		
		public void addIntListeners(ActionListener al) {
			for (int n = 0; n < Max; n++)
				this.ptfs[n].addActionListener(al);
		}
		public void save() {
			ce.def.permutation = cb[PERMUTATION].isSelected();
			ce.def.useSpeed = cb[USESPEED].isSelected();
			ce.def.useLoop = cb[USELOOP].isSelected();
			ce.def.useCascade = cb[USECASCADE].isSelected();
			ce.def.range = Converter.getInt(ptfs[RANGE].getText(), 0);
			ce.def.trigger = Converter.getInt(ptfs[TRIGGER].getText(), 3);
			ce.def.cascadeCount = Converter.getInt(ptfs[CASCADECOUNT].getText(), 5);
			ce.def.stepUp = Converter.getInt(ptfs[STEPUP].getText(), 1);
			ce.def.stepDown = Converter.getInt(ptfs[STEPDOWN].getText(), 1);
			ce.def.loopDepth = Converter.getInt(ptfs[LOOPDEPTH].getText(), 1);
			ce.def.loopRepeat = Converter.getInt(ptfs[LOOPREPEAT].getText(), 1);
			ce.def.minTempo = Converter.getInt(ptfs[MINTEMPO].getText(), 1);
			ce.def.maxTempo = Converter.getInt(ptfs[MAXTEMPO].getText(), 1);
			ce.def.speedStep = Converter.getInt(ptfs[STEP].getText(), 1);
			
		}
	}
	
	class StateUi extends JPanel{
		JTabbedPane tab;
		
		public StateUi() {
			this.setLayout(new BorderLayout());
			this.tab = new JTabbedPane();
			this.tab.add(do1stLayout(), "Main");
			this.tab.add(doCascadeLayout(), ce.def.text[106]);
			this.tab.add(doLoopLayout(), ce.def.text[107]);
			this.tab.add(doSpeedLayout(), ce.def.text[108]);
			
			this.add("Center", this.tab); 
			this.add("South", getTool()); 
		}
		private JPanel getTool() {
			JPanel p = new JPanel();
			p.add(model.close);
			p.add(model.quit);
			
			return p;
		}
		
		private JComponent do1stLayout() {
	    	FormLayout layout = new FormLayout(
	    			"left:pref,3dlu,l:pref,5dlu,  l:pref, 3dlu, l:pref,5dlu",
	    			"p, 3dlu, " +
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu, p,1dlu,p,10dlu," +
	    			"p, 3dlu,"+		
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu");
	    			
	    	layout.setColumnGroups(new int[][]{{3, 7}});
	        PanelBuilder builder = new PanelBuilder(layout);
	        builder.setDefaultDialogBorder();
	        // Obtain a reusable constraints object to place components in the grid.
	        CellConstraints cc = new CellConstraints();
	        int row = 1;
	        //--------------- First Row
	        builder.addSeparator(ce.def.text[51],		cc.xyw (1,  row, 8));
	        row += 2;
	        builder.add(model.cb[model.USECASCADE],		cc.xy (3,  row));
	        builder.add(model.cb[model.USELOOP],		cc.xy (7,  row));
	        row += 2;
	        builder.add(model.cb[model.USESPEED],		cc.xy (3,  row));
	        row += 2;
	        builder.add(model.la[model.RANGE],			cc.xy (1,  row));
	        builder.add(model.ptfs[model.RANGE],		cc.xy (3,  row));
	        builder.add(model.la[model.TRIGGER],		cc.xy (5,  row));
	        builder.add(model.ptfs[model.TRIGGER],		cc.xy (7,  row));
	        row += 2;
	        //builder.addSeparator("",		cc.xyw (1,  row, 8));
	        return builder.getPanel();
		}
		private JComponent doCascadeLayout() {
	    	FormLayout layout = new FormLayout(
	    			"left:pref,3dlu,l:pref,5dlu,  l:pref, 3dlu, l:pref,5dlu",
	    			"p, 3dlu, " +
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu, p,1dlu,p,10dlu," +
	    			"p, 3dlu,"+		
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu");
	    			
	    	layout.setColumnGroups(new int[][]{{3, 7}});
	        PanelBuilder builder = new PanelBuilder(layout);
	        builder.setDefaultDialogBorder();
	        // Obtain a reusable constraints object to place components in the grid.
	        CellConstraints cc = new CellConstraints();
	        int row = 1;
	        //--------------- First Row
	        builder.addSeparator(ce.def.text[106],		cc.xyw (1,  row, 8));
	        row += 2;
	        builder.add(model.la[model.CASCADECOUNT],	cc.xy (1,  row));
	        builder.add(model.ptfs[model.CASCADECOUNT],	cc.xy (3,  row));
	        row += 2;
	        builder.add(model.la[model.STEPUP],			cc.xy (1,  row));
	        builder.add(model.ptfs[model.STEPUP],		cc.xy (3,  row));
	        builder.add(model.la[model.STEPDOWN],		cc.xy (5,  row));
	        builder.add(model.ptfs[model.STEPDOWN],		cc.xy (7,  row));
	        return builder.getPanel();
		}
		private JComponent doLoopLayout() {
	    	FormLayout layout = new FormLayout(
	    			"left:pref,3dlu,l:pref,5dlu,  l:pref, 3dlu, l:pref,5dlu",
	    			"p, 3dlu, " +
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu, p,1dlu,p,10dlu," +
	    			"p, 3dlu,"+		
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu");
	    			
	    	layout.setColumnGroups(new int[][]{{3, 7}});
	        PanelBuilder builder = new PanelBuilder(layout);
	        builder.setDefaultDialogBorder();
	        // Obtain a reusable constraints object to place components in the grid.
	        CellConstraints cc = new CellConstraints();
	        int row = 1;
	        //--------------- First Row
	        builder.addSeparator(ce.def.text[107],		cc.xyw (1,  row, 8));
	        row += 2;
	        builder.add(model.la[model.LOOPDEPTH],		cc.xy (1,  row));
	        builder.add(model.ptfs[model.LOOPDEPTH],	cc.xy (3,  row));
	        builder.add(model.la[model.LOOPREPEAT],		cc.xy (5,  row));
	        builder.add(model.ptfs[model.LOOPREPEAT],	cc.xy (7,  row));
	        row += 2;
	        builder.add(model.cb[model.PERMUTATION],	cc.xy (3,  row));
	        return builder.getPanel();
		}
		
		private JComponent doSpeedLayout() {
	    	FormLayout layout = new FormLayout(
	    			"left:pref,3dlu,l:pref,5dlu,  l:pref, 3dlu, l:pref,5dlu",
	    			"p, 3dlu, " +
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu, p,1dlu,p,10dlu," +
	    			"p, 3dlu,"+		
	    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu");
	    			
	    	layout.setColumnGroups(new int[][]{{3, 7}});
	        PanelBuilder builder = new PanelBuilder(layout);
	        builder.setDefaultDialogBorder();
	        // Obtain a reusable constraints object to place components in the grid.
	        CellConstraints cc = new CellConstraints();
	        int row = 1;
	        //--------------- First Row
	        builder.addSeparator(ce.def.text[108],		cc.xyw (1,  row, 8));
	        row += 2;
	        builder.add(model.la[model.MINTEMPO],		cc.xy (1,  row));
	        builder.add(model.ptfs[model.MINTEMPO],		cc.xy (3,  row));
	        builder.add(model.la[model.MAXTEMPO],		cc.xy (5,  row));
	        builder.add(model.ptfs[model.MAXTEMPO],		cc.xy (7,  row));
	        row += 2;
	        builder.add(model.la[model.STEP],			cc.xy (1,  row));
	        builder.add(model.ptfs[model.STEP],			cc.xy (3,  row));
	       
	        return builder.getPanel();
		}
	}
}