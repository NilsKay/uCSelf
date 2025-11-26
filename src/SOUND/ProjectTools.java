package SOUND;

import java.io.PrintWriter;
import java.util.Vector;
import java.util.function.BiConsumer;

import Utils.Converter;

public class ProjectTools {

	public final static int maxFOFLoudIndex = 8; // do not reach this index !
	
	 public static final String[] Loudness = new String[] {
				"pause", "ppp", "pp", "p", "mp", "mf", "f", "ff", "fff"};

	 public static final int[] loud = new int[] {
			    		0 , 35   , 45  , 55 , 62  , 68  , 75 , 85  , 90 };
			    
	 public static double getBal(int fittest, int freq, int start, int stop) {
	    	return (0.5 * ( 1 + (double) (freq - fittest) / (double) ( stop - start)));
		
	    }
	   
	 
	 public static int getAmplitude(double weight, double Wmax, int Amin, int Amax, double amplifier) {
		//	debugOut("getAmplitude: weight="+Converter.formatDouble(weight, 8)+" Wmax="+Converter.formatDouble(Wmax, 8)+" Amin="+Amin+" Amax="+Amax+" amplifier="+Converter.formatDouble(amplifier, 8), 6);//6
			double ampval;	// weight 
			ampval = weight * amplifier;// the weight from the table for this freq * amplifier
			// loudness
			int amplitude = Amin + (int) ((Amax - Amin) * (1 - (Wmax - ampval) / Wmax));
			//debugOut("amplitude = "+amplitude, 6); // 6
			// Now map this amplitude to or loud-table
			int val;
			val = getValFromTable(loud, amplitude);
			//System.out.println("Amin="+Amin+" Amax="+Amax+" ampval="+ampval+" amplitude="+amplitude+" val="+val);
			return val; // default
	    }
	   
	    /**
	     * @return the tbl value from the given val
	     */
	    private static int getValFromTable(int[] tbl, int val) {
			int n, diff, pos;
			for (n = 1; n < tbl.length; n++) {
			    diff = tbl[n] - tbl[n-1];
			    pos = tbl[n-1] + diff/2;	
			    if ( val < pos ) return tbl[n-1];
			    else if (val <= tbl[n]) return tbl[n];
			}
			return tbl[n-1]; // max
	    }
		
	    public static void makeWeight(int n, Soundscape rt, double r_Weight, BiConsumer<String, Integer> debugOut) {
			int i;
			double t;
			//rt.weight = new double[rt.freq.length];
			// first get the weight from the RandomTable:
			debugOut.accept("makeWeight() Get the weight for the new random frequencys:", 6);
			for (i = 0; i < rt.freq.length; i++) {
			    rt.freq[i].weight = t = rt.readEntry((int)rt.freq[i].freq); // read individuum from soundscape
			    // randomize this weight:
			    // 1. : this.weight[i] = this.weight[i] * r_Weight * java.lang.Math.random();
			    rt.freq[i ].weight -= r_Weight * java.lang.Math.random();
			    debugOut.accept("makeWeight() weight for frequenz ="+rt.freq[i]+" Hz ="+t+" randomized="+rt.freq[i].weight, 6);
			    //debugOut("makeWeight() weight for frequenz ="+this.freq[i]+" Hz ="+t+" randomized="+this.weight[i]+" index="+i, 55);
			}
	    }
	    
	/**
     * Check if this freq is a double
     * @param freq the frequency to be tested
     * @param sti the vector with the used frequencys
     * @return true if it is a double
     */
    @SuppressWarnings("rawtypes")
	public static boolean checkDouble(int freq, Vector sti) {
		int n, v;
		for (n = 0; n < sti.size(); n++) {
		    v = ((Integer) sti.elementAt(n)).intValue();
		    if (v == freq) return true;
		}
		return false;
    }

    /**
     * The fitter an individual is, the smaller the resulting value is, within the 
     * Bandwitdth that we provide:
     * @return the calculated double value
     */
    public static double getKleiner(double w, double Wmax, double Amin, double Amax) {
    	if (Wmax == 0.0) return Amin;
    	//Utils.Converter.doBreak();
    	double v = (Amin + ((Amax - Amin) * (Wmax - w) / Wmax));
    	if (v < 0.0) {
    		//System.out.println("dauer="+v);
    		v = Amin;
    	}
    	return v;
    }
    
