package SOUND;
import java.util.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.*;
import java.text.*;

import javax.swing.JFrame;

import Utils.Converter;
import Utils.GetEnviroment;
import Utils.Utils;
/**
 * This class (deflts = Defaults) saves User settings for the next time that we start
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Defaults {
	int maxText = 140;
	String text[] = new String[maxText];
    // ---------- All the variables from the ui-mask :
	public int max_freq, min_freq, lang;
	public int population, fittest;
	public int interval, iterations, diff_freq, delay;
	public double length, r_Weight, degression;
	public double impact;
	public boolean tonal, stereo, preferLowerNotes, useSelectNotes = false, akkord, pedal;
	public boolean fof;	// To switch between Input panels
	public boolean newFOF;	// To modify just the Oszi to behave like FOF
    // Other vars, also saved as Properties:
    public String lastPath = null;
    public String min_amp, max_amp;	// pp, p, mp, mf, f, ff 
    public final int voiceMax = 7;
    public final int melodyChannels = 1;
    public double amplify_amp;
    public double min_tempo, max_tempo;
    public int duration_step_index = 0;	// 0 = free
    public int min_voice = 1, max_voice=1;
    public int seed, mode;
    // Project3 values:
    double vibrato1_ampl_min, vibrato1_ampl_max;
    double vibrato1_speed_min, vibrato1_speed_max;
    double vibrato2_ampl_min, vibrato2_ampl_max;
    double vibrato2_speed_min, vibrato2_speed_max; 
    double iOkt_min, iOkt_max;
    double splitter_t1_min, splitter_t1_max;
    double splitter_t2_min, splitter_t2_max;
    double splitter_t3_min, splitter_t3_max;
    double gamma;
    int IForm_min, IForm_max;
    int iBand_min, iBand_max;
    Vector<OneNote> selectedNotes;
    Vector<Integer> channels; 
    //--- State machine ----------
    public int range, trigger, cascadeCount, stepUp, stepDown;
    public int loopDepth, loopRepeat;
    public int minTempo, maxTempo, speedStep;
    public boolean useLoop, useSpeed, useCascade, permutation, doLoopBug, doMelody, useTrans, doDelay;
    public int m_range, m_trigger;
    public int transP_min, transP_max, minOct, maxOct;
    
    String[] durationTable = new String[]{
    	"free", "1/16", "1/8", "1/4", "1/2", "1", "2"
    };
    int[] durations = new int[] {
    	-1, 250, 500, 1000, 2000, 4000, 8000	
    };
/**
 * This Method loads the saved Default values from .motor.def
 * @param path which default file to use
 */
