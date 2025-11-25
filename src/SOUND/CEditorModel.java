package SOUND;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import Utils.Converter;

public class CEditorModel {
	// Text Input:
	public int MINFREQ = 0;
	public int MAXFREQ = 1;
	public int SEED = 2;
	public int MINDUR = 3;
	public int MAXDUR = 4;
	public int POPULATION = 5;
	public int NUMBERFITTEST = 6;
	public int INTERVAL = 7;
	public int FOOTPRINT = 8;
	public int RANDOMWEIGHT = 9;
	public int DEGRESSION = 10;
	public int ITERATIONEN = 11;
	public int DELAY = 12;
	public int Max = 13;
	
	//Checkbox
	public int USENOTES = 0;
	public int PREFLOWERNOTES = 1;
	public int DOSTEREO = 2;
	public int DOAKKORD = 3;
	public int DOSHADOW = 4;
	public int USESELECTION = 5;
	public int cMax = 6;
	
	//Combo
	public int DURSTEPS = 0;
	public int MINLOUD = 1;
	public int MAXLOUD = 2;
	public int MINVOICE = 3;
	public int MAXVOICE = 4;
	public int GAMMA = 5;
	public int dMax = 6;
	
	// Button
	public int SELECT = 0;
	public int PLAY = 1;
	public int SAVE = 2;
	public int START = 3;
	public int STOP = 4;
	public int bMax = 5;
	
	public JLabel[] la, uni, cla;
	public JTextField[] ptfs;
	public JComboBox[] combo;
	JButton bt[];
    JCheckBox cb[];
    
