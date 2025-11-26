package SOUND.models;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import SOUND.CSoundNote;
import SOUND.Defaults;
import SOUND.OneNote;
import SOUND.SoundInfoListener;
import SOUND.Soundscape;
import SOUND.ui.CDebug;
import Utils.GetEnviroment;

public class ProjectModel {

	public final static int DELAY = 50;
	public final static int DDELAY = 10;
	public SoundInfoListener sil;
	public Vector<String> cache;
    
    public int[] icache;
    public int cIndex;
    public String home;
    public PrintWriter logw = null;
    public PrintWriter imageIt = null;
    public DataOutputStream dos;
    // Loudness:
     // bei fof ist 90 zu laut, bei 85 Schluﬂ
    public static final double[] GAMMAT = new double[] {
	0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
	1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9,
        2.0, 2.1, 2.2, 2.3, 2.4, 3.0, 4.0, 5.0, 6.0 };

    public final static String[] nts = new String[] {
    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
    };
    
//-------------------
    public final static String DO = "i1"; 
    public final static String DOMELODY = "i7"; 
    
// ------------------    
    /**
     * Verbosity: 0= no, 1=important Messages, 2=less important, 3=low debug, 4=higher debug, 5 = high debug, 6 = crazy debug
     */
    public int Verbosity = 1;
    public int note_mode = 1;
    String header[];
    int noise_samples[];
    public  Vector<OneNote> notes;// a vector that holds the tonal notes frequencys
    //---------- Variables:
    public boolean halt = false;
    public int anfOkt;
    public double balance;
    public Soundscape rt, shadow;
    public Defaults def;
   // @SuppressWarnings("rawtypes")
    public Vector<CSoundNote> komposition;	// this is our Komposition ! (elements of Note)
    public PrintWriter prs;
    public  CDebug qd;
    public PrintWriter skompo = null;
    
    public ProjectModel(CDebug qd, int ver, SoundInfoListener sil, PrintWriter prs) {
    	this.prs = prs;
    	this.sil = sil;
    	this.Verbosity = ver;
    	this.qd = qd;
    }
    
    public void debugOut(String tmp, int v) {
		if (v == 55 && this.prs != null) { // print !
		    this.prs.println(tmp);
		}
		else {
		    if (v > this.Verbosity) return;
		    if (this.qd != null) this.qd.put(tmp);
		    else System.out.println(tmp);
		}
    }
    
