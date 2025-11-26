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
   
     // bei fof ist 90 zu laut, bei 85 Schluï¿½ !

    

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
    
   
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public Vector<OneNote> createNoteTable(int mode, int min_freq, int max_freq) {
		int maxOkt;
		int n, m, q, freq;
		double p, f;
		this.notes = new Vector();
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
				this.notes.addElement(nt);
				debugOut("Note "+nt, 3);
			}
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
		    freq = ProjectTools.makeRandomFrequency(notes, this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
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
			    sFreq =  ProjectTools.makeRandomFrequency(notes, this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
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
		int max_amplitude =  ProjectTools.getLoundness(this.def.max_amp, def.fof); // Border values
		int min_amplitude =  ProjectTools.getLoundness(this.def.min_amp, def.fof);
		
		
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
		    
		    ProjectTools.createPopulation(notes, rt, this.def.population, 
					  this.def.min_freq, this.def.max_freq,def.tonal, def.preferLowerNotes, def.useSelectNotes); // create new Population
		    // Now weight array for each random frequency:
		    ProjectTools.makeWeight(this.def.population, rt, def.r_Weight, this::debugOut);// Judge them
		    if (shadow != null) {
		    	 ProjectTools.createPopulation(notes, shadow, this.def.population, 
					  this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes); 
		    	 ProjectTools.makeWeight(this.def.population, shadow, def.r_Weight, this::debugOut);// Judge them
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
			    m_Tension = (int)  ProjectTools.getKleiner(melody.max, rt.max, this.def.min_tempo, this.def.max_tempo);
			    m_amplitude =  ProjectTools.getAmplitude(shadow.readEntry((int)sfreq.freq), shadow.max, min_amplitude, 
					     max_amplitude, this.def.amplify_amp);
			    if (this.def.stereo) 
			    	m_bal =  ProjectTools.getBal(shadow.fittest_freq, (int)sfreq.freq, shadow.start, shadow.stop);
			   
		    }
		    
		    debugOut("Resulting amplitude ="+o.amplitude, 5);
		    if (this.def.stereo) o.bal =  ProjectTools.getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		   
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
			ret = ProjectTools.quantisize(ret, def.duration_step_index);
		if (this.logw != null) {
			this.logw.close();
		}
		return ret;
	
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
    
    private void flowControl() {
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
   
	public Vector<String> doProject(Defaults def, String home) {	
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
     * Das Ergebnis ist ein Vector mit den .sco Zeilen für die Töne.
     * @param parm ein Übergabeparameter, kann auch leer sein (vom Textfeld)
     * @return Vector with .sco lines
     */
    @SuppressWarnings({ "unused"})
	public Vector<String> generateSound(String home) {
		Note nt;
		String tmp;
		this.komposition = doKomposition(home);	// Evolution
		Vector<String> result = new Vector<String>();
		
		for (int n = 0; n < this.komposition.size(); n++) {
			result.addElement(ProjectTools.digestNote(this.komposition.elementAt(n), def.fof, def.delay, this::debugOut));
		}
		//this.kompo = kompo;
		return result;
		
    }
   
    
	
}// end of class







