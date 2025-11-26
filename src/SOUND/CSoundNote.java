package SOUND;

import Utils.Converter;
import Utils.Utils;


/**

 * This class Object desribes a Tonal Value for the C_Sound sco file
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class CSoundNote implements Cloneable{
    // Start from one tone to another: start_old + dauer
    // it is ok to have more than one note at the same time mark !
	public double delay;	// delay before the note starts. delay begins with start!
    public double start;	//When this tone will start
    public double dauer, tempo;	// how long it lasts
    public double pDauer; // pedal duration
    public double freq;		// the frequency
    public int generator;	// The used generator function
    public int amplitude;	// the amplitude to make the tone loud or not
    public int envelope, misc;	// the used envelope function for this
    public double balance;	// balance between the 2 stereo channels
    // Project3 
    public double v1a;        // 2 times vibrato, amplitude, speed (4 Entrys)
    public double v1s;
    public double v2a;        
    public double v2s;
    public double iokt;
    public double t1,t2,t3;// Splitter
    public int iform,iband;
    public OneNote note = null, parent;
    //---------------
    public int voices;		// Number of voices active at this time (together with this) 
    public int channel = 0; 	// The number of the used MidiChannel for this note (equals Instrument)
    public boolean isAkkord = false;
    OneNote ndVoice = null;
    	
    public CSoundNote(double s, double t, double d, double b, double freq, int gen, int amp, int env, boolean isAkkord) {
		this.start = s;
		this.isAkkord = isAkkord;
		this.dauer = d;
		this.tempo = t;
		this.freq = freq;
		this.generator = gen;
		this.amplitude = amp;
		this.envelope = env;
		this.balance = b;
    }
    public String toString() {
    	String out = "Start="+start+" tempo="+tempo+" Dauer="+dauer+" freq="+freq+" gen="+generator+" amp="+amplitude+" env="+envelope+" bal="+balance;
    	return out;
    }
    
    public String saveAsString() {
    	String s = this.start+","+this.isAkkord+","+this.dauer+","+this.freq+","+this.voices+","+this.channel; // 5
    	s += ","+this.generator+","+this.amplitude+","+this.envelope+","+this.balance+","+this.pDauer; // 10
    	s += ","+this.note.pedal+","+this.note.freq+","+this.note.note+","+this.note.Octave; // 14
    	s += ","+this.note.noteIndex+","+this.note.midiIndex+","+this.note.distance+","+this.note.note;//18
    	s += ","+this.tempo+","+this.delay;
    	return s;
    }
    
    public static CSoundNote getFromString(String s) {
    	String v[] = new Utils().getSeparatedValues(s, ',');
    	double st = Converter.getDouble(v[0], 0.0);
    	double da = Converter.getDouble(v[2], 0.0);
    	double ba = Converter.getDouble(v[9], 0.0);
    	double fr = Converter.getDouble(v[3], 0.0);
    	int gen = Converter.getInt(v[6], 0);
    	int amp = Converter.getInt(v[7], 0);
    	int env = Converter.getInt(v[8], 0);
    	boolean isAk = Converter.checkState(v[1]);
    	double te = Converter.getDouble(v[19], 0.0);
    	CSoundNote ret = new CSoundNote(st, te, da, ba, fr, gen, amp, env, isAk);
    	ret.pDauer = Converter.getDouble(v[10], 0.0);
    	ret.voices = Converter.getInt(v[4], 0);
    	ret.channel = Converter.getInt(v[5], 0);
    	double nfr = Converter.getDouble(v[12], 0.0);
    	int nn = Converter.getInt(v[13], 0);
    	int oct = Converter.getInt(v[14], 0);
    	ret.note = new OneNote(nfr, nn, oct);
    	ret.note.pedal = Converter.checkState(v[11]);
    	ret.note.noteIndex = Converter.getInt(v[15], 0);
    	ret.note.midiIndex = Converter.getInt(v[16], 0);
    	ret.note.distance = Converter.getInt(v[17], 0);
    	ret.note.note = v[18];
    	ret.tempo = Converter.getDouble(v[19], 0.0);
    	ret.delay = Converter.getDouble(v[20], 0.0);
    	return ret;
    }
    public CSoundNote clone() throws CloneNotSupportedException {
		return (CSoundNote) super.clone();
	}
    
    public int compareTo(CSoundNote n) {
    	Double o1 = new Double(this.start);
    	Double o2 = new Double(n.start);
    	return o1.compareTo(o2); 
    }
    
    public double getEndTime() {
    	return this.start + this.dauer + this.delay / 1000.0; 
    }
} // end of class