    Font font, subFont;
    int fontsize = 11;	
    int subFontsize = 9;
    int digits = 10;	// Anzahl Stellen
    int post = 5;		// Post colon digits
    CEditor ce;
    //Defaults def;
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CEditorModel(CEditor ce) {
		this.ce = ce;
		Toolkit tool = Toolkit.getDefaultToolkit();
	    Dimension d = tool.getScreenSize();
	    if (d.width <= 800) {
	    	this.font = new Font("Helvetica", Font.PLAIN, fontsize);
	    	this.subFont = new Font("Helvetica", Font.PLAIN, subFontsize);
	    	ce.gd = new Dimension(550, 130);
	    }
	    else {
	    	this.font = new Font("Helvetica", Font.PLAIN, fontsize+2);
	    	this.subFont = new Font("Helvetica", Font.PLAIN, subFontsize+2);
	    	ce.gd = new Dimension(550, 250);
	    }
		this.la = new JLabel[Max];
		this.uni = new JLabel[Max];
		this.ptfs = new JTextField[Max];
		this.cb = new JCheckBox[cMax];
		this.cla = new JLabel[dMax];
		this.combo = new JComboBox[dMax];
		this.bt = new JButton[bMax];
		
		this.font = new Font("Helvetica", Font.PLAIN, fontsize);
		this.subFont = new Font("Helvetica", Font.PLAIN, subFontsize);
		
		bt[SELECT] = new JButton(ce.def.text[74]);
		bt[PLAY] = new JButton(ce.def.text[63]);
		bt[SAVE] = new JButton(ce.def.text[64]);
		bt[START] = new JButton(ce.def.text[14]);
		bt[STOP] = new JButton(ce.def.text[31]);
		
		bt[PLAY].setEnabled(false);
		bt[SAVE].setEnabled(false);
		bt[STOP].setEnabled(false);
		//---------------------
		getTextPanel(MINFREQ, ce.def.text[19], ce.def.text[25]); // Hz
		getTextPanel(MAXFREQ, ce.def.text[18], ce.def.text[25]); // Hz
		getTextPanel(SEED, ce.def.text[46], ce.def.text[25]); // Hz
		getTextPanel(MINDUR, ce.def.text[24], ce.def.text[26]); // sec
		getTextPanel(MAXDUR, ce.def.text[42], ce.def.text[26]); // sec
		getTextPanel(POPULATION, ce.def.text[20], null);
		getTextPanel(NUMBERFITTEST, ce.def.text[21], null);
		getTextPanel(INTERVAL, ce.def.text[23], ce.def.text[25]);
		getTextPanel(FOOTPRINT, ce.def.text[29], ce.def.text[25]);//footprint
		getTextPanel(RANDOMWEIGHT, ce.def.text[27], null); //Randomweight
		getTextPanel(DEGRESSION, ce.def.text[72], null);
		getTextPanel(ITERATIONEN, ce.def.text[28], null);
		getTextPanel(DELAY, ce.def.text[99], ce.def.text[100]);
		
		cb[USENOTES] = new JCheckBox(ce.def.text[30]);
		cb[USENOTES].setSelected(ce.def.tonal);
		cb[PREFLOWERNOTES] = new JCheckBox(ce.def.text[65]);
		cb[PREFLOWERNOTES].setSelected(ce.def.preferLowerNotes);
		cb[DOSTEREO] = new JCheckBox(ce.def.text[43]);
	    cb[DOSTEREO].setSelected(ce.def.stereo);
	    cb[DOAKKORD] = new JCheckBox(ce.def.text[91]);
	    cb[DOAKKORD].setSelected(ce.def.akkord);
	    cb[DOSHADOW] = new JCheckBox(ce.def.text[92]);
	    cb[DOSHADOW].setSelected(ce.def.pedal);
	    cb[USESELECTION] = new JCheckBox(ce.def.text[79]);
	    if (!ce.def.tonal) {
	    	ce.def.useSelectNotes = false;
	    }
	    cb[USESELECTION].setSelected(ce.def.useSelectNotes);
	    
		Vector elements = new Vector();
		for(int n = 0; n < ce.def.durationTable.length; n++) 
			elements.addElement(ce.def.durationTable[n]);
		getComboPanel(DURSTEPS, ce.def.text[71], elements);
	    combo[DURSTEPS].setSelectedIndex(ce.def.duration_step_index);	
	    //---
	    elements = setLChoice(ce.def);
	    getComboPanel(MINLOUD, ce.def.text[44], elements);
	    combo[MINLOUD].setSelectedItem(ce.def.min_amp);	
	  //---
	    elements = setHChoice(ce.def);
	    getComboPanel(MAXLOUD, ce.def.text[45], elements);
	    combo[MAXLOUD].setSelectedItem(ce.def.max_amp);	
	    
	    elements = setLVoice(ce.def);
	    getComboPanel(MINVOICE, ce.def.text[52], elements);
	    combo[MINVOICE].setSelectedItem(Integer.toString(ce.def.min_voice));
	    
	    elements = setLVoice(ce.def);
	    getComboPanel(MAXVOICE, ce.def.text[53], elements);
	    combo[MAXVOICE].setSelectedItem(Integer.toString(ce.def.max_voice));	
	    
	    elements = new Vector();
	    for (int qz = 0; qz < Project2.GAMMAT.length; qz++) 
	    	elements.addElement(Double.toString(Project2.GAMMAT[qz]));
	    getComboPanel(GAMMA, ce.def.text[54], elements);
	    combo[GAMMA].setSelectedItem(Double.toString(ce.def.gamma));	
	    
	    //--- Font:
	    for (int n = 0; n < this.ptfs.length; n++)
			this.ptfs[n].setFont(this.font);
	    for (int n = 0; n < this.la.length; n++)
			this.la[n].setFont(this.font);
	    for (int n = 0; n < this.cla.length; n++)
			this.cla[n].setFont(this.font);
		for (int n = 0; n < this.ptfs.length; n++)
			this.ptfs[n].setFont(this.font);
		for (int n = 0; n < this.cb.length; n++)
			this.cb[n].setFont(this.font);
		for (int n = 0; n < this.combo.length; n++)
			this.combo[n].setFont(this.font);
		for (int n = 0; n < this.bt.length; n++)
			this.bt[n].setFont(this.font);
	}
	
