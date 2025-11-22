package Utils;

import java.util.Vector;

import SOUND.OneNote;
import SOUND.Project2;

@SuppressWarnings("serial")
/**
 * This is for 2 Voices
 * 
 * @author nik
 *
 */
public class Akkord extends Vector<OneNote> {
	OneNote base_note;
	
	/**
	 * 
	 * @param base the base note 
	 * @param o the 24 2nd voices. Later there may be 3 or more voices, 
	 * in that case we would have to split the note string here
	 */
	public Akkord(String base, Object[] o) { // c1, c2
		this.base_note = getNote(base);
		for (int n = 0; n < o.length; n++) {
			String t = (String) o[n]; 
			this.addElement(getNote(t));
		}
	}
	
	private OneNote getNote(String s) {
		String z = s.substring(s.length() -1, s.length());
		String n = s.substring(0, s.length() -1);
		int oc = Converter.getInt(z, 0);
		int nt = Project2.getNoteFromString(n);
		OneNote no = new OneNote(0, nt, oc);
		return no;
	}
	
}