package SOUND;

import java.util.Vector;

public class ScoGenerator {

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public static Vector getHeader(boolean fof) {
		int generator1 = 1;
		int tempo = 150;
		Vector header = new Vector();
		header.addElement("; sco************* ");
		if (!fof) { // For Oszi
		    header.addElement("f1 0 2048 10 1");
		    header.addElement("; Instr. Beginn Dauer Frequenz Lautstaerke Verteilung"); 
		}
		else {
		    header.addElement("; Oszi in FOF mode !");
		    header.addElement("f1	0	8192	-7   0 8192 0");
		    header.addElement("f2   	0   	8192   	-7   0 8192 .07");
		    header.addElement("f3	0   	8192   	-7   0 8192 .11");
		    header.addElement("f4	0   	8192   	-7   0 8192 .37");
		    header.addElement("f5	0   	8192   	-7   0 8192 .43");
		    header.addElement("f6	0   	8192   	-7   0 8192 0");
		    header.addElement("f7	0   	8192   	-7   0 8192 -.07");  
		    header.addElement("f8	0   	8192   	-7   0 8192 -.11"); 
		    header.addElement("f9	0   	8192   	-7   0 8192 -.37"); 
		    header.addElement("f10 	0   	8192   	-7   0 8192 -.43");
		    header.addElement("f18 	0   	8192  	10   1");
		    header.addElement("f19 	0   	1024  	19   .5   .5   270   .5");
		    header.addElement("; Instr. Beginn Dauer Frequenz Lautstaerke Verteilung Stimmen"); 
		}
		return header;
    }

