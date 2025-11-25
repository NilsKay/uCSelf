package SOUND;
import java.text.DecimalFormat;

public class OneNote implements Cloneable {
	public boolean pedal;
	public double freq;
	public double weight;
	public String note;
	public int Octave, noteIndex, midiIndex;
	public int distance = 0;
	public String[] nts = new String[] {
    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
    };
	
	/**
	 * Maximum for Midi is Octave = 9, n = 9 max = 12543.9 Hz
	 * @param f a frequency
	 * @param n [0-11] the note we want to play
	 * @param oct the octave [-1 - 9]
	 */
	public OneNote(double f, int n, int oct) {
		this.freq = f;
		this.noteIndex = n;
		this.Octave = oct;
		this.pedal = false;
	}

	public boolean equals(OneNote n) {
		return this.freq == n.freq;
	}
	
	public String toString() {
		DecimalFormat f = new DecimalFormat("#.###");
		return f.format(this.freq)+";"+this.note+";"+this.Octave;
	}
	public OneNote clone() throws CloneNotSupportedException {
		return (OneNote) super.clone();
	}
	/**
	 * 
	 * @param n [0-11] der gew�hlte Ton
	 */
	public void setMidiFreqency() {
		//double e = (n * this.Octave) * 440.0 / 32.0;
		//this.freq = Math.pow(2.0, e);
		this.midiIndex = this.noteIndex + (12 * (this.Octave +1));// [-1 - 9]
		this.freq = 440 * Math.pow( 2.0, (midiIndex - 69.0) / 12); // OK
		this.note = this.nts[this.noteIndex];
	}
	/**
	 * Set note and octave by midiNr
	 * @param mideNr
	 */
	public void setMidiNr(int midiNr) {
		this.midiIndex = midiNr;
		this.noteIndex = midiNr % 12;
		this.Octave = (midiNr / 12) -1;
		this.note = nts[this.noteIndex];
		setMidiFreqency();
		//return this.note;
	}
	/**
	 * Convert this Freqency to a Midi Note
	 * @return
	 */
	public String getMidiNote() {
		double q = this.freq / 440.0;
		
		int n = (int) ((12.0 * Math.log(q) / Math.log(2.0)) + 69.0);
		this.midiIndex = n;
		int nt = n % 12;
		this.Octave = (n / 12) -1;
		this.note = nts[nt];
		return nts[nt];
	}
}