	public void addListeners(CEditor c) {
		for (int n = 0; n < this.ptfs.length; n++)
			this.ptfs[n].addActionListener(c);
		
		for (int n = 0; n < this.ptfs.length; n++)
			this.ptfs[n].addFocusListener(c);
		for (int n = 0; n < this.cb.length; n++)
			this.cb[n].addItemListener(c);
		for (int n = 0; n < this.combo.length; n++)
			this.combo[n].addActionListener(c);
		for (int n = 0; n < this.bt.length; n++)
			this.bt[n].addActionListener(c);
	}
	
	private void getTextPanel(int index, String latext, String unit) {
	
		la[index] = new JLabel(latext, JLabel.LEFT);
		la[index].setFont(this.subFont);
		ptfs[index] = new JTextField(this.formatValue(0.0), this.digits);
		if (unit != null) {
		    uni[index] = new JLabel(unit, JLabel.LEFT);
		    uni[index].setFont(this.subFont);
		}
    }
	
	@SuppressWarnings("rawtypes")
	public void getComboPanel(int index, String latext, Vector elements) {
		cla[index] = new JLabel(latext, JLabel.LEFT);
		cla[index].setFont(this.subFont);
		combo[index] = new JComboBox(elements);
		combo[index].setFont(this.subFont);
    }
	
	 public String formatValue(double v) {
	    	return formatDValue(v);
	 }
	 
	 /**
	  * Convert double Value to a formatted Display Value
	  */
	 public String formatDValue(double v) {
		 return Converter.formatDouble(v, this.digits, this.post);
	 }
	 
