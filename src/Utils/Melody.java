package Utils;

import java.util.Vector;

import SOUND.OneNote;

public class Melody {
	public String[] nts = new String[] {
			"c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "a#", "h"
	};
	public double max = 6.0;
	String[] c = new String[] 	{ "c", "d#", "e", "g", "g#", "a" }; // c, d#, e, g, g#, a
	String[] c_ = new String[] 	{ "c#", "e", "f", "g#", "a", "a#" }; // c#, e, f, g#, a, a#
	String[] d = new String[] 	{ "d", "f", "f#", "a", "a#", "h" }; // d, f, f#, a, a#, h
	String[] d_ = new String[] 	{ "d#", "f#", "g", "a#", "h", "c" }; // d#, f#, g, a#, h, c
	String[] e = new String[] 	{ "e", "g", "g#", "h", "c", "c#" };  // e, g, g#, h, c, c#
	String[] f = new String[] 	{ "f", "g#", "a", "c", "c#", "d" };  // f, g#, a, c, c#, d
	String[] f_ = new String[] 	{ "f#", "a", "a#", "c#", "d", "d#" }; // f#, a, a#, c#, d, d#
	String[] g = new String[] 	{ "g", "a#", "h", "d", "d#", "e" };  // g, a#, h, d, d#, e
	String[] g_ = new String[] 	{ "g#", "h", "c", "d#", "e", "f" }; // g#, h, c, d#, e, f
	String[] a = new String[] 	{ "a", "c", "c#", "e", "f", "f#" };  // a, c, c#, e, f, f#
	String[] a_ = new String[] 	{ "a#", "c#", "d", "f", "f#", "g" }; // a#, c#, d, f, f#, g
	String[] h = new String[] 	{ "h", "d", "d#", "f#", "g", "g#" };  // h, d, d#, f#, g, g#
	
	
	Vector<OneNote[]> melody;
	
	public Melody() {
		this.melody = new Vector<OneNote[]>(); // Tonleiter
		melody.addElement(getNote(c));
		melody.addElement(getNote(c_));
		melody.addElement(getNote(d));
		melody.addElement(getNote(d_));
		melody.addElement(getNote(e));
		melody.addElement(getNote(f));
		melody.addElement(getNote(f_));
		melody.addElement(getNote(g));
		melody.addElement(getNote(g_));
		melody.addElement(getNote(a));
		melody.addElement(getNote(a_));
		melody.addElement(getNote(h));
	}
	/**
	 * 
	 * @param noteIndex Hauptspur note
	 * @param octave Melody octave
	 * @param actualNote Melody note
	 * @return
	 */
	public OneNote findNextMelodyNote(int noteIndex, int octave, int actualNote) {
		// Hauptnote bestimmt welche möglichen Melodienoten passen,
		// von denen wird die nächste frequenz zur aktuell gespielten genommen
		OneNote[] possible = this.melody.elementAt(noteIndex);
		int foundIndex = 0;
		int diff = 148668383;
		for (int n = 0; n < possible.length; n++) {
			int di = Math.abs(possible[n].noteIndex - actualNote);
			if (di < diff && di != 0) { // nicht die gleiche Note nehmen !
				diff = di;
				foundIndex = n;
			}
		}
		OneNote res = new OneNote(0, possible[foundIndex].noteIndex, octave);
		res.setMidiFreqency();
		return res;
	}
	
	/**
	 * 
	 * @param noteIndex Hauptspur note
	 * @param octave Melody octave
	 * @param tension 
	 * @return
	 */
	public OneNote findMelodyNote(int noteIndex, int octave, int tension) {
		// Hauptnote bestimmt welche möglichen Melodienoten passen,
		
		OneNote[] possible = this.melody.elementAt(noteIndex);
		OneNote res = new OneNote(0, possible[tension].noteIndex, octave);
		res.setMidiFreqency();
		return res;
	}
	
	public OneNote[] getNote(String[] d) {
		OneNote[] r = new OneNote[d.length];
		for (int n = 0; n < d.length; n++) {
			r[n] = new OneNote(0, identifyNote(d[n]), 0);
			r[n].note = r[n].nts[r[n].noteIndex];
		}
		return r;
	}
	public int identifyNote(String s) {
		int r = 0;
		for(int n = 0; n < nts.length; n++) {
			if (nts[n].equalsIgnoreCase(s)) {
				r = n;
				break;
			}
		}
		return r;
	}
	
}
/*1)Hauptspur:
c
dann möglich:
c, d#, e, g, g#, a

2) Hauptspur:
c#
dann möglich:
c#, e, f, g#, a, a#

3) Hauptspur:
d
dann möglich:
d, f, f#, a, a#, h

4) Hauptspur:
d#
dann möglich:
d#, f#, g, a#, h, c

5) Hauptspur:
e
dann möglich:
e, g, g#, h, c, c#

6) Hauptspur:
f
dann möglich:
f, g#, a, c, c#, d

7) Hauptspur:
f#
dann möglich:
f#, a, a#, c#, d, d#

8) Hauptspur:
g
dann möglich:
g, a#, h, d, d#, e

9) Hauptspur:
g#
dann möglich:
g#, h, c, d#, e, f

10) Hauptspur:
a
dann möglich:
a, c, c#, e, f, f#

11) Hauptspur:
a#
dann möglich:
a#, c#, d, f, f#, g

12) Hauptspur:
h
dann möglich:
h, d, d#, f#, g, g#
*/

