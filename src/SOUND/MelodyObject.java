package SOUND;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import Utils.Melody;
import Utils.QSort;

public class MelodyObject {

	private BiConsumer<String, Integer> debugOut;
	private Consumer<String> addLog;
	private Defaults def;
	private int min_amplitude;
	private int max_amplitude;
	private CDebug qd;
	
	public MelodyObject(CDebug qd, Defaults def, BiConsumer<String, Integer> debugOut, Consumer<String> addLog,
			int min_amplitude, int max_amplitude) {
		this.debugOut = debugOut;
		this.addLog = addLog;
		this.def = def;
		this.min_amplitude = min_amplitude;
		this.max_amplitude = max_amplitude;
		this.qd = qd;
	}
	/**
     * Examine the Main Komposition and add a Melody to it
     * @param in
     * @param channel
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector<Note> addMelody( Vector<OneNote> notes, Vector<Note> in, int channel, DecimalFormat f, int Verbosity, PrintWriter prs) {
    	addLog.accept("#-#-#-#-#-#-#- MELODY Iteration #-#-#-#-#-#-#");
    	/*
    	for (int n = 0; n < in.size(); n++) {
    		Note nt = in.elementAt(n);
    		System.out.println(nt.start+" "+nt.note);
    	}*/
    	int generator = 0;
    	int envelope = 0;
    	Melody melody = new Melody();
    	//int melodyChannel = def.voiceMax; // (7) first melody channel (after maxVoices) 
    	Note melodyTone = null;
    	Soundscape shadow = new Soundscape(this.qd, Verbosity, 
   		     this.def.min_freq, this.def.max_freq, this.def.interval, 0.0, 
   		     this.def.impact, prs);
    	OneNote sFreq = null;
    	if (this.def.seed <= this.def.min_freq | this.def.tonal) { // do random or invalid
    		sFreq = Project2.makeRandomFrequency(notes, this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes);
    	}
    	else 
    		sFreq = new OneNote(this.def.seed, 0, 0);
    	shadow.seed = (int)sFreq.freq;
    	shadow.writeNEntrys(sFreq, this.def.degression, this.def.diff_freq );
    	double start = 0;
    	//-------------
    	Vector<Vector> interval = Project2.getInterval(in, false, def.duration_step_index); // A vector of vectors for each interval 
    	//printInvteral(interval);
    	Vector<Note> all = null; // all notes of one intervall!
    	int it = 0;
    	for (it = 0; it < interval.size(); it++) { // loop over all intervals
    		all = interval.elementAt(it);
    		Note first = all.firstElement();
    		start = first.start;
    		shadow.iteration = it;
    		Project2.createPopulation(notes, shadow, this.def.population, 
        			this.def.min_freq, this.def.max_freq, def.tonal, def.preferLowerNotes, def.useSelectNotes); 
    		Project2.makeWeight(this.def.population, shadow, def.r_Weight, debugOut);// Judge them
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
        	double dte = Project2.getKleiner(val, shadow.max, 0.0, melody.max);	
        	int m_Tension = (int) (dte + 0.5);
        	//int m_Tension = (int) (dt * melody.max / dbereich +0.5);
        	//int m_Tension = (int) (1.5 * dt * melody.max / dbereich +0.5);
        	if ( m_Tension > melody.max )
        		m_Tension = (int) melody.max;
        	
        	//System.out.println("m_tension="+m_Tension);
        	int m_amplitude = Project2.getAmplitude(shadow.readEntry((int)sfreq.freq), shadow.max, min_amplitude, 
        			max_amplitude, this.def.amplify_amp);
        	if (this.def.stereo) 
        		m_bal = Project2.getBal(shadow.fittest_freq, (int)sfreq.freq, shadow.start, shadow.stop);
 
        	
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
			    	double ly =  Project2.getKleiner(val, shadow.max, 0.0, this.def.max_tempo) * 1000.0;
			    	melodyTone.delay = 0.0;
			    	if (ly > 0 && this.def.doDelay)
			    		melodyTone.delay = ly;
			    	//melodyTone.delay = 500; Test
			    	melodyTone.note = next.clone();
			    	melodyTone.parent = first.note.clone(); 
			    	addLog.accept("#### Trigger Melody: note from "+first.note.noteIndex+","+first.note.note+";"+first.note.Octave+" tension="+m_Tension+" --> "+melodyTone.note.note+";"+melodyTone.note.Octave);
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
}
