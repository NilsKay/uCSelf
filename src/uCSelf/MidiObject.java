package uCSelf;

public class MidiObject {
	
	public static final String[] nts = new String[] {
	    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
	};
	
	public int midiIndex;
	int octave;
	int freq;
	int noteIndex;
	String note;
	
	public MidiObject(int index, int freq, String note) {
		this.midiIndex = index;
		this.freq = freq;
		this.note = note;
	}
	
	/**
	 * Convert this Freqency to a Midi Note
	 * @return
	 */
	public static MidiObject getMidiNote(int freq) {
		double q = freq / 440.0;
		int n = (int) ((12.0 * Math.log(q) / Math.log(2.0)) + 69.0);
		MidiObject midi = new MidiObject(n, freq, "");
		int nt = n % 12;
		midi.octave = (n / 12) -1;
		midi.note = nts[nt];
		return midi;
	}
	
	public void setMidiNr(int midiNr) {
		this.midiIndex = midiNr;
		this.noteIndex = midiNr % 12;
		this.octave = (midiNr / 12) -1;
		this.note = nts[this.noteIndex];
		setMidiFreqency();
		//return this.note;
	}
	
	/**
	 * 
	 * @param n [0-11] der gew�hlte Ton
	 */
	public void setMidiFreqency() {
		//double e = (n * this.Octave) * 440.0 / 32.0;
		//this.freq = Math.pow(2.0, e);
		midiIndex = this.noteIndex + (12 * (this.octave +1));// [-1 - 9]
		this.freq = (int) (440 * Math.pow( 2.0, (midiIndex - 69.0) / 12)); // OK
		this.note = this.nts[this.noteIndex];
	}
}
