package SOUND;

import java.text.DecimalFormat;
import java.util.*;

import com.sun.jdi.Method;

import java.io.DataOutputStream;
import java.io.PrintWriter;

import Utils.Converter;
import Utils.Utils;


/**
 * This class Object holds a table of weights for a frequency range
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Soundscape {

    /** The intervall between 2 frequency entrys in the table */
    int step;

    /** The start frequenz of the frequency table */
    int start;

    /** The stop frequenz of the frequency table */
    int stop;

    /** The default (min) value for the weight */
    double min;

    /** This is the table */
    OneNote table[];
    Vector<FitnessTable> history;
    private double addWeight[];
    double impact;
    CDebug qd;
    int Verbosity;
    double max;		// the maximum value in the table (weight)
    int fittest_freq = 0;	// The center frequency of the fittest individual
    int centerFreq;	// the new center frequency
    int size;		// size of the table
    int iteration;	// the n. iteration step
    public int seed;
    int f_cnt;
    
    PrintWriter prs;
    
    public OneNote freq[];		// first random Frequencys, weight is a param in this class
    //double weight[]; 	// weight for these freqencys deprecated!
    
    // State machine:
    int stateCnt = 0; 	// 0 is turned off, > 0 active !
    boolean up; 		// true is going up, false going down
    int mode = 0;		// which state machine to use to use 0= off 1=up/down
    OneNote trigger_index = null; // invalid 
    
    public Soundscape() {
    	this(null, 1);
    }
/**
 * Constructs a default table with 0-20000 Hz, in steps of 20, preset with 1.0
 */
    public Soundscape(CDebug qd, int ver) {
    	this(qd, ver, 0, 20000, 20, 0.0, 0.0, null);
    }
