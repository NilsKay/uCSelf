package uCSelf;

import java.util.Vector;

import javax.swing.JFrame;

import SOUND.OneNote;
import SOUND.Project2;
import SOUND.SoundInfoListener;
import SOUND.Soundscape;


public class Generator {
	public static final String[] nts = new String[] {
	    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
	};
	
	World world;
	
	public static void test() {
		boolean tonal = true;
		boolean preferLowerNotes = false;
		boolean useSelectNotes = false;
		int seedFrequency = 440; // Hz
		int step = 50;
		double weight = 20.0;
		double impact = 15.0;
		double degression = 15.0;
		double randomWeight = 21.0;
		int numberOfIndividuums = 20;
		int iterations = 5;
		int interval = 10;
		int min = 20;
		int max = 2000;
		Project2 p2 = new Project2(null, null);
		p2.createNoteTable(1, min, max);
		Soundscape rt = new Soundscape(null, 1, 
			     min, max, interval, 0.0, 
			     impact, null);
		p2.createPopulation(rt, numberOfIndividuums, min, max, tonal, preferLowerNotes, useSelectNotes); // create new Population
		p2.makeWeight(numberOfIndividuums, rt, randomWeight);// change weight 
		
		//rt.writeNEntrys( freq, this.def.degression, this.def.diff_freq ); // degression
		//----
		World world = new World(min, max, step, impact, tonal, randomWeight);
		world.presetWeight(weight); 
		System.out.println("Soundscape created");
		int diff = 100;
		CreatePopulation pop = new  CreatePopulation(world);
		// plant one Individuum (seed)
		pop.writeNEntrys(seedFrequency, degression, diff);
		
		for (int n = 0; n < iterations; n++) {
			pop.createPopulation(numberOfIndividuums, world.start, world.stop); // create new Population
		    // Now weight array for each random frequency:
		  // footprint? frequenzen immer 0, fittest?
			world.makeWeight();// Judge them
		    
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