package SOUND;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import SOUND.models.ProjectModel;
import Utils.Akkorde;
import Utils.Converter;

public class LoopObject {
	private DecimalFormat f = new DecimalFormat("#.###");
	public OneNote freq;
	private Vector<CSoundNote> loop;
	public double start;
	public int f_cnt; 
	private Vector<CSoundNote> loopCopy;
	private int loopCounter;
	private boolean requestToInitLoopCopy; 
	public int transpositionNote;
	public int it;
	private Defaults def;
	public int amplitude;
	public int max_amplitude; // Border values
	public int min_amplitude;
	private CSoundNote loopNote  = null; 
	double bal;
	double dauer = 0;	// Tondauer
	double tempo = 0, pDauer = 0;
	public OneNote ndVoice = null;
	private boolean speedDir = true; // up
	private int speedModifier = 60; // neutral !
	public int is = 0;
	public double frq;
	private BiConsumer<String, Integer> debugOut;
	private Consumer<String> addLog;
	public int stimmen = 0;
	private Akkorde akk;
	private boolean addToLoop = false;
	
	
	public LoopObject(Defaults def, BiConsumer<String, Integer> debugOut, Consumer<String> addLog) {
		this.akk = new Akkorde();
		this.debugOut = debugOut;
		this.addLog = addLog;
		this.def = def;
		this.freq = null;
		this.loop = new Vector<CSoundNote>();
		this.start = 0;
		this.f_cnt = 0; 
		this.loopCopy = new Vector<CSoundNote>();
		this.loopCounter = 0;
		this.requestToInitLoopCopy = false; 
		this.transpositionNote = 0;
		this.it = 0;
	}
	