/**
 * Create a Table indexed by frequencys. The content of the table is a weight (double)
 * @param min the minimum frequenz (Hz)
 * @param max the maximum frequenz (Hz)
 * @param step the step - interval for the table (Hz)
 * @param def the default value for initial start
 * impact is random weight
 */
	public Soundscape(CDebug qd, int ver, int min, int max, int step, double def, double impact, PrintWriter prs) {
		this.qd = qd;
		this.prs = prs;
		this.Verbosity = ver;
		this.start = min;
		this.stop = max;
		this.step = step;
		this.min = def;
		this.impact = impact;
		// quantisized frequency steps for the usable spectrum (min-max) freq
		this.size = getStep( (this.stop - this.start), this.step);  // Size of the table !
		this.table = new OneNote[this.size + 1]; // a OneNote entry for each freq-step
		this.addWeight = new double[this.size + 1];
		this.history = new Vector<FitnessTable>(); 
		this.mode = 0;
		this.up = true;
		this.stateCnt = 0;
		
		debugOut("RandomTable.Constructor min="+min+"Hz max="+max+"Hz step="+step+"Hz table-size ="+size+" def="+def, 4); 
		for ( int n = 0; n < this.size +1; n++) {
		    table[n] = new OneNote(this.min, 0, 0);
		    addWeight[n] = impact;	// Vorbelegung des variablen degressionswertes
		}
    }
    // This calculates the table-index from the given frequency 
    private int getStep( int freq, int step) {
    	//return (int) ((double) freq / (double) this.step + 0.5);
    	return (int) ((double) freq / (double) this.step);
    }
    // This calculates the table-index from the given frequency 
    private int getFreq( int index) {
    	return this.start + index * this.step;
    }
    public void debugOut(String tmp, int v) {
		if (v == 55 && this.prs != null)  // print !
		    this.prs.println(tmp);
		else {
		    if (v > this.Verbosity) return;
		    if (this.qd != null) this.qd.put(tmp);
		    else System.out.println(tmp);
		}
    }
    /**
     * Find the maximum weight in this table
     * @return maximum
     */
    public double getMaxx() {
		double max = 0.0;
		for (int n = 0; n < this.size; n++) {
		    if (table[n].weight > max) {
			max = table[n].weight;
			fittest_freq = start + n * this.step;
		    }
		}
		debugOut("RandomTable.getMax: max="+max, 6);
		this.max = max;
		return max;
    }

    /**
     * Write a value for one frequency into the table and modify the neighbours too !
     * Do an impact into the soundscape
     * @param freq the center frequency to address the table
     * @param val the value to be set at the center - frequency (not anmore,
     * Now the val comes from the addWeight - table
     * @param degression a value that describes how much the center freq. add value should be changed
     * @param footPrint the difference frequency we have to respect (left and right of the center frequency) // footprint
     * @return true if sucessful 
     */
    @SuppressWarnings("unused")
	public boolean writeNEntrys( OneNote seed, double degression, int footPrint) {
		debugOut("RandomTable.writeNEntrys() freq="+seed.freq, 5);
		int center = getStep( (int)seed.freq - this.start, this.step);	// the center index for this frequenz
		if (center < 0)
			center = 0;
		debugOut("RandomTable.writeNEntrys() that is rt-Index="+center+" equals f ="+getFreq(center), 5);
		//debugOut("RandomTable.writeNEntrys() that is rt-Index="+center+" equals f ="+getFreq(center), 55);
		
		this.centerFreq = (int)seed.freq;
		int steps = getStep( footPrint, this.step); // how many steps to go left and right
		int index;
		double f;
		//System.out.println("writeNEntrys() this.size="+this.size+" freq="+freq+" center="+center+" steps="+steps); 
		//------------------
		if (center < 0)
			debugOut("Too small !", 1); 
		double val = this.addWeight[center]; // random Weight
		//debugOut("RandomTable.writeNEntrys(1) table[center]="+table[center]+" addWeight[center]="+addWeight[center], 55);
		this.table[center].weight += val;	// set center value using freq for weight...
		// Hier kann man nun eine ganze menge beeinflussen:
		// wenn nicht bei 0.0 for den rt-tabellenwert gestoppt wird, kann der
		// sogar negativ werden und somit kommt dieser ton wohl niemals wieder.
		// Man braucht also eine Abbruchbedingung f�r die degression ...
		if (this.table[center].weight < 0.0) {
		    this.table[center].weight = 0.0;
		    this.addWeight[center] = this.impact;// random Weight
		}
		//------- Now correct the center weight value: -------
		else 
			this.addWeight[center] += degression;
		//debugOut("RandomTable.writeNEntrys(2) table[center]="+table[center]+" addWeight[center]="+addWeight[center], 55);
		debugOut("RandomTable.writeNEntrys() this.size="+this.size+" index of center="+center+" steps, left and right="+steps, 6);
		for (int n = 1; n < steps; n++) {	// apply to neighbours
			 String me = "";
			index = center + n;
			f = (double) (steps - n) * val / (double) steps; // function value, linear curve
			me = addFootPrint(index,n,f);
		    // 2nd half
		    index = center - n;
		    me = addFootPrint(index,n,f);
		}
		return true;
    }
    
    private String addFootPrint(int index, int n, double f) {
    	// Note: I do not use warp around for array index overflows !
    	String me = "";
	    if (index < this.size) {
			this.table[index].weight += f;
			me = "table[index]="+this.table[index];
			debugOut("RandomTable.writeNEntrys() write value="+f+" to center+"+n+" ="+index, 6);
			if (this.table[index].weight < 0.0) {
			    this.table[index].weight = 0.0;
			    this.addWeight[index] = this.impact;//
			}
	    }
	    return me;
    }
    
    @SuppressWarnings("unused")
	public String getTableAsString(double start) {
    	DecimalFormat f = new DecimalFormat("#.###");
    	String r = start+";";
    	for (int n= 0; n < this.table.length; n++) {
    		int i = (int)this.table[n].freq;
    		//r+= f.format(this.table[n].freq);
    		r += i;
    		if (n < (this.table.length-1 ))
    			r += ";";
    	}
    	return r;
    }
    
    public void writeTable(DataOutputStream dos) {
    	try {
    		
	    	for (int n= 0; n < this.table.length; n++) {
	    		int i = (int)this.table[n].freq;
	    		dos.writeInt(i);
	    	}
    	} catch (Exception ex) {}
    	return;
    }
    
    public void writeTable(DataOutputStream dos, int[] icache, int cIndex) {
    	System.out.println("Now write the table");
    	try {
	    	for (int n= 0; n < cIndex; n++) {
	    		int i = icache[n];
	    		dos.writeInt(i);
	    	}
    	} catch (Exception ex) {}
    	
    }
    
    public void setTableFromString( String s) {
    	s = s.replaceAll(",", ".");
    	String[] t = new Utils().getSeparatedValues(s, ';');
    	this.table = new OneNote[t.length -1];
    	
    	for (int n= 1; n < t.length; n++) {
    		OneNote nt = new OneNote(Converter.getDouble(t[n], 0.0), 0, 0);
    		this.table[n-1] = nt;
    	}
    	
    }
    /**
     * Write a value into the table
     * @param freq the frequency to address the table
     * @param val the value to be set
     * @return true if sucessful 
     */
    public boolean writeEntry(int freq, double val) {
		int index;
		try {
		    index = getStep( freq - this.start, this.step);
		    this.table[index].freq = val;
		} catch (ArrayIndexOutOfBoundsException e) {
		    e.printStackTrace();
		    return false;
		}
		return true;
    }
    /**
     * Read a value from the table
     * @param freq the frequency to address the table (index)
     * @return the value of weight, -1.0 if error
     */
    public double readEntry(int freq) {
		int index;
		double val = -1;
		try {
		    index = getStep( freq - this.start, this.step);
		    //System.out.println("maxindex="+this.table.length+" freq="+freq+" index="+index);
		   if (index >= 0)
			   val = addWeight[index]; //error from OneNote introduction this.table[index].freq;
		    //System.out.println("val="+val);
		} catch (ArrayIndexOutOfBoundsException e) {
		    e.printStackTrace();
		    return -1.0;
		}
		return val;
    }
    /**
     * Add a value to the table
     * @param freq the frequency to address the table
     * @param val the value to be added
     * @return the new value , -1.0 if error
     */
    public double addEntry(int freq, double val) {
	int index;
	try {
	    index = getStep( freq - this.start, this.step);
	    this.table[index].freq += val;
	} catch (ArrayIndexOutOfBoundsException e) {
	    e.printStackTrace();
	    return -1.0;
	}
	return this.table[index].freq;
    }
    /**
     * Substract a value from the table
     * @param freq the frequency to address the table
     * @param val the value to be substracted, 'min' is the minimum value, we can not go below it
     * reaching 'min', will not return an error !
     * @return the new value , -1.0 if error
     */
    public double subEntry(int freq, double val) {
		int index;
		try {
		    index = getStep( freq - this.start, this.step);
		    this.table[index].freq -= val;
		    if (this.table[index].freq < this.min) this.table[index].freq = this.min;
		} catch (ArrayIndexOutOfBoundsException e) {
		    e.printStackTrace();
		    return -1.0;
		}
		return this.table[index].freq;
    }
} // end of class
