package SOUND;
import java.text.DecimalFormat;
import java.util.*;

import SOUND.models.ProjectModel;
import Utils.GetEnviroment;
import Utils.Melody;
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
   
    /**
     * Zuerst wird eine Soundscape mit einer Start- und Endfrequenz sowie einer Schrittweite festgelegt. 
In einer Schleife mit n-Iterationen, wobei n die Anzahl der zu erzeugenen Töne ist, wird dies gemacht:
Eine Bevölkerung mit einer Anzahl x Individuen wird zufällig erzeugt
Die Individuen werden mit einem Gewicht für die Fitness bewertet
Das fitteste Indiviuum wird ausgewählt
Es wird in die Soundscape übertragen
Anhand der Eigenschaften des fittesten Indiviuums werden Eigenschaften des Tones errechnet, wie Lautstärke, Platz im Stereofeld, Dauer etc.
Das ist ganz grob, ich beschreibe hier z.B. nicht, dass der Fitteste mit einem gewissen Zufall ausgewählt wird.
     */
	ProjectModel m;
	
    /**
     * Constructor des 1. Projektes:
     * Eine ganze musikalische Note errechnet sich so:
     * (Oktave * 220) * 2 ^ (x/12);
     * x geht von 0-11:
     * 0=A, 1=A#, B=2, C=3, C#=4, D=5, D#=6, E=7, F=8, F#=9, G=10, G#=11
     * Es gibt ca. 12 Oktaven ?
     */
    public Project2(ProjectModel m) {
		// Constructor
		m.debugOut("Project2 : Constructor", 1);
		this.m = m;
    }
   
    @SuppressWarnings({"unused"})
	private Vector<CSoundNote> doKomposition(String home) { // Evolution
    	m.home = home;
    	//boolean doBug = def.doLoopBug;
    	Melody melody = new Melody();
    	int melodyChannel = m.def.voiceMax; // (7) first melody channel (after maxVoices) 
    	//System.out.println("min_dur="+this.def.min_tempo);
    //	this.log = new Vector<String>(); 
    	m.logw = null; 
    	CSoundNote melodyTone = null; // the actual played melody Note
    	String tmp;
    	DecimalFormat f = new DecimalFormat("#.###");
    	CSoundNote nt;
    	long dela = ProjectModel.DELAY;
    	if (m.def.mode == 0) 
    		dela = ProjectModel.DDELAY;
    	int n, z, it, st;
    	
		String kompo = home+File.separator+GetEnviroment.KOMPOSITION;
		
		try {
			m.skompo = new PrintWriter(new FileOutputStream(kompo));
			// Headder Information
			//skompo.println("# Logfile File for JcSelf.");
		}
		catch (java.io.IOException e) {
			m.skompo = null;
		} // End of IO Excep
		LoopObject o = new LoopObject(m.def, m::debugOut, m::addLog);
		o.bal = m.balance;
		o.amplitude = o.max_amplitude;	// actual value
		o.max_amplitude =  ProjectTools.getLoundness(m.def.max_amp, m.def.fof); // Border values
		o.min_amplitude = ProjectTools.getLoundness(m.def.min_amp, m.def.fof);
		o.generator = 0; 	// ((Integer) this.generators.elementAt(0)).intValue();
		m.debugOut("KOMPOSITION: (Evolution)", 1);
		Defaults def = m.def;
		m.addLog("KOMPOSITION: (Evolution)");
		// Angangsbedingung:
		m.addLog("State Machine: range="+def.range+" trigger="+def.trigger);
		if (def.useCascade)
			m.addLog("Cascade amount="+def.cascadeCount+" step up="+def.stepUp+" step down="+def.stepDown);
		if (def.useLoop)
			m.addLog("Loop depth="+def.loopDepth+" repeat="+def.loopRepeat+" permutation="+def.permutation);
		if (def.useSpeed)
			m.addLog("Speed minTempo="+def.minTempo+" maxTemp="+def.maxTempo+" step="+def.speedStep);
		if (def.doMelody)
			m.addLog("Do Melody ");
		//----------- Test:
		//this.rndStatic = new int[this.notes.size()];
		// init seed frequency
		OneNote freq; // start Note
		if (def.seed <= def.min_freq | def.tonal) { // do random or invalid
		    freq = ProjectModel.makeRandomFrequency(m.notes, def.min_freq, def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
		}
		else freq = new OneNote(def.seed, 0, 0);
		
		// init OneNote tables
		m.shadow = new Soundscape(m.qd, m.Verbosity, 
			     def.min_freq, def.max_freq, def.interval, 0.0, 
			     def.impact, m.prs);
		
		/*this.melodyTable = new RandomTable(this.qd, this.Verbosity, 
			     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
			     this.def.impact, this.prs);
		*/
		// Soundscape
		m.rt = new Soundscape(m.qd, m.Verbosity, 
				     def.min_freq, def.max_freq, def.interval, 0.0, 
				     def.impact, m.prs);
		m.rt.seed = (int)freq.freq;
		m.rt.mode = 0; // off
		
		m.addLog("Seed ="+m.rt.seed+" Hz");
		OneNote sFreq = null;
		// -------- Initalise Tables -----------
		if (m.shadow != null) {
			if (def.seed <= def.min_freq | def.tonal) { // do random or invalid
			    sFreq =  ProjectModel.makeRandomFrequency(m.notes, def.min_freq, def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
			}
			else 
				sFreq = new OneNote(def.seed, 0, 0);
			m.shadow.seed = (int)sFreq.freq;
			m.addLog("Shadow Seed ="+m.shadow.seed+" Hz");
		}
		
		// Veraendere die Tabelle
		// freq, um wieviel nach oben, welche Frequenz-differenz zu beachten ist
		m.rt.writeNEntrys(freq, def.degression, def.diff_freq );
		if (m.shadow != null)
			m.shadow.writeNEntrys(sFreq, def.degression, def.diff_freq );
		//-----------------------
		m.debugOut("This is our seed :"+freq+" Hz", 5);
		m.debugOut("################### Start of new Komposition: (Oszi) ###################", 55);
		m.debugOut("min freq="+def.min_freq+" max_freq="+def.max_freq+" population="+def.population+" degression="+def.degression+" weight="+def.r_Weight, 55);
		m.addLog("min freq="+def.min_freq+" max_freq="+def.max_freq+" population="+def.population+" degression="+def.degression+" weight="+def.r_Weight);
		flowControl();
		
		
		// Now iterate and create the komposition
		for (it = 0; it < m.def.iterations; it++) {
			if (!SoundIteration.doIteration(m, it, o, dela, melody))
				break;
			flowControl();
		} // end of iterations loop
		// flush
		if (m.cIndex > 0 & false) {
			m.rt.writeTable(m.dos, m.icache, m.cIndex);
		}
		if (m.cache != null && m.cache.size() > 0) {
			for (int n1 = 0; n1 < m.cache.size(); n1++)
				m.imageIt.println(m.cache.elementAt(n1));
			m.cache = new Vector<String>();
		}
		/*if (def.doMelody && melodyTone != null & false) {
			melodyTone.tempo = 0; //start - melodyTone.start;
			melodyTone.dauer = start - melodyTone.start;
			addToKomposition(melodyTone);
		}
		*/
		if (m.skompo != null)
			m.skompo.close();
		// Komposition is now saved.
		if (m.imageIt != null)
			m.imageIt.close();
		
		if (m.dos != null) {
			try {
				m.dos.close();
			}catch (Exception ex) {}
		}
		Vector<CSoundNote> ret = sortKomposition(kompo);
		if (def.doMelody) {
			MelodyObject mo = new MelodyObject(m.qd, def, m::debugOut, m::addLog, o.min_amplitude, o.max_amplitude);
			ret = mo.addMelody(m.notes, ret, melodyChannel, f, m.Verbosity, m.prs);
			saveOverview(ret);
			Collections.sort(ret, new Comparator<CSoundNote>() {
				public int compare(CSoundNote o1, CSoundNote o2){
					return o1.compareTo(o2);
				}
			});
		}
		if (def.duration_step_index > 0) 
			ret = ProjectTools.quantisize(ret, def.duration_step_index);
		if (m.logw != null) {
			m.logw.close();
		}
		return ret;
	
    }
    
    @SuppressWarnings("unused")
	private void printDebug(Vector<CSoundNote> v) {
    	System.out.println("debug---------------");
    	Vector<String> vl = new Vector<String>();
    	for (int n = 0; n < v.size(); n++) { 
    		CSoundNote nt = v.elementAt(n);
    		String l = nt.saveAsString();
    		System.out.println(l);
    		vl.addElement(l);
    	}
    	String path = m.home+File.separator+"test.sav";
    	Utils t = new Utils();
    	t.save(path, "ASCII", vl);
    	
    }
    
    @SuppressWarnings({ "unused", "unchecked" })
	private Vector<CSoundNote> getDebug() {
    	Vector<CSoundNote> v = new Vector<CSoundNote>();
    	
    	String path = m.home+File.separator+"test.sav";
    	Utils t = new Utils();
    	Vector<String> li = t.readTextFile(path, false);
    	for (int n = 0; n < li.size(); n++)
    		v.addElement(CSoundNote.getFromString(li.elementAt(n)));
    	return v;
    }
    
    private void saveOverview(Vector<CSoundNote> v) {
    	System.out.println("Melody Verify-----------------------------------------");
    	Vector<String> vl = new Vector<String>();
    	for (int n = 0; n < v.size(); n++) { 
    		CSoundNote nt = v.elementAt(n);
    		String l = getOneLine(nt);
    		System.out.println(l);
    		vl.addElement(l);
    	}
    	String path = m.home+File.separator+"kompositionLog.txt";
    	Utils t = new Utils();
    	t.save(path, "ASCII", vl);
    }
    
    private String getOneLine(CSoundNote nt) {
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
    		Vector<CSoundNote> iv = interval.elementAt(n);
    		for (int q = 0; q < iv.size(); q++) {
    			CSoundNote nt = iv.elementAt(q);
    			System.out.println(nt.start+" "+nt.note);
    		}
    	}
    }
    
    
    private Vector<CSoundNote> sortKomposition(String kompo) {
    	Vector<CSoundNote> res = null;
    	try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(kompo)));
			BufferedReader br = new BufferedReader(isr);
			res = new Vector<CSoundNote>();
			String res0;
		    while((res0 = br.readLine()) != null) {
		    	CSoundNote nt = CSoundNote.getFromString(res0);
		    	res.addElement(nt);
		    }
		    br.close();	// close the Buffered Reader
    	} catch (java.io.IOException e) {
    		System.out.println("CEditor.playKomposition(): catched "+e);
    		return null;
    	}
    	// Now we have the komposition as Vector
    	Collections.sort(res, new Comparator<CSoundNote>() {
 		   public int compare(CSoundNote o1, CSoundNote o2){
 			  return o1.compareTo(o2);
 		   }
 		});
    	return res;
    }
    
    private void flowControl() {
		if (m.qd != null) {
		    if (m.qd.halt ) {
			while(m.qd.halt) {
			    try {
			    	java.lang.Thread.sleep(100); // 
			    }
			    catch (InterruptedException e){}
			}
			if (m.qd.stepp) m.qd.halt = true;
		    }
		    else if (m.qd.langsam) {
				try {
				    java.lang.Thread.sleep(200); // 
				}
				catch (InterruptedException e){}
		    }
		}
    }

    public int getChromaticFromSeed(int seed) {
		int v,n;
		for (n = 0; n < m.notes.size(); n++) {
		    v = (int) m.notes.elementAt(n).freq;
		    if (v > seed) return v;
		}
		v = (int) m.notes.elementAt(n).freq;
		// Not in Table
		return v; // max maximum possible
    }
   
	public Vector<String> doProject(Defaults def, String home) {	
		m.def = def;
		m.balance = 0.5;	// symetrical default
		
		if (def.tonal) m.notes =  ProjectModel.createNoteTable(m.note_mode, def.min_freq, def.max_freq, m.anfOkt);
		
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
			m.notes = sel;
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
		CSoundNote nt;
		String tmp;
		m.komposition = doKomposition(home);	// Evolution
		Vector<String> result = new Vector<String>();
		
		for (int n = 0; n < m.komposition.size(); n++) {
			result.addElement(ProjectTools.digestNote(m.komposition.elementAt(n), m.def.fof, m.def.delay, m::debugOut));
		}
		//this.kompo = kompo;
		return result;
		
    }
   
    
	
}// end of class