public boolean loadDef(String path, int build) {
	System.out.println("Def path="+path);
	this.text = getText(build);
    String res0, res1;
    File fdes;
    BufferedReader br = null;
    boolean firstChannel = false;
    //double d;
    int q, ii;
    this.selectedNotes = new Vector<OneNote>();
    // set default, overwrite if possible
    this.channels = new Vector<Integer>();
    for(int i = 0; i < (voiceMax+melodyChannels); i++) {
    	this.channels.addElement(new Integer(10+i*3)); // Old default
    }
    minOct = 0;
    maxOct = 5;
    transP_min = -3;
    transP_max = 3;
    m_range = 2;
    m_trigger = 3;
    range = 3;
    trigger = 3;
    cascadeCount = 5;
    stepUp = 2;
    stepDown = 1;
    loopDepth = 6;
    loopRepeat = 1;
    this.minTempo = 30;
    this.maxTempo = 120;
    speedStep = 1;
    seed = 0;
    mode = 0;
    max_freq = 20000;
    lang = 0; // eng
    min_freq = 20;
    interval = 50;
    population = 200;
    fittest = 15;
    iterations = 50;
    delay = 0;
    diff_freq = 250;
    r_Weight = 20;
    degression = 0.0;
    tonal = false;
    akkord = false;
    pedal = false;
    preferLowerNotes = true;
    stereo = true;
    fof = false;
    newFOF = false;
    impact = 200;
    min_amp = Project2.Loudness[0];
    max_amp = Project2.Loudness[Project2.Loudness.length - 1];
    amplify_amp = 1.0;
    min_tempo = 0.5;
    max_tempo = 1.0;
    duration_step_index = 0; //free
    gamma = 0.6;
    vibrato1_ampl_min = 0.0;
    vibrato1_ampl_max = 20.0;
    vibrato1_speed_min = 0.0;
    vibrato1_speed_max = 1;
    vibrato2_ampl_min = 0.0;
    vibrato2_ampl_max = 20;
    vibrato2_speed_min = 0.0;
    vibrato2_speed_max = 1.0; 
    iOkt_min = 0.0;
    iOkt_max = 2.0;
    splitter_t1_min = 0.0001;
    splitter_t1_max = 0.01;
    splitter_t2_min = 0.0001;
    splitter_t2_max = 0.01;
    splitter_t3_min = 0.0001; 
    splitter_t3_max = 0.01;
    IForm_min = 0;
    IForm_max = 650;
    iBand_min = 0; 
    iBand_max = 50;
    
    fdes = new File( path );   
    if (!fdes.exists()) {
	System.out.println("Can not load save file:"+path+" File does not exist !");
	return false;
    }
    try {
	br = new BufferedReader(new InputStreamReader(new FileInputStream(fdes)));
    } catch (java.io.FileNotFoundException e) {
	System.out.println("Exception "+e);
	return false;
    }
    
    try {
	while((res0 = br.readLine()) != null) {
	    if (!(res0.startsWith("#"))) { //no comment Line
		q = res0.indexOf('=');
		if (q >= 0) {	// Line with a = char

		    ii = res0.indexOf('\t'); // throw away all after a tab !
		    if (ii <= 0) ii = res0.indexOf('#'); // throw away all after a comment
		    if (ii <= 0) ii = res0.length(); // all of it
		    res1 = res0.substring(q+1,ii);
		    res0 = res0.substring(0,q);
		    // check which parameter this is:
		    //System.out.println("value=|"+res1+"|");
		    if (res0.equals( "LastPath")) {
			lastPath = res1;
			File f = new File(lastPath);
			if (!f.exists()) lastPath = null; // invalid entry
		    }
		    else if (res0.equals("MAXFREQUENCY")) this.max_freq = Converter.getInt(res1, 20000); 
		    else if (res0.equals("MINFREQUENCY")) this.min_freq = Converter.getInt(res1, 20); 
		    else if (res0.equals("SEED")) this.seed = Converter.getInt(res1, 0);
		    else if (res0.equals("range")) this.range = Converter.getInt(res1, 0);
		    else if (res0.equals("trigger")) this.trigger = Converter.getInt(res1, 0);
		    else if (res0.equals("m_range")) this.m_range = Converter.getInt(res1, 0);
		    
		    else if (res0.equals("transP_min")) this.transP_min = Converter.getInt(res1, -3);
		    else if (res0.equals("transP_max")) this.transP_max = Converter.getInt(res1, 3);
		    else if (res0.equals("minOct")) this.minOct = Converter.getInt(res1, 0);
		    else if (res0.equals("maxOct")) this.maxOct = Converter.getInt(res1, 5);
		    
		    else if (res0.equals("m_trigger")) this.m_trigger = Converter.getInt(res1, 0);
		    else if (res0.equals("cascadeCount")) this.cascadeCount = Converter.getInt(res1, 0);
		    else if (res0.equals("stepUp")) this.stepUp = Converter.getInt(res1, 0);
		    else if (res0.equals("stepDown")) this.stepDown = Converter.getInt(res1, 0);
		    else if (res0.equals("loopDepth")) this.loopDepth = Converter.getInt(res1, 6);
		    else if (res0.equals("loopRepeat")) this.loopRepeat = Converter.getInt(res1, 1);
		    else if (res0.equals("minSTempo")) this.minTempo = Converter.getInt(res1, 10);
		    else if (res0.equals("maxSTempo")) this.maxTempo = Converter.getInt(res1, 120);
		    else if (res0.equals("speedStep")) this.speedStep = Converter.getInt(res1, 1);
		    else if (res0.equals("MODE")) this.mode = Converter.getInt(res1, 0);
		    else if (res0.equals("POPULATION")) this.population = Converter.getInt(res1, 200); 
		    else if (res0.equals("FITTEST")) this.fittest = Converter.getInt(res1, 30); 
		    else if (res0.equals("INTERVAL")) this.interval = Converter.getInt(res1, 50); 
		    else if (res0.equals("MINVOICE")) this.min_voice = Converter.getInt(res1, 1);
		    else if (res0.equals("MAXVOICE")) this.max_voice = Converter.getInt(res1, 1);
		    else if (res0.equals("ITERATIONS")) this.iterations = Converter.getInt(res1, 20); 
		    else if (res0.equals("DELAY")) this.delay = Converter.getInt(res1, 0); 
		    else if (res0.equals("RANDOMWEIGHT")) {
			    this.r_Weight = Converter.getDouble(res1, 1.0);
				this.impact = this.r_Weight;
		    }
		    else if (res0.equalsIgnoreCase("language")) this.lang = Converter.getInt(res1, 0); 
		    
		    else if (res0.equalsIgnoreCase("DEGRESSION")) this.degression = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("DIFFFREQ")) this.diff_freq = Converter.getInt(res1, 250); 
		    else if (res0.equals("TONAL")) this.tonal = Converter.checkState(res1);
		    else if (res0.equals("useSpeed")) this.useSpeed = Converter.checkState(res1);
		    else if (res0.equals("useTrans")) this.useTrans = Converter.checkState(res1);
		    
		    else if (res0.equals("useCascade")) this.useCascade = Converter.checkState(res1);
		    else if (res0.equals("permutation")) this.permutation = Converter.checkState(res1);
		    else if (res0.equals("doLoopBug")) this.doLoopBug = Converter.checkState(res1);
		    else if (res0.equals("useLoop")) this.useLoop = Converter.checkState(res1);
		    else if (res0.equals("doMelody")) this.doMelody = Converter.checkState(res1);
		    else if (res0.equals("doDelay")) this.doDelay = Converter.checkState(res1);
		    
		    else if (res0.equals("akkord")) this.akkord = Converter.checkState(res1);
		    else if (res0.equals("pedal")) this.pedal = Converter.checkState(res1);
		    else if (res0.equalsIgnoreCase("duration_step_index")) this.duration_step_index =
									       Converter.getInt(res1, 0); 
		    else if (res0.equals("preferLowerNotes")) this.preferLowerNotes = Converter.checkState(res1);
		    //else if (res0.equals("FOF")) this.fof = Converter.checkState(res1); // out since V1.7a1, use modified oszi instaed
		    else if (res0.equals("useSelectNotes")) this.useSelectNotes = Converter.checkState(res1);
		    else if (res0.equals("newFOF")) this.newFOF = Converter.checkState(res1);
		    else if (res0.equals("STEREO")) this.stereo = Converter.checkState(res1);
		    else if (res0.equals("MINAMP")) min_amp = res1;
		    else if (res0.equals("MAXAMP")) max_amp = res1;
		    else if (res0.equals("AMPLIFYAMP")) amplify_amp = Converter.getDouble(res1, 1.0);
		    else if (res0.equals("MINTEMPO")) min_tempo = Converter.getDouble(res1, 0.5);
		    else if (res0.equals("GAMMA")) gamma = Converter.getDouble(res1, 0.6);
		    else if (res0.equals("MAXTEMPO")) max_tempo = Converter.getDouble(res1, 1.0);

		    else if (res0.equals("VIBRATO1_AMPL_MIN")) vibrato1_ampl_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("VIBRATO1_AMPL_MAX")) vibrato1_ampl_max = Converter.getDouble(res1, 40.0);
		    else if (res0.equals("VIBRATO1_SPEED_MIN")) vibrato1_speed_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("VIBRATO1_SPEED_MAX")) vibrato1_speed_max = Converter.getDouble(res1, 1.0);
		    else if (res0.equals("VIBRATO2_AMPL_MIN")) vibrato2_ampl_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("VIBRATO2_AMPL_MAX")) vibrato2_ampl_max = Converter.getDouble(res1, 40.0);
		    else if (res0.equals("VIBRATO2_SPEED_MIN")) vibrato2_speed_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("VIBRATO2_SPEED_MAX")) vibrato2_speed_max = Converter.getDouble(res1, 1.0);
		    else if (res0.equals("IOKT_MIN")) iOkt_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("IOKT_MAX")) iOkt_max = Converter.getDouble(res1, 5.0);
		    else if (res0.equals("SPLITTER_T1_MIN")) splitter_t1_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("SPLITTER_T1_MAX")) splitter_t1_max = Converter.getDouble(res1, 1.0);
		    else if (res0.equals("SPLITTER_T2_MIN")) splitter_t2_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("SPLITTER_T2_MAX")) splitter_t2_max = Converter.getDouble(res1, 1.0);
		    else if (res0.equals("SPLITTER_T3_MIN")) splitter_t3_min = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("SPLITTER_T3_MAX")) splitter_t3_max = Converter.getDouble(res1, 1.0);

		    else if (res0.equals("IFORM_MIN")) IForm_min = Converter.getInt(res1, 0);
		    else if (res0.equals("IFORM_MAX")) IForm_max = Converter.getInt(res1, 1000);
		    else if (res0.equals("IBAND_MIN")) iBand_min = Converter.getInt(res1, 0);
		    else if (res0.equals("IBAND_MAX")) iBand_max = Converter.getInt(res1, 40);
		    else if (res0.equals("ONENOTE")) {
		    	String[] sep = new Utils().getSeparatedValues(res1, ';');
		    	if (sep != null && sep.length > 1) {
		    		res1 = sep[0];
		    	}
		    		/*OneNote on = new OneNote(Converter.getDouble(sep[0], 0),
		    				sep[1], 
		    				Converter.getInt(sep[2], 0));
		    		this.selectedNotes.addElement(on);
		    	}
		    	*/
		    	double f = Converter.getDouble(res1, 0.0);
		    	OneNote on = new OneNote(f, 0, 0);
		    	on.getMidiNote();
		    	this.selectedNotes.addElement(on);
		    }
		    else if (res0.equals("CHANNEL")) {
		    	if (!firstChannel) {
		    		firstChannel = true;
		    		this.channels = null;
		    	}
		    	if (this.channels == null)
		    		this.channels = new Vector<Integer>();
		    	int ic = Converter.getInt(res1.trim(), 0);
		    	this.channels.addElement(new Integer(ic));
		    }
		}
	    }
	} // end of while
    } // end of try
    catch (java.io.IOException e) {
	System.out.println("loadDef in Defaults.java : "+e);
	
	return false;
    }
    while(channels.size() < (melodyChannels + this.voiceMax)) {
    	channels.addElement(new Integer(0));
    }
    //System.out.println("Default.load() (2) : amplify_amp = "+this.amplify_amp);
    this.text = getText(build);
    return true;
}// end of method