	public void addLoop(Soundscape rt, Soundscape shadow, OneNote sFreq) {
		
		this.addToLoop = false;
		if (rt.stateCnt <= 0) {
	    	// update the loop
	    	CSoundNote tn = new CSoundNote(this.start, 0, 0, 0, 0, 0, 0, 0, false);
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
	    
		if (f_cnt >= def.trigger) { // Trigger the state machine, execute it at the next interval
	    	try{
	    		// Auswahl der State machine: INIT 
	    		Vector<Integer> ssel = new Vector<Integer>();
	    		
	    		if (def.useCascade )
	    			ssel.addElement(1);
	    		if (def.useLoop )
	    			ssel.addElement(2);
	    		if (def.useTrans )
	    			ssel.addElement(3);
	    		if (ssel.size() <= 0)
	    			rt.mode = 0;
	    		else { // select one of the effects randomly
	    			int sIndex = (int) (Math.random() * ssel.size()); 
	    			rt.mode = (ssel.elementAt(sIndex)).intValue();
	    			System.out.println("Trigger State machine! selected mode="+rt.mode);
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
	 	    	else if (rt.mode == 3) {
	 	    		// transposition
	 	    		int area = Math.abs(def.transP_min) + def.transP_max;
	 	    		int tsel= (int) (Math.random() * area + 0.5); 
	 	    		int diff = tsel + def.transP_min;
	 	    		System.out.println("-> Trigger Trans-diff="+diff);
	 	    		transpositionNote += diff;
	 	    	}
	    	} catch (Exception ex) {}
 	    	f_cnt = 0;
 	    	if (rt.mode == 1)
 	    		addLog.accept("#### Trigger Cascade count="+def.cascadeCount+" direction="+(rt.up?"up":"down"));
 	    	else if (rt.mode == 2)
 	    		addLog.accept("#### Trigger Loop effect depth="+loop.size()+" repeat="+loopCounter);
 	    	else if (rt.mode == 2)
 	    		addLog.accept("#### Trigger Transposition effect trans-index="+transpositionNote);
	    }
	    else if (rt.stateCnt > 0) { // State Machine active !
	    	if (rt.mode == 1) { //cascade
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
	    			CSoundNote tn = loopCopy.elementAt(0);
	    			freq = tn.note.clone(); // use the oldest element,
	    			if (!def.permutation)
	    				loopNote = tn.clone(); // use saved Value
	    			loopCopy.removeElementAt(0); // remove oldest element
	    			System.out.println("it="+it+" Insert loop note: "+freq+" loop size left:"+loopCopy.size());
	    			if (loopCopy.size() <= 0) {
	    				loopCounter--;
	    				if (loopCounter > 0) { // still busy, init again
	    					rt.stateCnt = loop.size();
	    	 	    		loopCopy = new Vector<CSoundNote>();
	    	 	    		loopCopy.addAll(loop);
	    	 	    		addLog.accept("# Repeat loop nr="+loopCounter);
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
	}
	
	public void addLoopProperties(Soundscape rt, Soundscape shadow, 
			OneNote sFreq, Vector<OneNote> notes, int generator, int envelope, PrintWriter skompo) {
		
		 // --------- Jetzt noch ein paar Properites dieser Frequenz : -----------
	    if (loopNote == null) { // not loop, not permutation, add a new modified note
	    	//addLog("Normal note, not loop");
	    	amplitude = ProjectTools.getAmplitude(rt.readEntry((int)freq.freq), rt.max, min_amplitude, 
					     max_amplitude, this.def.amplify_amp);	
		    // amplitude = loud[0];
		    debugOut.accept("Resulting amplitude ="+amplitude, 5);
		    if (this.def.stereo) bal = ProjectTools.getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		    debugOut.accept("Resulting balance: bal="+bal, 5);
		    //addLog("Resulting amplitude ="+amplitude+" balance: bal="+bal);
		    
		    double val = rt.readEntry((int)freq.freq);
		    
		    //------ tempo : ---------------
		    dauer = ProjectTools.getKleiner(val, rt.max, this.def.min_tempo, this.def.max_tempo);	
		    if (dauer < this.def.min_tempo || dauer > this.def.max_tempo) {
		    	debugOut.accept("Project2: tempo out of range ="+dauer, 4);
		    }
		    /*
		    if (this.def.duration_step_index > 0) {
		    	dauer = getDurationFromTable(dauer);
		    }
		    */
		    tempo = dauer;
		    if (tempo < this.def.min_tempo)
		    	tempo = this.def.min_tempo;
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
		    
		    addLog.accept("Resulting amplitude="+amplitude+" bal="+f.format(bal)+" tempo="+f.format(tempo)+" dauer="+f.format(dauer));
		    if (def.useSpeed)
		    	addLog.accept("speedModifier="+speedModifier+" direction="+speedDir);
		    //-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-##-##-#-
		    pDauer = dauer;
		    boolean pedal = shadow != null && def.pedal;
		    if (pedal) {
		    	//freq.freq = 44.0;
		    	//freq.getMidiNote();
		    	double sval = shadow.readEntry((int)sFreq.freq);
		    	pDauer = ProjectTools.getKleiner(sval, shadow.max, this.def.min_tempo, this.def.max_tempo);
		    	//System.out.println("dauer="+dauer+" pedal dauer="+pDauer);
		    	//pDauer = 3.0; // test !
		    	addLog.accept("Shadow: pDauer="+f.format(pDauer));
		    	freq.pedal = true;
		    }
		    //---------------------------
		    //System.out.println("(2)At iteration nr.:"+it+" dauer="+dauer);
		    frq = (double) freq.freq;
		    
		    if (this.def.tonal) {
		    	// get freq as double !
		    	frq = ProjectModel.getExactFreq((int)freq.freq, notes).freq;
		    }
		    //else System.out.println("Free: frq="+frq);
		    //---- erzeuge eine neue Note mit den oben ermittelten Werten.--------------
		    if (tempo <= 0) {
		    	System.out.println("Dauer (a)="+dauer);
		    	Converter.doBreak();
		    }
		    // Create the Note : 
		    CSoundNote nt = new CSoundNote(start, tempo, dauer, bal, frq, generator, amplitude, envelope, false);
		    nt.note = freq;
		    boolean transOK = true;
		    if (def.useTrans) {
		    	// modify the selected note by transposition effect:
		    	//transpositionNote
		    	transOK = false;
		    	int om = nt.note.midiIndex;
		    	OneNote tn = doTransposition(nt.note, transpositionNote, def.min_freq, def.max_freq);
		    	if (tn != null) {
		    		try {
		    			nt.note = tn.clone();
		    			transOK = true;
		    			
		    		} catch (Exception ex) {}
		    	}
		    	if (transOK) 
		    		System.out.println("Transposition: diff="+transpositionNote+" change midiIndex "+om+" to "+nt.note.midiIndex);
		    	else 
		    		System.out.println("Transposition: Note ignored. Reason: Outside freq.- bounds!");
		    }
		    nt.pDauer = freq.pedal?pDauer:0;
		    
		    // ------- Nun prüfe, ob noch Stimmen dazukommen ? --------------------------
		    //stimmen = 2; // nur für Test
		    boolean doOct = this.def.akkord;
		    
		    stimmen = getVoices(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		    

		    if (doOct) {
		    	double ten = ProjectTools.getKleiner(val, rt.max, 0.0, 23.0);	
		    	int tens = (int) (ten + 0.5);
		    	if (tens >= 24)
		    		tens = 23;
		    	ndVoice = this.akk.get2ndVoice(ProjectTools.getNoteFromString(freq.note), freq.Octave, tens);
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
			    		loopCopy = new Vector<CSoundNote>();
			    		loopCopy.addAll(loop);
			    		requestToInitLoopCopy = false;
			    	}
		    	} catch (Exception ex){}
		    }
		    if (transOK)
		    	ProjectTools.addToKomposition(nt, skompo);
		    dauer = nt.dauer;
	    }
	    else {
	    	try {
	    		if (!def.doLoopBug) {
	    			CSoundNote ln = loopNote.clone();
	    			ln.start = start;
	    			ProjectTools.addToKomposition(ln, skompo);
	    		}
	    		else 
	    			ProjectTools.addToKomposition(loopNote, skompo);
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
		    	addLog.accept("Use Loop old Index="+loopNote.misc+" Resulting amplitude="+amplitude+" bal="+f.format(bal)+" tempo="+f.format(tempo)+" dauer="+f.format(dauer));
		    	
		    	/*if (def.useSpeed)
			    	addLog("speedModifier="+speedModifier+" direction="+speedDir);
			    	*/
	    	} catch (Exception ex){}
	    }
	}
	
	public void doAkkordeUndStimmen(Soundscape rt,  Vector<OneNote> notes, int generator, int envelope, PrintWriter skompo) {
		 if( ndVoice != null) {
		    	// Akkorde !
		    	CSoundNote nta = new CSoundNote(start, tempo, dauer, bal, ndVoice.freq, generator, amplitude, envelope, true);
		    	nta.channel = 0; 	// first instrument
		    	nta.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
		    	nta.pDauer = freq.pedal?pDauer:0;
		    	try {
		    		nta.note = ndVoice.clone();
		    		addLog.accept("Akkord active: "+nta.note);
				 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
		    		//komposition.addElement(nta);
		    		ProjectTools.addToKomposition(nta, skompo);
		    	} catch(Exception ex) {
		    		ex.printStackTrace();
		    	}
		    }
		    debugOut.accept("Dies wird "+(stimmen)+" stimmig.", 3);
		    List<Integer> sti = new ArrayList<>();
		    sti.add(new Integer((int)freq.freq));
		    // Now see if there are more than 1 voice(s)
		    for (int st = 1; st < stimmen; st++) { // noch eine Stimme dazu:
		    	int z = (this.def.population -1 ) - 
		    			(int) ((double) this.def.fittest * java.lang.Math.random());
		    	//debugOut("Index that will be selected:"+z, 5);
		    	// --------- Nun ist ein Individuum selektiert ! ----------------
		    	freq = rt.freq[z];
				 //freq = this.def.max_freq; // test
				 //debugOut("This is our new selection:"+freq+" Hz", 5);
				 // Mark the new individual in the RT
				 //rt.writeNEntrys( freq, this.impact, this.def.diff_freq );
				 //sil.displayRT(rt);
				 if (ProjectTools.checkDouble((int)freq.freq, sti)) amplitude = 0; // is double
				 else { 
				     amplitude = ProjectTools.getAmplitude(rt.readEntry((int)freq.freq), rt.max, min_amplitude, 
							      max_amplitude, this.def.amplify_amp);	
				     sti.add(new Integer((int)freq.freq));
				 }
				 //debugOut("Resulting amplitude ="+amplitude, 5);
				 if (this.def.stereo) bal = ProjectTools.getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
				 //debugOut("Resulting balance: bal="+bal, 5);
				 frq = (double) freq.freq;
				 if (this.def.tonal) {
				     // get freq as double !
				     frq = ProjectModel.getExactFreq((int)freq.freq, notes).freq;
				 }
				 if (tempo <= 0) {
				     System.out.println("tempo (b)="+tempo);
				     Converter.doBreak();
				 }
				 try {
					 CSoundNote ntn = new CSoundNote(start, tempo, dauer, bal, frq, generator, amplitude, envelope, false);
					 ntn.voices = stimmen;	// Anzahl der Stimmen since 1.7a1 for FOF
					 ntn.channel = st;
					 ntn.note = freq.clone();
					 ntn.note.pedal = false;
					 boolean transOK = false;
					 if (def.useTrans) {
					    	// modify the selected note by transposition effect:
					    	//transpositionNote
					    	transOK = false;
					    	int om = ntn.note.midiIndex;
					    	OneNote tn = LoopObject.doTransposition(ntn.note, transpositionNote, def.min_freq, def.max_freq);
					    	if (tn != null) {
					    		try {
					    			ntn.note = tn.clone();
					    			transOK = true;
					    			
					    		} catch (Exception ex) {}
					    	}
					    	if (transOK) 
					    		System.out.println("Voice "+st+" Transposition: diff="+transpositionNote+" change midiIndex "+om+" to "+ntn.note.midiIndex);
					    	else 
					    		System.out.println("Voice "+st+" Transposition: Note ignored. Reason: Outside freq.- bounds!");
					    }
					 // ------- addiere diesen neuen Noteneintrag zur Komposition -----------
					 if (amplitude != 0 && transOK) {
						 addLog.accept((st+1)+". Stimme : dauer="+f.format(tempo)+" "+ntn.note);
						 ProjectTools.addToKomposition(ntn, skompo);
						 //komposition.addElement(ntn);
					 }
				 } catch (Exception ex) {
					 ex.printStackTrace();
				 }
		    }
	}
	 /**
     * Find out, how many voices this should have
     * Je fitter, desto 
     * @param all freq
     */ 
    public int getVoices(int fittest, int freq, int start, int stop) {
		double diff = java.lang.Math.abs(fittest-freq); // Abstand vom Fittesten [freq]
		debugOut.accept("getVoices(): fittest="+fittest+" freq="+freq+" diff="+diff, 3);
		double max = (double) (stop -start); // Wertebereich [freq]
		double anzStimmen = (double) (this.def.max_voice - this.def.min_voice);	// [Stimmen]
		// je grï¿½ï¿½er diff, desto mehr stimmen (weiter weg)
		double v = anzStimmen * java.lang.Math.pow( diff / max, this.def.gamma);
		// je kleiner diff, desto mehr stimmen (nï¿½her dran)
		double v1 = anzStimmen * java.lang.Math.pow( (max - diff) / max, this.def.gamma);
		//double v = diff * anzStimmen / max; // je weiter weg, desto mehr Stimmen
		//double v1 = (max - diff) * anzStimmen / max; // je nï¿½her, desto mehr Stimmen
		int val = this.def.min_voice + java.lang.Math.abs((int) (v - v1));
		//System.out.println("getVoices(): diff="+diff+" anzStimmen="+anzStimmen);
		debugOut.accept("v="+v+" v1="+v1+" val="+val, 3);
		return val;
    }
    
    public static OneNote doTransposition(OneNote nt, int transpositionNote, int min_freq, int max_freq) {
    	
    	if (transpositionNote != 0) {
    		OneNote no = null;
    		try  {
    			no = nt.clone();
    		} catch (Exception ex) {
    			no = null;
    		}
    		if (no != null) {
    			int old = nt.midiIndex;
    			int n = old + transpositionNote;
    			no.setMidiNr(n);
    			if (no.freq >= min_freq && no.freq <= max_freq) {
    				try {
    					nt = no.clone();
    				} catch (Exception ex) {
    	    			no = null;
    	    		}
    			}
    			else
    				nt = null; // outside bounds, so ignore this note!
    		}
    	}
    	return nt;
    }
}
