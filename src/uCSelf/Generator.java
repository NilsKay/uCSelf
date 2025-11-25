package uCSelf;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.function.BiConsumer;

import javax.swing.JFrame;

import SOUND.OneNote;
import SOUND.Project2;
import SOUND.ProjectTools;
import SOUND.SoundInfoListener;
import SOUND.Soundscape;
import Utils.QSort;


public class Generator {
	public static final String[] nts = new String[] {
	    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
	};
	
	World world;
	
	public static void debugOut(String s, int i) {
		
	}
	
	public static void test() {
		boolean tonal = true;
		boolean preferLowerNotes = false;
		boolean useSelectNotes = false;
		int seedFrequency = 440; // Hz
		int step = 50;
		int numberOfFittest = 10;
		double weight = 20.0;
		double impact = 15.0; // random weight
		double degression = 1.0;
		int footPrint = 21;
		int numberOfIndividuums = 20;
		int iterations = 5;
		int interval = 10;
		int min = 20;
		int max = 2000;
		Project2 p2 = new Project2(null, null);
		Vector<OneNote> notes = p2.createNoteTable(1, min, max);
		Soundscape rt = new Soundscape(null, 1, 
			     min, max, interval, 0.0, 
			     impact, null);
		ProjectTools.createPopulation(notes, rt, numberOfIndividuums, min, max, tonal, preferLowerNotes, useSelectNotes); // create new Population
		ProjectTools.makeWeight(numberOfIndividuums, rt, impact, Generator::debugOut);// change weight 
		
		//rt.writeNEntrys( freq, this.def.degression, this.def.diff_freq ); // degression
		//----
		World world = new World(min, max, step, impact, tonal, impact);
		world.presetWeight(weight); 
		System.out.println("Soundscape created");
		int diff = 100;
		CreatePopulation pop = new  CreatePopulation(world);
		// plant one Individuum (seed)
		pop.writeNEntrys(seedFrequency, degression, diff);
		world.fittest = seedFrequency;
		for (int n = 0; n < iterations; n++) {
			pop.createPopulation(numberOfIndividuums, world.start, world.stop); // create new Population
		    // Now weight array for each random frequency:
		  // footprint? frequenzen immer 0, fittest?
			world.makeWeight();// Judge them
			Arrays.sort(rt.freq, Comparator.comparing(p -> p.weight));
			//QSort q = new QSort(); // ( index = max equals the highest weight)
		    //q.sort(rt.weight, rt.freq);
		  //--------------------------------------------------------------------
		    // Next select randomly the fittest (the last ones in the array are the fittest !):
		    int is = (int) ((double) numberOfFittest * java.lang.Math.random());
		    int z = (numberOfIndividuums -1 ) - is;
		    //debugOut("Index that will be selected:"+z, 5);
		    // --------- Nun ist ein Individuum selektiert ! ----------------
		    OneNote freq = rt.freq[z];
		    rt.writeNEntrys( freq, degression, footPrint );
		}
		System.out.println("All done");
	}
	
	
	/**
     * Get a random freq. from the note-table. This table starts with our min.-freq
     * and ends with the max.-freq.
     * the indecees are equally distributed, checked 2nd of July 2003 nik
     * @return a random freq.
     */
    private static Individuum getChromaticFrequency(Vector<Individuum> notes) {
		int index = (int) ((double) (notes.size()) * java.lang.Math.random()); // index
		Individuum f = notes.elementAt(index);
		return f;
    }
    
   
	public static void main(String[] argv) {
		test();
	}
}