package SOUND;

import java.util.Arrays;
import java.util.Comparator;

import SOUND.models.ProjectModel;
import Utils.Melody;

public class SoundIteration {

	
	/**
	 * How it works:
	 * 1. create population
	 * 2. weight them
	 * 3. select the fittest
	 * 4. transfer it to the soundcape
	 * 5. calculate properties from the fittest individuum
	 * 
	 * @param m
	 * @param it
	 * @param o
	 * @param dela
	 * @param melody
	 * @return
	 */
	public static boolean doIteration(ProjectModel m, int it, LoopObject o, long dela, Melody melody) {
		long time = System.currentTimeMillis();
	    //debugOut("-------> New Iteration step:"+it, 55);
	    m.addLog("-------> New Iteration step: "+it);
	    // Step 1 generate n random Frequencys:
	    m.rt.iteration = it;// needed for the display 
	    if (m.shadow != null)
	    	m.shadow.iteration = it;
	    // Step 1
	    ProjectModel.createPopulation(m.notes, m.rt, m.def.population, 
	    		m.def.min_freq, m.def.max_freq,m.def.tonal, m.def.preferLowerNotes, m.def.useSelectNotes); // create new Population
	    // Step 2
	    // Now weight array for each random frequency:
	    ProjectTools.makeWeight(m.def.population, m.rt, m.def.r_Weight, m::debugOut);// Judge them
	    if (m.shadow != null) {
	    	ProjectModel.createPopulation(m.notes, m.shadow, m.def.population, 
	    			m.def.min_freq, m.def.max_freq, m.def.tonal, m.def.preferLowerNotes, m.def.useSelectNotes); 
	    	 ProjectTools.makeWeight(m.def.population, m.shadow, m.def.r_Weight, m::debugOut);// Judge them
	    }
	    // Step 3:
	    // now sort new population by weight;
	    Arrays.sort(m.rt.freq, Comparator.comparing(p -> p.weight));
	   
	    if (m.shadow != null)
	    	Arrays.sort(m.shadow.freq, Comparator.comparing(p -> p.weight));
	    	//q.sort(shadow.weight, shadow.freq);
	    
	    if (m.Verbosity >= 6 ) {
	    	for (int z = 0; z < m.def.population; z++) 
	    		m.debugOut("Sorted: index="+z+" freq="+m.rt.freq[z]+" weight="+m.rt.freq[z].weight, 6);
	    }
	   
	    //--------------------------------------------------------------------
	    // Next select randomly the fittest (the last ones in the array are the fittest !):
	    int is = (int) ((double) m.def.fittest * java.lang.Math.random());
	    o.is = is;
	    int z = (m.def.population -1 ) - is;
	    m.debugOut("Index that will be selected:"+z, 5);
	    // --------- Nun ist ein Individuum selektiert ! ----------------
	    o.freq = m.rt.freq[z];
	    o.it = it;
	    // --- end of step 3 ---
	    
	    
	    // ------------------State Machine :------------------------------
	    if (is < m.def.range && m.rt.stateCnt == 0) 
	    	o.f_cnt++;
	    else 
	    	o.f_cnt = 0; // reset hit counter
	    o.addLoop(m.rt, m.shadow, o.sFreq);
	    //----------------f_cnt------------------------------
	    //System.out.println("Selected Index ="+is+" f_cnt="+f_cnt);
	    m.addLog("Frequenz selected f="+o.freq);
	    m.debugOut("This is our new selection:"+o.freq+" Hz", 5);
	    //debugOut("This is our new selection:"+freq+" Hz", 55);
	   
	    // Step 4:
	    // Mark the new individual in the RT, now update Tables
	    m.rt.writeNEntrys( o.freq, m.def.degression, m.def.diff_freq ); // footprint
	    
	    m.sil.displayRT(m.rt);
	    m.rt.getMaxx();
	    m.addImageS(m.rt, o.start);
	   // addImage(rt, start);
	    int m_amplitude = 0; // not used?
	    int m_Tension = 0;
	    double m_bal = 0;
	    
	    //-------------------------------------------------------------------
	    //------Selection from the shadow table:--------------------------------------------------------------
	    // Next select randomly the fittest (the last ones in the array are the fittest !):
	    is = (int) ((double) m.def.fittest * java.lang.Math.random());
	    int sz = (m.def.population -1 ) - is;
	   // --------- Nun ist ein Individuum selektiert ! ----------------
	    OneNote sfreq = null;
	    if (m.shadow != null) {
	    	//addLog("Shadow Index that will be selected:"+sz);
		    sfreq = m.shadow.freq[sz];
		    m.addLog("Shadow Index that will be selected:"+sz+" f="+sfreq);
		    m.shadow.writeNEntrys( sfreq, m.def.degression, m.def.diff_freq );
		   // sil.displayRT(rt);
		    m.shadow.getMaxx();
		    m_Tension = (int)  ProjectTools.getKleiner(melody.max, m.rt.max, m.def.min_tempo, m.def.max_tempo);
		    m_amplitude =  ProjectTools.getAmplitude(m.shadow.readEntry((int)sfreq.freq), m.shadow.max, o.min_amplitude, 
				     o.max_amplitude, m.def.amplify_amp);
		    if (m.def.stereo) 
		    	m_bal =  ProjectTools.getBal(m.shadow.fittest_freq, (int)sfreq.freq, m.shadow.start, m.shadow.stop);
	    }
	    
	    m.debugOut("Resulting amplitude ="+o.amplitude, 5);
	    if (m.def.stereo) o.bal =  ProjectTools.getBal(m.rt.fittest_freq, (int)o.freq.freq, m.rt.start, m.rt.stop);
	   
	    //-------------------------------------------------------------------
	    o.addLoopProperties(m.rt, m.shadow, o.sFreq, m.notes, o.generator, o.envelope, m.skompo);
	    //----
	    o.doAkkordeUndStimmen(m.rt,  m.notes, o.generator, o.envelope, m.skompo);
	    //---- StartzeitpunkT nächste note:
	    o.start += o.tempo;
	    //---------
	    if (m.halt) return false;	// stop doing it
	   
	    if (m.def.mode > 0 | true) {
			long diff = System.currentTimeMillis() - time;
			long sleep = dela - diff;
			//System.out.println("Run() delay ="+DELAY+" dela="+dela+" needed:"+diff+" sleep:"+sleep);
			try {
			    if (sleep > 0) {
			    	Thread.sleep(sleep);
			    }
			    dela -= sleep;
			}
			catch (InterruptedException e) {}
	    }
	    return true;
	}
}