/**
 * This Method saves some default values.
 * @param path wich file to save
 * @param user the name of the user
 */
@SuppressWarnings("unused")
public boolean saveDef(String path, String user) {
    //Date actualTime = null;
    Calendar rightNow;
    double d;
    String wert;
    int n;
    File fdes;
    long millis;
    //System.out.println("Deflts: saveDefaults. path="+path+" fof="+fof);
    GregorianCalendar greg = new GregorianCalendar();
    SimpleDateFormat datef = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    millis = new Date().getTime();
    wert = datef.format(new Date(millis)); // actualTime
    fdes = new File( path );   
    n = 0; 	// we use the first of all images in a job as default
    try {
	System.out.println("Save "+path+" iterations="+this.iterations+" tonale="+this.tonal+" RANDOMWEIGHT="+this.r_Weight);
	PrintWriter prs = new PrintWriter(new FileOutputStream(fdes));
	// Headder Information
	prs.println("# Default save File for JcSelf.");
	prs.println("# File was generated by user "+user);
	prs.println("# , written "+ wert);
	prs.println("# ----------- JcSelf Default:");
	prs.println("LastPath="+this.lastPath);
	
	prs.println("Language="+this.lang+"	# 0=eng 1=ger");
	prs.println("MODE="+this.mode+"	# 0=2d, 1=3d, 2= My3d");
	prs.println("MAXFREQUENCY="+this.max_freq+"	# Maximal used frequency");
	prs.println("MINFREQUENCY="+this.min_freq+"	# Minimal used frequency");
	prs.println("SEED="+this.seed+"	# Initial frequency 0 = use random");
	prs.println("INTERVAL="+this.interval+"		# The step between 2 table entry's");
	prs.println("MINVOICE="+this.min_voice+"		# min. amount voices");
	prs.println("MAXVOICE="+this.max_voice+"		# max. amount voices");
	prs.println("POPULATION="+this.population+"	# Number of random frequencys per Iteration");
	prs.println("FITTEST="+this.fittest+"	# Choose from these with the highest weight");
	prs.println("DELAY="+this.delay+"	# ");
	prs.println("ITERATIONS="+this.iterations+"	# ");
	prs.println("RANDOMWEIGHT="+this.r_Weight+"	# how much the weight will be influenced by the random function.");
	prs.println("DEGRESSION="+this.degression+"	# modify the random weight.");
	prs.println("DIFFFREQ="+this.diff_freq+"	# Frequency area influenced by one impact");
	prs.println("TONAL="+this.tonal+"	# If true, use just tonel notes, else all possible frequencys");
	prs.println("akkord="+this.akkord+"	# If true, generate an akkord on the first voice");
	prs.println("pedal="+this.pedal+"	# If true, use a pedal to modify duration on the first voice");
	
	prs.println("preferLowerNotes="+preferLowerNotes+"	# If true, use the same frequency distribution as with non tonal, if fase, prefer lower notes !");
	prs.println("useSelectNotes="+useSelectNotes+"	# If true, use only the selected frequencies !");
	prs.println("FOF="+this.fof+"	# if true, work in FOF mode, else OSZI ");
	prs.println("newFOF="+this.newFOF+"	# if true, work OSZI in FOF mode");
	prs.println("STEREO="+this.stereo+"	# if true, use stereo effects");
	prs.println("MINAMP="+this.min_amp+"	# Minimal amplitude, use pp, p, mp, mf, f, ff");
	prs.println("MAXAMP="+this.max_amp+"	# Maximal amplitude, use pp, p, mp, mf, f, ff");
	prs.println("AMPLIFYAMP="+this.amplify_amp+"	# factor on the weight to incease/decrease amplitude differences");
	prs.println("MINTEMPO="+this.min_tempo+"	# minimal duration of a tone");
	prs.println("MAXTEMPO="+this.max_tempo+"	# maximal duration of a tone");
	prs.println("duration_step_index="+duration_step_index+"	# If duration is free or in steps."); 
	prs.println("GAMMA="+this.gamma+"	# How voices are influenced");
	prs.println("VIBRATO1_AMPL_MIN="+vibrato1_ampl_min+" # Vibrato 1 minimal Amplitude"); 
	prs.println("VIBRATO1_AMPL_MAX="+ vibrato1_ampl_max+" # Vibrato 1 maximal Amplitude");
	prs.println("VIBRATO1_SPEED_MIN="+ vibrato1_speed_min+" # Vibrato 1 minimal speed");
	prs.println("VIBRATO1_SPEED_MAX="+ vibrato1_speed_max+"	# Vibrato 1 maximal speed");
	prs.println("VIBRATO2_AMPL_MIN="+ vibrato2_ampl_min+" # Vibrato 2 minimal Amplitude");
	prs.println("VIBRATO2_AMPL_MAX="+ vibrato2_ampl_max+" # Vibrato 2 maximal Amplitude");
	prs.println("VIBRATO2_SPEED_MIN="+ vibrato2_speed_min+" # Vibrato 2 minimal speed");
	prs.println("VIBRATO2_SPEED_MAX="+ vibrato2_speed_max +" # Vibrato 2 minimal speed");
	prs.println("IOKT_MIN="+ iOkt_min+" # minimal IOktave value");
	prs.println("IOKT_MAX="+ iOkt_max+" # maximal IOktave value");
	prs.println("SPLITTER_T1_MIN="+ splitter_t1_min+" # Splitter rise time minimum");
	prs.println("SPLITTER_T1_MAX="+ splitter_t1_max+" # Splitter rise time maximum");
	prs.println("SPLITTER_T2_MIN="+ splitter_t2_min+" # Splitter duration time minimum");
	prs.println("SPLITTER_T2_MAX="+ splitter_t2_max+" # Splitter duration time maximum");
	prs.println("SPLITTER_T3_MIN="+ splitter_t3_min+" # Splitter down time minimum");
	prs.println("SPLITTER_T3_MAX="+ splitter_t3_max+" # Splitter down time maximum");
	prs.println("IFORM_MIN="+ IForm_min+" # Iform factor minimum");
	prs.println("IFORM_MAX="+ IForm_max+" # Iform factor maximum");
	prs.println("IBAND_MIN="+ iBand_min+" # IBand minimum");
	prs.println("IBAND_MAX="+ iBand_max+" # IBand maximum");
	if (this.selectedNotes.size() > 0) {
		for (int q = 0; q < this.selectedNotes.size(); q++) {
			OneNote o = this.selectedNotes.elementAt(q);
			prs.println("ONENOTE="+o.freq);
		}
	}
	if (this.channels != null) {
		for (int q = 0; q < (this.voiceMax+melodyChannels); q++) {
			Integer I = this.channels.elementAt(q);
			prs.println("CHANNEL="+ I.intValue()+" # channel Instrument");
		}
	}
	prs.println("range="+this.range+"	# increase trigger count if the number of the selected freq. is below this");
	prs.println("trigger="+this.trigger+"	# start state machine if range was n-time hit");
	prs.println("useSpeed="+this.useSpeed+"	# If true, use the speed-state-machine");
	prs.println("useTrans="+this.useTrans+"	# If true, use the Transposition-state-machine");
	
	prs.println("useCascade="+this.useCascade+"	# If true, use the cascade-state-machine");
	prs.println("permutation="+this.permutation+"	# If true, vary the loop, else not");
	prs.println("doLoopBug="+this.doLoopBug+"	# If true, do buggy loops");
	prs.println("useLoop="+this.useLoop+"	# If true, use the loop-state-machine");
	prs.println("doMelody="+this.doMelody+"	# If true, use the melody option");
	prs.println("doDelay="+this.doDelay+"	# If true, allow melody-delay");
	
	prs.println("cascadeCount="+this.cascadeCount+"	# Number of steps to do the cascade");
	prs.println("stepUp="+this.stepUp+"	# Notes to go up");
	prs.println("stepDown="+this.stepDown+"	# Notes to go down");
	prs.println("loopDepth="+this.loopDepth+"	# How many notes for a loop");
	prs.println("loopRepeat="+this.loopRepeat+"	# How many repeats for loops");
	prs.println("minSTempo="+this.minTempo+"	# Minimal tempo change");
	prs.println("maxSTempo="+this.maxTempo+"	# Maximal tempo change");
	prs.println("speedStep="+this.speedStep+"	# How much to chan ge the speed");
	prs.println("m_range="+this.m_range+"	# melody: increase trigger count if the number of the selected freq. is below this");
	prs.println("m_trigger="+this.m_trigger+"	# melody: start state machine if range was n-time hit");
	prs.println("transP_min="+this.transP_min+"	# Transposition effect - number of notes");
	prs.println("transP_max="+this.transP_max+"	# Transposition effect + number of notes");
	prs.println("minOct="+this.minOct+"	# Transposition effect minimal Octave");
	prs.println("maxOct="+this.maxOct+"	# Transposition effect + maximal Octave");
	
	prs.close();

    }
    catch (java.io.IOException e) {
	System.out.println("SaveJob: "+e );
	return false; 	// Kann File nicht schreiben ! 
    } // End of IO Excep
    System.out.println("Deflts: saveDefaults was done in file:"+path); 
    return true;
}

