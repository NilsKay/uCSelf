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
		OneNote nt = a.elementAt(tension); // dies ist nun die 2. Stimme. nun die Octave wählen !
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
		this.cmds.addElement(new Command("Oktave abwärts", 0,0)); 		// c
		this.cmds.addElement(new Command("Oktave aufwärts",0,2)); 		// c
		this.cmds.addElement(new Command("Quinte abwärts", 5, 0)); 		// f
		this.cmds.addElement(new Command("Quinte aufwärts", 7, 1)); 	// g
		this.cmds.addElement(new Command("Quarte abwärts", 7, 0)); 		// g
		this.cmds.addElement(new Command("Quarte aufwärts", 5,1)); 		// f
		this.cmds.addElement(new Command("kleine Terz abwärts", 9,0)); 	// a
		this.cmds.addElement(new Command("grosse Terz aufwärts", 4,1)); // e
		this.cmds.addElement(new Command("grosse Terz abwärts", 8, 0)); // gis
		this.cmds.addElement(new Command("kleine Terz aufwärts", 3,1)); // dis
		this.cmds.addElement(new Command("kleine Sexte abwärts",4,0)); 	// e
		this.cmds.addElement(new Command("grosse Sexte aufwärts", 9,1)); //a
		this.cmds.addElement(new Command("grosse Sexte abwärts", 3, 0)); // dis
		this.cmds.addElement(new Command("kleine Sexte aufwärts", 8,1)); // gis
		this.cmds.addElement(new Command("Tritonus abwärts", 6,0));		 // fis
		this.cmds.addElement(new Command("Tritonus aufwärts", 6,1));	 // fis
		this.cmds.addElement(new Command("kleine Septime abwärts", 2,0)); // d
		this.cmds.addElement(new Command("kleine Septime aufwärts",10,1)); // ais
		this.cmds.addElement(new Command("grosse Sekunde abwärts",10,0)); // ais
		this.cmds.addElement(new Command("grosse Sekunde aufwärts", 2,1)); // d
		this.cmds.addElement(new Command("grosse Septime abwärts", 1, 0)); // cis
		this.cmds.addElement(new Command("grosse Septime aufwärts", 11,1)); // h
		this.cmds.addElement(new Command("kleine Sekunde abwärts", 11,0)); // h
		this.cmds.addElement(new Command("kleine Sekunde aufwärts", 1, 1)); // cis
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
1 Oktave abwärts c1 c0
2 Okatve aufwärts c1 c2
3 Quinte abwärts c1 f0
4 Quinte aufwärts c1 g1
5 Quarte abwärts c1 g0
6 Quarte aufwärts c1 f1
7 kleine Terz abwärts c1 a0
8 grosse Terz aufwärts c1 e1
9 grosse Terz abwärts c1 g#0
10 kleine Terz aufwärts c1 d#1
12 kleine Sexte abwärts c1 e0
12 grosse Sexte aufwärts c1 a1
13 grosse Sexte abwärts c1 d#0
14 kleine Sexte aufwärts c1 g#1
15 Tritonus abwärts c1 f#0
16 Tritonus aufwärts c1 f#1
17 kleine Septime abwärts c1 d0
18 kleine Septime aufwärts c1 a#1
19 grosse Sekunde abwärts c1 a#0
20 grosse Sekunde aufwärts c1 d1
21 grosse Septime abwärts c1 c#0
22 grosse Septime aufwärts c1 h1
23 kleine Sekunde abwärts c1 h0
24 kleine Sekunde aufwärts c1 c#1 

	
	Auf cis1 (c#1)
1 Oktave abwärts c#1 c#0
2 Okatve aufwärts c#1 c#2
3 Quinte abwärts c#1 f#0
4 Quinte aufwärts c#1 g#1
5 Quarte abwärts c#1 g#0
6 Quarte aufwärts c#1 f#1
7 kleine Terz abwärts c#1 a#0
8 grosse Terz aufwärts c#1 f1
9 grosse Terz abwärts c#1 a0
10 kleine Terz aufwärts c#1 e1
12 kleine Sexte abwärts c#1 f0
12 grosse Sexte aufwärts c#1 a#1
13 grosse Sexte abwärts c#1 e0
14 kleine Sexte aufwärts c#1 a1
15 Tritonus abwärts c#1 g0
16 Tritonus aufwärts c#1 g1
17 kleine Septime abwärts c#1 d#0
18 kleine Septime aufwärts c#1 h1
19 grosse Sekunde abwärts c#1 h0
20 grosse Sekunde aufwärts c#1 d#1
21 grosse Septime abwärts c#1 d0
22 grosse Septime aufwärts c#1 c2
23 kleine Sekunde abwärts c#1 c1
24 kleine Sekunde aufwärts c#1 d1 

Auf d1
1 Oktave abwärts d1 d0
2 Okatve aufwärts d1 d2
3 Quinte abwärts d1 g0
4 Quinte aufwärts d1 a1
5 Quarte abwärts d1 a0
6 Quarte aufwärts d1 g1
7 kleine Terz abwärts d1 h0
8 grosse Terz aufwärts d1 f#1
9 grosse Terz abwärts d1 a#0
10 kleine Terz aufwärts d1 f1
12 kleine Sexte abwärts d1 f#0
12 grosse Sexte aufwärts d1 h1
13 grosse Sexte abwärts d1 f0
14 kleine Sexte aufwärts d1 a#1
15 Tritonus abwärts d1 g#0
16 Tritonus aufwärts d1 g#1
17 kleine Septime abwärts d1 e0
18 kleine Septime aufwärts d1 c2
19 grosse Sekunde abwärts d1 c1
20 grosse Sekunde aufwärts d1 e1
21 grosse Septime abwärts d1 d#0
22 grosse Septime aufwärts d1 c#2
23 kleine Sekunde abwärts d1 c#1
24 kleine Sekunde aufwärts d1 d#1 

Auf d#1
1 Oktave abwärts d#1 d#0
2 Okatve aufwärts d#1 d#2
3 Quinte abwärts d#1 g#0
4 Quinte aufwärts d#1 a#1
5 Quarte abwärts d#1 a#0
6 Quarte aufwärts d#1 g#1
7 kleine Terz abwärts d#1 c1
8 grosse Terz aufwärts d#1 g1
9 grosse Terz abwärts d#1 h0
10 kleine Terz aufwärts d#1 f#1
12 kleine Sexte abwärts d#1 g0
12 grosse Sexte aufwärts d#1 c2
13 grosse Sexte abwärts d#1 f#0
14 kleine Sexte aufwärts d#1 h1
15 Tritonus abwärts d#1 a0
16 Tritonus aufwärts d#1 a1
17 kleine Septime abwärts d#1 f0
18 kleine Septime aufwärts d#1 c#2
19 grosse Sekunde abwärts d#1 c#1
20 grosse Sekunde aufwärts d#1 f1
21 grosse Septime abwärts d#1 e0
22 grosse Septime aufwärts d#1 d2
23 kleine Sekunde abwärts d#1 d1
24 kleine Sekunde aufwärts d#1 e1 


Auf e1
1 Oktave abwärts e1 e0
2 Okatve aufwärts e1 e2
3 Quinte abwärts e1 a0
4 Quinte aufwärts e1 h1
5 Quarte abwärts e1 h0
6 Quarte aufwärts e1 a1
7 kleine Terz abwärts e1 c#1
8 grosse Terz aufwärts e1 g#1
9 grosse Terz abwärts e1 c1
10 kleine Terz aufwärts e1 g1
12 kleine Sexte abwärts e1 g#0
12 grosse Sexte aufwärts e1 c#2
13 grosse Sexte abwärts e1 g0
14 kleine Sexte aufwärts e1 c2
15 Tritonus abwärts e1 a#0
16 Tritonus aufwärts e1 a#1
17 kleine Septime abwärts e1 f#0
18 kleine Septime aufwärts e1 d2
19 grosse Sekunde abwärts e1 d1
20 grosse Sekunde aufwärts e1 f#1
21 grosse Septime abwärts e1 f0
22 grosse Septime aufwärts e1 d#2
23 kleine Sekunde abwärts e1 d#1
24 kleine Sekunde aufwärts e1 f1 

Auf f1
1 Oktave abwärts f1 f0
2 Okatve aufwärts f1 f2
3 Quinte abwärts f1 a#0
4 Quinte aufwärts f1 c2
5 Quarte abwärts f1 c1
6 Quarte aufwärts f1 a#1
7 kleine Terz abwärts f1 d1
8 grosse Terz aufwärts f1 a1
9 grosse Terz abwärts f1 c#1
10 kleine Terz aufwärts f1 g#1
12 kleine Sexte abwärts f1 a0
12 grosse Sexte aufwärts f1 d2
13 grosse Sexte abwärts f1 g#0
14 kleine Sexte aufwärts f1 c#2
15 Tritonus abwärts f1 h0
16 Tritonus aufwärts f1 h1
17 kleine Septime abwärts f1 g0
18 kleine Septime aufwärts f1 d#2
19 grosse Sekunde abwärts f1 d#1
20 grosse Sekunde aufwärts f1 g1
21 grosse Septime abwärts f1 f#0
22 grosse Septime aufwärts f1 e2
23 kleine Sekunde abwärts f1 e1
24 kleine Sekunde aufwärts f1 f#1 

Auf f#1
1 Oktave abwärts f#1 f#0
2 Okatve aufwärts f#1 f#2
3 Quinte abwärts f#1 h0
4 Quinte aufwärts f#1 c#2
5 Quarte abwärts f#1 c#1
6 Quarte aufwärts f#1 h1
7 kleine Terz abwärts f#1 d#1
8 grosse Terz aufwärts f#1 a#1
9 grosse Terz abwärts f#1 d1
10 kleine Terz aufwärts f#1 a1
12 kleine Sexte abwärts f#1 a#0
12 grosse Sexte aufwärts f#1 d#2
13 grosse Sexte abwärts f#1 a0
14 kleine Sexte aufwärts f#1 d2
15 Tritonus abwärts f#1 c1
16 Tritonus aufwärts f#1 c2
17 kleine Septime abwärts f#1 g#0
18 kleine Septime aufwärts f#1 e2
19 grosse Sekunde abwärts f#1 e1
20 grosse Sekunde aufwärts f#1 g#1
21 grosse Septime abwärts f#1 g0
22 grosse Septime aufwärts f#1 f2
23 kleine Sekunde abwärts f#1 f1
24 kleine Sekunde aufwärts f#1 g1 


Auf g1
1 Oktave abwärts g1 g0
2 Okatve aufwärts g1 g2
3 Quinte abwärts g1 c1
4 Quinte aufwärts g1 d2
5 Quarte abwärts g1 d1
6 Quarte aufwärts g1 c2
7 kleine Terz abwärts g1 e1
8 grosse Terz aufwärts g1 h1
9 grosse Terz abwärts g1 d#1
10 kleine Terz aufwärts g1 a#1
12 kleine Sexte abwärts g1 h0
12 grosse Sexte aufwärts g1 e2
13 grosse Sexte abwärts g1 a#0
14 kleine Sexte aufwärts g1 d#2
15 Tritonus abwärts g1 c#1
16 Tritonus aufwärts g1 c#2
17 kleine Septime abwärts g1 a0
18 kleine Septime aufwärts g1 f2
19 grosse Sekunde abwärts g1 f1
20 grosse Sekunde aufwärts g1 a1
21 grosse Septime abwärts g1 g#0
22 grosse Septime aufwärts g1 f#2
23 kleine Sekunde abwärts g1 f#1
24 kleine Sekunde aufwärts g1 g#1 

Auf g#1
1 Oktave abwärts g#1 g#0
2 Okatve aufwärts g#1 g#2
3 Quinte abwärts g#1 c#1
4 Quinte aufwärts g#1 d#2
5 Quarte abwärts g#1 d#1
6 Quarte aufwärts g#1 c#2
7 kleine Terz abwärts g#1 f1
8 grosse Terz aufwärts g#1 c2
9 grosse Terz abwärts g#1 e1
10 kleine Terz aufwärts g#1 h1
12 kleine Sexte abwärts g#1 c1
12 grosse Sexte aufwärts g#1 f2
13 grosse Sexte abwärts g#1 h0
14 kleine Sexte aufwärts g#1 e2
15 Tritonus abwärts g#1 d1
16 Tritonus aufwärts g#1 d2
17 kleine Septime abwärts g#1 a#0
18 kleine Septime aufwärts g#1 f#2
19 grosse Sekunde abwärts g#1 f#1
20 grosse Sekunde aufwärts g#1 a#1
21 grosse Septime abwärts g#1 a0
22 grosse Septime aufwärts g#1 g2
23 kleine Sekunde abwärts g#1 g1
24 kleine Sekunde aufwärts g#1 a1 

Auf a1
Oktave abwärts a1 a0
2 Okatve aufwärts a1 a2
3 Quinte abwärts a1 d1
4 Quinte aufwärts a1 e2
5 Quarte abwärts a1 e1
6 Quarte aufwärts a1 d2
7 kleine Terz abwärts a1 f#1
8 grosse Terz aufwärts a1 c#2
9 grosse Terz abwärts a1 f1
10 kleine Terz aufwärts a1 c2
12 kleine Sexte abwärts a1 c#1
12 grosse Sexte aufwärts a1 f#2
13 grosse Sexte abwärts a1 c1
14 kleine Sexte aufwärts a1 f2
15 Tritonus abwärts a1 d#1
16 Tritonus aufwärts a1 d#2
17 kleine Septime abwärts a1 h0
18 kleine Septime aufwärts a1 g2
19 grosse Sekunde abwärts a1 g1
20 grosse Sekunde aufwärts a1 h1
21 grosse Septime abwärts a1 a#0
22 grosse Septime aufwärts a1 g#2
23 kleine Sekunde abwärts a1 g#1
24 kleine Sekunde aufwärts a1 a#1 


Auf a#1
Oktave abwärts a#1 a#0
2 Okatve aufwärts a#1 a#2
3 Quinte abwärts a#1 d#1
4 Quinte aufwärts a#1 f2
5 Quarte abwärts a#1 f1
6 Quarte aufwärts a#1 d#2
7 kleine Terz abwärts a#1 g1
8 grosse Terz aufwärts a#1 d2
9 grosse Terz abwärts a#1 f#1
10 kleine Terz aufwärts a#1 c#2
12 kleine Sexte abwärts a#1 d1
12 grosse Sexte aufwärts a#1 g2
13 grosse Sexte abwärts a#1 c#1
14 kleine Sexte aufwärts a#1 f#2
15 Tritonus abwärts a#1 e1
16 Tritonus aufwärts a#1 e2
17 kleine Septime abwärts a#1 c1
18 kleine Septime aufwärts a#1 g#2
19 grosse Sekunde abwärts a#1 g#1
20 grosse Sekunde aufwärts a#1 c2
21 grosse Septime abwärts a#1 h0
22 grosse Septime aufwärts a#1 a2
23 kleine Sekunde abwärts a#1 a1
24 kleine Sekunde aufwärts a#1 h1 


Auf h1
Oktave abwärts h1 h0
2 Okatve aufwärts h1 h2
3 Quinte abwärts h1 e1
4 Quinte aufwärts h1 f#2
5 Quarte abwärts h1 f#1
6 Quarte aufwärts h1 e2
7 kleine Terz abwärts h1 g#1
8 grosse Terz aufwärts h1 d#2
9 grosse Terz abwärts h1 g1
10 kleine Terz aufwärts h1 d2
12 kleine Sexte abwärts h1 d#1
12 grosse Sexte aufwärts h1 g#2
13 grosse Sexte abwärts h1 d1
14 kleine Sexte aufwärts h1 g2
15 Tritonus abwärts h1 f1
16 Tritonus aufwärts h1 f2
17 kleine Septime abwärts h1 c#1
18 kleine Septime aufwärts h1 a2
19 grosse Sekunde abwärts h1 a1
20 grosse Sekunde aufwärts h1 c#2
21 grosse Septime abwärts h1 c1
22 grosse Septime aufwärts h1 a#2
23 kleine Sekunde abwärts h1 a#1
24 kleine Sekunde aufwärts h1 c2 

	*/
