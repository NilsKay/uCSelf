package Sound;
import java.util.*;
import java.lang.Math;
import Utils.QSort;
import Utils.Converter;
/**
 * This class holds our Project 2 (Sound evolution)
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Project2 {
    CDebug qd;
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
    int freq[];		// frist random Frequencys
    double weight[]; 	// weight for these freqencys
    Vector generators, envelopes;
    Vector notes;	// a vector that holds the tonal notes frequencys
    //---------- Variables:
    public boolean halt = false;
    int anfOkt;
    double balance;
    RandomTable rt;
    Defaults def;
    Vector komposition;	// this is our Komposition ! (elements of Note)

    public Project2(SoundInfoListener sil) {
	this(null, 5, sil);
    }
    
    /**
     * Constructor des 1. Projektes:
     * Eine ganze musikalische Note errechnet sich so:
     * (Oktave * 220) * 2 ^ (x/12);
     * x geht von 0-11:
     * 0=A, 1=A#, B=2, C=3, C#=4, D=5, D#=6, E=7, F=8, F#=9, G=10, G#=11
     * Es gibt ca. 12 Oktaven ?
     */
    public Project2(CDebug qd, int ver, SoundInfoListener sil) {
	// Constructor
	this.qd = qd;
	this.sil = sil;
	this.Verbosity = ver;
	debugOut("Project2 : Constructor", 1);
    }

    public void createNoteTable(int mode) {
	int maxOkt;
	int n, m, q, freq;
	double p, f;
	this.notes = new Vector();
	//System.out.println("Tonal, createNotetable: mode="+mode);
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
			this.notes.addElement(new Double(f));
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
			this.notes.addElement(new Double(f));
			//System.out.println("Tonal,add f="+f);
			debugOut("Note "+(n-1)+"="+freq, 3); 
		    }
		}
	    }
	    break;
	}
	// debug:
	for (n = 0; n < this.notes.size(); n++) {
	    double d = ((Double)this.notes.elementAt(n)).doubleValue();
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
	    d = ((Double)this.notes.elementAt(n)).doubleValue();
	    if (d >= in ) return n+off;
	}
	return n+off; // max
    }

    public void debugOut(String tmp, int v) {
	if (v > this.Verbosity) return;
	if (this.qd != null) this.qd.put(tmp);
	else System.out.println(tmp);
    }

    public Vector getHeader(boolean fof) {
	String tmp;
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

    public Vector doKomposition() { // Evolution
	String tmp;
	Note nt;
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
	if (this.def.seed <= this.def.min_freq) { // do random
	    freq = (int) makeRandomFrequency(this.def.min_freq, this.def.max_freq);
	}
	else if (this.def.tonal) 
	    freq = (int) makeRandomFrequency(this.def.min_freq, this.def.max_freq);
	else freq = this.def.seed;
	rt = new RandomTable(this.qd, this.Verbosity, 
			     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0);
	rt.seed = freq;
	// Veraendere die Tabelle:
	// freq, um wieviel nach oben, welche Frequenz-differenz zu beachten ist
	rt.writeNEntrys(freq, this.def.impact, this.def.diff_freq );
	//this.ce.gc.setTable(rt);	// Anzeigen !
	sil.displayRT(rt);
	debugOut("This is our seed :"+freq+" Hz", 5);
	flowControl();
	// Now iterate:
	for (it = 0; it < this.def.iterations; it++) {
	    // Step 1 generate n random Frequencys:
	    rt.iteration = it;// needed for the display 
	    makeNRandomFrequencys(this.def.population, 
				  this.def.min_freq, this.def.max_freq); // create Population
	    // Now weight array for each random frequency:
	    makeWeight(this.def.population, rt);// Judge them
	    // now sort it;
	    QSort q = new QSort(); // ( index = max equals the highest weight)
	    q.sort(this.weight, this.freq);
	    if (this.Verbosity >= 6 ) {
		for (z = 0; z < this.def.population; z++) 
		    debugOut("Sorted: index="+z+" freq="+this.freq[z]+" weight="+this.weight[z], 6);
	    }
	    // Next select randomly the fittest:
	    z = (this.def.population -1 ) - (int) ((double) this.def.fittest * java.lang.Math.random());
	    debugOut("Index that will be selected:"+z, 5);
	    // --------- Nun ist ein Individuum selektiert ! ----------------
	    freq = this.freq[z];
	    //System.out.println("New freq="+freq);
	    debugOut("This is our new selection:"+freq+" Hz", 5);
	    // Mark the new individual in the RT
	    rt.writeNEntrys( freq, this.def.impact, this.def.diff_freq );
	    sil.displayRT(rt);
	    //this.ce.gc.setTable(rt);	// Anzeigen ! (also find max in RandomTable)
	    // --------- Jetzt noch ein paar Properites dieser Frequenz : -----------
	    amplitude = getAmplitude(rt.readEntry(freq), rt.max, min_amplitude, 
				     max_amplitude, this.def.amplify_amp);	
	    // amplitude = loud[0];
	    debugOut("Resulting amplitude ="+amplitude, 5);
	    if (this.def.stereo) bal = getBal(rt.fittest_freq, freq, rt.start, rt.stop);
	    debugOut("Resulting balance: bal="+bal, 5);
	    dauer = getKleiner(rt.readEntry(freq), rt.max, this.def.min_tempo, this.def.max_tempo);	
	    if (dauer < this.def.min_tempo || dauer > this.def.max_tempo) debugOut("Project2: tempo out of range ="+dauer, 4);
	    frq = (double) freq;
	    if (this.def.tonal) {
		// get freq as double !
		frq = getExactFreq(freq);
		//System.out.println("Tonal: freq="+freq+" wird zu:"+frq);
	    }
	    //else System.out.println("Free: frq="+frq);
	    //---- erzeuge eine neue Note mit den oben ermittelten Werten.--------------
	    nt = new Note(start, dauer, bal, frq, generator, amplitude, envelope);
	    // ------- Nun prüfe, ob noch Stimmen dazukommen ? --------------------------
	    stimmen = 2; // nur für Test
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
		 nt = new Note(start, dauer, bal, frq, generator, amplitude, envelope);
		 nt.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
		 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
		 komposition.addElement(nt);
	    }
	    start+= dauer;
	    if (this.halt) break;	// stop doing it
	    flowControl();
	}
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
	return (Amin + ((Amax - Amin) * (Wmax - w) / Wmax));
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
    
    public int getAmplitude(double weight, double Wmax, int Amin, int Amax, double amplifier) {
	debugOut("getAmplitude: weight="+Converter.formatDouble(weight, 8)+" Wmax="+Converter.formatDouble(Wmax, 8)+" Amin="+Amin+" Amax="+Amax+" amplifier="+Converter.formatDouble(amplifier, 8), 6);//6
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
	}
    }
    public int getChromaticFromSeed(int seed) {
	int v,n;
	for (n = 0; n < this.notes.size(); n++) {
	    v = (int) ((Double) this.notes.elementAt(n)).doubleValue();
	    if (v > seed) return v;
	}
	v = (int) ((Double) this.notes.elementAt(n)).doubleValue();
	// Not in Table
	return v; // max maximum possible
    }

    public double getExactFreq(int freq) {
	double v;
	int n;
	for (n = 0; n < this.notes.size(); n++) {
	    v = ((Double) this.notes.elementAt(n)).doubleValue();
	    if (v > freq) return v;
	}
	v = ((Double) this.notes.elementAt(n-1)).doubleValue();
	// Not in Table
	return v; // max maximum possible
    }

    public double getChromaticFrequency() {
	int index = (int) ((double) (this.notes.size() -1) * java.lang.Math.random()); // index
	//System.out.println("getChromaticFrequency() r_index="+index);
	return ((Double) this.notes.elementAt(index)).doubleValue();
    }

    public double makeRandomFrequency(int min, int max) {
	double f = 0.0;
	double diff = (double) (max - min);
	f = min + (int) (diff  * java.lang.Math.random());
	if (this.def.tonal) {
	    if (!this.def.preferLowerNotes) { // equal
		f = getExactFreq((int) f);
	    }
	    else { // prefer lower notes
		f = getChromaticFrequency();
	    }
	}
	return f;
    }

    public void makeNRandomFrequencys(int n, int min, int max) {
	this.freq = new int[n];
	for (int i = 0; i < n; i++) {
	    this.freq[i] = (int) makeRandomFrequency(min, max);
	}	
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

    public Vector doProject(Defaults def) {	
	this.def = def;
	/*
	this.max_amp = def.max_amp;
	this.min_amp = def.min_amp;
	this.seed = def.seed;
	this.min_freq = def.min_freq;
	this.max_freq = def.max_freq;
	this.step = def.interval;
	this.impact = def.impact;
	this.diff_freq = def.diff_freq;
	this.firstAmount = def.population;
	this.iterations = def.iterations;
	this.toChooseFrom = def.fittest;
	this.amplifier = def.amplify_amp;
	this.stereo = def.stereo;
	this.min_tempo = def.min_tempo;
	this.max_tempo = def.max_tempo;
	this.r_Weight = def.r_Weight;
	this.tonal = def.tonal;
	this.preferLowerNotes = def.preferLowerNotes;
	
	System.out.println("def.min_voice="+def.min_voice+" def.max_voice="+def.max_voice);
	this.min_voice = def.min_voice;
	this.max_voice = def.max_voice;
	this.gamma = def.gamma;
	this.fof = def.newFOF;
	*/
	this.balance = 0.5;	// symetrical default
	if (this.def.tonal) createNoteTable(this.note_mode);
	return generateSound();
    }
    
    /**
     * Hier nun das Herz der Methode: wie baut man eine Ton-Abfolge.
     * Das Ergebnis ist ein Vector mit den .sco Zeilen für die Töne.
     * @param parm ein Übergabeparameter, kann auch leer sein (vom Textfeld)
     * @return Vector with .sco lines
     */
    public Vector generateSound() {
	Note nt;
	String tmp;
	Vector result = new Vector();
	komposition = doKomposition();	// Evolution
	//ce.mi1_5.setEnabled(true);
	// ----------------------------------------------------------
	// so macht man dann aus einem Note - Eintrag eine sco-line :
	for(int n = 0; n < komposition.size(); n++) {
	    nt = (Note) komposition.elementAt(n);
	    tmp = Project2.DO+"\t"+Converter.formatDouble(nt.start, 10)+"\t"+Converter.formatDouble(nt.dauer, 10);
	    if (nt.freq > 0) tmp+= "\t"+Converter.formatDouble(nt.freq, 10);
	    if (nt.generator > 0) tmp+= "\t"+nt.generator;
	    if (nt.amplitude >= 0) tmp+= "\t"+nt.amplitude;
	    if (nt.envelope > 0 ) tmp+= "\t"+nt.envelope;
	    if (nt.balance > 0 ) tmp+= "\t"+Converter.formatDouble(nt.balance, 6, 4);// auch formatieren
	    // Anzahl Stimmen bei fof
	    if (this.def.fof ) tmp+= "\t\t"+nt.voices;
	    result.addElement(tmp);
	    debugOut("add line:"+tmp, 6);
	}
	return result;
    }
    
    public int mapAplitude(int a) {
	int max = this.loud[loud.length-1]; // max. c-sound ampl.
	return ( a * 127 / max); // midi has 127 as max. amplitude val.
    }
	
}// end of class