public Vector<OneNote> clipNotes(Vector<OneNote> source) {
	Vector<OneNote> res = new Vector<OneNote>();
	for (int n = 0; n < source.size(); n++) {
		OneNote no = source.elementAt(n);
		if (no.freq >= this.min_freq && no.freq <= this.max_freq)
			res.addElement(no);
	}
	return res;
}
public String[] getText(int b) {
	String[] t = new String[maxText];
    t[0] = "JcSelf "+GetEnviroment.sVersionCode+" build "+b+" � Copyright by Nils Kay, Peter Heeren. All rights reserved (2000-2013)";
    t[1] = "Alarmbox"; 
    t[10] = "Do you really want to quit ?";
    t[11] = "File";
    t[12] = "Select Path";
    t[13] = "Quit";
    t[14] = "Generate";
    t[15] = "Select .sco file";
    t[16] = "Unable to save file:";
    t[17] = "Ready";
    t[18] = "Maximum Frequency:";
    t[19] = "Minimum Frequency:";
    t[20] = "Population:";
    t[21] = "Amount to select from:";	//"Number of Fittest:";
    t[22] = "Hit Impact:";
    t[23] = "Frequency Raster:";
    t[24] = "Minimum Duration:";
    t[25] = "Hz";
    t[26] = "sec";
    t[27] = "Random Weight:";
    t[28] = "Iterations:";
    t[29] = "Footprint:"; 
    t[30] = "12 Temperated Steps";	//"Use chromatic Notes";
    t[31] = "Abort";
    t[32] = "About";
    t[33] = "Options";
    t[34] = "CSound Editor";
    t[35] = "This program is used to generate an 'orchestra file' and a 'score file' for CSound.";
    t[36] = "The rule to generate one tone is based on random and evolution.";
    t[37] = "The distribution of the probabilities for the frequency values";
    t[38] = "changes self-structuring while the program is working."; 
    //t[39] = "In the end, with the correct parameters, a nearly constant note will appear";
    t[39] = "Idea : Peter Heeren (German Composer) www.peter-heeren.de";
    t[40] = "Program: written in 100% Java by Nils Kay (Engineer) nigeka@yahoo.de";
    t[42] = "Maximium Duration:";
    t[43] = "Stereo Effects";
    t[44] = "Minimum Velocity:";
    t[45] = "Maximum Velocity:";
    t[46] = "Seed Frequency:";
    t[47] = "FOF Komposer";
    t[48] = "OSZI Komposer";
    t[49] = "Area";
    t[50] = "Stochastic";
    t[51] = "Komposition";
    t[52] = "Min. Voices:";
    t[53] = "Max. Voices:";
    t[54] = "Gamma:";
    t[55] = "Save Template";
    t[56] = "Load Template";
    t[57] = "Select Template to load";
    t[58] = "Select Template to save";
    t[59] = "Unable to load template: ";
    t[60] = "Unable to save template: ";
    t[61] = "Actual settings have been modified.";
    t[62] = "Do you want to save the settings in a template ?";
    t[63] = "Play";
    t[64] = "Save Midi";
    t[65] = "Prefer Lower Notes";
    t[66] = "Since version 'V1.08 alpha1' there was some Midi-options added.";
    t[67] = "It is now possible to play the generated composition with JcSelf and once";
    t[68] = "it was played it can be saved as a '*.mid' file to be used by other programs";
    t[69] = "Unable to open Midi-Device. Device may be busy by another application !";
    t[70] = "Load";
    t[71] = "Duration Steps";
    t[72] = "Degression:";
    t[73] = "Select Valid Notes";
    t[74] = "Select";
    t[75] = "Add";
    t[76] = "Remove";
    t[77] = "Enter freqencies. E.g. 100; 150; 200";
    t[78] = "Close";
    t[79] = "Use seleced Freq.";
    t[80] = "Your selection had to be reduced to fit the Min-Max Frequency!";
    t[81] = "Source Frequency";
    t[82] = "Selected Frequency";
    t[83] = "Move frequency to selection.";
    t[84] = "Remove frequency from selection.";
    t[85] = "Your selection has less than two frequencys. The result will be rather dull...";
    t[86] = "OK";
    t[87] = "Cancel";
    t[88] = "Your selection has no frequencys, aborting...";
    t[89] = "Select Instruments...";
    t[90] = "No Soundbank found, Instrument Selection not possible !";
    t[91] = "Akkord";
    t[92] = "Shadow";
    t[93] = "Language";
    t[94] = "English";
    t[95] = "German";
    t[96] = "Display 2D";
    t[97] = "Display 3D";
    t[98] = "Restart the programm to make the change effective";
    t[99] = "Delay";
    t[100] = "msec";
    t[101] = "Range";
    t[102] = "Trigger";
    t[103] = "Up-Step";
    t[104] = "Down-Step";
    t[105] = "Amount";
    t[106] = "Kascade Effect";
    t[107] = "Loop Effect";
    t[108] = "Speed Effect";
    t[109] = "Setup State Machine...";
    t[110] = "The larger this number the more values are added to the trigger count value";
    t[111] = "Threshhold, at this value of the trigger count value the effect is triggered";
    t[112] = "Number of intervals where this effect is active";
    t[113] = "How many notes it goes up";
    t[114] = "How many notes it goes down";
    t[115] = "Loop Depth";
    t[116] = "Loop Repeats";
    t[117] = "How many intervals will be repeated";
    t[118] = "How may times the loop will be repeated";
    t[119] = "Min. Tempo";
    t[120] = "Max. Tempo";
    t[121] = "Step";
    t[122] = "How much the speed will be changed per trigger event";
    t[123] = "Permutation";
    t[124] = "If selected the loop will vary in speed, amplitude etc. but not in frequency";
    t[125] = "Do Loop Bug";
    t[126] = "Do Melody";
    t[127] = "Melody";
    t[128] = "Transposition";
    t[129] = "Minus Notes";
    t[130] = "Plus Notes";
    t[131] = "Min. Octave";
    t[132] = "Max. Octave";
    t[133] = "Free Melody";
//t[38] = "";
//t[36] = "";
    if (this.lang == 1)
    	t = getGermanText(b);
   
    return t;
}
public String[] getGermanText(int b) {
	String[] t = new String[maxText];
    t[0] = "JcSelf "+GetEnviroment.sVersionCode+" build "+b+" � Copyright by Nils Kay, Peter Heeren. All rights reserved (2000-2011)";
    t[1] = "Alarmbox"; 
    t[10] = "Wollen Sie das Programm wirklich beenden ?";
    t[11] = "Datei";
    t[12] = "Pfad ausw�hlen";
    t[13] = "Beenden";
    t[14] = "Erzeugen";
    t[15] = ".sco Datei ausw�hlen";
    t[16] = "Es is nicht m�glich die Datei zu speichern:";
    t[17] = "Fertig";
    t[18] = "Maximum Frequenz:";
    t[19] = "Minimum Frequenz:";
    t[20] = "Population:";
    t[21] = "Auswahlmenge:";	//"Number of Fittest:";
    t[22] = "Treffer Effekt:";
    t[23] = "Frequenzraster:";
    t[24] = "Minimum Dauer:";
    t[25] = "Hz";
    t[26] = "sec";
    t[27] = "Zufallsgewichtung:";
    t[28] = "Durchl�ufe:";
    t[29] = "Gewichtung:"; 
    t[30] = "12 temperierte Noten";	//"Use chromatic Notes";
    t[31] = "Abbruch";
    t[32] = "�ber";
    t[33] = "Optionen";
    t[34] = "CSound Editor";
    t[35] = "Die Programm wurde geschrieben um eine 'orchestra Datei' und eine 'score Datei' f�r CSound zu erzeugen.";
    t[36] = "Die Regeln um einen Ton zu erzeugen basieren auf Zufall und Evolution.";
    t[37] = "Die Verteilung der Wahrscheinlichkeiten f�r die Frequenzen ver�ndern";
    t[38] = "sich selbststruckturierend w�hrend eines Programmdurchlaufs."; 
    //t[39] = "In the end, with the correct parameters, a nearly constant note will appear";
    t[39] = "Idee : Peter Heeren (Deutscher Komponist) www.peter-heeren.de";
    t[40] = "Programm: geschrieben in 100% Java by Nils Kay (Diplomingenieur) Nils.Kay@esko.com";
    t[42] = "Maximale Dauer:";
    t[43] = "Stereo Effekte";
    t[44] = "Minimale Lautst�rke:";
    t[45] = "Maximale Lautst�rke:";
    t[46] = "Saatfrequenz:";
    t[47] = "FOF Komposer";
    t[48] = "OSZI Komposer";
    t[49] = "Bereich";
    t[50] = "Stochastik";
    t[51] = "Komposition";
    t[52] = "Min. Stimmen:";
    t[53] = "Max. Stimmen:";
    t[54] = "Gamma:";
    t[55] = "Template speichern";
    t[56] = "Template laden";
    t[57] = "Template zum Laden ausw�hlen";
    t[58] = "Template zum Speichern ausw�hlen";
    t[59] = "Es ist nicht m�glich dies Template zu laden: ";
    t[60] = "Es ist nicht m�glich dies Template zu speichern: ";
    t[61] = "Die aktuellen Einstellungen wurden modifiziert.";
    t[62] = "M�chten Sie diese Einstellungen als Template speichern ?";
    t[63] = "Abspielen";
    t[64] = "Midi speichern";
    t[65] = "Tiefere Noten bevorzugen";
    t[66] = "Since version 'V1.08 alpha1' there was some Midi-options added.";
    t[67] = "It is now possible to play the generated composition with JcSelf and once";
    t[68] = "it was played it can be saved as a '*.mid' file to be used by other programs";
    t[69] = "Es ist nicht m�glich das MIDI device zu �ffnen. Es wird m�glicherweise von einer anderen Applikation benutzt!";
    t[70] = "Laden";
    t[71] = "Dauerintervall";
    t[72] = "Abnahme:";
    t[73] = "W�hlen Sie g�ltige Noten";
    t[74] = "Auswahl";
    t[75] = "Hinzuf�gen";
    t[76] = "Entfernen";
    t[77] = "Eingabe der Feqenzen. Z.B. 100; 150; 200";
    t[78] = "Schlie�en";
    t[79] = "Benutze Frequenzauswahl.";
    t[80] = "Ihre Auswahl mu�te Reduziert werden, um zwischen die Minimum- und Maximum-Frequenz zu passen!";
    t[81] = "M�gliche Frequenzen";
    t[82] = "Ausgew�hlte Frequenzen";
    t[83] = "Die Frequenz in die Auswahl bewegen.";
    t[84] = "Die Frequenz von der Auswahl entfernen.";
    t[85] = "Ihre Auswahl hat weniger als 2 frequenzen, das klingt vermutlich eher langweilig...";
    t[86] = "OK";
    t[87] = "Abbruch";
    t[88] = "Ihre Auswahl enth�lt keine Frequenzen, Abbruch...";
    t[89] = "Instrumente Ausw�hlen";
    t[90] = "Es wurde keine Soundbank gefunden, darum ist es nicht m�glich Instrumente auszuw�hlen !";
    t[91] = "Akkord";
    t[92] = "Schatten";
    t[93] = "Sprachen";
    t[94] = "Englisch";
    t[95] = "Deutsch";
    t[96] = "Anzeige 2D";
    t[97] = "Anzeige 3D";
    t[98] = "Starten Sie das Programm erneut, um die �nderungen wirksam zu machen."; 
    t[99] = "Verl�ngerung";
    t[100] = "msec";
    t[101] = "Bereich";
    t[102] = "Schwellwert";
    t[103] = "Schritt nach oben";
    t[104] = "Schritt nach unten";
    t[105] = "Anzahl";
    t[106] = "Kaskadeneffect";
    t[107] = "Schleifeneffect";
    t[108] = "Geschwindigkeitsffect";
    t[109] = "Statusmachine konfigurieren...";
    t[110] = "Je h�her dieser Wert, desto schneller wird der Z�hler erh�ht";
    t[111] = "Schwellwert, bei diesem Wert des Z�hlers wird der Effekt angesto�en";
    t[112] = "Anzahl der Intervalle, in denen der Effekt wirksam ist";
    t[113] = "Wieviele Noten es pro Intervall nach oben geht";
    t[114] = "Wieviele Noten es pro Intervall nach unten geht";
    t[115] = "Schleifentiefe";
    t[116] = "Schleifenwiederholungen";
    t[117] = "Wie viele T�ne wiederholt werden sollen";
    t[118] = "Wie oft die Schleife wiederholt werden soll";
    t[119] = "Min. Tempo";
    t[120] = "Max. Tempo";
    t[121] = "Schritte";
    t[122] = "Um wieviel die Geschwindigkeit pro Treffer ge�ndert wird";
    t[123] = "Permutation";
    t[124] = "Wenn gew�hlt variiert in der Schleife die Geschwindigkeit, Amplitude etc., nicht aber die Frequenz";
    t[125] = "Benutze Schleifenfehler";
    t[126] = "Melodieeffekt";
    t[127] = "Melodie";
    t[128] = "Transposition";
    t[129] = "Minus Noten";
    t[130] = "Plus Noten";
    t[131] = "Min. Octave";
    t[132] = "Max. Octave";
    t[133] = "Freie Melodie";
    return t;
}
public int getInstrumentForChannel(int c) {
	if (c < (this.voiceMax+melodyChannels)) 
		return this.channels.elementAt(c).intValue();
	else return c;
}

