package uCSelf;

import java.util.Vector;

import SOUND.OneNote;

public class CreatePopulation {

	World world;
	Individuum[] notes;
	
	public CreatePopulation(World world) {
		this.world = world;
	}
	
	public void createPopulation(int population, int min, int max) {
		world.population = new Individuum[population];
    	for (int i = 0; i < population; i++) 
    		world.population[i] =  makeRandomFrequency(world, min, max); 
    }
	
	public static Individuum makeRandomFrequency(World world, int min, int max) {
		double f = 0.0;
		double diff = (double) (max - min);
		f = min + (int) (diff  * java.lang.Math.random());
		Individuum nf = new Individuum((int) f, 0, 0.0);
		boolean tonal = false;
		boolean preferLowerNotes = false;
		boolean useSelectNotes = true;
	/*	if (tonal) {
		    if (!preferLowerNotes) { // 
		    	nf = getExactFreq((int) f);
		    }
		    else { // prefer lower notes !, so we choose the next lower freq of our frequency
		    	// 
		    	nf = getChromaticFrequency();
		    }
		}
		else */
		if (useSelectNotes) {
			nf = getExactFreq(world, (int) f);
		}
		return nf;
    }
	
	private static Individuum getExactFreq(World world, int freq) {
		Individuum right;
		double f = (double) freq;
		int n;
		Individuum left = world.notes[0]; // first element
		for (n = 1; n < world.notes.length; n++) {
			// right border:
			right = world.notes[n]; 
			if (right.freq > freq) {
				left = getClosestMatch(f, left, right);
				break;
			}
			left = right; // next left border
		}
		return left; // max maximum possible
	 }
	
	private static Individuum getClosestMatch(double v, Individuum left, Individuum right) {
    	double diff = (right.freq - left.freq) / 2.0; // half distance between left and right
    	double i = v - left.freq;
    	return ( i > diff)?right:left;
    }
	
	public boolean writeNEntrys( int seed, double degression, int footPrint) {
		int center = World.getStep( seed - world.start, world.step);	// the center index for the seed frequenz
		if (center < 0)
			center = 0;
		int size = world.soundscape.length;
		int steps = World.getStep( footPrint, world.step); // how many steps to go left and right
		int index;
		double f;
		//System.out.println("writeNEntrys() this.size="+this.size+" freq="+freq+" center="+center+" steps="+steps); 
		//------------------
		
		double val = world.addWeight[center]; // random Weight
		//debugOut("RandomTable.writeNEntrys(1) table[center]="+table[center]+" addWeight[center]="+addWeight[center], 55);
		//BUG!
		world.soundscape[center].freq += val;	// set center value using freq for weight...
		// Hier kann man nun eine ganze menge beeinflussen:
		// wenn nicht bei 0.0 for den rt-tabellenwert gestoppt wird, kann der
		// sogar negativ werden und somit kommt dieser ton wohl niemals wieder.
		// Man braucht also eine Abbruchbedingung f�r die degression ...
		if (world.soundscape[center].freq < 0) {
			world.soundscape[center].freq = 0;
		    world.addWeight[center] = world.impact;// random Weight
		}
		//------- Now correct the center weight value: -------
		else 
			world.addWeight[center] += degression;
		for (int n = 1; n < steps; n++) {	// apply to neighbours
		    index = center + n;
		    // linear  auch neg ?: 
		    f = (double) (steps - n) * val / (double) steps; // function value, linear curve
		   
		    // Note: I do not use warp around for array index overflows !
		    if (index < size) {
		    	world.soundscape[index].freq += f;
				if (world.soundscape[index].freq < 0) {
					world.soundscape[index].freq = 0;
				    world.addWeight[index] = world.impact;//
				}
		    }
		    // 2nd half
		    index = center - n;
		    //System.out.println("(b) n="+n+" index="+index+" f="+f);
		    if (index >= 0) {
		    	world.soundscape[index].freq += f;
				if (world.soundscape[index].freq < 0) {
					world.soundscape[index].freq = 0;
				    world.addWeight[index] = world.impact;//
				}
		    }
		}
		return true;
    }
	
	
}
