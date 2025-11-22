package Sound;

import java.util.*;
import java.io.PrintWriter;


/**
 * This class Object holds a table of weights for a frequency range
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class RandomTable {

    /** The intervall between 2 frequency entrys in the table */
    int step;

    /** The start frequenz of the frequency table */
    int start;

    /** The stop frequenz of the frequency table */
    int stop;

    /** The default (min) value for the weight */
    double min;

    /** This is the table */
    double table[];
    Vector history;
    double addWeight[];
    double impact;
    CDebug qd;
    int Verbosity;
    double max;		// the maximum value in the table (weight)
    int fittest_freq = 0;	// The center frequency of the fittest individual
    int centerFreq;	// the new center frequency
    int size;		// size of the table
    int iteration;	// the n. iteration step
    public int seed;
    PrintWriter prs;
    
    public RandomTable() {
	this(null, 1);
    }
/**
 * Constructs a default table with 0-20000 Hz, in steps of 20, preset with 1.0
 */
    public RandomTable(CDebug qd, int ver) {
	this(qd, ver, 0, 20000, 20, 0.0, 0.0, null);
    }
/**
 * Create a Table indexed by frequencys. The content of the table is a weight (double)
 * @param min the minimum frequenz (Hz)
 * @param max the maximum frequenz (Hz)
 * @param step the step - interval for the table (Hz)
 * @param def the default value for initial start
 */
    public RandomTable(CDebug qd, int ver, int min, int max, int step, double def, double impact, PrintWriter prs) {
	this.qd = qd;
	this.prs = prs;
	this.Verbosity = ver;
	this.start = min;
	this.stop = max;
	this.step = step;
	this.min = def;
	this.impact = impact;
	this.size = getStep( (this.stop - this.start), this.step);  // Size of the table !
	this.table = new double[this.size + 1];
	this.addWeight = new double[this.size + 1];
	this.history = new Vector(); 
	debugOut("RandomTable.Constructor min="+min+"Hz max="+max+"Hz step="+step+"Hz table-size ="+size+" def="+def, 4); 
	for ( int n = 0; n < this.size; n++) {
	    table[n] = this.min;
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
	    if (table[n] > max) {
		max = table[n];
		fittest_freq = start + n * this.step;
	    }
	}
	debugOut("RandomTable.getMax: max="+max, 6);
	this.max = max;
	return max;
    }

    /**
     * Write a value for one frequency into the table and modify the neighbours too !
     * @param freq the center frequency to address the table
     * @param val the value to be set at the center - frequency (not anmore,
     * Now the val comes from the addWeight - table
     * @param degression a value that describes how much the center freq. add value should be changed
     * @param diff the difference frequency we have to respect (left and right of the center frequency)
     * @return true if sucessful 
     */
    public boolean writeNEntrys( int freq, double degression, int diff) {
	debugOut("RandomTable.writeNEntrys() freq="+freq, 5);
	int center = getStep( freq - this.start, this.step);	// the center index for this frequenz
	debugOut("RandomTable.writeNEntrys() that is rt-Index="+center+" equals f ="+getFreq(center), 5);
	//debugOut("RandomTable.writeNEntrys() that is rt-Index="+center+" equals f ="+getFreq(center), 55);
	
	this.centerFreq = freq;
	int steps = getStep( diff, this.step); // how many steps to go left and right
	int index;
	double f;
	//System.out.println("writeNEntrys() this.size="+this.size+" freq="+freq+" center="+center+" steps="+steps); 
	//------------------
	double val = this.addWeight[center];
	//debugOut("RandomTable.writeNEntrys(1) table[center]="+table[center]+" addWeight[center]="+addWeight[center], 55);
	this.table[center] += val;	// set center value
	// Hier kann man nun eine ganze menge beeinflussen:
	// wenn nicht bei 0.0 for den rt-tabellenwert gestoppt wird, kann der
	// sogar negativ werden und somit kommt dieser ton wohl niemals wieder.
	// Man braucht also eine Abbruchbedingung für die degression ...
	if (this.table[center] < 0.0) {
	    this.table[center] = 0.0;
	    this.addWeight[center] = this.impact;
	}
	//------- Now correct the center weight value: -------
	else this.addWeight[center] += degression;
	//debugOut("RandomTable.writeNEntrys(2) table[center]="+table[center]+" addWeight[center]="+addWeight[center], 55);
	debugOut("RandomTable.writeNEntrys() this.size="+this.size+" index of center="+center+" steps, left and right="+steps, 6);
	for (int n = 1; n < steps; n++) {	// apply to neighbours
	    index = center + n;
	    // linear  auch neg ?: 
	    f = (double) (steps - n) * val / (double) steps; // function value, linear curve
	    //System.out.println("(a) n="+n+" index="+index+" f="+f);
	    // Note: I do not use warp around for array index overflows !
	    if (index < this.size) {
		this.table[index] += f;
		debugOut("RandomTable.writeNEntrys() write value="+f+" to center+"+n+" ="+index, 6);
		//System.out.println("writeNEntrys() < index="+index);
	    }
	    // 2nd half
	    index = center - n;
	    //System.out.println("(b) n="+n+" index="+index+" f="+f);
	    if (index >= 0) {
		this.table[index] += f;
		debugOut("RandomTable.writeNEntrys() write value="+f+" to center-"+n+" ="+index, 6);
		//System.out.println("writeNEntrys() >= index="+index);
	    }
	}
	return true;
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
	    this.table[index] = val;
	} catch (ArrayIndexOutOfBoundsException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }
    /**
     * Read a value from the table
     * @param freq the frequency to address the table
     * @return the value , -1.0 if error
     */
    public double readEntry(int freq) {
	int index;
	double val;
	try {
	    index = getStep( freq - this.start, this.step);
	    val = this.table[index];
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
	    this.table[index] += val;
	} catch (ArrayIndexOutOfBoundsException e) {
	    e.printStackTrace();
	    return -1.0;
	}
	return this.table[index];
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
	    this.table[index] -= val;
	    if (this.table[index] < this.min) this.table[index] = this.min;
	} catch (ArrayIndexOutOfBoundsException e) {
	    e.printStackTrace();
	    return -1.0;
	}
	return this.table[index];
    }
} // end of class