	/**
     * 
     * @param in
     * @param quant
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static Vector<Vector> getInterval(Vector<Note> in, boolean quant, int duration_step_index) {
    	double start = 0.0;
    	int index = 0;
    	Vector<Vector> mi = new Vector<Vector>();
    	double oneQuant = 0.0; 
    	if (duration_step_index > 0) {
	    	oneQuant = (double) Defaults.durations[duration_step_index] / 1000.0;//this.durations[this.def.duration_step_index]; //getDurationFromTable(dauer);
	    }
    	while(index < in.size()) {
    		Note main = in.elementAt(index);
    		Vector<Note> intervall = new Vector<Note>();
    		//System.out.println(index+" Add first element at"+main.start+" "+main.note);
    		if (quant) 
				main.start = start;
    		intervall.addElement(main);
    		// find the next time-start (beginn of the next interval)
    		index += 1;
    		
    		if (index < in.size()) {
	    		Note nextN = in.elementAt(index);
	    		double limit = start;
	    		if (quant)
	    			limit = start + oneQuant - 0.01;
	    		boolean add = nextN.start <= limit;
	    		while(index < in.size() && add) {
	    			//System.out.println(index+" Interval="+mi.size()+" Add next element at "+nextN.start+" "+nextN.note);
	    			if (quant) {
	    				nextN.start = start;
	    				//System.out.println("Quantisation: change start to"+start);
	    			}
	    			intervall.addElement(nextN);
	    			index += 1;
	    			if (index < in.size()) {
	    				nextN = in.elementAt(index);
	    				add = nextN.start <= limit;
	    			}
	    			else 
	    				break;
	    			//System.out.println("interval start="+start+" actual ="+nextN.start);
	    		}
	    		mi.addElement(intervall);
	    		if (!quant)
	    			start = nextN.start;
	    		else {
	    			start += oneQuant;
	    			//System.out.println("New Intervall start="+start);
	    		}
	    		//System.out.println("Do Next Interval start ="+start+ " at index="+index);
    		}
    		else 
    			break; // end of loop
    	}
    	
    	return mi;
    }
    
	public static void addToKomposition(Note nt, PrintWriter skompo) {
    	if (skompo != null)
    		skompo.println(nt.saveAsString());
    }
	
	 /**
     * Align all note-start times to match the quantisation
     * @param in
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked"})
	public static Vector<Note> quantisize(Vector<Note> in, int duration_step_index) {
    	Vector<Vector> interval = getInterval(in, true, duration_step_index); // A vector of vectors for each interval 
    	
    	// now convert the intervalls back to one Note Vector:
    	Vector<Note> result = new Vector<Note>();
    	for (int n = 0; n < interval.size(); n++) {
    		Vector<Note> iv = (Vector) interval.elementAt(n);
    		result.addAll(iv);
    	}
    	
    	return result;
    }
    
	public static int getNoteFromString(String s) {
    	int res = 0;
    	for (int n = 0; n < ProjectModel.nts.length; n++) {
    		if(s.equalsIgnoreCase(ProjectModel.nts[n])) {
    			res = n;
    			break;
    		}
    	}
    	return res;
    }
	
	/**
     * Calculate the frequency from the note-nr
     * @param n the number of this note
     * @return the frequency
     */
    private static double getFreq(int n) {
    	double p = java.lang.Math.pow(2.0, ((double) n++ / 12.0));	// 2^(n/12)
    	double f = 8.175798916 * p; 
    	return f;
    }
    
    private static double getFreq(int n, int o) {
    	n += 12 * o;
    	return getFreq(n);
    }
    


    @SuppressWarnings("static-access")
	public static int mapAplitude(int a) {
    	int max = loud[loud.length-1]; // max. c-sound ampl.
    	int res = ( a * 127 / max);
    	return res; // midi has 127 as max. amplitude val.
    }
    
	 public static String digestNote(Note nt, boolean fof, int delay, BiConsumer<String, Integer> debugOut) {
	    	double st = nt.start + (nt.delay / 1000.0);
	    	String tmp  = ProjectModel.DO+"\t"+Converter.formatDouble(st, 12,6)+"\t";
	    	if (nt.misc == 222)
	    		tmp  = ProjectModel.DOMELODY+"\t"+Converter.formatDouble(st, 12,6)+"\t";
	    	double dau = nt.dauer;
	    	if (nt.note.pedal)
	    		dau = nt.pDauer;
	    	if (delay > 0)
	    		dau += delay / 1000.0;
	    	tmp += Converter.formatDouble(dau, 12, 6);
	    	if (nt.freq > 0) tmp += "\t"+Converter.formatDouble(nt.freq, 10);
	    	if (nt.generator > 0) tmp += "\t"+nt.generator;
	    	if (nt.amplitude >= 0) tmp += "\t"+nt.amplitude;
	    	if (nt.envelope > 0 ) tmp += "\t"+nt.envelope;
	    	if (nt.balance > 0 ) tmp += "\t"+Converter.formatDouble(nt.balance, 6, 4);// auch formatieren
	    	// Anzahl Stimmen bei fof
	    	if (fof ) tmp += "\t\t"+nt.voices;
	    	//debugOut("dauer="+nt.dauer+" dd="+dd, 55);
	    	//debugOut(nt.toString(), 55);
	    	//debugOut(tmp, 55);
	    	debugOut.accept("add line:"+tmp, 6);
	    	return tmp;
	    }
	
/**
 * Translate the Loudness-code (f, ff etc.) into an amplitude value
 */
    public static int getLoundness(String code, boolean fof) {
		int n;
		int max = loud.length;
		if (fof) max = maxFOFLoudIndex;
		for (n = 0; n < max; n++) {
		    //System.out.println("Loudness["+n+"]="+Loudness[n]+"code="+code);
		    if (code.equals(Loudness[n])) return loud[n];
		}
		// default
		return loud[n]; //MAX
    }
}
