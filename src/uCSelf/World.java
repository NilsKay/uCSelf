package uCSelf;

import SOUND.OneNote;
import SOUND.Soundscape;

public class World {
	public  Individuum[] soundscape;
	public  double addWeight[];
	public  double weight[];
	public  Individuum[] population;	
	public  Individuum[] notes;
	public  int start;
	public  int stop; 
	public  int step;
	public  double impact;
	double randomWeight;
	public int fittest; 
	
	public World(int min, int max, int step, double impact, boolean tonal, double randomWeight) {
		this.start = min;
		this.stop = max;
		this.step = step;
		this.impact = impact;
		this.randomWeight = randomWeight;
		if (tonal)
			this.notes = MidiNotes.createNoteTable(min, max);
		
		int size = getStep( (max - min), step);  // Size of the table !
		this.soundscape = new Individuum[size];
		// init with default
		for (int n = 0; n < soundscape.length; n++) 
			soundscape[n] = new Individuum(0, 0, 0);
		this.addWeight = new double[size];
	}	
	
	public static int getStep( int freq, int step) {
    	return (int) ((double) freq / (double) step);
    }
	
	public void presetWeight(double weight) {
		for (int n = 0; n < addWeight.length; n++) {
			this.addWeight[n] = weight;
		}
	}
	
	public void makeWeight() {
		int i;
		double t;
		this.weight = new double[population.length];
		// first get the weight from the RandomTable:
		for (i = 0; i < population.length; i++) {
		    this.weight[i] = t = readEntry((int)population[i].freq); // read individuum from soundscape
		    // randomize this weight:
		    // 1. : this.weight[i] = this.weight[i] * r_Weight * java.lang.Math.random();
		    this.weight[i] -= randomWeight * java.lang.Math.random();
		   // debugOut("makeWeight() weight for frequenz ="+rt.freq[i]+" Hz ="+t+" randomized="+rt.weight[i], 6);
		    //debugOut("makeWeight() weight for frequenz ="+this.freq[i]+" Hz ="+t+" randomized="+this.weight[i]+" index="+i, 55);
		}
    }
	
	/**
     * Read a value from the table
     * @param freq the frequency to address the table
     * @return the value , -1.0 if error
     */
    public double readEntry(int freq) {
		int index;
		double val = -1;
		try {
		    index = getStep( freq - this.start, this.step);
		    //System.out.println("maxindex="+this.table.length+" freq="+freq+" index="+index);
		   if (index >= 0)
			   val = this.soundscape[index].freq;
		    //System.out.println("val="+val);
		} catch (ArrayIndexOutOfBoundsException e) {
		    e.printStackTrace();
		    return -1.0;
		}
		return val;
    }
	
	
}