    /**
     * The .orc file
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Vector getOrc(boolean fof, Defaults def) {
		Vector orc = new Vector();
		if (!fof) { // For Oszi
		    orc.addElement(";orc*************");
		    orc.addElement("nchnls = 2 ");
		    orc.addElement("instr 1 ");
		    orc.addElement("idur = p3; Dauer ");
		    orc.addElement("ifreq = p4; Frequenz ");
		    orc.addElement("iamp = p5; Lautstaerke ");
		    orc.addElement("ich1 = p6; Kanal1 ");
		    orc.addElement("ich2 = 1-p6; Kanal2 ");
		    orc.addElement("ifact pow .00125/p3, .6; Steigung ");
		    orc.addElement("ifact = (p3 < .0039686 ? .5 : ifact) ");
		    orc.addElement("kenv linseg 0, idur*ifact, 1, idur*(1-2*ifact), 1, idur*ifact, 0 ");
		    orc.addElement("aosc oscili ampdb(iamp)*kenv, ifreq, 1 ");
		    orc.addElement("outs aosc*ich1, aosc*ich2 ");
		    orc.addElement("endin ");
		    if (def.doMelody) {
		    	orc.addElement("; orc*************  ");
		    	orc.addElement("nchnls = 2  ");
		    	orc.addElement("instr 7  ");
		    	orc.addElement("idur = p3; Dauer ");
		    	orc.addElement("ifreq = p4; Frequenz ");
		    	orc.addElement("iamp = p5; Lautstaerke ");
		    	orc.addElement("ich1 = p6; Kanal1 ");
		    	orc.addElement("ich2 = 1-p6; Kanal2 ");
		    	orc.addElement("ifact pow .00125/p3, .6; Steigung ");
		    	orc.addElement("ifact = (p3 < .0039686 ? .5 : ifact)  ");
		    	orc.addElement("kenv linseg 0, idur*ifact, 1, idur*(1-2*ifact), 1, idur*ifact, 0 ");
		    	orc.addElement("aosc oscili ampdb(iamp)*kenv, ifreq, 1  ");
		    	orc.addElement("outs aosc*ich1, aosc*ich2 ");
		    	orc.addElement(" endin ;*********** ");
		    }
	
		}
		else {// For FOF
		    orc.addElement("; FOF***********************");
		    orc.addElement("nchnls = 2");
		    orc.addElement("instr 1");
	
		    orc.addElement("itotdur init p3 ; Dauer");
		    orc.addElement("ifund init p4 ; Grundton");
		    orc.addElement("iamp init p5 ; Amplitude");
		    orc.addElement("irate init p6 ; Panning");
		    orc.addElement("itab init 1 ; Kformmodtabelle");
	
		    orc.addElement("; Umfang von iamp in Abhaengigkeit von ifund");
	
		    orc.addElement("iamp = (iamp >= 85 && ifund <= 20 ? 85 : iamp)");
		    orc.addElement("iamp = (iamp >= 83 && ifund > 20 && ifund <= 100 ? 83 : iamp)");
		    orc.addElement("iamp = (iamp >= 81 && ifund > 100 && ifund <=300 ? 81 : iamp)");
		    orc.addElement("iamp = (iamp >= 80 && ifund > 300 ? 80 : iamp)");
	
		    orc.addElement("iamp = (iamp <= 65 ? 65 : iamp)");
	
		    orc.addElement("; Tabellenzuordnung in Abhaengigkeit von der itotdur und iamp");
	
		    orc.addElement("if irate > .35 && irate < .65 || itotdur < 1 igoto keiniformmod");
	
		    orc.addElement("itab = (iamp >= 80 ? 2 : itab)");
		    orc.addElement("itab = (iamp >= 75 && iamp < 80 ? 3 : itab)");
		    orc.addElement("itab = (iamp >= 68 && iamp < 75 ? 4 : itab)");
		    orc.addElement("itab = (iamp <= 68 ? 5 : itab)");
	
		    orc.addElement("itab = (irate > .65 ? itab + 5 : itab)");
	
		    orc.addElement("keiniformmod:");
	
		    orc.addElement("; bestimme selbst!");
		    orc.addElement("icompose = 350");
	
		    orc.addElement("; Operatoren ");
		    orc.addElement("iop1 = ifund^.07");
		    orc.addElement("iop2 = (2 * ifund^.4 - 7 * ifund^.51 + icompose / ifund^.07) + 1.001^ifund");
		    orc.addElement("iop3 = -(1.5492 * irate - .7745)^2 + 1; je extremer irate desto leiser");
		    orc.addElement("iop4 = 1.00042^ifund");
		    orc.addElement("iop4 = ((ifund > 300 && ifund < 500 || ifund > 700 && ifund < 900) && itotdur >= 2&& irate <.35 || itotdur >= 2 && ifund > 1800 && irate >.65 ? iop4 : 1) ");
		    orc.addElement("iop5 = (irate < .15 || irate >. 85) ? (ifund-.89)^.6 + 1 / (ifund-.89)^2 + 3  : (ifund-.8)^.89  + 1 / (ifund-.89)^2 + 3");
		    orc.addElement("iop5 = (ifund > 2 && iop5 >= 8 ? 8 : iop5)");
	
		    orc.addElement("iform = iop1 * ifund + iop2 ; Formantfrequenz");
		    orc.addElement("ioct = 0");
		    orc.addElement("iband = 40");
		    orc.addElement("iris = .003  + 1 / (iop5 * ifund^2.8) ; Huellkurvenparamenter fuer die Grains");
		    orc.addElement("idur = .02");
		    orc.addElement("idec = .007");
	
		    orc.addElement("ifna = 18");
		    orc.addElement("ifnb = 19");
		    orc.addElement("iolaps = int(ifund*idur) + 1");
	
		    orc.addElement("kformmod oscili iform/iop4, 1/itotdur, itab");
	
		    orc.addElement("ain  fof ampdb(iamp) * iop3 / iop1, ifund, iform + kformmod, ioct, iband, iris, idur, idec, iolaps, ifna, ifnb, itotdur");
	
		    orc.addElement("; Panning");
		    orc.addElement("klfo1 expon (1-irate), itotdur, irate");
		    orc.addElement("klfo2 expon irate, itotdur, (1-irate)");
		    orc.addElement("ain      = ain*klfo2");
		    orc.addElement("al       = ain*sqrt(klfo1)");
		    orc.addElement("ar       = ain*sqrt(1 - klfo1)");
	
		    orc.addElement("outs     al, ar");
	
		    orc.addElement("endin");
		}
		return orc;
    }	
}
