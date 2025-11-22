package Utils;

import java.util.Vector;

import SOUND.OneNote;

public class Akkorde {
	
	public Vector<Akkord> akk;
	public Vector<Command> cmds;
	public String[] nts = new String[] {
			"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
	};
	
	public Akkorde() {
		this.akk = new Vector<Akkord>();
		this.akk.addElement(new Akkord("c", this.c1_s));
		this.akk.addElement(new Akkord("cis", this.cis1_s));
		this.akk.addElement(new Akkord("d", this.d1_s));
		this.akk.addElement(new Akkord("dis", this.dis1_s));
		this.akk.addElement(new Akkord("e", this.e1_s));
		this.akk.addElement(new Akkord("f", this.f1_s));
		this.akk.addElement(new Akkord("fis", this.fis1_s));
		this.akk.addElement(new Akkord("g", this.g1_s));
		this.akk.addElement(new Akkord("gis", this.gis1_s));
		this.akk.addElement(new Akkord("a", this.a1_s));
		this.akk.addElement(new Akkord("ais", this.ais1_s));
		this.akk.addElement(new Akkord("h", this.h1_s));
		// test:
		setCommands ();
		int note = 1;
		for (int n= 0; n < this.cmds.size(); n++) {
			Command c = this.cmds.elementAt(n);
			int nn = note + c.diffNote;
			if (nn > 11)
				nn -= 11;
			System.out.println((n+1)+" "+c.name+" "+this.nts[note]+"1 "+this.nts[nn]+c.diffOctave);
		}
	}

	/**
	 * 
	 * @param n number of the chosen note (0-11)
	 * @param oc Octave of the chosen (2nd voice go -1,0,1,2)
	 * @param tension [0-11] 0=low tension, 1=high tension
	 * @return null on error
	 */
	public OneNote get2ndVoice(int n, int oc, int tension) {
		if ( oc < -1 || oc > 9 || tension >= 24) 
			return null;
		Akkord a = this.akk.elementAt(n);
		OneNote nt = a.elementAt(tension); // dies ist nun die 2. Stimme. nun die Octave w�hlen !
		int noc = oc;
		switch(nt.Octave) {
		case 0: // minus one octave
			noc -= 1;
			break;
		case 1: // keep octave
			noc = oc;
			break;
		case 2: // plus one octave
			noc += 1;
		}
		OneNote not =  new OneNote(0, nt.noteIndex, noc);
		not.setMidiFreqency();
		not.distance = tension;
		return not;
	}
	
	public void setCommands () {
		this.cmds = new Vector<Command>();
		this.cmds.addElement(new Command("Oktave abw�rts", 0,0)); 		// c
		this.cmds.addElement(new Command("Oktave aufw�rts",0,2)); 		// c
		this.cmds.addElement(new Command("Quinte abw�rts", 5, 0)); 		// f
		this.cmds.addElement(new Command("Quinte aufw�rts", 7, 1)); 	// g
		this.cmds.addElement(new Command("Quarte abw�rts", 7, 0)); 		// g
		this.cmds.addElement(new Command("Quarte aufw�rts", 5,1)); 		// f
		this.cmds.addElement(new Command("kleine Terz abw�rts", 9,0)); 	// a
		this.cmds.addElement(new Command("grosse Terz aufw�rts", 4,1)); // e
		this.cmds.addElement(new Command("grosse Terz abw�rts", 8, 0)); // gis
		this.cmds.addElement(new Command("kleine Terz aufw�rts", 3,1)); // dis
		this.cmds.addElement(new Command("kleine Sexte abw�rts",4,0)); 	// e
		this.cmds.addElement(new Command("grosse Sexte aufw�rts", 9,1)); //a
		this.cmds.addElement(new Command("grosse Sexte abw�rts", 3, 0)); // dis
		this.cmds.addElement(new Command("kleine Sexte aufw�rts", 8,1)); // gis
		this.cmds.addElement(new Command("Tritonus abw�rts", 6,0));		 // fis
		this.cmds.addElement(new Command("Tritonus aufw�rts", 6,1));	 // fis
		this.cmds.addElement(new Command("kleine Septime abw�rts", 2,0)); // d
		this.cmds.addElement(new Command("kleine Septime aufw�rts",10,1)); // ais
		this.cmds.addElement(new Command("grosse Sekunde abw�rts",10,0)); // ais
		this.cmds.addElement(new Command("grosse Sekunde aufw�rts", 2,1)); // d
		this.cmds.addElement(new Command("grosse Septime abw�rts", 1, 0)); // cis
		this.cmds.addElement(new Command("grosse Septime aufw�rts", 11,1)); // h
		this.cmds.addElement(new Command("kleine Sekunde abw�rts", 11,0)); // h
		this.cmds.addElement(new Command("kleine Sekunde aufw�rts", 1, 1)); // cis
	}
	
