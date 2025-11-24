package SOUND;
import java.util.*;
import java.lang.Math;
/**
 * This class holds our Project 1
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Projectx { // Project1
    public final static String DO = "i1"; 
    public final int maxOkt = 6;	// 7 Oktaven maximal ?
    public final int KOMP1 = 0;	// Random
    public final int KOMP2 = 1;	// Tonleiter
    public final int KOMP3 = 2;	// Noise (nicht so gut)
    public final int KOMP4 = 3;	// Noise2
    public final int KOMP5 = 4;	// Evolution
    int type = KOMP5; 
    String header[];
    int noise_samples[];
    Vector generators, envelopes;
    Vector notes;	// a vector that holds the tonal notes frequencys
    GraphCanvas gc;
    CDebug qd;
    /**
     * Constructor des 1. Projektes:
     * Eine ganze musikalische Note errechnet sich so:
     * (Oktave * 220) * 2 ^ (x/12);
     * x geht von 0-11:
     * 0=A, 1=A#, B=2, C=3, C#=4, D=5, D#=6, E=7, F=8, F#=9, G=10, G#=11
     * Es gibt ca. 12 Oktaven ?
     */
    public Projectx() {
	// Constructor
	int n, m, q, freq;
	double p;
	this.notes = new Vector();
	n = 0;
	for (m = 0; m < maxOkt; m++) {
	    for (q = 0; q < 12; q++) {
		p = java.lang.Math.pow(2.0, ((double) n++ / 12.0));	// 2^(n/12)
		double f = (double) (220.0) * p;
		freq = (int) f;
		//System.out.println("Oktave="+m+" Note="+n+" p="+p+" f="+f+" freq="+freq);
		this.notes.addElement(new Integer(freq));
		//System.out.println("Note "+((m-1)*12+n)+"="+freq); 
	    }
	}

    }
    public String[] getHeader(int parm) {
	String tmp;
	switch (this.type) {
	case KOMP1:	// Random
	case KOMP2:	// Tonleiter
	case KOMP5:	// Evolution
	    int generator1 = 11;
	    int generator2 = 12;
	    int envelope1 = 31;
	    int envelope2 = 51;
	    int tempo = 150;
	    this.generators = new Vector();
	    this.generators.addElement(new Integer(generator1));
	    this.generators.addElement(new Integer(generator2));
	    this.envelopes = new Vector();
	    this.envelopes.addElement(new Integer(envelope1));
	    this.envelopes.addElement(new Integer(envelope2));
	    header = new String[16];
	    header[0] = "; ************************************************************************";
	    header[0] = "; ACCCI:     01_01_1B.SCO";
	    header[1] = "; source:    #301 Piano-like Excerpt, Risset(1969)";
	    header[2] = "; coded:     jpg 8/93";
	    header[3] = "; GEN functions **********************************************************";
	    header[4] = "; carriers";
	    header[5] = ";                  f11 is for ifq1 < 250 Hz: ten weighted sinusoids";
	    header[6] = "f"+generator1+" 0  1024 10    .158 .316 1     1   .282 .112 .063 .079 .126 .071";
	    header[7] = ";                f12 is for ifq1 > 250 Hz: seven weighted sinusoids";
	    header[8] = "f"+generator2+" 0  1024 10   1     .282  .089  .1 .071 .089 .050";
	    header[9] = "; envelopes";
	    header[10] = "f"+envelope1+" 0  1024   7  0   20  .99   380  .4  400  .2  224  0 ; duration < .2sec";
	    header[11] = "f"+envelope2+" 0  1025   5  .0156 3 1 1021  .0156                  ; duration > .2sec";
	    header[12] = "; score ******************************************************************";
	    header[13] = "t 0 "+tempo+"  ; tempo is 150 beats per minute:";
	    header[14] = "         ; durations below change according to ratio 60/150 = idur * .4";
	    header[15] = ";   start  idur  ifq1  if1   iamp  if2";
	    break;
	case KOMP3:
	    int n, z, h = 0;
	    tmp = "";
	    int sample = parm;
	    int max_amplitude = 3000;	// +- max_amplitude !
	    int itemPerRow = 20; // Zeilenbreite
	    header = new String[10000];
	    noise_samples = new int[sample];
	    header[h++] = "f1 0 "+sample+" -2";
	    // now fill with the values:
	    z = 0;	// itemzahler
	    for (n = 0; n < (sample-1); n++) {
		noise_samples[n] = (int) ( (double) max_amplitude - 
					   (double) max_amplitude * 2 *java.lang.Math.random());
		tmp+=Integer.toString(noise_samples[n])+" ";
		if (++z > itemPerRow) {
		    z = 0;
		    header[h++] = tmp;
		    tmp = "";
		}
	    }
	    break;
	case KOMP4:	// Noise 2
	    header = new String[10];
	    generator1 = 1;
	    this.generators = new Vector();
	    this.generators.addElement(new Integer(generator1));
	    header[0] = "f"+generator1+" 0 1024 10 1";
	    break;
	}
	return header;
    }
    private Vector doKomposition1(int anzahl_toene) { // Random Frequency
	String tmp;
	Note nt;
	int n, z;
	Vector komposition = new Vector(); 
	double start = 0.0;
	int freq = 104;
	double dauer = 0.8;
	int amplitude = 3000;
	int generator, envelope;
	//sco_lines.addElement("i1   0     1.66   104   11   3000   51       ; time 0: pedal on ...");
	// Hier sollte man nun viele verschiedene Noten erzeugen und in einem Vector speichern:
	// *
	// Note(start, dauer, freq, generator, amplitude, envelope)
	System.out.println("KOMPOSITION: (Random Frequency)");
	double dauer_max = 1.0;
	double dauer_min = 0.4;
	int env = 0;
	for(n = 0; n < anzahl_toene; n++) {
	    z = (int) ((double) this.notes.size() * java.lang.Math.random());
	    freq = ((Integer) this.notes.elementAt(z)).intValue();
	    dauer = dauer_min + (dauer_max-dauer_min) * java.lang.Math.random();
	    env = (int) (java.lang.Math.random() + 0.5);
	    envelope = ((Integer) this.envelopes.elementAt(env)).intValue();
	    if (freq < 250) generator = ((Integer) this.generators.elementAt(0)).intValue();
	    else generator = ((Integer) this.generators.elementAt(1)).intValue();
	    nt = new Note(start, dauer, 0.0, freq, generator, amplitude, envelope);
	    komposition.addElement(nt);
	    //System.out.println("ADDed: "+freq);
	    start+=dauer;
	}
	return komposition;
    }
    private Vector doKomposition2() {
	String tmp;
	Note nt;
	int n, z;
	Vector komposition = new Vector(); 
	double start = 0.0;
	int freq = 104;
	double dauer = 0.8;
	int amplitude = 3000;
	int generator, envelope;
	envelope = ((Integer) this.envelopes.elementAt(1)).intValue();
	System.out.println("KOMPOSITION: (Tonleiter)");
	for(n = 0; n < this.notes.size(); n++) {
	    freq = ((Integer) this.notes.elementAt(n)).intValue();
	    if (freq < 250) generator = ((Integer) this.generators.elementAt(0)).intValue();
	    else generator = ((Integer) this.generators.elementAt(1)).intValue();
	    nt = new Note(start, dauer, 0.0 , freq, generator, amplitude, envelope);
	    komposition.addElement(nt);
	    //System.out.println("ADDed: "+freq);
	    start+=dauer;
	}
	return komposition;
    }
