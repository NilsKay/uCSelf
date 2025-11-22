package uCSelf;

public class MidiNotes {

	public static Individuum[] createNoteTable(int min, int max) {
		// All possible midi notes
		Individuum start = new Individuum(min, 0, 0.0);
		start.midi = MidiObject.getMidiNote(min);
		start.midi.setMidiNr(start.midi.midiIndex);
		if (start.freq < min) {
			start.midi.midiIndex++;
			start.midi.setMidiNr(start.midi.midiIndex);
		}
		Individuum stop =  new Individuum(max, 0, 0.0);
		stop.midi = MidiObject.getMidiNote(max);
		int size = stop.midi.midiIndex - start.midi.midiIndex;
		Individuum notes[] = new Individuum[size];
		for (int n = 0; n < size; n++) {
			if (n >=0 ) {
				Individuum nt = new Individuum(0, 0, 0);
				nt.midi = MidiObject.getMidiNote(max); // init midi Object
				nt.midi.setMidiNr(n+start.midi.midiIndex);
				notes[n] = nt;
			}
		}
		return notes;
    }
	
}
