package Utils;

import java.util.Vector;

import SOUND.OneNote;

@SuppressWarnings("serial")
public class Akkord extends Vector<OneNote> {
	
	public Akkord(Object[] o) {
		for (int n = 0; n < o.length; n++) {
			String t = (String) o[n]; // +, - ,=
			
			this.addElement(new OneNote(0.0, t, 0));
		}
	}
}