/**
 * @return a Vector of Note objects
 */
    private Vector doKomposition3() { // Noise
	String tmp;
	Note nt;
	int n, z;
	Vector komposition = new Vector(); 
	double start = 0.0;
	double dauer = 2;
	int freq = 0;
	int anzahl = 1;
	int amplitude = 0;
	int generator, envelope;
	envelope = 0;
	generator = 0;
	System.out.println("KOMPOSITION: (Noise)");
	for(n = 0; n < anzahl; n++) {
	    nt = new Note(start, dauer, 0.0, freq, generator, amplitude, envelope);
	    komposition.addElement(nt);
	    start+=dauer;
	}
	return komposition;
    }
    private Vector doKomposition4(int anzahl_Freq) { // Random Frequency to generate noise !
	String tmp;
	Note nt;
	int n, z;
	double diff, diff1;
	int freq, amplitude;
	Vector komposition = new Vector(); 
	double start = 0.0;
// hier die Rausch-Kennwerte:
	double dauer = 10;
	double max_freq = 20000.0; 	// Hz
	int min_freq = 16;		// Hz
	double max_amplitude = 300.0;
	int min_amplitude = 10;
// bestimme welchen generator und welche Hüllkurve zu nehmen ist:
	int generator, envelope;
	envelope = 0;	// not used
	generator = 0;	// use default
	// Note(start, dauer, freq, generator, amplitude, envelope)
	System.out.println("KOMPOSITION: (Rauschen 2)");
	diff = max_freq - (double) min_freq;
	diff1 = max_amplitude - (double) min_amplitude;

	for(n = 0; n < anzahl_Freq; n++) {
	    freq = min_freq + (int) (diff * java.lang.Math.random()); // Zufallsfrequenz
	    amplitude = min_amplitude + (int) (diff1 * java.lang.Math.random()); // Zufallsamplitude
	    nt = new Note(start, dauer, 0.0, freq, generator, amplitude, envelope);
	    komposition.addElement(nt);
	    //System.out.println("ADDed: "+freq);
	}
	//start+=dauer; // Hier wäre dann das nächste rauschen
	return komposition;
    }

    private Vector doKomposition5() { // Evolution
	String tmp;
	Note nt;
	int n, z;
	Vector komposition = new Vector(); 
	double start = 0.0;
	double dauer = 1;
	double max_amplitude = 300.0;
	int min_amplitude = 10;
	int max_freq = 20000; 	// Hz
	int min_freq = 10;		// Hz
	int step = 50;	// Hz Schritte
	int diff_freq = 250; 	// Hz
	int amplitude = 0;
	int generator, envelope;
	envelope = envelope = ((Integer) this.envelopes.elementAt(0)).intValue();
	generator = ((Integer) this.generators.elementAt(0)).intValue();
	System.out.println("KOMPOSITION: (Evolution)");
	// Angangsbedingung:
	int freq = min_freq + (int) ((double) (max_freq - min_freq) * java.lang.Math.random());
	RandomTable rt = new RandomTable(this.qd, 1, min_freq, max_freq, step, 0.0);
	// Veraendere die Tabelle:
	// freq, um wieviel nach oben, welche Frequenz-differenz zu beachten ist
	rt.writeNEntrys( freq, 200.0, diff_freq );
	gc.setTable(rt);	// Anzeigen !

	freq = min_freq + (int) ((double) (max_freq - min_freq) * java.lang.Math.random());
	rt.writeNEntrys( freq, 200.0, diff_freq );
	gc.setTable(rt);	// Anzeigen !
/*
	for(n = 0; n < anzahl; n++) {
	    nt = new Note(start, dauer, 0.0, freq, generator, amplitude, envelope);
	    komposition.addElement(nt);
	    start+=dauer;
	}
*/
	return komposition;
    }

    public Vector generateSoundWithGraphic(GraphCanvas gc, int parm) {
	this.gc = gc;
	return generateSound(parm);
    }

    /**
     * Hier nun das Herz der Methode: wie baut man eine Ton-Abfolge.
     * Das Ergebnis ist ein Vector mit den .sco Zeilen für die Töne.
     * @param parm ein Übergabeparameter, kann auch leer sein (vom Textfeld)
     * @return Vector with .sco lines
     */
    public Vector generateSound(int parm) {
	Vector komposition;
	Vector v = new Vector();
	Note nt;
	String tmp;
	// Erst mal wähle aus, welche Komposition verwendet werden soll:
	switch (this.type) {
	case KOMP1:
	    komposition = doKomposition1(parm); // 100 Töne Random 
	    break;
	case KOMP2:
	    komposition = doKomposition2();	// Tonleiter
	    break;
	case KOMP3:
	    komposition = doKomposition3();	// Noise1
	    break;
	case KOMP4:
	    komposition = doKomposition4(parm);	// Noise2 mit n (parm) Frequenzen
	    break;
	case KOMP5:
	default:
	    komposition = doKomposition5();	// Evolution
	    break;
	}
	// ----------------------------------------------------------
	// so macht man dann aus einen Note - Eintrag eine sco-line :
	for(int n = 0; n < komposition.size(); n++) {
	    nt = (Note) komposition.elementAt(n);
	    tmp = Projectx.DO+" "+nt.start+" "+nt.dauer;
	    if (nt.freq > 0) tmp+= " "+nt.freq;
	    if (nt.generator > 0) tmp+= " "+nt.generator;
	    if (nt.amplitude > 0) tmp+= " "+nt.amplitude;
	    if (nt.envelope > 0 ) tmp+= " "+nt.envelope;
	    v.addElement(tmp);
	    //System.out.println("add line:"+tmp);
	}
	return v;
    }
}// end of class

