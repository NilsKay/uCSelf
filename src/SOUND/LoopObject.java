package SOUND;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import Utils.Converter;

public class LoopObject {
	OneNote freq;
	Vector<Note> loop;
	double start;
	int f_cnt; 
	Vector<Note> loopCopy;
	int loopCounter;
	boolean requestToInitLoopCopy; 
	int transpositionNote;
	int it;
	Soundscape rt;
	Defaults def;
	int amplitude;
	int max_amplitude; // Border values
	int min_amplitude;
	
	BiConsumer<String, Integer> debugOut;
	Consumer<String> addLog;
	
	public LoopObject(Soundscape rt, Defaults def, BiConsumer<String, Integer> debugOut, Consumer<String> addLog) {
		this.debugOut = debugOut;
		this.addLog = addLog;
		this.rt = rt;
		this.def = def;
		this.freq = null;
		this.loop = new Vector<Note>();
		this.start = 0;
		this.f_cnt = 0; 
		this.loopCopy = new Vector<Note>();
		this.loopCounter = 0;
		this.requestToInitLoopCopy = false; 
		this.transpositionNote = 0;
		this.it = 0;
	}
	
	public void addLoop() {
   	 boolean addToLoop = false;
   	 if (rt.stateCnt <= 0) {
	    	// update the loop
	    	Note tn = new Note(this.start, 0, 0, 0, 0, 0, 0, 0, false);
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
	
	public void addLoopProperties() {
		 // --------- Jetzt noch ein paar Properites dieser Frequenz : -----------
	    if ( == null) { // not loop, not permutation, add a new modified note
	    	//addLog("Normal note, not loop");
	    	amplitude = Project2.getAmplitude(rt.readEntry((int)freq.freq), rt.max, min_amplitude, 
					     max_amplitude, this.def.amplify_amp);	
		    // amplitude = loud[0];
		    debugOut("Resulting amplitude ="+amplitude, 5);
		    if (this.def.stereo) bal = Project2.getBal(rt.fittest_freq, (int)freq.freq, rt.start, rt.stop);
		    debugOut("Resulting balance: bal="+bal, 5);
		    //addLog("Resulting amplitude ="+amplitude+" balance: bal="+bal);
		    
		    double val = rt.readEntry((int)freq.freq);
		    
		    //------ tempo : ---------------
		    dauer = Project2.getKleiner(val, rt.max, this.def.min_tempo, this.def.max_tempo);	
		    if (dauer < this.def.min_tempo || dauer > this.def.max_tempo) {
		    	debugOut("Project2: tempo out of range ="+dauer, 4);
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
		    	double sval = shadow.readEntry((int)sfreq.freq);
		    	pDauer = getKleiner(sval, shadow.max, this.def.min_tempo, this.def.max_tempo);
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
		    boolean transOK = true;
		    if (def.useTrans) {
		    	// modify the selected note by transposition effect:
		    	//transpositionNote
		    	transOK = false;
		    	int om = nt.note.midiIndex;
		    	OneNote tn = doTransposition(nt.note, o.transpositionNote);
		    	if (tn != null) {
		    		try {
		    			nt.note = tn.clone();
		    			transOK = true;
		    			
		    		} catch (Exception ex) {}
		    	}
		    	if (transOK) 
		    		System.out.println("Transposition: diff="+o.transpositionNote+" change midiIndex "+om+" to "+nt.note.midiIndex);
		    	else 
		    		System.out.println("Transposition: Note ignored. Reason: Outside freq.- bounds!");
		    }
		    nt.pDauer = freq.pedal?pDauer:0;
		    
		    // ------- Nun pr�fe, ob noch Stimmen dazukommen ? --------------------------
		    //stimmen = 2; // nur f�r Test
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
		    if (o.addToLoop) {
		    	// update the loop element :
		    	try {
			    	int last = o.loop.size() -1;
			    	o.loop.setElementAt(nt.clone(), last);
			    	//addLog("Update last loop element size="+loop.size());
			    	if (o.requestToInitLoopCopy ) {
			    		o.loopCopy = new Vector<Note>();
			    		o.loopCopy.addAll(o.loop);
			    		o.requestToInitLoopCopy = false;
			    	}
		    	} catch (Exception ex){}
		    }
		    if (transOK)
		    	addToKomposition(nt);
		    dauer = nt.dauer;
	    }
	    else {
	    	try {
	    		if (!doBug) {
	    			Note ln = loopNote.clone();
	    			ln.start = start;
	    			addToKomposition(ln);
	    		}
	    		else 
	    			addToKomposition(loopNote);
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
}
