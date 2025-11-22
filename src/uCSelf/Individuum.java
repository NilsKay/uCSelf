package uCSelf;

public class Individuum {
	int freq;
	double fitness;
	int index;
	MidiObject midi;
	
	public Individuum(int freq, int a, double w) {
		this.freq = freq;
		this.fitness = w;
		this.index = a;
	}
}
