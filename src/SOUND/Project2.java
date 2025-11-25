package SOUND;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import Utils.Akkorde;
import Utils.GetEnviroment;
import Utils.Melody;
import Utils.QSort;
import Utils.Converter;
import Utils.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
/**
 * This class holds our Project 2 (Sound evolution)
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Project2 {
    CDebug qd;
    int DELAY = 50;
    int DDELAY = 10;
    SoundInfoListener sil;
    Vector<String> cache;
    
    int[] icache;
    int cIndex;
    String home;
    PrintWriter logw = null;
    PrintWriter imageIt = null;
    DataOutputStream dos;
    
    // Loudness:
    public static final String[] Loudness = new String[] {
	"pause", "ppp", "pp", "p", "mp", "mf", "f", "ff", "fff"};

    public static final int[] loud = new int[] {
    		0 , 35   , 45  , 55 , 62  , 68  , 75 , 85  , 90 };
     // bei fof ist 90 zu laut, bei 85 Schluï¿½ !

    public static int maxFOFLoudIndex = 8; // do not reach this index !

    public static final double[] GAMMAT = new double[] {
	0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
	1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9,
        2.0, 2.1, 2.2, 2.3, 2.4, 3.0, 4.0, 5.0, 6.0 };

    /*public static String[] dura = new String[] {
    	"free", "1/16", "1/8", "3/16", "1/4"
    };
    */
    public static String[] nts = new String[] {
    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
    };
    //int[] rndStatic;
//-------------------
    public final static String DO = "i1"; 
    public final static String DOMELODY = "i7"; 
    //public final int FORMULA1 = 0;
    //public final int FORMULA2 = 1;
