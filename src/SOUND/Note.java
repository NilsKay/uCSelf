package SOUND;

import java.util.*;

/**

 * This class Object desribes a Tonal Value for the C_Sound sco file
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Note {
    // Start from one tone to another: start_old + dauer
    // it is ok to have more than one note at the same time mark !
    public double start;	//When this tone will start
    public double dauer;	// how long it lasts
    public double freq;		// the frequency
    public int generator;	// The used generator function
    public int amplitude;	// the amplitude to make the tone loud or not
    public int envelope;	// the used envelope function for this
    public double balance;	// balance between the 2 stereo channels
    // Project3 
    public double v1a;        // 2 times vibrato, amplitude, speed (4 Entrys)
    public double v1s;
    public double v2a;        
    public double v2s;
    public double iokt;
    public double t1,t2,t3;// Splitter
    public int iform,iband;

    public Note(double s, double d, double b, double freq, int gen, int amp, int env) {
	this.start = s;
	this.dauer = d;
	this.freq = freq;
	this.generator = gen;
	this.amplitude = amp;
	this.envelope = env;
	this.balance = b;
    }
} // end of class





