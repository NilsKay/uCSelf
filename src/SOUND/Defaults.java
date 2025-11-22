package Sound;
import java.util.*;
import java.io.*;
import java.text.*;
import Utils.GetEnviroment;
import Utils.Converter;
/**
 * This class (deflts = Defaults) saves User settings for the next time that we start
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Defaults {

    // ---------- All the variables from the ui-mask :
    int max_freq, min_freq;
    int population, fittest;
    int interval, iterations, diff_freq;
    double length, r_Weight, degression;
    double impact;
    boolean tonal, stereo, preferLowerNotes;
    boolean fof;	// To switch between Input panels
    boolean newFOF;	// To modify just the Oszi to behave like FOF
    // Other vars, also saved as Properties:
    public String lastPath = null;
    String min_amp, max_amp;	// pp, p, mp, mf, f, ff 
    public final int voiceMax = 7;
    double amplify_amp;
    double min_tempo, max_tempo;
    int duration_step_index = 0;	// 0 = free
    int min_voice = 1, max_voice=1;
    int seed, mode;
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
    
/**
 * This Method loads the saved Default values from .motor.def
 * @param path which default file to use
 */
public boolean loadDef(String path) {
    String res0, res1;
    File fdes;
    BufferedReader br = null;
    double d;
    int q, ii;
    // set default, overwrite if possible
    seed = 0;
    mode = 0;
    max_freq = 20000;
    min_freq = 20;
    interval = 50;
    population = 200;
    fittest = 15;
    iterations = 50;
    diff_freq = 250;
    r_Weight = 20;
    degression = 0.0;
    tonal = false;
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
		    else if (res0.equals("MODE")) this.mode = Converter.getInt(res1, 0);
		    else if (res0.equals("POPULATION")) this.population = Converter.getInt(res1, 200); 
		    else if (res0.equals("FITTEST")) this.fittest = Converter.getInt(res1, 30); 
		    else if (res0.equals("INTERVAL")) this.interval = Converter.getInt(res1, 50); 
		    else if (res0.equals("MINVOICE")) this.min_voice = Converter.getInt(res1, 1);
		    else if (res0.equals("MAXVOICE")) this.max_voice = Converter.getInt(res1, 1);
		    else if (res0.equals("ITERATIONS")) this.iterations = Converter.getInt(res1, 20); 
		    else if (res0.equals("RANDOMWEIGHT")) {
			this.r_Weight = Converter.getDouble(res1, 1.0);
			this.impact = this.r_Weight;
		    }
		    else if (res0.equalsIgnoreCase("DEGRESSION")) this.degression = Converter.getDouble(res1, 0.0);
		    else if (res0.equals("DIFFFREQ")) this.diff_freq = Converter.getInt(res1, 250); 
		    else if (res0.equals("TONAL")) this.tonal = Converter.checkState(res1);
		    else if (res0.equalsIgnoreCase("duration_step_index")) this.duration_step_index =
									       Converter.getInt(res1, 0); 
		    else if (res0.equals("preferLowerNotes")) this.preferLowerNotes = Converter.checkState(res1);
		    //else if (res0.equals("FOF")) this.fof = Converter.checkState(res1); // out since V1.7a1, use modified oszi instaed
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
		}
	    }
	} // end of while
    } // end of try
    catch (java.io.IOException e) {
	System.out.println("loadDef in Defaults.java : "+e);
	return false;
    }
    //System.out.println("Default.load() (2) : amplify_amp = "+this.amplify_amp);
    return true;
}// end of method

/**
 * This Method saves some default values.
 * @param path wich file to save
 * @param user the name of the user
 */
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
	prs.println("MODE="+this.mode+"	# 0=2d, 1=3d, 2= My3d");
	prs.println("MAXFREQUENCY="+this.max_freq+"	# Maximal used frequency");
	prs.println("MINFREQUENCY="+this.min_freq+"	# Minimal used frequency");
	prs.println("SEED="+this.seed+"	# Initial frequency 0 = use random");
	prs.println("INTERVAL="+this.interval+"		# The step between 2 table entry's");
	prs.println("MINVOICE="+this.min_voice+"		# min. amount voices");
	prs.println("MAXVOICE="+this.max_voice+"		# max. amount voices");
	prs.println("POPULATION="+this.population+"	# Number of random frequencys per Iteration");
	prs.println("FITTEST="+this.fittest+"	# Choose from these with the highest weight");
	prs.println("ITERATIONS="+this.iterations+"	# ");
	prs.println("RANDOMWEIGHT="+this.r_Weight+"	# how much the weight will be influenced by the random function.");
	prs.println("DEGRESSION="+this.degression+"	# modify the random weight.");
	prs.println("DIFFFREQ="+this.diff_freq+"	# Frequency area influenced by one impact");
	prs.println("TONAL="+this.tonal+"	# If true, use just tonel notes, else all possible frequencys");
	prs.println("preferLowerNotes="+preferLowerNotes+"	# If true, use the same frequency distribution as with non tonal, if fase, prefer lower notes !");
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
//prs.println("="+this.+"	# ");

	prs.close();

    }
    catch (java.io.IOException e) {
	System.out.println("SaveJob: "+e );
	return false; 	// Kann File nicht schreiben ! 
    } // End of IO Excep
    System.out.println("Deflts: saveDefaults was done in file:"+path); 
    return true;
}

} // end of class