// ------------------    
    /**
     * Verbosity: 0= no, 1=important Messages, 2=less important, 3=low debug, 4=higher debug, 5 = high debug, 6 = crazy debug
     */
    int Verbosity = 1;
    int note_mode = 1;
    String header[];
    int noise_samples[];
    //Vector notes;	// a vector that holds the tonal notes frequencys
    Vector<OneNote> notes;// a vector that holds the tonal notes frequencys
    //double[] durations;	// a table with the used durations
    //---------- Variables:
    public boolean halt = false;
    int anfOkt;
    double balance;
    Soundscape rt, shadow;
    Defaults def;
   // @SuppressWarnings("rawtypes")
    Vector<Note> komposition;	// this is our Komposition ! (elements of Note)
    PrintWriter prs;
   
    
    public Project2(SoundInfoListener sil, PrintWriter prs) {
		this(null, 5, sil, prs);
    }
    
    /**
     * Constructor des 1. Projektes:
     * Eine ganze musikalische Note errechnet sich so:
     * (Oktave * 220) * 2 ^ (x/12);
     * x geht von 0-11:
     * 0=A, 1=A#, B=2, C=3, C#=4, D=5, D#=6, E=7, F=8, F#=9, G=10, G#=11
     * Es gibt ca. 12 Oktaven ?
     */
    public Project2(CDebug qd, int ver, SoundInfoListener sil, PrintWriter prs) {
		// Constructor
		this.qd = qd;
		this.prs = prs;
		this.sil = sil;
		this.Verbosity = ver;
		debugOut("Project2 : Constructor", 1);
		
    }
    
    public static int getNoteFromString(String s) {
    	int res = 0;
    	for (int n = 0; n < Project2.nts.length; n++) {
    		if(s.equalsIgnoreCase(Project2.nts[n])) {
    			res = n;
    			break;
    		}
    	}
    	return res;
    }
    
    /**
     * Create the duration table
     * @param min the min-duration
     * @param max the maximum duration
     * @param step the used interval e.g. 1/16=0.25 sec
     * @return the duration table
     */
    private double[] createDurationTable(double min, double max, double step) {
		double tmp = (max - min) / step;
		int len = (int) (tmp + 0.5) + 1;
		
		//System.out.println("Min="+min+" max="+max+" Duration step="+step+" diff="+(max-min)+" len ="+len);
		double start = min / step + 0.5;
		start = (double) ((int) start) * step;
		if( start <= 0.0 )
		    start = step;
		double[] res = new double[len];
		res[0] = start;	// minimum duration for sure !
		//System.out.println("Duration table [0]="+res[0]);
		for (int n = 1; n < len; n++) {
		    res[n] = res[n-1] + step;
		    System.out.println("Duration table ["+n+"]="+Converter.formatDouble(res[n], 8));
		}
		return res;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public Vector<OneNote> createNoteTable(int mode, int min_freq, int max_freq) {
		int maxOkt;
		int n, m, q, freq;
		double p, f;
		this.notes = new Vector();
		System.out.println("Tonal, createNotetable: mode="+mode);
		switch (mode) {
		case 0:
		default:
		   /* n = 0;
		    maxOkt = 6;	// 6 Oktaven maximal
		    debugOut(maxOkt+" Oktaven", 3);
		    for (m = 0; m < maxOkt; m++) {
				for (q = 0; q < 12; q++) {
				    p = java.lang.Math.pow(2.0, ((double) n++ / 12.0));	// 2^(n/12)
				    f = 220.0 * p;
				    freq = (int) f;
				    if (freq >= this.def.min_freq && freq <= this.def.max_freq ) {
						debugOut("Oktave="+m+" Note="+n+" p="+p+" f="+f+" freq="+freq, 6);
						//this.notes.addElement(new Integer(freq));
						this.notes.addElement(new OneNote(f, Project2.nts[q], m));
						//System.out.println("Tonal,add f="+f);
						// make this a double for non-integer frequencys
						debugOut("Note "+((m-1)*12+n)+"="+f, 3); 
				    }
				}
		    }
		    */
		    break;
		case 1:
		   /* maxOkt = 11;
		    this.anfOkt = 2; // in wirklichkeit 4, da es keine 0. Oktave gibt ?
		    n = 12 * anfOkt;
		    debugOut("First Oktave="+anfOkt+" last Oktave ="+maxOkt, 3);
		    for (m = anfOkt; m < maxOkt; m++) {
				for (q = 0; q < 12; q++) { // 12 Tï¿½ne: a, a', b, c, c', d, d', e, f, f', g, g'
				    f = getFreq(n++);
				    freq = (int) f;
				    if (freq >= this.def.min_freq && freq <= this.def.max_freq ) {
						debugOut("Oktave="+m+" Note="+n+" freq="+freq, 6);
						this.notes.addElement(new OneNote(f, Project2.nts[q], m-1));
						//System.out.println("Tonal,add f="+f);
						debugOut("Note "+(n-1)+"="+freq, 3); 
				    }
				}
		    }
		    */
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
					this.notes.addElement(nt);
					debugOut("Note "+nt, 3);
				}
			}
		    break;
		}
		// debug:
		for (n = 0; n < this.notes.size(); n++) {
		    double d = (this.notes.elementAt(n)).freq;
		    int idx = getNoteIndex(d);
		    debugOut("Note "+idx+"="+d, 3); 
		}
		return notes;
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
    
    /**
     * Calculate the note-nr from the frequency
     * @param in the frequency
     * @return the number of this note
     */
    private int getNoteIndex(double in) {
		double d;
		int n;
		int off = 12 * this.anfOkt;
		for (n = 0; n < this.notes.size(); n++) {
		    d = this.notes.elementAt(n).freq;
		    if (d >= in ) return n+off;
		}
		return n+off; // max
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

   
    public void addLog(String t) {
    	if (this.logw == null) {
    		
    		String log = home+File.separator+GetEnviroment.KOMPOSITIONLOG;
    		try {
    			this.logw = new PrintWriter(new FileOutputStream(log));
    			// Headder Information
    			this.logw.println("# Logfile File for JcSelf.");
    		}
    		catch (java.io.IOException e) {
    			this.logw = null;
    		} // End of IO Excep
    	}
    	this.logw.println(t);
    	//System.out.println(t);
    }
    
    PrintWriter skompo = null;
    
    public static void addToKomposition(Note nt, PrintWriter skompo) {
    	if (skompo != null)
    		skompo.println(nt.saveAsString());
    }
    
    /**
     * Save the actual random table in a line
     * @param rt
     * @param start
     */
    private void addImageS(Soundscape rt, double start) {
    	
    	if (this.imageIt == null) {
    		this.cache = new Vector<String>();
    		String log = home+File.separator+GetEnviroment.IMAGELOG;
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
    
    
    @SuppressWarnings({"unused"})
	private Vector<Note> doKomposition(String home) { // Evolution
    	this.home = home;
    	//boolean doBug = def.doLoopBug;
    	Melody melody = new Melody();
    	int melodyChannel = def.voiceMax; // (7) first melody channel (after maxVoices) 
    	//System.out.println("min_dur="+this.def.min_tempo);
    //	this.log = new Vector<String>(); 
    	this.logw = null; 
    	Note melodyTone = null; // the actual played melody Note
    	String tmp;
    	DecimalFormat f = new DecimalFormat("#.###");
    	Note nt;
    	long dela = DELAY;
    	if (this.def.mode == 0) 
    		dela = DDELAY;
    	int n, z, it, st;
    	int generator, envelope = 0;
		String kompo = home+File.separator+GetEnviroment.KOMPOSITION;
		
		try {
			this.skompo = new PrintWriter(new FileOutputStream(kompo));
			// Headder Information
			//skompo.println("# Logfile File for JcSelf.");
		}
		catch (java.io.IOException e) {
			skompo = null;
		} // End of IO Excep
		generator = 0; 	// ((Integer) this.generators.elementAt(0)).intValue();
		debugOut("KOMPOSITION: (Evolution)", 1);
		addLog("KOMPOSITION: (Evolution)");
		// Angangsbedingung:
		addLog("State Machine: range="+def.range+" trigger="+def.trigger);
		if (def.useCascade)
			addLog("Cascade amount="+def.cascadeCount+" step up="+def.stepUp+" step down="+def.stepDown);
		if (def.useLoop)
			addLog("Loop depth="+def.loopDepth+" repeat="+def.loopRepeat+" permutation="+def.permutation);
		if (def.useSpeed)
			addLog("Speed minTempo="+def.minTempo+" maxTemp="+def.maxTempo+" step="+def.speedStep);
		if (def.doMelody)
			addLog("Do Melody ");
		//----------- Test:
		//this.rndStatic = new int[this.notes.size()];
		// init seed frequency
		OneNote freq; // start Note
		if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
		    freq = makeRandomFrequency(notes, this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
		}
		else freq = new OneNote(this.def.seed, 0, 0);
		
		// init OneNote tables
		this.shadow = new Soundscape(this.qd, this.Verbosity, 
			     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
			     this.def.impact, this.prs);
		
		/*this.melodyTable = new RandomTable(this.qd, this.Verbosity, 
			     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
			     this.def.impact, this.prs);
		*/
		// Soundscape
		rt = new Soundscape(this.qd, this.Verbosity, 
				     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
				     this.def.impact, this.prs);
		rt.seed = (int)freq.freq;
		rt.mode = 0; // off
		
		addLog("Seed ="+rt.seed+" Hz");
		OneNote sFreq = null;
		// -------- Initalise Tables -----------
		if (shadow != null) {
			if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
			    sFreq = makeRandomFrequency(notes, this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
			}
			else 
				sFreq = new OneNote(this.def.seed, 0, 0);
			this.shadow.seed = (int)sFreq.freq;
			addLog("Shadow Seed ="+shadow.seed+" Hz");
		}
		
		// Veraendere die Tabelle
		// freq, um wieviel nach oben, welche Frequenz-differenz zu beachten ist
		rt.writeNEntrys(freq, this.def.degression, this.def.diff_freq );
		if (shadow != null)
			shadow.writeNEntrys(sFreq, this.def.degression, this.def.diff_freq );
		//-----------------------
		debugOut("This is our seed :"+freq+" Hz", 5);
		debugOut("################### Start of new Komposition: (Oszi) ###################", 55);
		debugOut("min freq="+this.def.min_freq+" max_freq="+this.def.max_freq+" population="+this.def.population+" degression="+this.def.degression+" weight="+this.def.r_Weight, 55);
		addLog("min freq="+this.def.min_freq+" max_freq="+this.def.max_freq+" population="+this.def.population+" degression="+this.def.degression+" weight="+this.def.r_Weight);
		flowControl();
		int max_amplitude = getLoundness(this.def.max_amp); // Border values
		int min_amplitude = getLoundness(this.def.min_amp);
		
		
		LoopObject o = new LoopObject(def, this::debugOut, this::addLog);
		o.bal = this.balance;
		o.amplitude = max_amplitude;	// actual value
		o.max_amplitude = max_amplitude;
		o.min_amplitude = min_amplitude;
		
		// Now iterate and create the komposition
		for (it = 0; it < this.def.iterations; it++) {
		    long time = System.currentTimeMillis();
		    //debugOut("-------> New Iteration step:"+it, 55);
		    addLog("-------> New Iteration step: "+it);
		    // Step 1 generate n random Frequencys:
		    rt.iteration = it;// needed for the display 
		    if (shadow != null)
		    	shadow.iteration = it;
		    
		    createPopulation(notes, rt, this.def.population, 
					  this.def.min_freq, this.def.max_freq,def.tonal, def.preferLowerNotes, def.useSelectNotes); // create new Population
		    // Now weight array for each random frequency:
		    makeWeight(this.def.population, rt, def.r_Weight, this::debugOut);// Judge them
		    if (shadow != null) {
		    	createPopulation(notes, shadow, this.def.population, 
					  this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes); 
		    	makeWeight(this.def.population, shadow, def.r_Weight, this::debugOut);// Judge them
		    }
		  
		    // now sort new population by weight;
		    Arrays.sort(rt.freq, Comparator.comparing(p -> p.weight));
		   if (shadow != null)
		    	Arrays.sort(shadow.freq, Comparator.comparing(p -> p.weight));
		    	//q.sort(shadow.weight, shadow.freq);
		    
		    if (this.Verbosity >= 6 ) {
		    	for (z = 0; z < this.def.population; z++) 
		    		debugOut("Sorted: index="+z+" freq="+rt.freq[z]+" weight="+rt.freq[z].weight, 6);
		    }
		   
		    //--------------------------------------------------------------------
		    // Next select randomly the fittest (the last ones in the array are the fittest !):
		    int is = (int) ((double) this.def.fittest * java.lang.Math.random());
		    o.is = is;
		    z = (this.def.population -1 ) - is;
		    debugOut("Index that will be selected:"+z, 5);
		    // --------- Nun ist ein Individuum selektiert ! ----------------
		    freq = rt.freq[z];
		    o.freq = freq;
		    o.it = it;
		    // ------------------State Machine :------------------------------
		    if (is < def.range && rt.stateCnt == 0) 
		    	o.f_cnt++;
		    else 
		    	o.f_cnt = 0; // reset hit counter
		    o.addLoop(rt, shadow, sFreq);
		    //----------------f_cnt------------------------------
		    //System.out.println("Selected Index ="+is+" f_cnt="+f_cnt);
		    addLog("Frequenz selected f="+freq);
		    debugOut("This is our new selection:"+freq+" Hz", 5);
		    //debugOut("This is our new selection:"+freq+" Hz", 55);
		    // Mark the new individual in the RT, now update Tables
		    rt.writeNEntrys( freq, this.def.degression, this.def.diff_freq ); // footprint
		    sil.displayRT(rt);
		    rt.getMaxx();
		    addImageS(rt, o.start);
		   // addImage(rt, start);
		    int m_amplitude = 0;
		    int m_Tension = 0;
		    double m_bal = 0;
		    //-------------------------------------------------------------------
		  //--------------------------------------------------------------------
		    // Next select randomly the fittest (the last ones in the array are the fittest !):
		    is = (int) ((double) this.def.fittest * java.lang.Math.random());
		    int sz = (this.def.population -1 ) - is;
		   // --------- Nun ist ein Individuum selektiert ! ----------------
		    OneNote sfreq = null;
		    if (shadow != null) {
		    	//addLog("Shadow Index that will be selected:"+sz);
			    sfreq = shadow.freq[sz];
			    addLog("Shadow Index that will be selected:"+sz+" f="+sfreq);
			    shadow.writeNEntrys( sfreq, this.def.degression, this.def.diff_freq );
			   // sil.displayRT(rt);
			    shadow.getMaxx();
			    m_Tension = (int) getKleiner(melody.max, rt.max, this.def.min_tempo, this.def.max_tempo);
			    m_amplitude = getAmplitude(shadow.readEntry((int)sfreq.freq), shadow.max, min_amplitude, 
					     max_amplitude, this.def.amplify_amp);
			    if (this.def.stereo) 
			    	m_bal = getBal(shadow.fittest_freq, (int)sfreq.freq, shadow.start, shadow.stop);
			   
		    }
		    
		    debugOut("Resulting amplitude ="+o.amplitude, 5);
		    if (this.def.stereo) o.bal = getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		   
		    //-------------------------------------------------------------------
		    o.addLoopProperties(rt, shadow, sFreq, notes, generator, envelope, skompo);
		    //----
		    o.doAkkordeUndStimmen(rt,  notes, generator, envelope, skompo);
		    //---- Startzeitpunk nächste note:
		    o.start += o.tempo;
		    //---------
		    if (this.halt) break;	// stop doing it
		    flowControl();
		    if (this.def.mode > 0 | true) {
			long diff = System.currentTimeMillis() - time;
			long sleep = dela - diff;
			int needed = (int) diff;
			//System.out.println("Run() delay ="+DELAY+" dela="+dela+" needed:"+diff+" sleep:"+sleep);
			try {
			    if (sleep > 0) {
				Thread.sleep(sleep);
			    }
			    //else dela -= sleep;
			    dela -= sleep;
			}
			catch (InterruptedException e) {}
		    }
		} // end of iterations loop
		// flush
		if (this.cIndex > 0 & false) {
			rt.writeTable(this.dos, icache, cIndex);
		}
		if (this.cache != null && this.cache.size() > 0) {
			for (int n1 = 0; n1 < cache.size(); n1++)
				this.imageIt.println(cache.elementAt(n1));
			this.cache = new Vector<String>();
		}
		/*if (def.doMelody && melodyTone != null & false) {
			melodyTone.tempo = 0; //start - melodyTone.start;
			melodyTone.dauer = start - melodyTone.start;
			addToKomposition(melodyTone);
		}
		*/
		if (this.skompo != null)
			this.skompo.close();
		// Komposition is now saved.
		if (this.imageIt != null)
			this.imageIt.close();
		
		if (this.dos != null) {
			try {
				this.dos.close();
			}catch (Exception ex) {}
		}
		Vector<Note> ret = sortKomposition(kompo);
		if (def.doMelody) {
			MelodyObject mo = new MelodyObject(qd, def, this::debugOut, this::addLog, min_amplitude, max_amplitude);
			ret = mo.addMelody(notes, ret, melodyChannel, f, Verbosity, prs);
			saveOverview(ret);
			Collections.sort(ret, new Comparator<Note>() {
				public int compare(Note o1, Note o2){
					return o1.compareTo(o2);
				}
			});
		}
		if (this.def.duration_step_index > 0) 
			ret = quantisize(ret, def.duration_step_index);
		if (this.logw != null) {
			this.logw.close();
		}
		return ret;
	
    }
    
    
    
    
    
    /**
     * Align all note-start times to match the quantisation
     * @param in
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked"})
	private static Vector<Note> quantisize(Vector<Note> in, int duration_step_index) {
    	Vector<Vector> interval = getInterval(in, true, duration_step_index); // A vector of vectors for each interval 
    	
    	// now convert the intervalls back to one Note Vector:
    	Vector<Note> result = new Vector<Note>();
    	for (int n = 0; n < interval.size(); n++) {
    		Vector<Note> iv = (Vector) interval.elementAt(n);
    		result.addAll(iv);
    	}
    	
    	return result;
    }
    
    
    
    
    
    
    @SuppressWarnings("unused")
	private void printDebug(Vector<Note> v) {
    	System.out.println("debug---------------");
    	Vector<String> vl = new Vector<String>();
    	for (int n = 0; n < v.size(); n++) { 
    		Note nt = v.elementAt(n);
    		String l = nt.saveAsString();
    		System.out.println(l);
    		vl.addElement(l);
    	}
    	String path = this.home+File.separator+"test.sav";
    	Utils t = new Utils();
    	t.save(path, "ASCII", vl);
    	
    }
    @SuppressWarnings({ "unused", "unchecked" })
	private Vector<Note> getDebug() {
    	Vector<Note> v = new Vector<Note>();
    	
    	String path = this.home+File.separator+"test.sav";
    	Utils t = new Utils();
    	Vector<String> li = t.readTextFile(path, false);
    	for (int n = 0; n < li.size(); n++)
    		v.addElement(Note.getFromString(li.elementAt(n)));
    	return v;
    }
    
    private void saveOverview(Vector<Note> v) {
    	System.out.println("Melody Verify-----------------------------------------");
    	Vector<String> vl = new Vector<String>();
    	for (int n = 0; n < v.size(); n++) { 
    		Note nt = v.elementAt(n);
    		String l = getOneLine(nt);
    		System.out.println(l);
    		vl.addElement(l);
    	}
    	String path = this.home+File.separator+"kompositionLog.txt";
    	Utils t = new Utils();
    	t.save(path, "ASCII", vl);
    }
    
    private String getOneLine(Note nt) {
    	String ret = (nt.misc == 222)?"-> Melody":"   Base\t";
    	String st = nt.note.note+nt.note.Octave;
    	ret += "\t"+st; 
    	
    	st = Converter.formatDouble(nt.start, 8);
    	ret += "\t base start="+st;
    	if (st.length() < 4)
    		ret += "\t";
    	ret += "\t dauer="+Converter.formatDouble(nt.dauer, 8);
    	double s = nt.start + nt.delay/1000.0;
    	ret += "\t start="+Converter.formatDouble(s, 8);
    	double e = nt.start + nt.dauer + nt.delay/1000.0;
    	ret += "\t end="+Converter.formatDouble(e, 8);
    	st = Converter.formatDouble(nt.delay, 8)+"ms";
    	ret += "\t delay="+st;
    	if (st.length() < 9)
    		ret += "\t";
    	if (nt.misc == 222)
    		ret += "\t base note:"+nt.parent.note+nt.parent.Octave;
    	return ret;
    }
    
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	private void printInvteral(Vector<Vector> interval) {
    	System.out.println("sorted into intervals----");
    	for (int n = 0; n < interval.size(); n++) {
    		Vector<Note> iv = interval.elementAt(n);
    		for (int q = 0; q < iv.size(); q++) {
    			Note nt = iv.elementAt(q);
    			System.out.println(nt.start+" "+nt.note);
    		}
    	}
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
    
    private Vector<Note> sortKomposition(String kompo) {
    	Vector<Note> res = null;
    	try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(kompo)));
			BufferedReader br = new BufferedReader(isr);
			res = new Vector<Note>();
			String res0;
		    while((res0 = br.readLine()) != null) {
		    	Note nt = Note.getFromString(res0);
		    	res.addElement(nt);
		    }
		    br.close();	// close the Buffered Reader
    	} catch (java.io.IOException e) {
    		System.out.println("CEditor.playKomposition(): catched "+e);
    		return null;
    	}
    	// Now we have the komposition as Vector
    	Collections.sort(res, new Comparator<Note>() {
 		   public int compare(Note o1, Note o2){
 			  return o1.compareTo(o2);
 		   }
 		});
    	return res;
    }
    
    public void flowControl() {
		if (qd != null) {
		    if (qd.halt ) {
			while(qd.halt) {
			    try {
			    	java.lang.Thread.sleep(100); // 
			    }
			    catch (InterruptedException e){}
			}
			if (qd.stepp) qd.halt = true;
		    }
		    else if (qd.langsam) {
				try {
				    java.lang.Thread.sleep(200); // 
				}
				catch (InterruptedException e){}
		    }
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
     * The fitter an individual is, the smaller the resulting value is, within the 
     * Bandwitdth that we provide:
     * @return the calculated int value
     */
    public int getIKleiner(double w, double Wmax, int Amin, int Amax) {
    	return Amin + (int) (((double) (Amax - Amin) * (Wmax - w) / Wmax) + 0.5);
    }
    /**
     * The fitter an individual is, the bigger the resulting value is, within the 
     * Bandwitdth that we provide:
     * @return the calculated double value
     */
    public double getGreater(double w, double Wmax, double Amin, double Amax) {
    	return (Amin + ((Amax - Amin) * (1 - (Wmax - w) / Wmax)));
    }


    /*public double getDauer(double weight, double Wmax, double min, double max) {
	return (min + ((max - min) * (Wmax - weight) / Wmax));
	}*/
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
     * Find out which border is closer to the given value:
     * v is somewhere inbetween left and right !
     * @param v the value to examin
     * @param left the left border
     * @param right the right border.
     */
    private double getClosestMatch(double v, double left, double right) {
    	double diff = (right - left) / 2.0; // half distance between left and right
    	double i = v - left;
    	return ( i > diff)?right:left;
    }

    private static OneNote getClosestMatch(double v, OneNote left, OneNote right) {
    	double diff = (right.freq - left.freq) / 2.0; // half distance between left and right
    	double i = v - left.freq;
    	return ( i > diff)?right:left;
    }
    
    public int getChromaticFromSeed(int seed) {
		int v,n;
		for (n = 0; n < this.notes.size(); n++) {
		    v = (int) this.notes.elementAt(n).freq;
		    if (v > seed) return v;
		}
		v = (int) this.notes.elementAt(n).freq;
		// Not in Table
		return v; // max maximum possible
    }

    public double getExactFreqOld(int freq) {
    	double right;
		double f = (double) freq;
		int n;
		double left = this.notes.elementAt(0).freq; // first element
		for (n = 1; n < this.notes.size(); n++) {
		    // right border:
		    right = this.notes.elementAt(n).freq; 
		    if (right > freq) {
		    	left = getClosestMatch(f, left, right);
			break;
		    }
		    left = right; // next left border
		}
		//System.out.println("getExactFreq() in="+freq+" out="+left);
		return left; // max maximum possible
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

/**
 * Translate the Loudness-code (f, ff etc.) into an amplitude value
 */
    public int getLoundness(String code) {
		int n;
		int max = loud.length;
		if (this.def.fof) max = maxFOFLoudIndex;
		for (n = 0; n < max; n++) {
		    //System.out.println("Loudness["+n+"]="+Loudness[n]+"code="+code);
		    if (code.equals(Loudness[n])) return loud[n];
		}
		// default
		return loud[n]; //MAX
    }

    @SuppressWarnings("rawtypes")
	public Vector doProject(Defaults def, String home) {	
		this.def = def;
		this.balance = 0.5;	// symetrical default
		
		if (this.def.tonal) createNoteTable(this.note_mode, this.def.min_freq, this.def.max_freq);
		
		if (def.useSelectNotes) {
			
			Vector<OneNote> sel  = def.clipNotes(def.selectedNotes);
			String text = null;
			if (sel.size() != def.selectedNotes.size()) {
				text = def.text[80];
			}
			if (sel.size() == 1) {
				if (text != null)
					text = "/n";
				else 
					text = "";
				text += def.text[85];
			}
			else if (sel.size() == 0) {
				text = def.text[88];
				new Utils().doMessagePane(text);
				return null;
				
			}
			Object[] ob = new Object[2];
			ob[0] = def.text[86]; // Ok
			ob[1] = def.text[87]; // Cancel
			if (text != null) {
				int o = new Utils().doMessagePane(text, ob); // cancel
				if (o == 1) {
					// abort
					return null;
				}
			}
			this.notes = sel;
		}
		/*if (this.def.duration_step_index > 0) {
		    double step = this.def.durations[this.def.duration_step_index];
		    this.durations = createDurationTable(def.min_tempo, def.max_tempo, step);
		}
		*/
		return generateSound(home);
    }
    String kompo;
    /**
     * Hier nun das Herz der Methode: wie baut man eine Ton-Abfolge.
     * Das Ergebnis ist ein Vector mit den .sco Zeilen fï¿½r die Tï¿½ne.
     * @param parm ein ï¿½bergabeparameter, kann auch leer sein (vom Textfeld)
     * @return Vector with .sco lines
     */
    @SuppressWarnings({ "unused"})
	public Vector<String> generateSound(String home) {
		Note nt;
		String tmp;
		this.komposition = doKomposition(home);	// Evolution
		Vector<String> result = new Vector<String>();
		
		for (int n = 0; n < this.komposition.size(); n++) {
			result.addElement(digestNote(this.komposition.elementAt(n)));
		}
		//this.kompo = kompo;
		return result;
		
    }
    private String digestNote(Note nt) {
    	double st = nt.start + (nt.delay / 1000.0);
    	String tmp  = Project2.DO+"\t"+Converter.formatDouble(st, 12,6)+"\t";
    	if (nt.misc == 222)
    		tmp  = Project2.DOMELODY+"\t"+Converter.formatDouble(st, 12,6)+"\t";
    	double dau = nt.dauer;
    	if (nt.note.pedal)
    		dau = nt.pDauer;
    	if (def.delay > 0)
    		dau += def.delay / 1000.0;
    	tmp += Converter.formatDouble(dau, 12, 6);
    	if (nt.freq > 0) tmp += "\t"+Converter.formatDouble(nt.freq, 10);
    	if (nt.generator > 0) tmp += "\t"+nt.generator;
    	if (nt.amplitude >= 0) tmp += "\t"+nt.amplitude;
    	if (nt.envelope > 0 ) tmp += "\t"+nt.envelope;
    	if (nt.balance > 0 ) tmp += "\t"+Converter.formatDouble(nt.balance, 6, 4);// auch formatieren
    	// Anzahl Stimmen bei fof
    	if (this.def.fof ) tmp += "\t\t"+nt.voices;
    	//debugOut("dauer="+nt.dauer+" dd="+dd, 55);
    	//debugOut(nt.toString(), 55);
    	//debugOut(tmp, 55);
    	debugOut("add line:"+tmp, 6);
    	return tmp;
    }
    
    @SuppressWarnings("static-access")
	public int mapAplitude(int a) {
    	int max = this.loud[loud.length-1]; // max. c-sound ampl.
    	int res = ( a * 127 / max);
    	//System.out.println("mapAmplitude() a="+a+" res="+res);
    	
    	return res; // midi has 127 as max. amplitude val.
    }
	
}// end of class







