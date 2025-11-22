package SOUND;
import java.text.DecimalFormat;
import java.util.*;

import Utils.Akkorde;
import Utils.GetEnviroment;
import Utils.QSort;
import Utils.Converter;
import Utils.Utils;

import java.io.BufferedReader;
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
    // Loudness:
    public static final String[] Loudness = new String[] {
	"pause", "ppp", "pp", "p", "mp", "mf", "f", "ff", "fff"};

    public static final int[] loud = new int[] {
	0      , 35   , 45  , 55 , 62  , 68  , 75 , 85  , 90 };
     // bei fof ist 90 zu laut, bei 85 Schluﬂ !

    public static int maxFOFLoudIndex = 8; // do not reach this index !

    public static final double[] GAMMAT = new double[] {
	0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
	1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9,
        2.0, 2.1, 2.2, 2.3, 2.4, 3.0, 4.0, 5.0, 6.0 };

    public static String[] dura = new String[] {
	"free", "1/16", "1/8", "3/16", "1/4"
    };
    
    public static String[] nts = new String[] {
    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
    };
    //int[] rndStatic;
//-------------------
    public final static String DO = "i1"; 
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
    
    @SuppressWarnings("rawtypes")
	Vector generators, envelopes;
   
    //Vector notes;	// a vector that holds the tonal notes frequencys
    Vector<OneNote> notes;// a vector that holds the tonal notes frequencys
    double[] durations;	// a table with the used durations
    //---------- Variables:
    public boolean halt = false;
    int anfOkt;
    double balance;
    RandomTable rt, shadow;
    Defaults def;
   // @SuppressWarnings("rawtypes")
   // Vector komposition;	// this is our Komposition ! (elements of Note)
    PrintWriter prs;
    Akkorde akk;
    
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
		this.akk = new Akkorde();
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
    public double[] createDurationTable(double min, double max, double step) {
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
    /**
     * Get the duration from the table, use val as index
     * @param val the duration we want to quatisize
     * @return the used duration
     */ 
    public double getDurationFromTable(double val) {
		double res = this.durations[0];
		for(int n = 1; n < this.durations.length; n++) {
		    if (this.durations[n] > val) {
			res = getClosestMatch(val, res, this.durations[n]);
			break;
		    }
		    res = this.durations[n]; // left border
		}
		//System.out.println("getDurationFromTable in="+val+" closest Match="+res);
		return res;
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public void createNoteTable(int mode) {
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
			for (q = 0; q < 12; q++) { // 12 Tˆne: a, a', b, c, c', d, d', e, f, f', g, g'
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
		OneNote start = new OneNote(this.def.min_freq, 0, 0);
		start.getMidiNote();
		OneNote stop = new OneNote(this.def.max_freq, 0, 0);
		stop.getMidiNote();
		for (n = start.midiIndex; n <= stop.midiIndex; n++) {
			OneNote nt = new OneNote(0, 0, 0);
			nt.setMidiNr(n);
			this.notes.addElement(nt);
			debugOut("Note "+nt, 3);
		}
	    break;
	}
	// debug:
	for (n = 0; n < this.notes.size(); n++) {
	    double d = (this.notes.elementAt(n)).freq;
	    int idx = getNoteIndex(d);
	    debugOut("Note "+idx+"="+d, 3); 
	}
    }

    /**
     * Calculate the frequency from the note-nr
     * @param n the number of this note
     * @return the frequency
     */
    public double getFreq(int n) {
    	double p = java.lang.Math.pow(2.0, ((double) n++ / 12.0));	// 2^(n/12)
    	double f = 8.175798916 * p; 
    	return f;
    }
    public static double getFreq(int n, int o) {
    	n += 12 * o;
    	double p = java.lang.Math.pow(2.0, ((double) n++ / 12.0));	// 2^(n/12)
    	double f = 8.175798916 * p; 
    	return f;
    }
    /**
     * Calculate the note-nr from the frequency
     * @param in the frequency
     * @return the number of this note
     */
    public int getNoteIndex(double in) {
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

    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public Vector getHeader(boolean fof) {
	//String tmp;
	/*int generator1 = 11;
	int generator2 = 12;
	int envelope1 = 31;
	int envelope2 = 51;
	*/
	int generator1 = 1;
	int tempo = 150;
	this.generators = new Vector();
	this.generators.addElement(new Integer(generator1));
	this.envelopes = new Vector();
	Vector header = new Vector();
	header.addElement("; sco************* ");
	if (!fof) { // For Oszi
	    header.addElement("f1 0 2048 10 1");
	    header.addElement("; Instr. Beginn Dauer Frequenz Lautstaerke Verteilung"); 
	}
	else {
	    header.addElement("; Oszi in FOF mode !");
	    header.addElement("f1	0	8192	-7   0 8192 0");
	    header.addElement("f2   	0   	8192   	-7   0 8192 .07");
	    header.addElement("f3	0   	8192   	-7   0 8192 .11");
	    header.addElement("f4	0   	8192   	-7   0 8192 .37");
	    header.addElement("f5	0   	8192   	-7   0 8192 .43");
	    header.addElement("f6	0   	8192   	-7   0 8192 0");
	    header.addElement("f7	0   	8192   	-7   0 8192 -.07");  
	    header.addElement("f8	0   	8192   	-7   0 8192 -.11"); 
	    header.addElement("f9	0   	8192   	-7   0 8192 -.37"); 
	    header.addElement("f10 	0   	8192   	-7   0 8192 -.43");
	    header.addElement("f18 	0   	8192  	10   1");
	    header.addElement("f19 	0   	1024  	19   .5   .5   270   .5");
	    header.addElement("; Instr. Beginn Dauer Frequenz Lautstaerke Verteilung Stimmen"); 
	}
	return header;
    }

    /**
     * The .orc file
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector getOrc(boolean fof) {
	Vector orc = new Vector();
	if (!fof) { // For Oszi
	    orc.addElement(";orc*************");
	    orc.addElement("nchnls = 2 ");
	    orc.addElement("instr 1 ");
	    orc.addElement("idur = p3; Dauer ");
	    orc.addElement("ifreq = p4; Frequenz ");
	    orc.addElement("iamp = p5; Lautstaerke ");
	    orc.addElement("ich1 = p6; Kanal1 ");
	    orc.addElement("ich2 = 1-p6; Kanal2 ");
	    orc.addElement("ifact pow .00125/p3, .6; Steigung ");
	    orc.addElement("ifact = (p3 < .0039686 ? .5 : ifact) ");
	    orc.addElement("kenv linseg 0, idur*ifact, 1, idur*(1-2*ifact), 1, idur*ifact, 0 ");
	    orc.addElement("aosc oscili ampdb(iamp)*kenv, ifreq, 1 ");
	    orc.addElement("outs aosc*ich1, aosc*ich2 ");
	    orc.addElement("endin ");
	}
	else {// For FOF
	    orc.addElement("; FOF***********************");
	    orc.addElement("nchnls = 2");
	    orc.addElement("instr 1");

	    orc.addElement("itotdur init p3 ; Dauer");
	    orc.addElement("ifund init p4 ; Grundton");
	    orc.addElement("iamp init p5 ; Amplitude");
	    orc.addElement("irate init p6 ; Panning");
	    orc.addElement("itab init 1 ; Kformmodtabelle");

	    orc.addElement("; Umfang von iamp in Abhaengigkeit von ifund");

	    orc.addElement("iamp = (iamp >= 85 && ifund <= 20 ? 85 : iamp)");
	    orc.addElement("iamp = (iamp >= 83 && ifund > 20 && ifund <= 100 ? 83 : iamp)");
	    orc.addElement("iamp = (iamp >= 81 && ifund > 100 && ifund <=300 ? 81 : iamp)");
	    orc.addElement("iamp = (iamp >= 80 && ifund > 300 ? 80 : iamp)");

	    orc.addElement("iamp = (iamp <= 65 ? 65 : iamp)");

	    orc.addElement("; Tabellenzuordnung in Abhaengigkeit von der itotdur und iamp");

	    orc.addElement("if irate > .35 && irate < .65 || itotdur < 1 igoto keiniformmod");

	    orc.addElement("itab = (iamp >= 80 ? 2 : itab)");
	    orc.addElement("itab = (iamp >= 75 && iamp < 80 ? 3 : itab)");
	    orc.addElement("itab = (iamp >= 68 && iamp < 75 ? 4 : itab)");
	    orc.addElement("itab = (iamp <= 68 ? 5 : itab)");

	    orc.addElement("itab = (irate > .65 ? itab + 5 : itab)");

	    orc.addElement("keiniformmod:");

	    orc.addElement("; bestimme selbst!");
	    orc.addElement("icompose = 350");

	    orc.addElement("; Operatoren ");
	    orc.addElement("iop1 = ifund^.07");
	    orc.addElement("iop2 = (2 * ifund^.4 - 7 * ifund^.51 + icompose / ifund^.07) + 1.001^ifund");
	    orc.addElement("iop3 = -(1.5492 * irate - .7745)^2 + 1; je extremer irate desto leiser");
	    orc.addElement("iop4 = 1.00042^ifund");
	    orc.addElement("iop4 = ((ifund > 300 && ifund < 500 || ifund > 700 && ifund < 900) && itotdur >= 2&& irate <.35 || itotdur >= 2 && ifund > 1800 && irate >.65 ? iop4 : 1) ");
	    orc.addElement("iop5 = (irate < .15 || irate >. 85) ? (ifund-.89)^.6 + 1 / (ifund-.89)^2 + 3  : (ifund-.8)^.89  + 1 / (ifund-.89)^2 + 3");
	    orc.addElement("iop5 = (ifund > 2 && iop5 >= 8 ? 8 : iop5)");

	    orc.addElement("iform = iop1 * ifund + iop2 ; Formantfrequenz");
	    orc.addElement("ioct = 0");
	    orc.addElement("iband = 40");
	    orc.addElement("iris = .003  + 1 / (iop5 * ifund^2.8) ; Huellkurvenparamenter fuer die Grains");
	    orc.addElement("idur = .02");
	    orc.addElement("idec = .007");

	    orc.addElement("ifna = 18");
	    orc.addElement("ifnb = 19");
	    orc.addElement("iolaps = int(ifund*idur) + 1");

	    orc.addElement("kformmod oscili iform/iop4, 1/itotdur, itab");

	    orc.addElement("ain  fof ampdb(iamp) * iop3 / iop1, ifund, iform + kformmod, ioct, iband, iris, idur, idec, iolaps, ifna, ifnb, itotdur");

	    orc.addElement("; Panning");
	    orc.addElement("klfo1 expon (1-irate), itotdur, irate");
	    orc.addElement("klfo2 expon irate, itotdur, (1-irate)");
	    orc.addElement("ain      = ain*klfo2");
	    orc.addElement("al       = ain*sqrt(klfo1)");
	    orc.addElement("ar       = ain*sqrt(1 - klfo1)");

	    orc.addElement("outs     al, ar");

	    orc.addElement("endin");
	}
	return orc;
}	
   String home;
    PrintWriter logw = null;
    private void addLog(String t) {
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
    private void addToKomposition(Note nt) {
    	if (this.skompo != null)
    		this.skompo.println(nt.saveAsString());
    }
    
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public String doKomposition(String home) { // Evolution
    	this.home = home;
	//System.out.println("min_dur="+this.def.min_tempo);
    //	this.log = new Vector<String>(); 
    	this.logw = null; 
	String tmp;
	DecimalFormat f = new DecimalFormat("#.###");
	Note nt;
	long dela = DELAY;
	if (this.def.mode == 0) 
	    dela = DDELAY;
	int n, z, it, st;
	double frq;
	// Tonal Values:
	double start = 0.0;	// Notenstart
	double dauer = 0;	// Tondauer
	int max_amplitude = getLoundness(this.def.max_amp); // Border values
	int min_amplitude = getLoundness(this.def.min_amp);
	int amplitude = max_amplitude;	// actual value
	int stimmen = 0;
	//int trigger = 3;
	double bal = this.balance;
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
	//----------- Test:
	//this.rndStatic = new int[this.notes.size()];
	OneNote freq;
	if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
	    freq = makeRandomFrequency(this.def.min_freq, this.def.max_freq);
	}
	else freq = new OneNote(this.def.seed, 0, 0);
	
	this.shadow = new RandomTable(this.qd, this.Verbosity, 
		     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
		     this.def.impact, this.prs);
		     
	rt = new RandomTable(this.qd, this.Verbosity, 
			     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
			     this.def.impact, this.prs);
	rt.seed = (int)freq.freq;
	rt.mode = 0; // off
	
	addLog("Seed ="+rt.seed+" Hz");
	OneNote sFreq = null;
	if (shadow != null) {
		if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
		    sFreq = makeRandomFrequency(this.def.min_freq, this.def.max_freq);
		}
		else 
			sFreq = new OneNote(this.def.seed, 0, 0);
		this.shadow.seed = (int)sFreq.freq;
		addLog("Shadow Seed ="+shadow.seed+" Hz");
	}
	// Veraendere die Tabelle:
	// freq, um wieviel nach oben, welche Frequenz-differenz zu beachten ist
	rt.writeNEntrys(freq, this.def.degression, this.def.diff_freq );
	if (shadow != null)
		shadow.writeNEntrys(sFreq, this.def.degression, this.def.diff_freq );
	//this.ce.gc.setTable(rt);	// Anzeigen !
	//sil.displayRT(rt);
	debugOut("This is our seed :"+freq+" Hz", 5);
	debugOut("################### Start of new Komposition: (Oszi) ###################", 55);
	debugOut("min freq="+this.def.min_freq+" max_freq="+this.def.max_freq+" population="+this.def.population+" degression="+this.def.degression+" weight="+this.def.r_Weight, 55);
	addLog("min freq="+this.def.min_freq+" max_freq="+this.def.max_freq+" population="+this.def.population+" degression="+this.def.degression+" weight="+this.def.r_Weight);
	flowControl();
	int f_cnt = 0; // count how often the fittest was hit in one row !
	Vector<Note> loop = new Vector<Note>();
	Vector<Note> loopCopy = new Vector<Note>();
	int loopCounter = 0;
	int speedModifier = 60; // neutral !
	boolean speedDir = true; // up
	boolean requestToInitLoopCopy = false;
	// Now iterate:
	for (it = 0; it < this.def.iterations; it++) {
	    long time = System.currentTimeMillis();
	    //debugOut("-------> New Iteration step:"+it, 55);
	    addLog("-------> New Iteration step: "+it);
	    // Step 1 generate n random Frequencys:
	    rt.iteration = it;// needed for the display 
	    if (shadow != null)
	    	shadow.iteration = it;
	    makeNRandomFrequencys(rt, this.def.population, 
				  this.def.min_freq, this.def.max_freq); // create Population
	    // Now weight array for each random frequency:
	    makeWeight(this.def.population, rt);// Judge them
	    if (shadow != null) {
	    	makeNRandomFrequencys(shadow, this.def.population, 
				  this.def.min_freq, this.def.max_freq); 
	    	makeWeight(this.def.population, shadow);// Judge them
	    }
	    /*for (z = 0; z < this.def.population; z++) 
		debugOut("makeWeight: index="+z+" freq="+this.freq[z]+" weight="+this.weight[z], 55);// log
	    */
	    // now sort it;
	    QSort q = new QSort(); // ( index = max equals the highest weight)
	    q.sort(rt.weight, rt.freq);
	    if (shadow != null)
	    	q.sort(shadow.weight, shadow.freq);
	    if (this.Verbosity >= 6 ) {
	    	for (z = 0; z < this.def.population; z++) 
	    		debugOut("Sorted: index="+z+" freq="+rt.freq[z]+" weight="+rt.weight[z], 6);
	    }
	    /*for (z = 0; z < this.def.population; z++) 
		debugOut("Sorted: index="+z+" freq="+this.freq[z]+" weight="+this.weight[z], 55);// log
	    */
	    //--------------------------------------------------------------------
	    // Next select randomly the fittest (the last ones in the array are the fittest !):
	    int is = (int) ((double) this.def.fittest * java.lang.Math.random());
	    z = (this.def.population -1 ) - is;
	    debugOut("Index that will be selected:"+z, 5);
	    // --------- Nun ist ein Individuum selektiert ! ----------------
	    freq = rt.freq[z];
	    
	    // ------------------State Machine :------------------------------
	    if (is < def.range && rt.stateCnt == 0) 
	    	f_cnt++;
	    else 
	    	f_cnt = 0; // reset hit counter
	    boolean addToLoop = false;
	    if (rt.stateCnt <= 0) {
	    	// update the loop
	    	Note tn = new Note(start, 0, 0, 0, 0, 0, 0, 0, false);
		    tn.note = freq;
	    	if (loop.size() < def.loopDepth)
	    		loop.addElement(tn);
	    	else { // cache
	    		loop.removeElementAt(0);
	    		loop.addElement(tn);
	    	}
	    	//addLog("Add Loop-element at it="+it+" size now "+loop.size());
	    	addToLoop = true;
	    }
	    
	    Note loopNote  = null; 
	    if (f_cnt >= def.trigger) { // Trigger the state machine, execute it at the next interval
	    	try{
	    		// Auswahl der State machine: INIT 
	    		Vector<Integer> ssel = new Vector<Integer>();
	    		
	    		if (def.useCascade )
	    			ssel.addElement(1);
	    		if (def.useLoop )
	    			ssel.addElement(2);
	    		if (ssel.size() <= 0)
	    			rt.mode = 0;
	    		else {
	    			int sIndex = (int) (Math.random() * ssel.size()); 
	    			rt.mode = (ssel.elementAt(sIndex)).intValue();
	    			System.out.println("Trigger State machine! selected="+rt.mode);
	    		}
	    		//------------
	    		
	 	    	if (rt.mode == 1) { // cascade
	 	    		rt.trigger_index = freq.clone(); // start the state machine
	 	    		rt.trigger_index.getMidiNote();
	 	    		rt.stateCnt = def.cascadeCount;
	 	    		//rt.up = true;
	 	    	}
	 	    	else if (rt.mode == 2)  { // loop
	 	    		rt.stateCnt = loop.size();
	 	    		requestToInitLoopCopy = true;
	 	    		loopCounter = def.loopRepeat;
	 	    	}
	    	} catch (Exception ex) {}
 	    	f_cnt = 0;
 	    	if (rt.mode == 1)
 	    		addLog("#### Trigger Cascade count="+def.cascadeCount+" direction="+(rt.up?"up":"down"));
 	    	else if (rt.mode == 2)
 	    		addLog("#### Trigger Loop effect depth="+loop.size()+" repeat="+loopCounter);
	    }
	    else if (rt.stateCnt > 0) { // State Machine active !
	    	if (rt.mode == 1) {
	    		int no = rt.trigger_index.midiIndex;
	    		if (rt.up) {
	    			// select the next freq.
	    			no = no + def.stepUp;
	    			rt.trigger_index.setMidiNr(no); // set it
	    			if (no > 127 || rt.trigger_index.freq > def.max_freq)
	    				no = no - def.stepUp;
	    			System.out.println(it+" do up:"+rt.trigger_index);
	    		}
	    		else {
	    			// select next freq
	    			no = no - def.stepDown;
	    			rt.trigger_index.setMidiNr(no); // set it
	    			if (no < 12 || rt.trigger_index.freq < def.min_freq)
	    				no = no + def.stepDown;
	    			System.out.println(it+" do down:"+rt.trigger_index);
	    		}
	    		rt.trigger_index.setMidiNr(no); // set it
	    		try {
	    			freq = rt.trigger_index.clone();
	    			System.out.println("New step: "+freq);
	    		} catch(Exception ex) {}
	    		rt.stateCnt--; // count down
		    	if (rt.stateCnt <= 0) {
		    		rt.up = !rt.up; // change dir
		    		System.out.println("Dir changed to :"+rt.up);
		    	}
	    	}
	    	else if (rt.mode == 2) { // LOOP
	    		// busy doing loop
	    		try {
	    			Note tn = loopCopy.elementAt(0);
	    			freq = tn.note.clone(); // use the oldest element,
	    			if (!def.permutation)
	    				loopNote = tn.clone(); // use saved Value
	    			loopCopy.removeElementAt(0); // remove oldest element
	    			System.out.println("it="+it+" Insert loop note: "+freq+" loop size left:"+loopCopy.size());
	    			if (loopCopy.size() <= 0) {
	    				loopCounter--;
	    				if (loopCounter > 0) { // still busy, init again
	    					rt.stateCnt = loop.size();
	    	 	    		loopCopy = new Vector<Note>();
	    	 	    		loopCopy.addAll(loop);
	    	 	    		addLog("# Repeat loop nr="+loopCounter);
	    	 	    		System.out.println("Add new loop "+loopCopy.size());
	    				}
	    			}
	    			else {
	    				rt.stateCnt--; // count down
	    			}
	    		} catch (Exception ex) {
	    			rt.stateCnt = 0;
	    		}
	    	}
	    }
	    //----------------------------------------------
	    //System.out.println("Selected Index ="+is+" f_cnt="+f_cnt);
	    addLog("Frequenz selected f="+freq);
	    debugOut("This is our new selection:"+freq+" Hz", 5);
	    //debugOut("This is our new selection:"+freq+" Hz", 55);
	    // Mark the new individual in the RT
	    rt.writeNEntrys( freq, this.def.degression, this.def.diff_freq );
	    sil.displayRT(rt);
	    rt.getMaxx();
	    //-------------------------------------------------------------------
	  //--------------------------------------------------------------------
	    // Next select randomly the fittest (the last ones in the array are the fittest !):
	    int sz = (this.def.population -1 ) - (int) ((double) this.def.fittest * java.lang.Math.random());
	   // --------- Nun ist ein Individuum selektiert ! ----------------
	    OneNote sfreq = null;
	    if (shadow != null) {
	    	//addLog("Shadow Index that will be selected:"+sz);
		    sfreq = shadow.freq[sz];
		    addLog("Shadow Index that will be selected:"+sz+" f="+sfreq);
		    shadow.writeNEntrys( sfreq, this.def.degression, this.def.diff_freq );
		   // sil.displayRT(rt);
		    shadow.getMaxx();
	    }
	    double tempo = 0, pDauer = 0;
	    OneNote ndVoice = null;
	    //-------------------------------------------------------------------
	    // --------- Jetzt noch ein paar Properites dieser Frequenz : -----------
	    if (loopNote == null) { // not loop, not permutation, add a new modified note
	    	//addLog("Normal note, not loop");
	    	amplitude = getAmplitude(rt.readEntry((int)freq.freq), rt.max, min_amplitude, 
					     max_amplitude, this.def.amplify_amp);	
		    // amplitude = loud[0];
		    debugOut("Resulting amplitude ="+amplitude, 5);
		    if (this.def.stereo) bal = getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		    debugOut("Resulting balance: bal="+bal, 5);
		    //addLog("Resulting amplitude ="+amplitude+" balance: bal="+bal);
		    
		    double val = rt.readEntry((int)freq.freq);
		    
		    //------ tempo : ---------------
		    dauer = getKleiner(val, rt.max, this.def.min_tempo, this.def.max_tempo);	
		    if (dauer < this.def.min_tempo || dauer > this.def.max_tempo) {
		    	debugOut("Project2: tempo out of range ="+dauer, 4);
		    }
		    if (this.def.duration_step_index > 0) {
		    	dauer = getDurationFromTable(dauer);
		    }
		    tempo = dauer;
		    if (def.useSpeed && is == 0)  {// the fittest !
		    	if (speedDir) { // up
		    		speedModifier += def.speedStep;
		    		if (speedModifier >= def.maxTempo) {
		    			speedModifier = def.maxTempo;
		    			speedDir = !speedDir;
		    		}
		    	}
		    	else { // down
		    		speedModifier -= def.speedStep;
		    		if (speedModifier <= def.minTempo) {
		    			speedModifier = def.minTempo;
		    			speedDir = !speedDir;
		    		}
		    	}
		    }
		    if (def.useSpeed) { // modify the speed !
		    	double factor = (double) speedModifier / 60.0; // e.g.: 30 = 0.5 120 = 2.0
		    	tempo = tempo / factor;
		    } // tempo changed, dauer stays !
		    //------------------------------
		    
		    addLog("Resulting amplitude="+amplitude+" bal="+f.format(bal)+" tempo="+f.format(tempo)+" dauer="+f.format(dauer));
		    if (def.useSpeed)
		    	addLog("speedModifier="+speedModifier+" direction="+speedDir);
		    //-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-##-##-#-
		    pDauer = dauer;
		    boolean pedal = shadow != null && def.pedal;
		    if (pedal) {
		    	//freq.freq = 44.0;
		    	//freq.getMidiNote();
		    	double sval = shadow.readEntry((int)sfreq.freq);
		    	pDauer = getKleiner(sval, shadow.max, this.def.min_tempo, this.def.max_tempo);
		    	//System.out.println("dauer="+dauer+" pedal dauer="+pDauer);
		    	//pDauer = 3.0; // test !
		    	addLog("Shadow: pDauer="+f.format(pDauer));
		    	freq.pedal = true;
		    }
		    //---------------------------
		    //System.out.println("(2)At iteration nr.:"+it+" dauer="+dauer);
		    frq = (double) freq.freq;
		    
		    if (this.def.tonal) {
		    	// get freq as double !
		    	frq = getExactFreq((int)freq.freq).freq;
		    }
		    //else System.out.println("Free: frq="+frq);
		    //---- erzeuge eine neue Note mit den oben ermittelten Werten.--------------
		    if (tempo <= 0) {
		    	System.out.println("Dauer (a)="+dauer);
		    	Converter.doBreak();
		    }
		    // Create the Note : 
		    nt = new Note(start, tempo, dauer, bal, frq, generator, amplitude, envelope, false);
		    nt.note = freq;
		    nt.pDauer = freq.pedal?pDauer:0;
		    
		    // ------- Nun pr¸fe, ob noch Stimmen dazukommen ? --------------------------
		    //stimmen = 2; // nur f¸r Test
		    boolean doOct = this.def.akkord;
		    
		    stimmen = getVoices(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		    

		    if (doOct) {
		    	double ten = getKleiner(val, rt.max, 0.0, 23.0);	
		    	int tens = (int) (ten + 0.5);
		    	if (tens >= 24)
		    		tens = 23;
		    	ndVoice = this.akk.get2ndVoice(getNoteFromString(freq.note), freq.Octave, tens);
		    	if (ndVoice != null) {
		    		ndVoice.distance = tens;
		    		ndVoice.pedal = nt.note.pedal;
		    	}
		    	//stimmen = 1;
		    }
		  
		    nt.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
		    nt.channel = 0; 	// first instrument
		    nt.ndVoice = ndVoice;
		    nt.misc = it;
		    if (addToLoop) {
		    	// update the loop element :
		    	try {
			    	int last = loop.size() -1;
			    	loop.setElementAt(nt.clone(), last);
			    	//addLog("Update last loop element size="+loop.size());
			    	if (requestToInitLoopCopy ) {
			    		loopCopy = new Vector<Note>();
			    		loopCopy.addAll(loop);
			    		requestToInitLoopCopy = false;
			    	}
		    	} catch (Exception ex){}
		    }
		    addToKomposition(nt);
		    dauer = nt.dauer;
	    }
	    else {
	    	addToKomposition(loopNote);
	    	try {
		    	stimmen = loopNote.voices;
		    	pDauer = loopNote.pDauer;
		    	dauer = loopNote.dauer;
		    	tempo = dauer;
		    	if (def.useSpeed)
		    		tempo = loopNote.tempo;
		    	generator = loopNote.generator;
		    	envelope = loopNote.envelope;
		    	amplitude = loopNote.amplitude;
		    	freq = loopNote.note.clone();
		    	ndVoice = loopNote.ndVoice;
		    	addLog("Use Loop old Index="+loopNote.misc+" Resulting amplitude="+amplitude+" bal="+f.format(bal)+" tempo="+f.format(tempo)+" dauer="+f.format(dauer));
		    	
		    	/*if (def.useSpeed)
			    	addLog("speedModifier="+speedModifier+" direction="+speedDir);
			    	*/
	    	} catch (Exception ex){}
	    }
	    
	    if( ndVoice != null) {
	    	// Akkorde !
	    	Note nta = new Note(start, tempo, dauer, bal, ndVoice.freq, generator, amplitude, envelope, true);
	    	nta.channel = 0; 	// first instrument
	    	nta.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
	    	nta.pDauer = freq.pedal?pDauer:0;
	    	try {
	    		nta.note = ndVoice.clone();
	    		addLog("Akkord active: "+nta.note);
			 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
	    		//komposition.addElement(nta);
	    		addToKomposition(nta);
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
	    debugOut("Dies wird "+(stimmen)+" stimmig.", 3);
	    Vector sti = new Vector();
	    sti.addElement(new Integer((int)freq.freq));
	    // Now see if there are more than 1 voice(s)
	    for (st = 1; st < stimmen; st++) { // noch eine Stimme dazu:
	    	z = (this.def.population -1 ) - 
	    			(int) ((double) this.def.fittest * java.lang.Math.random());
	    	//debugOut("Index that will be selected:"+z, 5);
	    	// --------- Nun ist ein Individuum selektiert ! ----------------
	    	freq = rt.freq[z];
			 //freq = this.def.max_freq; // test
			 //debugOut("This is our new selection:"+freq+" Hz", 5);
			 // Mark the new individual in the RT
			 //rt.writeNEntrys( freq, this.impact, this.def.diff_freq );
			 //sil.displayRT(rt);
			 if (checkDouble((int)freq.freq, sti)) amplitude = 0; // is double
			 else { 
			     amplitude = getAmplitude(rt.readEntry((int)freq.freq), rt.max, min_amplitude, 
						      max_amplitude, this.def.amplify_amp);	
			     sti.addElement(new Integer((int)freq.freq));
			 }
			 //debugOut("Resulting amplitude ="+amplitude, 5);
			 if (this.def.stereo) bal = getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
			 //debugOut("Resulting balance: bal="+bal, 5);
			 frq = (double) freq.freq;
			 if (this.def.tonal) {
			     // get freq as double !
			     frq = getExactFreq((int)freq.freq).freq;
			 }
			 if (tempo <= 0) {
			     System.out.println("tempo (b)="+tempo);
			     Converter.doBreak();
			 }
			 try {
				 Note ntn = new Note(start, tempo, dauer, bal, frq, generator, amplitude, envelope, false);
				 ntn.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
				 ntn.channel = st;
				 ntn.note = freq.clone();
				 ntn.note.pedal = false;
				 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
				 if (amplitude != 0) {
					 addLog((st+1)+". Stimme : dauer="+f.format(tempo)+" "+ntn.note);
					 addToKomposition(ntn);
					 //komposition.addElement(ntn);
				 }
			 } catch (Exception ex) {
				 ex.printStackTrace();
			 }
	    }
	    
	    
	    start += tempo;
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
	}
	// show statistic of random:
	/*for (n = 0; n < this.notes.size(); n++) {
	    double f = ((Double) this.notes.elementAt(n)).doubleValue();
	    System.out.println("rndStatic["+n+"]="+this.rndStatic[n]+"="+f);
	}
	*/
	/*GetEnviroment gsp= new GetEnviroment(null); // root is the grapholasnt toplevel dir
	gsp.getPathes();
	String Home = gsp.getHome();
	*/
	//String logf = home+File.separator+GetEnviroment.KOMPOSITIONLOG;
	//new Utils().save(logf,  "ASCII", this.log);
	if (this.logw != null) {
		this.logw.close();
	}
	if (this.skompo != null)
		this.skompo.close();
	return kompo;
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
	public boolean checkDouble(int freq, Vector sti) {
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
    public double getKleiner(double w, double Wmax, double Amin, double Amax) {
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
    public double getBal(int fittest, int freq, int start, int stop) {
    	return (0.5 * ( 1 + (double) (freq - fittest) / (double) ( stop - start)));
	
    }
    /**
     * Find out, how many voices this should have
     * Je fitter, desto 
     * @param all freq
     */ 
    public int getVoices(int fittest, int freq, int start, int stop) {
		double diff = java.lang.Math.abs(fittest-freq); // Abstand vom Fittesten [freq]
		debugOut("getVoices(): fittest="+fittest+" freq="+freq+" diff="+diff, 3);
		double max = (double) (stop -start); // Wertebereich [freq]
		double anzStimmen = (double) (this.def.max_voice - this.def.min_voice);	// [Stimmen]
		// je grˆﬂer diff, desto mehr stimmen (weiter weg)
		double v = anzStimmen * java.lang.Math.pow( diff / max, this.def.gamma);
		// je kleiner diff, desto mehr stimmen (n‰her dran)
		double v1 = anzStimmen * java.lang.Math.pow( (max - diff) / max, this.def.gamma);
		//double v = diff * anzStimmen / max; // je weiter weg, desto mehr Stimmen
		//double v1 = (max - diff) * anzStimmen / max; // je n‰her, desto mehr Stimmen
		int val = this.def.min_voice + java.lang.Math.abs((int) (v - v1));
		//System.out.println("getVoices(): diff="+diff+" anzStimmen="+anzStimmen);
		debugOut("v="+v+" v1="+v1+" val="+val, 3);
		return val;
    }
    
    @SuppressWarnings("unused")
	public int getAmplitude(double weight, double Wmax, int Amin, int Amax, double amplifier) {
//	debugOut("getAmplitude: weight="+Converter.formatDouble(weight, 8)+" Wmax="+Converter.formatDouble(Wmax, 8)+" Amin="+Amin+" Amax="+Amax+" amplifier="+Converter.formatDouble(amplifier, 8), 6);//6
	double ampval;	// weight 
	ampval = weight * amplifier;// the weight from the table for this freq * amplifier
	// loudness
	int amplitude = Amin + (int) ((Amax - Amin) * (1 - (Wmax - ampval) / Wmax));
	debugOut("amplitude = "+amplitude, 6); // 6
	// Now map this amplitude to or loud-table
	int n, val;
	int max = loud.length;
	if (this.def.fof) max = maxFOFLoudIndex;
	val = getValFromTable(loud, amplitude);
	//System.out.println("Amin="+Amin+" Amax="+Amax+" ampval="+ampval+" amplitude="+amplitude+" val="+val);
	return val; // default
    }
    /**
     * @return the tbl value from the given val
     */
    private int getValFromTable(int[] tbl, int val) {
		int n, diff, pos;
		for (n = 1; n < tbl.length; n++) {
		    diff = tbl[n] - tbl[n-1];
		    pos = tbl[n-1] + diff/2;	
		    if ( val < pos ) return tbl[n-1];
		    else if (val <= tbl[n]) return tbl[n];
		}
		return tbl[n-1]; // max
	    }
	
    public void makeWeight(int n, RandomTable rt) {
		int i;
		double t;
		rt.weight = new double[n];
		// first get the weight from the RandomTable:
		debugOut("makeWeight() Get the weight for the new random frequencys:", 6);
		for (i = 0; i < n; i++) {
		    rt.weight[i] = t = rt.readEntry((int)rt.freq[i].freq);
		    // randomize this weight:
		    // 1. : this.weight[i] = this.weight[i] * r_Weight * java.lang.Math.random();
		    rt.weight[i] = rt.weight[i] - this.def.r_Weight * java.lang.Math.random();
		    debugOut("makeWeight() weight for frequenz ="+rt.freq[i]+" Hz ="+t+" randomized="+rt.weight[i], 6);
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

    private OneNote getClosestMatch(double v, OneNote left, OneNote right) {
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
    
    public OneNote getExactFreq(int freq) {
    	OneNote right;
		double f = (double) freq;
		int n;
		OneNote left = this.notes.elementAt(0); // first element
		for (n = 1; n < this.notes.size(); n++) {
		    // right border:
		    right = this.notes.elementAt(n); 
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
    public OneNote getChromaticFrequency() {
		int index = (int) ((double) (this.notes.size()) * java.lang.Math.random()); // index
		//System.out.println("getChromaticFrequency() from max="+this.notes.size()+" r_index="+index);
		OneNote f = this.notes.elementAt(index);
		//this.rndStatic[index]++;
		//System.out.println("getChromaticFrequency() min="+this.def.min_freq+" max="+this.def.max_freq+" rnd="+f);
		return f;
    }

    public OneNote makeRandomFrequency(int min, int max) {
		double f = 0.0;
		double diff = (double) (max - min);
		f = min + (int) (diff  * java.lang.Math.random());
		OneNote nf = new OneNote(f, 0, 0);
		
		if (this.def.tonal) {
		    if (!this.def.preferLowerNotes) { // 
		    	nf = getExactFreq((int) f);
		    }
		    else { // prefer lower notes !, so we choose the next lower freq of our frequency
		    	// 
		    	nf = getChromaticFrequency();
		    }
		}
		else if (this.def.useSelectNotes) {
			nf = getExactFreq((int) f);
		}
		return nf;
    }

    public void makeNRandomFrequencys(RandomTable rt, int n, int min, int max) {
    	rt.freq = new OneNote[n];
    	double maxf = 0;
    	for (int i = 0; i < n; i++) {
    		rt.freq[i] = makeRandomFrequency(min, max+1);
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
		
		if (this.def.tonal) createNoteTable(this.note_mode);
		
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
		if (this.def.duration_step_index > 0) {
		    double step = this.def.duration_step_index / 16.0;
		    this.durations = createDurationTable(def.min_tempo, def.max_tempo, step);
		}
		return generateSound(home);
    }
    String kompo;
    /**
     * Hier nun das Herz der Methode: wie baut man eine Ton-Abfolge.
     * Das Ergebnis ist ein Vector mit den .sco Zeilen f¸r die Tˆne.
     * @param parm ein ‹bergabeparameter, kann auch leer sein (vom Textfeld)
     * @return Vector with .sco lines
     */
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public Vector generateSound(String home) {
		Note nt;
		String tmp;
		Vector result = new Vector();
		String kompo = doKomposition(home);	// Evolution
		this.kompo = null;
		//ce.mi1_5.setEnabled(true);
		// ----------------------------------------------------------
		//String tst;
		//double dd;
		// so macht man dann aus einem Note - Eintrag eine sco-line :
		// loop through the komposition save file:
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(kompo)));
			BufferedReader br = new BufferedReader(isr);
			Vector res = new Vector();
			String res0;
		    while((res0 = br.readLine()) != null) {
		    	nt = Note.getFromString(res0);
		    	result.addElement(digestNote(nt));
		    }
		    br.close();	// close the Buffered Reader
		} catch (java.io.IOException e) {
		    System.out.println("Utils.readTextFile(): catched "+e);
		    return null;
		}
		this.kompo = kompo;
		return result;
		
    }
    private String digestNote(Note nt) {
    	
    	String tmp  = Project2.DO+"\t"+Converter.formatDouble(nt.start, 12,6)+"\t";
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
	return ( a * 127 / max); // midi has 127 as max. amplitude val.
    }
	
}// end of class







