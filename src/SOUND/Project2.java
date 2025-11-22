package SOUND;
import java.util.*;

import Utils.QSort;
import Utils.Converter;
import Utils.Utils;

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
     // bei fof ist 90 zu laut, bei 85 Schluß !

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
    int freq[];		// first random Frequencys
    double weight[]; 	// weight for these freqencys
    @SuppressWarnings("rawtypes")
	Vector generators, envelopes;
   
    //Vector notes;	// a vector that holds the tonal notes frequencys
    Vector<OneNote> notes;// a vector that holds the tonal notes frequencys
    double[] durations;	// a table with the used durations
    //---------- Variables:
    public boolean halt = false;
    int anfOkt;
    double balance;
    RandomTable rt;
    Defaults def;
    @SuppressWarnings("rawtypes")
    Vector komposition;	// this is our Komposition ! (elements of Note)
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void createNoteTable(int mode) {
	int maxOkt;
	int n, m, q, freq;
	double p, f;
	this.notes = new Vector();
	System.out.println("Tonal, createNotetable: mode="+mode);
	switch (mode) {
	case 0:
	default:
	    n = 0;
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
					this.notes.addElement(new OneNote(f, this.nts[q], m));
					//System.out.println("Tonal,add f="+f);
					// make this a double for non-integer frequencys
					debugOut("Note "+((m-1)*12+n)+"="+f, 3); 
			    }
			}
	    }
	    break;
	case 1:
	    maxOkt = 11;
	    this.anfOkt = 2; // in wirklichkeit 4, da es keine 0. Oktave gibt ?
	    n = 12 * anfOkt;
	    debugOut("First Oktave="+anfOkt+" last Oktave ="+maxOkt, 3);
	    for (m = anfOkt; m < maxOkt; m++) {
			for (q = 0; q < 12; q++) { // 12 Töne: a, a', b, c, c', d, d', e, f, f', g, g'
			    f = getFreq(n++);
			    freq = (int) f;
			    if (freq >= this.def.min_freq && freq <= this.def.max_freq ) {
					debugOut("Oktave="+m+" Note="+n+" freq="+freq, 6);
					this.notes.addElement(new OneNote(f, this.nts[q], m-1));
					//System.out.println("Tonal,add f="+f);
					debugOut("Note "+(n-1)+"="+freq, 3); 
			    }
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

    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public Vector doKomposition() { // Evolution
	//System.out.println("min_dur="+this.def.min_tempo);
	String tmp;
	Note nt;
	long dela = DELAY;
	if (this.def.mode == 0) 
	    dela = DDELAY;
	int n, z, it, st;
	double frq;
	// Tonal Values:
	double start = 0.0;	// Notenstart
	double dauer;	// Tondauer
	int max_amplitude = getLoundness(this.def.max_amp); // Border values
	int min_amplitude = getLoundness(this.def.min_amp);
	int amplitude = max_amplitude;	// actual value
	int stimmen;
	double bal = this.balance;
	int generator, envelope = 0;
	Vector komposition = new Vector(); 
	//envelope = envelope = ((Integer) this.envelopes.elementAt(0)).intValue();
	generator = 0; 	// ((Integer) this.generators.elementAt(0)).intValue();
	debugOut("KOMPOSITION: (Evolution)", 1);
	// Angangsbedingung:
	int freq;
	//----------- Test:
	//this.rndStatic = new int[this.notes.size()];

	if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
	    freq = (int) makeRandomFrequency(this.def.min_freq, this.def.max_freq);
	}
	else freq = this.def.seed;

	rt = new RandomTable(this.qd, this.Verbosity, 
			     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
			     this.def.impact, this.prs);
	rt.seed = freq;
	// Veraendere die Tabelle:
	// freq, um wieviel nach oben, welche Frequenz-differenz zu beachten ist
	rt.writeNEntrys(freq, this.def.degression, this.def.diff_freq );
	//this.ce.gc.setTable(rt);	// Anzeigen !
	//sil.displayRT(rt);
	debugOut("This is our seed :"+freq+" Hz", 5);
	debugOut("################### Start of new Komposition: (Oszi) ###################", 55);
	debugOut("min freq="+this.def.min_freq+" max_freq="+this.def.max_freq+" population="+this.def.population+" degression="+this.def.degression+" weight="+this.def.r_Weight, 55);
	flowControl();
	// Now iterate:
	for (it = 0; it < this.def.iterations; it++) {
	    long time = System.currentTimeMillis();
	    //debugOut("-------> New Iteration step:"+it, 55);
	    // Step 1 generate n random Frequencys:
	    rt.iteration = it;// needed for the display 
	    makeNRandomFrequencys(this.def.population, 
				  this.def.min_freq, this.def.max_freq); // create Population
	    // Now weight array for each random frequency:
	    makeWeight(this.def.population, rt);// Judge them
	    /*for (z = 0; z < this.def.population; z++) 
		debugOut("makeWeight: index="+z+" freq="+this.freq[z]+" weight="+this.weight[z], 55);// log
	    */
	    // now sort it;
	    QSort q = new QSort(); // ( index = max equals the highest weight)
	    q.sort(this.weight, this.freq);
	    if (this.Verbosity >= 6 ) {
		for (z = 0; z < this.def.population; z++) 
		    debugOut("Sorted: index="+z+" freq="+this.freq[z]+" weight="+this.weight[z], 6);
	    }
	    /*for (z = 0; z < this.def.population; z++) 
		debugOut("Sorted: index="+z+" freq="+this.freq[z]+" weight="+this.weight[z], 55);// log
	    */
	    // Next select randomly the fittest (the last ones in the array are the fittest !):
	    z = (this.def.population -1 ) - (int) ((double) this.def.fittest * java.lang.Math.random());
	    debugOut("Index that will be selected:"+z, 5);
	    //debugOut("Index that will be selected:"+z, 55);
	    // --------- Nun ist ein Individuum selektiert ! ----------------
	    freq = this.freq[z];
	    //freq = this.def.max_freq - 1; // test
	    //System.out.println("New freq="+freq);
	    debugOut("This is our new selection:"+freq+" Hz", 5);
	    //debugOut("This is our new selection:"+freq+" Hz", 55);
	    // Mark the new individual in the RT
	    rt.writeNEntrys( freq, this.def.degression, this.def.diff_freq );
	    sil.displayRT(rt);
	    rt.getMaxx();
	    //this.ce.gc.setTable(rt);	// Anzeigen ! (also find max in RandomTable)
	    // --------- Jetzt noch ein paar Properites dieser Frequenz : -----------
	    amplitude = getAmplitude(rt.readEntry(freq), rt.max, min_amplitude, 
				     max_amplitude, this.def.amplify_amp);	
	    // amplitude = loud[0];
	    debugOut("Resulting amplitude ="+amplitude, 5);
	    if (this.def.stereo) bal = getBal(rt.fittest_freq, freq, rt.start, rt.stop);
	    debugOut("Resulting balance: bal="+bal, 5);
	    double val = rt.readEntry(freq);
	    dauer = getKleiner(val, rt.max, this.def.min_tempo, this.def.max_tempo);	
	    if (dauer < this.def.min_tempo || dauer > this.def.max_tempo) {
	    	debugOut("Project2: tempo out of range ="+dauer, 4);
	    }
	   /*if (rt.max < val ) 
		   System.out.println("it="+it+" val="+val+" max="+rt.max);
		   */
	    //System.out.println("(1)At iteration nr.:"+it+" dauer="+dauer);
	    if (this.def.duration_step_index > 0) {
	    	dauer = getDurationFromTable(dauer);
	    }
	    //System.out.println("(2)At iteration nr.:"+it+" dauer="+dauer);
	    frq = (double) freq;
	    
	    if (this.def.tonal) {
		// get freq as double !
		frq = getExactFreq(freq);
	    }
	    //else System.out.println("Free: frq="+frq);
	    //---- erzeuge eine neue Note mit den oben ermittelten Werten.--------------
	    if (dauer <= 0) {
	    	System.out.println("Dauer (a)="+dauer);
	    	Converter.doBreak();
	    }
	    nt = new Note(start, dauer, bal, frq, generator, amplitude, envelope);
	    // ------- Nun prüfe, ob noch Stimmen dazukommen ? --------------------------
	    //stimmen = 2; // nur für Test
	    stimmen = getVoices(rt.fittest_freq, freq, rt.start, rt.stop);
	    // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
	    nt.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
	    komposition.addElement(nt);
	    debugOut("Dies wird "+(stimmen)+" stimmig.", 3);
	    Vector sti = new Vector();
	    sti.addElement(new Integer(freq));
	    // Now see if there are more than 1 voice(s)
	    for (st = 1; st < stimmen; st++) {
		// noch eine Stimme dazu:
		 z = (this.def.population -1 ) - 
		     (int) ((double) this.def.fittest * java.lang.Math.random());
		 //debugOut("Index that will be selected:"+z, 5);
		 // --------- Nun ist ein Individuum selektiert ! ----------------
		 freq = this.freq[z];
		 //freq = this.def.max_freq; // test
		 //debugOut("This is our new selection:"+freq+" Hz", 5);
		 // Mark the new individual in the RT
		 //rt.writeNEntrys( freq, this.impact, this.def.diff_freq );
		 //sil.displayRT(rt);
		 if (checkDouble(freq, sti)) amplitude = 0; // is double
		 else { 
		     amplitude = getAmplitude(rt.readEntry(freq), rt.max, min_amplitude, 
					      max_amplitude, this.def.amplify_amp);	
		     sti.addElement(new Integer(freq));
		 }
		 //debugOut("Resulting amplitude ="+amplitude, 5);
		 if (this.def.stereo) bal = getBal(rt.fittest_freq, freq, rt.start, rt.stop);
		 //debugOut("Resulting balance: bal="+bal, 5);
		 frq = (double) freq;
		 if (this.def.tonal) {
		     // get freq as double !
		     frq = getExactFreq(freq);
		 }
		 if (dauer <= 0) {
		     System.out.println("Dauer (b)="+dauer);
		     Converter.doBreak();
		 }
		 nt = new Note(start, dauer, bal, frq, generator, amplitude, envelope);
		 nt.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
		 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
		 komposition.addElement(nt);
	    }
	    start += dauer;
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
	return komposition;
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
     * @param all freq
     */ 
    public int getVoices(int fittest, int freq, int start, int stop) {
	double diff = java.lang.Math.abs(fittest-freq); // Abstand vom Fittesten [freq]
	debugOut("getVoices(): fittest="+fittest+" freq="+freq+" diff="+diff, 3);
	double max = (double) (stop -start); // Wertebereich [freq]
	double anzStimmen = (double) (this.def.max_voice - this.def.min_voice);	// [Stimmen]
	// je größer diff, desto mehr stimmen (weiter weg)
	double v = anzStimmen * java.lang.Math.pow( diff / max, this.def.gamma);
	// je kleiner diff, desto mehr stimmen (näher dran)
	double v1 = anzStimmen * java.lang.Math.pow( (max - diff) / max, this.def.gamma);
	//double v = diff * anzStimmen / max; // je weiter weg, desto mehr Stimmen
	//double v1 = (max - diff) * anzStimmen / max; // je näher, desto mehr Stimmen
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
	this.weight = new double[n];
	// first get the weight from the RandomTable:
	debugOut("makeWeight() Get the weight for the new random frequencys:", 6);
	for (i = 0; i < n; i++) {
	    this.weight[i] = t = rt.readEntry(this.freq[i]);
	    // randomize this weight:
	    // 1. : this.weight[i] = this.weight[i] * r_Weight * java.lang.Math.random();
	    this.weight[i] = this.weight[i] - this.def.r_Weight * java.lang.Math.random();
	    debugOut("makeWeight() weight for frequenz ="+this.freq[i]+" Hz ="+t+" randomized="+this.weight[i], 6);
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

    public double getExactFreq(int freq) {
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

    /**
     * Get a random freq. from the note-table. This table starts with our min.-freq
     * and ends with the max.-freq.
     * the indecees are equally distributed, checked 2nd of July 2003 nik
     * @return a random freq.
     */
    public double getChromaticFrequency() {
		int index = (int) ((double) (this.notes.size()) * java.lang.Math.random()); // index
		//System.out.println("getChromaticFrequency() from max="+this.notes.size()+" r_index="+index);
		double f = this.notes.elementAt(index).freq;
		//this.rndStatic[index]++;
		//System.out.println("getChromaticFrequency() min="+this.def.min_freq+" max="+this.def.max_freq+" rnd="+f);
		return f;
    }

    public double makeRandomFrequency(int min, int max) {
		double f = 0.0;
		double diff = (double) (max - min);
		f = min + (int) (diff  * java.lang.Math.random());
		if (this.def.tonal) {
		    if (!this.def.preferLowerNotes) { // 
		    	f = getExactFreq((int) f);
		    }
		    else { // prefer lower notes !, so we choose the next lower freq of our frequency
		    	// 
		    	f = getChromaticFrequency();
		    }
		}
		else if (this.def.useSelectNotes) {
			f = getExactFreq((int) f);
		}
		return f;
    }

    public void makeNRandomFrequencys(int n, int min, int max) {
    	this.freq = new int[n];
    	double maxf = 0;
    	for (int i = 0; i < n; i++) {
    		this.freq[i] = (int) makeRandomFrequency(min, max+1);
    		if (freq[i] > maxf )
    			maxf = freq[i];
    	}	
    	//System.out.println("Max="+maxf);
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
	public Vector doProject(Defaults def) {	
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
		return generateSound();
    }
    
    /**
     * Hier nun das Herz der Methode: wie baut man eine Ton-Abfolge.
     * Das Ergebnis ist ein Vector mit den .sco Zeilen für die Töne.
     * @param parm ein Übergabeparameter, kann auch leer sein (vom Textfeld)
     * @return Vector with .sco lines
     */
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public Vector generateSound() {
	Note nt;
	String tmp;
	Vector result = new Vector();
	komposition = doKomposition();	// Evolution
	//ce.mi1_5.setEnabled(true);
	// ----------------------------------------------------------
	String tst;
	double dd;
	// so macht man dann aus einem Note - Eintrag eine sco-line :
	for(int n = 0; n < komposition.size(); n++) {
	    nt = (Note) komposition.elementAt(n);
	    tmp = Project2.DO+"\t"+Converter.formatDouble(nt.start, 10)+"\t";
	    tmp += Converter.formatDouble(nt.dauer, 10);
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
	    result.addElement(tmp);
	    debugOut("add line:"+tmp, 6);
	}
	return result;
    }
    
    @SuppressWarnings("static-access")
	public int mapAplitude(int a) {
	int max = this.loud[loud.length-1]; // max. c-sound ampl.
	return ( a * 127 / max); // midi has 127 as max. amplitude val.
    }
	
}// end of class