public void setInstrumentForChannel(int c, int i) {
	if (c < (this.voiceMax+melodyChannels)) 
		this.channels.setElementAt(new Integer(i), c);
}

Dimension screenSize = null;

/**
 * Set the position of a child centered to the parent frame
 * @param parnt
 * @param child
 */
public void setNewLocation(JFrame parnt, Component child) {
	// Location :
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize(); 
	this.screenSize = d;
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] screens = ge.getScreenDevices();
    int wi = 0;
    for (int n= 0; n < screens.length; n++) {
    	DisplayMode m = screens[n].getDisplayMode();
    	wi += m.getWidth();
    }
    this.screenSize = new Dimension(wi, d.height);
	Point p = new Point(0,0);
	int parent_w = 0;
	int parent_h = 0;
	int childW = 0;
	int childH = 0;
	try {
	    p = parnt.getLocationOnScreen(); // Absolute Pos. on Screen
	    parent_w = parnt.getSize().width;
	    parent_h = parnt.getSize().height;
	    childW = child.getSize().width;
	    childH = child.getSize().height;
	    p.x += (parent_w - childW) / 2;
	    p.y += (parent_h - childH) / 2;
	    
	}catch (IllegalComponentStateException e) {
	}
	int endX = p.x + childW;
	int endY = p.y + childH;
	if (endX > screenSize.width)
		p.x = screenSize.width - childW;
	if (endY > screenSize.height)
		p.y = screenSize.height - childH;
	if (p.x < 0)
		p.x = 0;
	if (p.y < 0)
		p.y = 0;
	
	child.setLocation(p.x,p.y);	// Where the Window will appear
}
} // end of class