	/**
	 * Beginning with C1
	 */
	String[] c1_s = new String[] { 
			"c0","c2", "f0","g1","g0",
			"f1","a0", "e1","gis0","dis1", 
			"e0","a1","dis0","gis1","fis0",
			"fis1","d0","ais1","ais0","d1",
			"cis0","h1","h0","cis1"}; //
	
	String[] cis1_s = new String[] {
			"cis0", "cis2", "fis0", "gis1", "gis0",
			"fis1", "ais0", "f1", "a0", "e1", 
			"f0", "ais1", "e0", "a1", "g0",
			"g1", "dis0", "h1", "h0", "dis1",
			"d0", "c2", "c1", "d1"};
	String[] d1_s = new String[] {
			"d0", "d2", "g0", "a1", "a0",
			"g1", "h0", "fis1", "ais0", "f1",
			"fis0", "h1", "f0", "ais1", "gis0",
			"gis1", "e0", "c2", "c1", "e1",
			"dis0", "cis2", "cis1", "dis1"};
	String[] dis1_s = new String[] {
			"dis0", "dis2", "gis0", "ais1", "ais0",
			"gis1", "c1", "g1", "h0", "fis1",
			"g0", "c2", "fis0", "h1", "a0",
			"a1", "f0", "cis2", "cis1", "f1",
			"e0", "d2", "d1", "e1"};
	String[] e1_s = new String[] {
			"e0", "e2", "a0", "h1", "h0",
			"a1", "cis1", "gis1", "c1", "g1",
			"gis0", "cis2", "g0", "c2", "ais0",
			"ais1", "fis0", "d2", "d1", "fis1",
			"f0", "dis2", "dis1", "f1"};
	String[] f1_s = new String[] {
			"f0", "f2", "ais0", "c2", "c1",
			"ais1", "d1", "a1", "cis1", "gis1",
			"a0", "d2", "gis0", "cis2", "h0",
			"h1", "g0", "dis2", "dis1", "g1",
			"fis0", "e2", "e1", "fis1"};
	String[] fis1_s = new String[] {
			"fis0", "fis2", "h0", "cis2", "cis1",
			"h1", "dis1", "ais1", "d1", "a1",
			"ais0", "dis2", "a0", "d2", "c1",
			"c2", "gis0", "e2", "e1", "gis1",
			"g0", "f2", "f1", "g1"};
	String[] g1_s = new String[] {
			"g0", "g2", "c1", "d2", "d1",
			"c2", "e1", "h1", "dis1", "ais1",
			"h0", "e2", "ais0", "dis2", "cis1",
			"cis2", "a0", "f2", "f1", "a1",
			"gis0", "fis2", "fis1", "gis1"};
	String[] gis1_s = new String[] {
			"gis0", "gis2", "cis1", "dis2", "dis1",
			"cis2", "f1", "c2", "e1", "h1",
			"c1", "f2", "h0", "e2", "d1",
			"d2", "ais0", "fis2", "fis1", "ais1",
			"a0", "g2", "g1", "a1"};

	String[] a1_s = new String[] {
			"a0", "a2", "d1", "e2", "e1",
			"d2", "fis1", "cis2", "f1", "c2",
			"cis1", "fis2", "c1", "f2", "dis1",
			"dis2", "h0", "g2", "g1", "h1",
			"ais0", "gis2", "gis1", "ais1"};
	
