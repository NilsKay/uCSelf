package SOUND;
import java.text.DecimalFormat;
import java.util.*;

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
    // Loudness:
    public static final String[] Loudness = new String[] {
	"pause", "ppp", "pp", "p", "mp", "mf", "f", "ff", "fff"};

    public static final int[] loud = new int[] {
	0      , 35   , 45  , 55 , 62  , 68  , 75 , 85  , 90 };
     // bei fof ist 90 zu laut, bei 85 Schlu� !

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
    
    @SuppressWarnings("rawtypes")
	Vector generators, envelopes;
   
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
    /*public double getDurationFromTable(double val) {
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
*/
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public void createNoteTable(int mode, int min_freq, int max_freq) {
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
				for (q = 0; q < 12; q++) { // 12 T�ne: a, a', b, c, c', d, d', e, f, f', g, g'
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
	public Vector getOrc(boolean fof, Defaults def) {
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
	    if (def.doMelody) {
	    	orc.addElement("; orc*************  ");
	    	orc.addElement("nchnls = 2  ");
	    	orc.addElement("instr 7  ");
	    	orc.addElement("idur = p3; Dauer ");
	    	orc.addElement("ifreq = p4; Frequenz ");
	    	orc.addElement("iamp = p5; Lautstaerke ");
	    	orc.addElement("ich1 = p6; Kanal1 ");
	    	orc.addElement("ich2 = 1-p6; Kanal2 ");
	    	orc.addElement("ifact pow .00125/p3, .6; Steigung ");
	    	orc.addElement("ifact = (p3 < .0039686 ? .5 : ifact)  ");
	    	orc.addElement("kenv linseg 0, idur*ifact, 1, idur*(1-2*ifact), 1, idur*ifact, 0 ");
	    	orc.addElement("aosc oscili ampdb(iamp)*kenv, ifreq, 1  ");
	    	orc.addElement("outs aosc*ich1, aosc*ich2 ");
	    	orc.addElement(" endin ;*********** ");
	    }

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
    PrintWriter imageIt = null;
    DataOutputStream dos;
    
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
    
    private void addToKomposition(Note nt) {
    	if (this.skompo != null)
    		this.skompo.println(nt.saveAsString());
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
    Vector<String> cache;
    
    /**
     * Save the actual random table in a line
     * @param rt
     * @param start
     */
   /* private void addImage(RandomTable rt, double start) {
    	int cacheLimit = 300;
    	if (this.dos == null) {
    		this.cIndex = 0;
    		this.icache = new int[cacheLimit * rt.table.length];
    		String log = home+File.separator+GetEnviroment.IMAGELOG;
    		try {
    			this.dos = new DataOutputStream (new FileOutputStream(log));
    			//this.dos.writeInt(rt.table.length);
    		}
    		catch (java.io.IOException e) {
    			this.dos = null;
    		} // End of IO Excep
    	}
    	if ((cIndex / rt.table.length) < cacheLimit) {
    		for (int n = 0; n < rt.table.length; n++)
    			this.icache[cIndex++] = (int) rt.table[n].freq;
    	}
    	else {
    		rt.writeTable(this.dos, icache, cIndex);
    		
    		this.cIndex = 0;
    	}
    	
    }
    */
    int[] icache;
    int cIndex;
    
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public Vector<Note> doKomposition(String home) { // Evolution
    	this.home = home;
    	boolean doBug = def.doLoopBug;
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
    	
    	// Tonal Values:
    	double start = 0.0;	// Notenstart
    	
    	
    	
    	
    	//int trigger = 3;
    	
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
	    freq = makeRandomFrequency(this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
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
		    sFreq = makeRandomFrequency(this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
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
	
	
	LoopObject o = new LoopObject(rt, def, this::debugOut, this::addLog);
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
	    
	    createPopulation(rt, this.def.population, 
				  this.def.min_freq, this.def.max_freq,def.tonal, def.preferLowerNotes, def.useSelectNotes); // create new Population
	    // Now weight array for each random frequency:
	    makeWeight(this.def.population, rt, def.r_Weight);// Judge them
	    if (shadow != null) {
	    	createPopulation(shadow, this.def.population, 
				  this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes); 
	    	makeWeight(this.def.population, shadow, def.r_Weight);// Judge them
	    }
	  
	    // now sort new population by weight;
	    Arrays.sort(rt.freq, Comparator.comparing(p -> p.weight));
	   // QSort q = new QSort(); // ( index = max equals the highest weight) sort population by weight, keep relation of weight and freq
	   // q.sort(rt.weight, rt.freq);
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
	    addImageS(rt, start);
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
	   
	    
	    //----------------------------------------------
	    
	   
	    //-------------------------------------------------------------------
	    o.addLoopProperties(rt, shadow, sFreq, notes, generator, envelope);
	    if( o.ndVoice != null) {
	    	// Akkorde !
	    	Note nta = new Note(start, o.tempo, o.dauer, o.bal, o.ndVoice.freq, generator, o.amplitude, envelope, true);
	    	nta.channel = 0; 	// first instrument
	    	nta.voices = o.stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
	    	nta.pDauer = freq.pedal?o.pDauer:0;
	    	try {
	    		nta.note = o.ndVoice.clone();
	    		addLog("Akkord active: "+nta.note);
			 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
	    		//komposition.addElement(nta);
	    		addToKomposition(nta);
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
	    debugOut("Dies wird "+(o.stimmen)+" stimmig.", 3);
	    Vector sti = new Vector();
	    sti.addElement(new Integer((int)freq.freq));
	    // Now see if there are more than 1 voice(s)
	    for (st = 1; st < o.stimmen; st++) { // noch eine Stimme dazu:
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
			 if (checkDouble((int)freq.freq, sti)) o.amplitude = 0; // is double
			 else { 
			     o.amplitude = getAmplitude(rt.readEntry((int)freq.freq), rt.max, min_amplitude, 
						      max_amplitude, this.def.amplify_amp);	
			     sti.addElement(new Integer((int)freq.freq));
			 }
			 //debugOut("Resulting amplitude ="+amplitude, 5);
			 if (this.def.stereo) o.bal = getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
			 //debugOut("Resulting balance: bal="+bal, 5);
			 o.frq = (double) freq.freq;
			 if (this.def.tonal) {
			     // get freq as double !
			     o.frq = getExactFreq((int)freq.freq, this.notes).freq;
			 }
			 if (o.tempo <= 0) {
			     System.out.println("tempo (b)="+o.tempo);
			     Converter.doBreak();
			 }
			 try {
				 Note ntn = new Note(start, o.tempo, o.dauer, o.bal, o.frq, generator, o.amplitude, envelope, false);
				 ntn.voices = o.stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
				 ntn.channel = st;
				 ntn.note = freq.clone();
				 ntn.note.pedal = false;
				 boolean transOK = false;
				 if (def.useTrans) {
				    	// modify the selected note by transposition effect:
				    	//transpositionNote
				    	transOK = false;
				    	int om = ntn.note.midiIndex;
				    	OneNote tn = LoopObject.doTransposition(ntn.note, o.transpositionNote, def.min_freq, def.max_freq);
				    	if (tn != null) {
				    		try {
				    			ntn.note = tn.clone();
				    			transOK = true;
				    			
				    		} catch (Exception ex) {}
				    	}
				    	if (transOK) 
				    		System.out.println("Voice "+st+" Transposition: diff="+o.transpositionNote+" change midiIndex "+om+" to "+ntn.note.midiIndex);
				    	else 
				    		System.out.println("Voice "+st+" Transposition: Note ignored. Reason: Outside freq.- bounds!");
				    }
				 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
				 if (o.amplitude != 0 && transOK) {
					 addLog((st+1)+". Stimme : dauer="+f.format(o.tempo)+" "+ntn.note);
					 addToKomposition(ntn);
					 //komposition.addElement(ntn);
				 }
			 } catch (Exception ex) {
				 ex.printStackTrace();
			 }
	    }
	    start += o.tempo;
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
		ret = addMelody(ret, melodyChannel, f);
		Collections.sort(ret, new Comparator<Note>() {
			public int compare(Note o1, Note o2){
				return o1.compareTo(o2);
			}
		});
	}
	if (this.def.duration_step_index > 0) 
		ret = quantisize(ret);
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
	private Vector<Note> quantisize(Vector<Note> in) {
    	Vector<Vector> interval = getInterval(in, true); // A vector of vectors for each interval 
    	
    	// now convert the intervalls back to one Note Vector:
    	Vector<Note> result = new Vector<Note>();
    	for (int n = 0; n < interval.size(); n++) {
    		Vector<Note> iv = (Vector) interval.elementAt(n);
    		result.addAll(iv);
    	}
    	
    	return result;
    }
    /**
     * Examine the Main Komposition and add a Melody to it
     * @param in
     * @param channel
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector<Note> addMelody(Vector<Note> in, int channel, DecimalFormat f) {
    	addLog("#-#-#-#-#-#-#- MELODY Iteration #-#-#-#-#-#-#");
    	/*
    	for (int n = 0; n < in.size(); n++) {
    		Note nt = in.elementAt(n);
    		System.out.println(nt.start+" "+nt.note);
    	}*/
    	int max_amplitude = getLoundness(this.def.max_amp); // Border values
    	int min_amplitude = getLoundness(this.def.min_amp);
    	int generator = 0;
    	int envelope = 0;
    	Melody melody = new Melody();
    	//int melodyChannel = def.voiceMax; // (7) first melody channel (after maxVoices) 
    	Note melodyTone = null;
    	this.shadow = new Soundscape(this.qd, this.Verbosity, 
   		     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
   		     this.def.impact, this.prs);
    	OneNote sFreq = null;
    	if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
    		sFreq = makeRandomFrequency(this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
    	}
    	else 
    		sFreq = new OneNote(this.def.seed, 0, 0);
    	this.shadow.seed = (int)sFreq.freq;
    	shadow.writeNEntrys(sFreq, this.def.degression, this.def.diff_freq );
    	double start = 0;
    	//-------------
    	Vector<Vector> interval = getInterval(in, false); // A vector of vectors for each interval 
    	//printInvteral(interval);
    	Vector<Note> all = null; // all notes of one intervall!
    	int it = 0;
    	for (it = 0; it < interval.size(); it++) { // loop over all intervals
    		all = interval.elementAt(it);
    		Note first = all.firstElement();
    		start = first.start;
    		shadow.iteration = it;
        	createPopulation(shadow, this.def.population, 
        			this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes); 
        	makeWeight(this.def.population, shadow, def.r_Weight);// Judge them
        	QSort q = new QSort(); // ( index = max equals the highest weight)
        	Arrays.sort(shadow.freq, Comparator.comparing(p -> p.weight));
        	//q.sort(shadow.weight, shadow.freq);
        	OneNote sfreq = null;
        	int is = (int) ((double) this.def.fittest * java.lang.Math.random());
    	    int sz = (this.def.population -1 ) - is;
        	sfreq = shadow.freq[sz];
        	//addLog("Shadow Index that will be selected:"+sz+" f="+sfreq);
        	shadow.writeNEntrys( sfreq, this.def.degression, this.def.diff_freq );
        	// sil.displayRT(rt);
        	shadow.getMaxx();
        	double m_bal = 0;
        	// fehlerhaft! int m_Tension = (int) getKleiner(melody.max, rt.max, this.def.min_tempo, this.def.max_tempo);
        	double val = shadow.readEntry((int)sfreq.freq);
        	// ??? int m_Tension = (int) getKleiner(val, shadow.max, 0.0, melody.max);
        	//double dauer = getKleiner(val, shadow.max, this.def.min_tempo, this.def.max_tempo);	
        	// aus der frequenz wird eine Tondauer, und daraus jetzt eine spannung [0-melody.max]//double v = getKleiner(dauer, shadow.max, this.def.min_tempo, this.def.max_tempo);
        	//double dt = dauer - this.def.min_tempo;
        	//double dbereich = this.def.max_tempo - this.def.min_tempo;
        	
        	/*
        	 * Spannung aus der Fitness nicht mehr aus der Dauer!
        	 */
        	double dte = getKleiner(val, shadow.max, 0.0, melody.max);	
        	int m_Tension = (int) (dte + 0.5);
        	//int m_Tension = (int) (dt * melody.max / dbereich +0.5);
        	//int m_Tension = (int) (1.5 * dt * melody.max / dbereich +0.5);
        	if ( m_Tension > melody.max )
        		m_Tension = (int) melody.max;
        	
        	//System.out.println("m_tension="+m_Tension);
        	int m_amplitude = getAmplitude(shadow.readEntry((int)sfreq.freq), shadow.max, min_amplitude, 
        			max_amplitude, this.def.amplify_amp);
        	if (this.def.stereo) 
        		m_bal = getBal(shadow.fittest_freq, (int)sfreq.freq, shadow.start, shadow.stop);
 
        	
        	// ################################################ //
        	//-------- Melody state machine:----------------
        	if (melodyTone == null) { // start the first melody note !
        		sfreq.getMidiNote(); // find the note from the frequency
		    	OneNote next = melody.findMelodyNote(first.note.noteIndex, 5, m_Tension);
		    	//OneNote next = melody.findNextMelodyNote(freq.noteIndex, 5, 0);
		    	melodyTone = new Note(start, 0.0, 0.0, m_bal, next.freq, generator, m_amplitude, envelope, false);
		    	melodyTone.channel = channel;
		    	try{
		    		melodyTone.note = next.clone(); 
		    		melodyTone.parent = first.note.clone(); 
		    	}catch (Exception ex) {}
		    }
		    
		    if (is < def.m_range && shadow.stateCnt == 0) 
		    	shadow.f_cnt++;
		    else 
		    	shadow.f_cnt = 0; // reset hit counter
		    
		    if (shadow.f_cnt >= def.m_trigger) { // Trigger the state machine, execute it at the next interval
		    	try{
		    		// change the melody !
		    		// first add the last MelodyNote to the komposition
		    		melodyTone.tempo = 0; //start - melodyTone.start;
		    		start = first.start; // neu bugfix wegen langer Tondauer!
		    		melodyTone.dauer = start - melodyTone.start - 0.1; // start
		    		System.out.println("Melody-Dauer(1) aus start="+start+" melodyTone.start="+melodyTone.start+" -> "+melodyTone.dauer);
		    		/*if (def.doDelay) {
		    			melodyTone.dauer -= melodyTone.delay / 1000.0;
		    			System.out.println("Melody-Dauer(1a) corrected with delay="+melodyTone.delay+" ->"+melodyTone.dauer);
		    		}
		    		*/
		    		if (melodyTone.dauer < 0)
		    			melodyTone.dauer = 0;
		    		
		    		melodyTone.misc = 222; // test to identify the melody notes
		    		all.addElement(melodyTone.clone());
		    		//addLog("Add melody: tone start="+f.format(melodyTone.start)+" dauer="+f.format(melodyTone.dauer)+" note="+melodyTone.note);
		    		//Hauptnote:
		    		int melody_octave = melodyTone.note.Octave;
		    		//int haupt_note = first.note.noteIndex;
		    		// Hauptnote besimmt welche m�glichen Melodienoten passen,
		    		first.note.setMidiNr(first.note.midiIndex); // Bugfix: Noteindex was always 0 (C)!
		    		OneNote next = melody.findMelodyNote(first.note.noteIndex, melody_octave, m_Tension);
		    		//OneNote next = melody.findNextMelodyNote(haupt_note, melody_octave, melodyTone.note.noteIndex);
		    		// new Melody not:
		    		int amplitude = m_amplitude; // war 70!
		    		melodyTone = new Note(start, 0.0, 0.0, m_bal, next.freq, generator, amplitude, envelope, false);
			    	melodyTone.channel = channel;
			    	double ly =  getKleiner(val, shadow.max, 0.0, this.def.max_tempo) * 1000.0;
			    	melodyTone.delay = 0.0;
			    	if (ly > 0 && this.def.doDelay)
			    		melodyTone.delay = ly;
			    	//melodyTone.delay = 500; Test
			    	melodyTone.note = next.clone();
			    	melodyTone.parent = first.note.clone(); 
			    	addLog("#### Trigger Melody: note from "+first.note.noteIndex+","+first.note.note+";"+first.note.Octave+" tension="+m_Tension+" --> "+melodyTone.note.note+";"+melodyTone.note.Octave);
		    	} catch (Exception ex) {
		    		ex.printStackTrace();
		    	}
		    	
		    	shadow.f_cnt = 0;
	 	    	
		    }
		    interval.setElementAt(all,  it);
		    //##############################################
	    } // end of for loop.
    	if (melodyTone != null) {
    		melodyTone.tempo = 0; //start - melodyTone.start;
    		melodyTone.dauer = start - melodyTone.start;
    		System.out.println("Melody-Dauer(2) aus start="+start+" melodyTone.start="+melodyTone.start+" -> "+melodyTone.dauer);
    		if (melodyTone.dauer > this.def.max_tempo)
    			melodyTone.dauer = this.def.max_tempo;
    		/*melodyTone.dauer -= melodyTone.delay / 1000.0;
    		if (melodyTone.dauer < 0)
    			melodyTone.dauer = 0;
    			*/
    		melodyTone.misc = 222; // test
    		all.addElement(melodyTone);
    		interval.setElementAt(all,  interval.size() -1);
    		//addToKomposition(melodyTone);
    	}
    	// now convert the intervalls back to one Note Vector:
    	Vector<Note> result = new Vector<Note>();
    	for (int n = 0; n < interval.size(); n++) {
    		Vector<Note> iv = interval.elementAt(n);
    		result.addAll(iv);
    	}
    	
    	Collections.sort(result, new Comparator<Note>() {
			public int compare(Note o1, Note o2){
				return o1.compareTo(o2);
			}
		});
    	if (def.doDelay) {
    		result = correctMelody(result);
    	}
    	saveOverview(result);
    	return result;
    }
    
    private Vector<Note> correctMelody(Vector<Note> v) {
    	Vector<Note> m = getMelody(v);
    	if (m.size() < 2)
    		return v;
    	Vector<Note> b = getBase(v);
    	// now analyze the melody track only
    	// - no overlaps
    	// - no neg. durations
    	Note old = null;
    	Vector<Note> mod = new Vector<Note>();
    	int index = 0;
    	for (int n = 0; n < m.size(); n++) {
    		Note nt = m.elementAt(n); // a new melody note
    		if (nt.dauer > 0.0)  { // first > 0!
    			old = nt;
    			index = n;
    			break;
    		}
    	}
    	//printDebug(m);
    	//m = getDebug();
    	for (int n = index; n < m.size(); n++) {
    		Note nt = m.elementAt(n); // a new melody note
    		double m_start = nt.start + nt.delay / 1000.0;
    		double end = nt.getEndTime(); 
    		/*System.out.println("n="+n+" base="+Converter.formatDouble(nt.start, 8)+
    				" delay="+Converter.formatDouble(nt.delay, 8)+
    				" m_start="+Converter.formatDouble(m_start, 8)+
    				" dauer="+Converter.formatDouble(nt.dauer, 8)+
    				" m_end="+Converter.formatDouble(end, 8));
    				*/
    		double oldend = old.getEndTime();
    		if (end > oldend) {
    			//Valid note
    			try {
	    			double tmp = m_start - old.getEndTime(); // um diesen Betrag mu� die vorherige note verl�ngert werden!
	    			if (tmp > 0.0)
	    				old.dauer += tmp;
	    			else {
	    				// this note starts before the last ends! so increase the delay!
	    				nt.delay += Math.abs(tmp) * 1000.0;
	    				nt.dauer += tmp;
	    				/*System.out.println("nt.delay="+nt.delay);
	    				m_start = nt.start + nt.delay / 1000.0;
	    	    		end = nt.getEndTime(); 
	    				System.out.println("n="+n+" base="+Converter.formatDouble(nt.start, 8)+
	    	    				" delay="+Converter.formatDouble(nt.delay, 8)+
	    	    				" m_start="+Converter.formatDouble(m_start, 8)+
	    	    				" dauer="+Converter.formatDouble(nt.dauer, 8)+
	    	    				" m_end="+Converter.formatDouble(end, 8));
	    	    				*/
	    			}
	    				
	    			mod.addElement(old.clone());
	    			old = nt.clone();
    			}catch(Exception ex) {}
    		}
    	}
    	mod.addAll(b);
    	Collections.sort(mod, new Comparator<Note>() {
			public int compare(Note o1, Note o2){
				return o1.compareTo(o2);
			}
		});
    	//saveOverview(mod);
    	//System.exit(1);
    	
    	return mod;
    }
    
    private Vector<Note> getBase(Vector<Note> v) {
    	Vector<Note> mel = new Vector<Note>();
    	for (int n= 0; n < v.size(); n++) {
    		Note nt = v.elementAt(n);
    		if (nt.misc != 222)
    			mel.addElement(nt);
    	}
    	return mel;
    }
    
    private Vector<Note> getMelody(Vector<Note> v) {
    	Vector<Note> mel = new Vector<Note>();
    	for (int n= 0; n < v.size(); n++) {
    		Note nt = v.elementAt(n);
    		if (nt.misc == 222)
    			mel.addElement(nt);
    	}
    	return mel;
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
	public Vector<Vector> getInterval(Vector<Note> in, boolean quant) {
    	double start = 0.0;
    	int index = 0;
    	Vector<Vector> mi = new Vector<Vector>();
    	double oneQuant = 0.0; 
    	if (this.def.duration_step_index > 0) {
	    	oneQuant = (double) this.def.durations[this.def.duration_step_index] / 1000.0;//this.durations[this.def.duration_step_index]; //getDurationFromTable(dauer);
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
	
    public void makeWeight(int n, Soundscape rt, double r_Weight) {
		int i;
		double t;
		//rt.weight = new double[rt.freq.length];
		// first get the weight from the RandomTable:
		debugOut("makeWeight() Get the weight for the new random frequencys:", 6);
		for (i = 0; i < rt.freq.length; i++) {
		    rt.freq[i].weight = t = rt.readEntry((int)rt.freq[i].freq); // read individuum from soundscape
		    // randomize this weight:
		    // 1. : this.weight[i] = this.weight[i] * r_Weight * java.lang.Math.random();
		    rt.freq[i ].weight -= r_Weight * java.lang.Math.random();
		    debugOut("makeWeight() weight for frequenz ="+rt.freq[i]+" Hz ="+t+" randomized="+rt.freq[i].weight, 6);
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
    public OneNote getChromaticFrequency() {
		int index = (int) ((double) (this.notes.size()) * java.lang.Math.random()); // index
		//System.out.println("getChromaticFrequency() from max="+this.notes.size()+" r_index="+index);
		OneNote f = this.notes.elementAt(index);
		//this.rndStatic[index]++;
		//System.out.println("getChromaticFrequency() min="+this.def.min_freq+" max="+this.def.max_freq+" rnd="+f);
		return f;
    }

    public OneNote makeRandomFrequency(int min, int max, boolean tonal, boolean preferLowerNotes, boolean useSelectNotes) {
		double f = 0.0;
		double diff = (double) (max - min);
		f = min + (int) (diff  * java.lang.Math.random());
		OneNote nf = new OneNote(f, 0, 0);
		
		if (tonal) {
		    if (!preferLowerNotes) { // 
		    	nf = getExactFreq((int) f, this.notes);
		    }
		    else { // prefer lower notes !, so we choose the next lower freq of our frequency
		    	// 
		    	nf = getChromaticFrequency();
		    }
		}
		else if (useSelectNotes) {
			nf = getExactFreq((int) f, this.notes);
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
    public void createPopulation(Soundscape rt, int population, int min, int max, boolean tonal, boolean preferLowerNotes, boolean useSelectNotes) {
    	rt.freq = new OneNote[population];
    	double maxf = 0;
    	for (int i = 0; i < population; i++) {
    		rt.freq[i] = makeRandomFrequency(min, max+1, tonal, preferLowerNotes, useSelectNotes);
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
     * Das Ergebnis ist ein Vector mit den .sco Zeilen f�r die T�ne.
     * @param parm ein �bergabeparameter, kann auch leer sein (vom Textfeld)
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







