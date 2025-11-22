package SOUND;

public class OneNote {
	
	double freq;
	String note;
	int Octave;
	
	public OneNote(double f, String note, int oct) {
		this.freq = f;
		this.note = note;
		this.Octave = oct;
	}
	public boolean equals(OneNote n) {
		return this.freq == n.freq;
	}
	
	public String toString() {
		return this.freq+";"+this.note+";"+this.Octave;
	}
}