	String[] ais1_s = new String[] {
			"ais0", "ais2", "dis1", "f2", "f1",
			"dis2", "g1", "d2", "fis1", "cis2",
			"d1", "g2", "cis1", "fis2", "e1",
			"e2", "c1", "gis2", "gis1", "c2",
			"h0", "a2", "a1", "h1"};
	
	String[] h1_s = new String[] {
			"h0", "h2", "e1", "fis2", "fis1",
			"e2", "gis1", "dis2", "g1", "d2",
			"dis1", "gis2", "d1", "g2", "f1",
			"f2", "cis1", "a2", "a1", "cis2",
			"c1", "ais2", "ais1", "c2"};
	
	class Command {
		String name;
		int diffNote, diffOctave;
		public Command(String name, int dn, int doc) {
			this.name = name;
			this.diffNote = dn;
			this.diffOctave = doc;
		}
	}
}
/*	 public static String[] nts = new String[] {
	    	"c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais", "h"
	    };
	    */
	
	/*
	Auf c1
1 Oktave abw�rts c1 c0
2 Okatve aufw�rts c1 c2
3 Quinte abw�rts c1 f0
4 Quinte aufw�rts c1 g1
5 Quarte abw�rts c1 g0
6 Quarte aufw�rts c1 f1
7 kleine Terz abw�rts c1 a0
8 grosse Terz aufw�rts c1 e1
9 grosse Terz abw�rts c1 g#0
10 kleine Terz aufw�rts c1 d#1
12 kleine Sexte abw�rts c1 e0
12 grosse Sexte aufw�rts c1 a1
13 grosse Sexte abw�rts c1 d#0
14 kleine Sexte aufw�rts c1 g#1
15 Tritonus abw�rts c1 f#0
16 Tritonus aufw�rts c1 f#1
17 kleine Septime abw�rts c1 d0
18 kleine Septime aufw�rts c1 a#1
19 grosse Sekunde abw�rts c1 a#0
20 grosse Sekunde aufw�rts c1 d1
21 grosse Septime abw�rts c1 c#0
22 grosse Septime aufw�rts c1 h1
23 kleine Sekunde abw�rts c1 h0
24 kleine Sekunde aufw�rts c1 c#1 

	
	Auf cis1 (c#1)
1 Oktave abw�rts c#1 c#0
2 Okatve aufw�rts c#1 c#2
3 Quinte abw�rts c#1 f#0
4 Quinte aufw�rts c#1 g#1
5 Quarte abw�rts c#1 g#0
6 Quarte aufw�rts c#1 f#1
7 kleine Terz abw�rts c#1 a#0
8 grosse Terz aufw�rts c#1 f1
9 grosse Terz abw�rts c#1 a0
10 kleine Terz aufw�rts c#1 e1
12 kleine Sexte abw�rts c#1 f0
12 grosse Sexte aufw�rts c#1 a#1
13 grosse Sexte abw�rts c#1 e0
14 kleine Sexte aufw�rts c#1 a1
15 Tritonus abw�rts c#1 g0
16 Tritonus aufw�rts c#1 g1
17 kleine Septime abw�rts c#1 d#0
18 kleine Septime aufw�rts c#1 h1
19 grosse Sekunde abw�rts c#1 h0
20 grosse Sekunde aufw�rts c#1 d#1
21 grosse Septime abw�rts c#1 d0
22 grosse Septime aufw�rts c#1 c2
23 kleine Sekunde abw�rts c#1 c1
24 kleine Sekunde aufw�rts c#1 d1 

Auf d1
1 Oktave abw�rts d1 d0
2 Okatve aufw�rts d1 d2
3 Quinte abw�rts d1 g0
4 Quinte aufw�rts d1 a1
5 Quarte abw�rts d1 a0
6 Quarte aufw�rts d1 g1
7 kleine Terz abw�rts d1 h0
8 grosse Terz aufw�rts d1 f#1
9 grosse Terz abw�rts d1 a#0
10 kleine Terz aufw�rts d1 f1
12 kleine Sexte abw�rts d1 f#0
12 grosse Sexte aufw�rts d1 h1
13 grosse Sexte abw�rts d1 f0
14 kleine Sexte aufw�rts d1 a#1
15 Tritonus abw�rts d1 g#0
16 Tritonus aufw�rts d1 g#1
17 kleine Septime abw�rts d1 e0
18 kleine Septime aufw�rts d1 c2
19 grosse Sekunde abw�rts d1 c1
20 grosse Sekunde aufw�rts d1 e1
21 grosse Septime abw�rts d1 d#0
22 grosse Septime aufw�rts d1 c#2
23 kleine Sekunde abw�rts d1 c#1
24 kleine Sekunde aufw�rts d1 d#1 

Auf d#1
1 Oktave abw�rts d#1 d#0
2 Okatve aufw�rts d#1 d#2
3 Quinte abw�rts d#1 g#0
4 Quinte aufw�rts d#1 a#1
5 Quarte abw�rts d#1 a#0
6 Quarte aufw�rts d#1 g#1
7 kleine Terz abw�rts d#1 c1
8 grosse Terz aufw�rts d#1 g1
9 grosse Terz abw�rts d#1 h0
10 kleine Terz aufw�rts d#1 f#1
12 kleine Sexte abw�rts d#1 g0
12 grosse Sexte aufw�rts d#1 c2
13 grosse Sexte abw�rts d#1 f#0
14 kleine Sexte aufw�rts d#1 h1
15 Tritonus abw�rts d#1 a0
16 Tritonus aufw�rts d#1 a1
17 kleine Septime abw�rts d#1 f0
18 kleine Septime aufw�rts d#1 c#2
19 grosse Sekunde abw�rts d#1 c#1
20 grosse Sekunde aufw�rts d#1 f1
21 grosse Septime abw�rts d#1 e0
22 grosse Septime aufw�rts d#1 d2
23 kleine Sekunde abw�rts d#1 d1
24 kleine Sekunde aufw�rts d#1 e1 


Auf e1
1 Oktave abw�rts e1 e0
2 Okatve aufw�rts e1 e2
3 Quinte abw�rts e1 a0
4 Quinte aufw�rts e1 h1
5 Quarte abw�rts e1 h0
6 Quarte aufw�rts e1 a1
7 kleine Terz abw�rts e1 c#1
8 grosse Terz aufw�rts e1 g#1
9 grosse Terz abw�rts e1 c1
10 kleine Terz aufw�rts e1 g1
12 kleine Sexte abw�rts e1 g#0
12 grosse Sexte aufw�rts e1 c#2
13 grosse Sexte abw�rts e1 g0
14 kleine Sexte aufw�rts e1 c2
15 Tritonus abw�rts e1 a#0
16 Tritonus aufw�rts e1 a#1
17 kleine Septime abw�rts e1 f#0
18 kleine Septime aufw�rts e1 d2
19 grosse Sekunde abw�rts e1 d1
20 grosse Sekunde aufw�rts e1 f#1
21 grosse Septime abw�rts e1 f0
22 grosse Septime aufw�rts e1 d#2
23 kleine Sekunde abw�rts e1 d#1
24 kleine Sekunde aufw�rts e1 f1 

Auf f1
1 Oktave abw�rts f1 f0
2 Okatve aufw�rts f1 f2
3 Quinte abw�rts f1 a#0
4 Quinte aufw�rts f1 c2
5 Quarte abw�rts f1 c1
6 Quarte aufw�rts f1 a#1
7 kleine Terz abw�rts f1 d1
8 grosse Terz aufw�rts f1 a1
9 grosse Terz abw�rts f1 c#1
10 kleine Terz aufw�rts f1 g#1
12 kleine Sexte abw�rts f1 a0
12 grosse Sexte aufw�rts f1 d2
13 grosse Sexte abw�rts f1 g#0
14 kleine Sexte aufw�rts f1 c#2
15 Tritonus abw�rts f1 h0
16 Tritonus aufw�rts f1 h1
17 kleine Septime abw�rts f1 g0
18 kleine Septime aufw�rts f1 d#2
19 grosse Sekunde abw�rts f1 d#1
20 grosse Sekunde aufw�rts f1 g1
21 grosse Septime abw�rts f1 f#0
22 grosse Septime aufw�rts f1 e2
23 kleine Sekunde abw�rts f1 e1
24 kleine Sekunde aufw�rts f1 f#1 

Auf f#1
1 Oktave abw�rts f#1 f#0
2 Okatve aufw�rts f#1 f#2
3 Quinte abw�rts f#1 h0
4 Quinte aufw�rts f#1 c#2
5 Quarte abw�rts f#1 c#1
6 Quarte aufw�rts f#1 h1
7 kleine Terz abw�rts f#1 d#1
8 grosse Terz aufw�rts f#1 a#1
9 grosse Terz abw�rts f#1 d1
10 kleine Terz aufw�rts f#1 a1
12 kleine Sexte abw�rts f#1 a#0
12 grosse Sexte aufw�rts f#1 d#2
13 grosse Sexte abw�rts f#1 a0
14 kleine Sexte aufw�rts f#1 d2
15 Tritonus abw�rts f#1 c1
16 Tritonus aufw�rts f#1 c2
17 kleine Septime abw�rts f#1 g#0
18 kleine Septime aufw�rts f#1 e2
19 grosse Sekunde abw�rts f#1 e1
20 grosse Sekunde aufw�rts f#1 g#1
21 grosse Septime abw�rts f#1 g0
22 grosse Septime aufw�rts f#1 f2
23 kleine Sekunde abw�rts f#1 f1
24 kleine Sekunde aufw�rts f#1 g1 


Auf g1
1 Oktave abw�rts g1 g0
2 Okatve aufw�rts g1 g2
3 Quinte abw�rts g1 c1
4 Quinte aufw�rts g1 d2
5 Quarte abw�rts g1 d1
6 Quarte aufw�rts g1 c2
7 kleine Terz abw�rts g1 e1
8 grosse Terz aufw�rts g1 h1
9 grosse Terz abw�rts g1 d#1
10 kleine Terz aufw�rts g1 a#1
12 kleine Sexte abw�rts g1 h0
12 grosse Sexte aufw�rts g1 e2
13 grosse Sexte abw�rts g1 a#0
14 kleine Sexte aufw�rts g1 d#2
15 Tritonus abw�rts g1 c#1
16 Tritonus aufw�rts g1 c#2
17 kleine Septime abw�rts g1 a0
18 kleine Septime aufw�rts g1 f2
19 grosse Sekunde abw�rts g1 f1
20 grosse Sekunde aufw�rts g1 a1
21 grosse Septime abw�rts g1 g#0
22 grosse Septime aufw�rts g1 f#2
23 kleine Sekunde abw�rts g1 f#1
24 kleine Sekunde aufw�rts g1 g#1 

Auf g#1
1 Oktave abw�rts g#1 g#0
2 Okatve aufw�rts g#1 g#2
3 Quinte abw�rts g#1 c#1
4 Quinte aufw�rts g#1 d#2
5 Quarte abw�rts g#1 d#1
6 Quarte aufw�rts g#1 c#2
7 kleine Terz abw�rts g#1 f1
8 grosse Terz aufw�rts g#1 c2
9 grosse Terz abw�rts g#1 e1
10 kleine Terz aufw�rts g#1 h1
12 kleine Sexte abw�rts g#1 c1
12 grosse Sexte aufw�rts g#1 f2
13 grosse Sexte abw�rts g#1 h0
14 kleine Sexte aufw�rts g#1 e2
15 Tritonus abw�rts g#1 d1
16 Tritonus aufw�rts g#1 d2
17 kleine Septime abw�rts g#1 a#0
18 kleine Septime aufw�rts g#1 f#2
19 grosse Sekunde abw�rts g#1 f#1
20 grosse Sekunde aufw�rts g#1 a#1
21 grosse Septime abw�rts g#1 a0
22 grosse Septime aufw�rts g#1 g2
23 kleine Sekunde abw�rts g#1 g1
24 kleine Sekunde aufw�rts g#1 a1 

Auf a1
Oktave abw�rts a1 a0
2 Okatve aufw�rts a1 a2
3 Quinte abw�rts a1 d1
4 Quinte aufw�rts a1 e2
5 Quarte abw�rts a1 e1
6 Quarte aufw�rts a1 d2
7 kleine Terz abw�rts a1 f#1
8 grosse Terz aufw�rts a1 c#2
9 grosse Terz abw�rts a1 f1
10 kleine Terz aufw�rts a1 c2
12 kleine Sexte abw�rts a1 c#1
12 grosse Sexte aufw�rts a1 f#2
13 grosse Sexte abw�rts a1 c1
14 kleine Sexte aufw�rts a1 f2
15 Tritonus abw�rts a1 d#1
16 Tritonus aufw�rts a1 d#2
17 kleine Septime abw�rts a1 h0
18 kleine Septime aufw�rts a1 g2
19 grosse Sekunde abw�rts a1 g1
20 grosse Sekunde aufw�rts a1 h1
21 grosse Septime abw�rts a1 a#0
22 grosse Septime aufw�rts a1 g#2
23 kleine Sekunde abw�rts a1 g#1
24 kleine Sekunde aufw�rts a1 a#1 


Auf a#1
Oktave abw�rts a#1 a#0
2 Okatve aufw�rts a#1 a#2
3 Quinte abw�rts a#1 d#1
4 Quinte aufw�rts a#1 f2
5 Quarte abw�rts a#1 f1
6 Quarte aufw�rts a#1 d#2
7 kleine Terz abw�rts a#1 g1
8 grosse Terz aufw�rts a#1 d2
9 grosse Terz abw�rts a#1 f#1
10 kleine Terz aufw�rts a#1 c#2
12 kleine Sexte abw�rts a#1 d1
12 grosse Sexte aufw�rts a#1 g2
13 grosse Sexte abw�rts a#1 c#1
14 kleine Sexte aufw�rts a#1 f#2
15 Tritonus abw�rts a#1 e1
16 Tritonus aufw�rts a#1 e2
17 kleine Septime abw�rts a#1 c1
18 kleine Septime aufw�rts a#1 g#2
19 grosse Sekunde abw�rts a#1 g#1
20 grosse Sekunde aufw�rts a#1 c2
21 grosse Septime abw�rts a#1 h0
22 grosse Septime aufw�rts a#1 a2
23 kleine Sekunde abw�rts a#1 a1
24 kleine Sekunde aufw�rts a#1 h1 


Auf h1
Oktave abw�rts h1 h0
2 Okatve aufw�rts h1 h2
3 Quinte abw�rts h1 e1
4 Quinte aufw�rts h1 f#2
5 Quarte abw�rts h1 f#1
6 Quarte aufw�rts h1 e2
7 kleine Terz abw�rts h1 g#1
8 grosse Terz aufw�rts h1 d#2
9 grosse Terz abw�rts h1 g1
10 kleine Terz aufw�rts h1 d2
12 kleine Sexte abw�rts h1 d#1
12 grosse Sexte aufw�rts h1 g#2
13 grosse Sexte abw�rts h1 d1
14 kleine Sexte aufw�rts h1 g2
15 Tritonus abw�rts h1 f1
16 Tritonus aufw�rts h1 f2
17 kleine Septime abw�rts h1 c#1
18 kleine Septime aufw�rts h1 a2
19 grosse Sekunde abw�rts h1 a1
20 grosse Sekunde aufw�rts h1 c#2
21 grosse Septime abw�rts h1 c1
22 grosse Septime aufw�rts h1 a#2
23 kleine Sekunde abw�rts h1 a#1
24 kleine Sekunde aufw�rts h1 c2 

	*/