    /**
     * Save the actual random table in a line
     * @param rt
     * @param start
     */
    public void addImageS(Soundscape rt, double start) {
    	
    	if (imageIt == null) {
    		cache = new Vector<String>();
    		String log = this.home+File.separator+GetEnviroment.IMAGELOG;
    		try {
    			this.imageIt = new PrintWriter(new FileOutputStream(log));
    		}
    		catch (java.io.IOException e) {
    			this.imageIt = null;
    		} // End of IO Excep
    	}
    	String t = rt.getTableAsString(start);
    	if (this.cache.size() < 50) {
    		cache.addElement(t);
    	}
    	else {
    		for (int n = 0; n < cache.size(); n++)
    			this.imageIt.println(cache.elementAt(n));
    		this.cache = new Vector<String>();
    	}
    }
    public void addLog(String t) {
    	if (logw == null) {
    		
    		String log = this.home+File.separator+GetEnviroment.KOMPOSITIONLOG;
    		try {
    			logw = new PrintWriter(new FileOutputStream(log));
    			// Headder Information
    			logw.println("# Logfile File for JcSelf.");
    		}
    		catch (java.io.IOException e) {
    			logw = null;
    		} // End of IO Excep
    	}
    	logw.println(t);
    	//System.out.println(t);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
   	public static Vector<OneNote> createNoteTable(int mode, int min_freq, int max_freq, int anfOkt) {
   		int maxOkt;
   		int n, m, q, freq;
   		double p, f;
   		Vector<OneNote> notes = new Vector();
   		System.out.println("Tonal, createNotetable: mode="+mode);
   		
   		// All possible midi notes
   		OneNote start = new OneNote(min_freq, 0, 0);
   		start.getMidiNote();
   		start.setMidiNr(start.midiIndex);
   		if (start.freq < min_freq) {
   			start.midiIndex++;
   			start.setMidiNr(start.midiIndex);
   		}
   		OneNote stop = new OneNote(max_freq, 0, 0);
   		stop.getMidiNote();
   		for (n = start.midiIndex; n <= stop.midiIndex; n++) {
   			if (n >=0 ) {
   				OneNote nt = new OneNote(0, 0, 0);
   				nt.setMidiNr(n);
   				notes.addElement(nt);
   				//debugOut("Note "+nt, 3);
   			}
   		}
   		   
   		// debug:
   		for (n = 0; n < notes.size(); n++) {
   		    double d = (notes.elementAt(n)).freq;
   		    int idx = getNoteIndex(d, notes, anfOkt);
//   		    debugOut("Note "+idx+"="+d, 3); 
   		}
   		return notes;
       }
    
    /**
     * Calculate the note-nr from the frequency
     * @param in the frequency
     * @return the number of this note
     */
    private static int getNoteIndex(double in, Vector<OneNote> notes, int anfOkt) {
		double d;
		int n;
		int off = 12 * anfOkt;
		for (n = 0; n < notes.size(); n++) {
		    d = notes.elementAt(n).freq;
		    if (d >= in ) return n+off;
		}
		return n+off; // max
    }
    
    /**
     * Enter new population into soundscape (With footprint)
     * @param rt
     * @param population
     * @param min
     * @param max
     */
    public static void createPopulation(Vector<OneNote> notes, Soundscape rt, int population, int min, int max, boolean tonal, boolean preferLowerNotes, boolean useSelectNotes) {
    	rt.freq = new OneNote[population];
    	double maxf = 0;
    	for (int i = 0; i < population; i++) {
    		rt.freq[i] = makeRandomFrequency(notes, min, max+1, tonal, preferLowerNotes, useSelectNotes);
    		if (rt.freq[i].freq > maxf )
    			maxf = rt.freq[i].freq;
    	}	
    	//System.out.println("makeNRandomFrequencys Max="+maxf);
    }

    public static OneNote makeRandomFrequency(Vector<OneNote> notes, int min, int max, boolean tonal, boolean preferLowerNotes, boolean useSelectNotes) {
		double f = 0.0;
		double diff = (double) (max - min);
		f = min + (int) (diff  * java.lang.Math.random());
		OneNote nf = new OneNote(f, 0, 0);
		
		if (tonal) {
		    if (!preferLowerNotes) { // 
		    	nf = getExactFreq((int) f, notes);
		    }
		    else { // prefer lower notes !, so we choose the next lower freq of our frequency
		    	// 
		    	nf = getChromaticFrequency(notes);
		    }
		}
		else if (useSelectNotes) {
			nf = getExactFreq((int) f, notes);
		}
		return nf;
    }

    public static OneNote getExactFreq(int freq, Vector<OneNote> notes) {
    	OneNote right;
		double f = (double) freq;
		int n;
		OneNote left = notes.elementAt(0); // first element
		for (n = 1; n < notes.size(); n++) {
		    // right border:
		    right = notes.elementAt(n); 
		    if (right.freq > freq) {
		    	left = getClosestMatch(f, left, right);
		    	break;
		    }
		    left = right; // next left border
		}
		//System.out.println("getExactFreq() in="+freq+" out="+left);
		return left; // max maximum possible
    }
    

    private static OneNote getClosestMatch(double v, OneNote left, OneNote right) {
    	double diff = (right.freq - left.freq) / 2.0; // half distance between left and right
    	double i = v - left.freq;
    	return ( i > diff)?right:left;
    }
    
    
	
	
	/**
     * Get a random freq. from the note-table. This table starts with our min.-freq
     * and ends with the max.-freq.
     * the indecees are equally distributed, checked 2nd of July 2003 nik
     * @return a random freq.
     */
    public static OneNote getChromaticFrequency(Vector<OneNote> notes) {
		int index = (int) ((double) (notes.size()) * java.lang.Math.random()); // index
		//System.out.println("getChromaticFrequency() from max="+this.notes.size()+" r_index="+index);
		OneNote f = notes.elementAt(index);
		//this.rndStatic[index]++;
		//System.out.println("getChromaticFrequency() min="+this.def.min_freq+" max="+this.def.max_freq+" rnd="+f);
		return f;
    }

}