	 public String formatIValue(int v) {
		 return Converter.formatInt(v, this.digits);
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	 public Vector setLChoice(Defaults def) {
		 int n;
		 int max = ProjectTools.Loudness.length;
		 Vector v = new Vector();
		 if (ce.def.newFOF) max = ProjectTools.maxFOFLoudIndex;
		 for(n = 0; n < max; n++) {
			 v.addElement(ProjectTools.Loudness[n]);
			 if (ce.def.max_amp.equals(ProjectTools.Loudness[n])) break;
		 }
		 return v;
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector setHChoice(Defaults def) {
		 int n;
		 boolean flag = false;
		 Vector v = new Vector();
		 int max = ProjectTools.Loudness.length;
		 if (ce.def.newFOF) max = ProjectTools.maxFOFLoudIndex;
		 for(n = 0; n < max; n++) {
			 if (ce.def.min_amp.equals(ProjectTools.Loudness[n])) flag = true;
			 if (flag) v.addElement(ProjectTools.Loudness[n]);
		 }
		 return v;
	 }
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	 public Vector setLVoice(Defaults def) {
		 int n;
		 Vector v = new Vector();
		 for(n = 0; n < ce.def.max_voice; n++) {
			 v.addElement(Integer.toString(n+1)); 
		 }
		 return v;
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	 public Vector setHVoice(Defaults def) {
		 int n;
		 Vector v = new Vector();
		 for(n = ce.def.min_voice; n <= ce.def.voiceMax; n++) {
			 v.addElement(Integer.toString(n));
		 }
		 return v;
	 }
	 @SuppressWarnings("rawtypes")
	public void setActual(CEditor ce) {
			String wert;
			if (ce.def.lastPath != null) 
				ce.CustomPath = ce.def.lastPath;
			ce.mi2_4.setSelected(this.ce.def.mode == 0);
			ce.mi2_5.setSelected(this.ce.def.mode > 0);
			wert = Converter.formatInt(this.ce.def.max_freq, this.digits);
			ptfs[MAXFREQ].setText(wert);
			wert = Converter.formatInt(this.ce.def.min_freq, this.digits);
			ptfs[MINFREQ].setText(wert);
			wert = Converter.formatInt(this.ce.def.interval, this.digits);
			ptfs[INTERVAL].setText(wert);
			wert = Converter.formatInt(this.ce.def.seed, this.digits);
			ptfs[SEED].setText(wert);
			wert = Converter.formatDouble(this.ce.def.min_tempo, this.digits, this.post);
			ptfs[MINDUR].setText(wert);
			wert = Converter.formatDouble(this.ce.def.max_tempo, this.digits, this.post);
			ptfs[MAXDUR].setText(wert);
			wert = Converter.formatInt(this.ce.def.fittest, this.digits);
			ptfs[NUMBERFITTEST].setText(wert);
			wert = Converter.formatDouble(this.ce.def.r_Weight, this.digits, this.post);
			ptfs[RANDOMWEIGHT].setText(wert);
			wert = Converter.formatDouble(this.ce.def.degression, this.digits, this.post);
			ptfs[DEGRESSION].setText(wert);
			wert = Converter.formatInt(this.ce.def.population, this.digits);
			ptfs[POPULATION].setText(wert);
			wert = Converter.formatInt(this.ce.def.iterations, this.digits);
			ptfs[ITERATIONEN].setText(wert);
			wert = Converter.formatInt(this.ce.def.delay, this.digits);
			ptfs[DELAY].setText(wert);
			wert = Converter.formatDouble(this.ce.def.diff_freq, this.digits, this.post);
			ptfs[FOOTPRINT].setText(wert);
			if (combo[MINLOUD] != null) {
			    Vector v = setLChoice(this.ce.def);
			    combo[MINLOUD].removeAllItems();
			    for (int pi = 0; pi < v.size() ; pi++) 
			    	combo[MINLOUD].addItem(v.elementAt(pi));
			    combo[MINLOUD].setSelectedItem(ce.def.min_amp);
			}
			if (combo[MAXLOUD] != null) {
			    Vector v = setHChoice(this.ce.def);
			    combo[MAXLOUD].removeAllItems();
			    for (int pi = 0; pi < v.size() ; pi++) 
			    	combo[MAXLOUD].addItem(v.elementAt(pi));
			    combo[MAXLOUD].setSelectedItem(ce.def.max_amp);
			}
			if (cb[DOSTEREO] != null) cb[DOSTEREO].setSelected(this.ce.def.stereo);
			if (cb[USENOTES] != null) 
				cb[USENOTES].setSelected(this.ce.def.tonal);
			this.bt[SELECT].setEnabled(this.ce.def.useSelectNotes);
			if (cb[USESELECTION] != null) cb[USESELECTION].setSelected(this.ce.def.useSelectNotes);
			if (cb[DOAKKORD] != null) cb[DOAKKORD].setSelected(this.ce.def.akkord);
			if (cb[DOSHADOW] != null) cb[DOSHADOW].setSelected(this.ce.def.pedal);
			if (combo[MINVOICE] != null) {
			    Vector v = setLVoice(this.ce.def);
			    combo[MINVOICE].removeAllItems();
			    for (int pi = 0; pi < v.size() ; pi++) 
			    	combo[MINVOICE].addItem(v.elementAt(pi));
			    combo[MINVOICE].setSelectedItem(Integer.toString(ce.def.min_voice));
			}
			if (combo[MAXVOICE] != null) {
			    Vector v = setHVoice(this.ce.def);
			    combo[MAXVOICE].removeAllItems();
			    for (int pi = 0; pi < v.size() ; pi++) 
			    	combo[MAXVOICE].addItem(v.elementAt(pi));
			    combo[MAXVOICE].setSelectedItem(Integer.toString(ce.def.max_voice));
			}
			if (combo[GAMMA] != null) combo[GAMMA].setSelectedItem(Double.toString(ce.def.gamma));
			if (combo[DURSTEPS] != null) 	
			    this.combo[DURSTEPS].setSelectedIndex(this.ce.def.duration_step_index);
	    }
	 
	 public void enableChildren(CEditor ce, boolean b) {
		 for (int n = 0; n < this.ptfs.length; n++)
			 this.ptfs[n].setEnabled(b);
		 for (int n = 0; n < this.cb.length; n++)
			 this.cb[n].setEnabled(b);
		 for (int n = 0; n < this.combo.length; n++)
			 this.combo[n].setEnabled(b);
		 ce.m1.setEnabled(b);
		 ce.m2.setEnabled(b);
		 cb[DOAKKORD].setEnabled(b & this.ce.def.tonal);
	    }
}