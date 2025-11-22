package Sound;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*; 
import javax.swing.event.*;
import java.io.*;
import java.util.Vector;
import java.text.*;
import java.lang.Thread;
import Utils.*; 

/**
 * This class is used to generate a control file for csound
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class CEditor extends Frame implements ItemListener, FocusListener, ActionListener, SoundInfoListener{
    Window wi;
    int w = 200, h = 100;	// initial Windowsize
    Color fg,bg,he;
    int x,y;	// windowPosition
    int fontsize = 11;	// Font for min. window size (800*600)
    int subFontsize = 9;
    boolean child = true, State; //true when we run as a child of another window
    boolean halt = false, mHalt = false;
    String Home;
    String User;
    String Host;
    int xgap = 2;	// gaps for Insets
    int min = 0;
    int ygap = 0;
    String dummy = "                 ";
    int left_Button;
    int right_Button;
    int digits = 8;	// Anzahl Stellen
    int post = 3;	// Post colon digits
    Runner ru;
    PlayMidi pm;
    // --------- Other classes: ---------------
    Defaults def;
    GraphCanvas gc;
    CDebug qd;
    Project2 pr2;
    Project3 pr3;
    MyMidi mid;
    Timer[] ti;
    // --------- The UI Elements: --------------
    Font font;
    Font subFont;
    Label la, la1, la2,la3,la4, la5, la6, la7, la8, la9, la10, la11, la12, la13, la14, la15;
    Label lau, lau1, lau2, lau3,lau4,lau5, lau6, lau8, lau9, lau12;
    TextField tf, tf1, tf2, tf3, tf4, tf5, tf6, tf7, tf8, tf9, tf12;
    Button start, stp, play, save;
    Checkbox cb, cb1, cb2;
    Choice ch, ch1, ch2, ch3, gam;	
    MenuBar mb=null;		//Der Menubalken
    Menu m1, m2;			//Das Menu im Balken
    MenuItem   mi1_1, mi1_2, mi2_1, mi2_2, mi2_3, mi1_3, mi1_4, mi1_5 ;		//
    Panel pane;
    BorderPanel body;
    BorderPanel pr, pt;
    // ---------- variables -------------
    Vector ton;
    String options = null;
    boolean debug = false;
    boolean enableFOF = false, playMidi = false, played = false, enableMidi = true;
    int idx[], ampl[], akt[];
    boolean isOn[] = new boolean[16];
    int balance[] = new int[16];
    long SysStartTime, tTime;
    /**
     * Verbosity: 0= no, 1=important Messages, 2=less important, 3=low debug, 4=higher debug, 5 = high debug, 6 = crazy debug
     */
    int Verbosity = 1;
    //boolean fof = false; // if true, we work as FOF generator, not osci
    String text[] = new String[100];
    String CustomPath = null;
    String sFile = null;
    String oFile = null;
/**
 * Creates an Evolution Window (Standalone)
 * @param file,path this is the file we should load !
 * @param fsize The Fontsize for this window
 */
public CEditor() {
    this(null, null, null);
}

/**
 * Creates a Evolution Window
 * @param dw the owner of the Window
 * @param file,path this is the file we should load !
 */
public CEditor(String home, String user, String opt) {
    Point p = new Point(0,0);
    xgap = 10 ;	// horiz. gap for Graidbag components
    ygap = 0 ;	// vert.gap 
    min = 0 ;	// minimum gap
    this.State = false;
    this.sFile = null; 
    Toolkit tool = Toolkit.getDefaultToolkit();
    Dimension d = tool.getScreenSize();
    Dimension gd;
    System.out.println("Screensize="+d);
    this.text = getText(this.text);
    //------------ Midi init ----------
    this.mid = new MyMidi();
    //this.enableMidi = doMidiOpen();
    //------------------------------------
    if (d.width <= 800) {
	this.font = new Font("Helvetica", Font.PLAIN, fontsize);
	this.subFont = new Font("Helvetica", Font.PLAIN, subFontsize);
	gd = new Dimension(550, 130);
    }
    else {
	this.font = new Font("Helvetica", Font.PLAIN, fontsize+2);
	this.subFont = new Font("Helvetica", Font.PLAIN, subFontsize+2);
	gd = new Dimension(550, 250);
    }
    x = p.x + 20; // Offset of Window to parent frame
    y = p.y + 30;
    GetEnviroment gsp= new GetEnviroment(null); // root is the grapholasnt toplevel dir
    if (home == null) { // we do not come from an applet
	if (gsp.getPathes()) {
	    System.out.println("No Path, FATAL TRY AGAIN.");
	    try {
		java.lang.Thread.sleep(5000); // wait 5 sec. until exit
	    }
	catch (InterruptedException e){}
	doQuit();
	}

    debugOut("GetEviroment got: SystemPath="+gsp.getEnviroment()+" ClassPath="+gsp.getClassPath()+" Home="+gsp.getHome()+" User="+gsp.getUser(), 2);
	this.Home = gsp.getHome();
	this.User = gsp.getUser();
	this.child = false;
	this.options = null;
    }
    else {
	this.child = true;
	this.Home = home;
	this.User = user;
	this.options = opt;
    }
    this.Host = gsp.getHost();
    if (this.CustomPath == null) this.CustomPath = this.Home;
    left_Button = gsp.getLeftButton();	//MouseEvent.BUTTON1_MASK;
    right_Button = gsp.getRightButton();
    // user defaults:
    this.def = new Defaults();
    boolean defLoaded = this.def.loadDef(Home+File.separator+GetEnviroment.SAVEFILE);
    if (def.lastPath != null) this.CustomPath = def.lastPath;
    //this.fof = def.fof;
    //this.fof = false;	// as long as FOF is disabled.
    setFont(this.font);
    mb = new MenuBar();
    mb.setFont(this.font); 
    setMenubar(); // erzeugt das Menu hier in guimain
    setMenuBar(mb);
    bg = Color.lightGray;
    pane = new Panel();
    pane.setBackground(bg);
    fg = Color.black;
    pane.setForeground(fg);
    pane.setLayout(new BorderLayout());
    BorderPanel top = new BorderPanel();
    top.setGap(0);
    gc = new GraphCanvas(gd, this.subFontsize);
    top.add(gc);
    pane.add("North", top);
    if (options == null) options = gsp.getOPTIONS();
    this.debug = examineOptions(options);
    if ( this.debug) {
	this.qd = new CDebug(this, "Debug Tool, just for Nils");
	this.qd.setVisible(true);
    }
    if (!enableFOF) this.def.fof = false; // oszi
    if (!this.def.fof) this.body = getBody(); // Oszi
    else this.body = getFOFBody();
    pane.add("Center", this.body);
    mi2_2.setEnabled(this.def.newFOF);	//fof);
    mi2_3.setEnabled(!this.def.newFOF);	//.fof);
    mi1_5.setEnabled(false);
    Panel bot = new Panel();
    // --------- Play Button ------------
    play = new Button(this.text[63]);
    play.setFont(this.subFont);
    play.addActionListener(new STListener());
    bot.add(play);
    play.setEnabled(false);
    // --------- Save Button ------------
    save = new Button(this.text[64]);
    save.setFont(this.subFont);
    save.addActionListener(new STListener());
    bot.add(save);
    save.setEnabled(false);
    // --------- Start Button ------------
    start = new Button(this.text[14]);
    start.setFont(this.subFont);
    start.addActionListener(new STListener());
    bot.add(start);
    start.requestFocus();
    // --------- Stop Button ------------
    stp = new Button(this.text[31]);
    stp.setFont(this.subFont);
    stp.addActionListener(this);
    bot.add(stp);
    stp.setEnabled(false);
    setValues(true);
    pane.add("South", bot);
    this.addWindowListener(new MDListener());
    this.add(pane);
    pack();
    relocate();
    
    super.setTitle(this.text[0]);
    debugOut(this.text[0], 1);
} // end of constructor

    private boolean doMidiOpen() {
	this.enableMidi = this.mid.open();
	if (!enableMidi) {
	    System.out.println("MidiOpen failed, msgs="+this.text[69]);
	    String txt[] = new String[2];
	    txt[0] = this.text[69];
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);	
	    al.setVisible(true);
	}
	return this.enableMidi;
    }
    /**
     * Adds the various Menu Items to the Menubar
     **/
public void setMenubar() {
    // erzeuge das Menu
    m1 = new Menu(this.text[11],true);	// File Tear off Menu(muss clicken)
    m1.setFont(this.font); 
    mb.add(m1);				// zum Balken addieren

    mi1_1 = new MenuItem(this.text[12]); // Select Path
    mi1_1.setFont(this.font);
    mi1_1.addActionListener(this);
    m1.add(mi1_1);				//Addiere MenuPunkt zum Menu
    mi1_1.setEnabled(true);
    m1.addSeparator();
    mi1_3 = new MenuItem(this.text[56]); // Load Template
    mi1_3.setFont(this.font);
    mi1_3.addActionListener(this);
    m1.add(mi1_3);				//Addiere MenuPunkt zum Menu
    mi1_3.setEnabled(true);
    mi1_4 = new MenuItem(this.text[55]); // Save Template
    mi1_4.setFont(this.font);
    mi1_4.addActionListener(this);
    m1.add(mi1_4);				//Addiere MenuPunkt zum Menu
    mi1_4.setEnabled(true);
    m1.addSeparator();
    mi1_2 = new MenuItem(this.text[13]); // Quit
    mi1_2.setFont(this.font);
    mi1_2.addActionListener(this);
    m1.add(mi1_2);				//Addiere MenuPunkt zum Menu
    mi1_2.setEnabled(true);

    m2 = new Menu(this.text[33],true);	// File Tear off Menu(muss clicken)
    m2.setFont(this.font); 
    mb.add(m2);				// zum Balken addieren
    mi2_2 = new MenuItem(this.text[48]); // Oszi
    mi2_2.setFont(this.font);
    mi2_2.addActionListener(this);
    if ( enableFOF ) m2.add(mi2_2);	
    mi2_3 = new MenuItem(this.text[47]); // FOF
    mi2_3.setFont(this.font);
    mi2_3.addActionListener(this);
    if ( enableFOF ) m2.add(mi2_3);
    m2.addSeparator();
    mi1_5 = new MenuItem(this.text[70]); // Load
    mi1_5.setFont(this.font);
    mi1_5.addActionListener(this);
    m2.add(mi1_5);
    m2.addSeparator();
    //--
    mi2_1 = new MenuItem(this.text[32]); // About
    mi2_1.setFont(this.font);
    mi2_1.addActionListener(this);
    m2.add(mi2_1);		
}

private BorderPanel getFOFBody() {
    int row = 0;
    BorderPanel pb = new BorderPanel();
    pb.setText(this.text[47]);
    debugOut(this.text[47], 1);
    String wert;
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    pb.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.weightx =	1.0; // Gewichtung fuer freien Raum
    c.weighty = 1.0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridwidth = 1;//GridBagConstraints.REMAINDER; // until end of row
    Insets left = new Insets(this.ygap, this.xgap, this.min, 0); // top, left, bottom, right
    Insets rig = new Insets(this.ygap, this.xgap, this.min, this.xgap); // top, left, bottom, right
// --------- max_freq Textfield ------------
    c.gridx= 0;
    c.gridy= row;
    la = new Label(this.text[18]);
    gridbag.setConstraints(la,c);
    pb.add(la);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.max_freq, this.digits);
    tf = new TextField(wert);
    tf.addFocusListener(this);
    tf.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf,c);
    pb.add(tf);
    c.gridx= 1;
    c.gridy= row + 1;
    lau = new Label(this.text[25], Label.LEFT);
    gridbag.setConstraints(lau,c);
    pb.add(lau);
// --------- min_freq Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la1 = new Label(this.text[19]);
    gridbag.setConstraints(la1,c);
    pb.add(la1);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.min_freq, this.digits);
    tf1 = new TextField(wert);
    tf1.addFocusListener(this);
    tf1.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf1,c);
    pb.add(tf1);
    c.gridx= 3;
    c.gridy= row + 1;
    lau1 = new Label(this.text[25], Label.LEFT);
    gridbag.setConstraints(lau1,c);
    pb.add(lau1);
// --------- Interval Textfield ------------
    c.gridx= 4;
    c.gridy= row;
    la4 = new Label(this.text[23]);
    gridbag.setConstraints(la4,c);
    pb.add(la4);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.interval, this.digits);
    tf4 = new TextField(wert);
    tf4.addFocusListener(this);
    tf4.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf4,c);
    pb.add(tf4);
    c.gridx= 5;
    c.gridy= row + 1;
    lau4 = new Label(this.text[25], Label.LEFT);
    gridbag.setConstraints(lau4,c);
    pb.add(lau4);
    // --------- Seed Textfield ------------
    row+=2;
    c.gridx= 0;
    c.gridy= row;
    la12 = new Label(this.text[46]);
    gridbag.setConstraints(la12,c);
    pb.add(la12);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.seed, this.digits);
    tf12 = new TextField(wert);
    tf12.addFocusListener(this);
    tf12.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf12,c);
    pb.add(tf12);
    c.gridx= 1;
    c.gridy= row + 1;
    lau12 = new Label(this.text[25], Label.LEFT);
    gridbag.setConstraints(lau12,c);
    pb.add(lau12);

// --------- Min Duration Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la5 = new Label(this.text[24]);
    gridbag.setConstraints(la5,c);
    pb.add(la5);
    c.gridx= 2;
    c.gridy=  row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.min_tempo, this.digits, this.post);
    tf5 = new TextField(wert);
    tf5.addFocusListener(this);
    tf5.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf5,c);
    pb.add(tf5);
    c.gridx= 3;
    c.gridy= row + 1;
    lau5 = new Label(this.text[26], Label.LEFT);
    gridbag.setConstraints(lau5,c);
    pb.add(lau5);
// --------- Max Duration Textfield ------------
    c.gridx= 4;
    c.gridy= row;
    la9 = new Label(this.text[42]);
    gridbag.setConstraints(la9,c);
    pb.add(la9);
    c.gridx= 4;
    c.gridy=  row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.max_tempo, this.digits, this.post);
    tf9 = new TextField(wert);
    tf9.addFocusListener(this);
    tf9.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf9,c);
    pb.add(tf9);
    c.gridx= 5;
    c.gridy= row + 1;
    lau9 = new Label(this.text[26], Label.LEFT);
    gridbag.setConstraints(lau9,c);
    pb.add(lau9);
    // --------- Number of fittest Textfield ------------
    row+=2;
    c.gridx= 0;
    c.gridy= row;
    la3 = new Label(this.text[21]);
    gridbag.setConstraints(la3,c);
    pb.add(la3);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.fittest, this.digits);
    tf3 = new TextField(wert);
    tf3.addFocusListener(this);
    tf3.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf3,c);
    pb.add(tf3);
    // --------- Random Weight Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la6 = new Label(this.text[27]);
    gridbag.setConstraints(la6,c);
    pb.add(la6);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.r_Weight, this.digits, this.post);
    tf6 = new TextField(wert);
    tf6.addFocusListener(this);
    tf6.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf6,c);
    pb.add(tf6);
// --------- Min. loudness Choice ------------
    c.gridx= 4;
    c.gridy= row;
    la10 = new Label(this.text[44]);
    gridbag.setConstraints(la10,c);
    pb.add(la10);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    ch = new Choice(); 	
    setLChoice(ch);
    ch.select(def.min_amp);
    ch.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(ch,c);
    pb.add(ch);
// --------- Population Textfield ------------
    row+=2;
    c.gridx= 0;
    c.gridy= row;
    la2 = new Label(this.text[20]);
    gridbag.setConstraints(la2,c);
    pb.add(la2);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.population, this.digits);
    tf2 = new TextField(wert);
    tf2.addFocusListener(this);
    tf2.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf2,c);
    pb.add(tf2);
    // --------- Iterations Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la7 = new Label(this.text[28]);
    gridbag.setConstraints(la7,c);
    pb.add(la7);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.iterations, this.digits);
    tf7 = new TextField(wert);
    tf7.addFocusListener(this);
    tf7.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf7,c);
    pb.add(tf7);
    // --------- Max. loudness Choice ------------
    c.gridx= 4;
    c.gridy= row;
    la11 = new Label(this.text[45]);
    gridbag.setConstraints(la11,c);
    pb.add(la11);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    ch1 = new Choice();
    setHChoice(ch1);
    ch1.select(def.max_amp);
    ch1.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(ch1,c);
    pb.add(ch1);
// --------- Diff. Freq. Textfield ------------
    row +=2;
    c.gridx= 0;
    c.gridy= row;
    la8 = new Label(this.text[29]);
    gridbag.setConstraints(la8,c);
    pb.add(la8);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.diff_freq, this.digits, this.post);
    tf8 = new TextField(wert);
    tf8.addFocusListener(this);
    tf8.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf8,c);
    pb.add(tf8);
    c.gridx= 1;
    c.gridy= row + 1;
    lau8 = new Label(this.text[25], Label.LEFT);
    gridbag.setConstraints(lau8,c);
    pb.add(lau8);
// --------- Use stereo effects Checkbox ------------
    c.gridx= 4;
    c.gridy= row-2;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    cb1 = new Checkbox(this.text[43]);
    cb1.addItemListener(this);
    cb1.setState(this.def.stereo);
    c.insets = rig;
    gridbag.setConstraints(cb1,c);
    pb.add(cb1);
    return pb;
}
/**
 * Oszi panel 
 */
private BorderPanel getBody() {
    int row = 0;
    BorderPanel pb = new BorderPanel();
    pb.setTextFont(this.font); 
    this.pr = new BorderPanel();	//Random related
    this.pt = new BorderPanel();	//Tone related
    //Font Tfont = new Font("TimesRoman", Font.PLAIN, 10);
    int gap = 0;
    pr.setTextFont(this.subFont); 
    pr.setText(this.text[50]);
    pr.setGap(gap);
    pt.setTextFont(this.subFont); 
    pt.setText(this.text[51]);
    pr.setGap(gap);
    if (this.def.newFOF) pb.setText(this.text[47]); // fof
    else pb.setText(this.text[48]);
    pb.setGap(gap);
    debugOut(this.text[48], 1);
    String wert;
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    pb.setLayout(gridbag);
    pr.setLayout(gridbag);
    pt.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.weightx =	1.0; // Gewichtung fuer freien Raum
    c.weighty = 1.0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridwidth = 1;//GridBagConstraints.REMAINDER; // until end of row
    Insets left = new Insets(this.ygap, this.xgap, this.min, 0); // top, left, bottom, right
    Insets rig = new Insets(this.ygap, this.xgap, this.min, this.xgap); // top, left, bottom, right
// --------- min_freq Textfield ------------
    c.gridx= 0;
    c.gridy= row;
    c.insets = left;
    la1 = new Label(this.text[19]);
    la1.setFont(this.subFont);
    gridbag.setConstraints(la1,c);
    pt.add(la1);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.min_freq, this.digits);
    tf1 = new TextField(wert);
    tf1.addFocusListener(this);
    tf1.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf1,c);
    pt.add(tf1);
    c.gridx= 1;
    c.gridy= row + 1;
    lau1 = new Label(this.text[25], Label.LEFT);
    lau1.setFont(this.subFont);
    gridbag.setConstraints(lau1,c);
    pt.add(lau1);
// --------- max_freq Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la = new Label(this.text[18]);
    la.setFont(this.subFont);
    gridbag.setConstraints(la,c);
    pt.add(la);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.max_freq, this.digits);
    tf = new TextField(wert);
    tf.addFocusListener(this);
    tf.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf,c);
    pt.add(tf);
    c.gridx= 3;
    c.gridy= row + 1;
    lau = new Label(this.text[25], Label.LEFT);
    lau.setFont(this.subFont);
    gridbag.setConstraints(lau,c);
    pt.add(lau);
    // --------- Seed Textfield ------------
    c.gridx= 4;
    c.gridy= row;
    la12 = new Label(this.text[46]);
    la12.setFont(this.subFont);
    gridbag.setConstraints(la12,c);
    pt.add(la12);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.seed, this.digits);
    tf12 = new TextField(wert);
    tf12.addFocusListener(this);
    tf12.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf12,c);
    pt.add(tf12);
    c.gridx= 5;
    c.gridy= row + 1;
    lau12 = new Label(this.text[25], Label.LEFT);
    lau12.setFont(this.subFont);
    gridbag.setConstraints(lau12,c);
    pt.add(lau12);
        // --------- Min Duration Textfield ------------
    row += 2;
    c.gridx= 0;
    c.gridy= row;
    la5 = new Label(this.text[24]);
    la5.setFont(this.subFont);
    gridbag.setConstraints(la5,c);
    pt.add(la5);
    c.gridx= 0;
    c.gridy=  row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.min_tempo, this.digits, this.post);
    tf5 = new TextField(wert);
    tf5.addFocusListener(this);
    tf5.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf5,c);
    pt.add(tf5);
    c.gridx= 1;
    c.gridy= row + 1;
    lau5 = new Label(this.text[26], Label.LEFT);
    lau5.setFont(this.subFont);
    gridbag.setConstraints(lau5,c);
    pt.add(lau5);
// --------- Max Duration Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la9 = new Label(this.text[42]);
    la9.setFont(this.subFont);
    gridbag.setConstraints(la9,c);
    pt.add(la9);
    c.gridx= 2;
    c.gridy=  row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.max_tempo, this.digits, this.post);
    tf9 = new TextField(wert);
    tf9.addFocusListener(this);
    tf9.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf9,c);
    pt.add(tf9);
    c.gridx= 3;
    c.gridy= row + 1;
    lau9 = new Label(this.text[26], Label.LEFT);
    lau9.setFont(this.subFont);
    gridbag.setConstraints(lau9,c);
    pt.add(lau9);
    // --------- Use notes Checkbox ------------
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    cb = new Checkbox(this.text[30]);
    cb.setFont(this.subFont);
    cb.addItemListener(this);
    cb.setState(this.def.tonal);
    c.insets = rig;
    gridbag.setConstraints(cb,c);
    pt.add(cb);
    // --------- Prefer lower Notes ------------
    c.gridx= 4;
    c.gridy= row+2;
    c.insets = left;
    cb2 = new Checkbox(this.text[65]);
    cb2.setFont(this.subFont);
    cb2.addItemListener(this);
    cb2.setState(this.def.preferLowerNotes);
    c.insets = rig;
    gridbag.setConstraints(cb2,c);
    pt.add(cb2);
    // --------- Min. loudness Choice ------------
    row += 2;
    c.gridx= 0;
    c.gridy= row;
    la10 = new Label(this.text[44]);
    la10.setFont(this.subFont);
    gridbag.setConstraints(la10,c);
    pt.add(la10);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    ch = new Choice(); 
    ch.setFont(this.subFont);
    setLChoice(ch);
    ch.select(def.min_amp);
    ch.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(ch,c);
    pt.add(ch);
    // --------- Max. loudness Choice ------------
    c.gridx= 2;
    c.gridy= row;
    la11 = new Label(this.text[45]);
    la11.setFont(this.subFont);
    gridbag.setConstraints(la11,c);
    pt.add(la11);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    ch1 = new Choice();
    ch1.setFont(this.subFont);
    setHChoice(ch1);
    ch1.select(def.max_amp);
    ch1.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(ch1,c);
    pt.add(ch1);
    // --------- Use stereo effects Checkbox ------------
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    cb1 = new Checkbox(this.text[43]);
    cb1.setFont(this.subFont);
    cb1.addItemListener(this);
    cb1.setState(this.def.stereo);
    c.insets = rig;
    gridbag.setConstraints(cb1,c);
    pt.add(cb1);
    // --------- Min. Voices ------------
    row += 2;
    c.gridx= 0;
    c.gridy= row;
    la13 = new Label(this.text[52]);
    la13.setFont(this.subFont);
    gridbag.setConstraints(la13,c);
    pt.add(la13);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    ch2 = new Choice(); 
    ch2.setFont(this.subFont);
    setLVoice(ch2);
    ch2.select(Integer.toString(def.min_voice));
    ch2.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(ch2,c);
    pt.add(ch2);
    // --------- Max. Voices ------------
    c.gridx= 2;
    c.gridy= row;
    la14 = new Label(this.text[53]);
    la14.setFont(this.subFont);
    gridbag.setConstraints(la14,c);
    pt.add(la14);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    ch3 = new Choice();
    ch3.setFont(this.subFont);
    setHVoice(ch3);
    ch3.select(Integer.toString(def.max_voice));
    ch3.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(ch3,c);
    pt.add(ch3);
    // --------- Gamma ------------
    c.gridx= 4;
    c.gridy= row;
    la15 = new Label(this.text[54]);
    la15.setFont(this.subFont);
    gridbag.setConstraints(la15,c);
    pt.add(la15);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    gam = new Choice();
    gam.setFont(this.subFont);
    int qz;
    for (qz = 0; qz < Project2.GAMMAT.length; qz++) 
	gam.add(Double.toString(Project2.GAMMAT[qz]));
    gam.select(Double.toString(def.gamma));
    gam.addItemListener(this);
    c.insets = rig;
    gridbag.setConstraints(gam, c);
    pt.add(gam);
    // ------------------ 2nd panel: -----------------------
    c.gridx= 0;
    c.gridy= 0;
    gridbag.setConstraints(pt,c);
    pb.add(pt);
// --------- Population Textfield ------------
    row = 0;
    c.gridx= 0;
    c.gridy= row;
    la2 = new Label(this.text[20]);
    la2.setFont(this.subFont);
    gridbag.setConstraints(la2,c);
    pr.add(la2);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.population, this.digits);
    tf2 = new TextField(wert);
    tf2.addFocusListener(this);
    tf2.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf2,c);
    pr.add(tf2);
// --------- Number of fittest Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la3 = new Label(this.text[21]);
    la3.setFont(this.subFont);
    gridbag.setConstraints(la3,c);
    pr.add(la3);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.fittest, this.digits);
    tf3 = new TextField(wert);
    tf3.addFocusListener(this);
    tf3.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf3,c);
    pr.add(tf3);
    // --------- Interval Textfield ------------
    c.gridx= 4;
    c.gridy= row;
    la4 = new Label(this.text[23]);
    la4.setFont(this.subFont);
    gridbag.setConstraints(la4,c);
    pr.add(la4);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.interval, this.digits);
    tf4 = new TextField(wert);
    tf4.addFocusListener(this);
    tf4.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf4,c);
    pr.add(tf4);
    c.gridx= 5;
    c.gridy= row + 1;
    lau4 = new Label(this.text[25], Label.LEFT);
    lau4.setFont(this.subFont);
    gridbag.setConstraints(lau4,c);
    pr.add(lau4);
    // --------- Diff. Freq. Textfield ------------
    row += 2;
    c.gridx= 0;
    c.gridy= row;
    la8 = new Label(this.text[29]);
    la8.setFont(this.subFont);
    gridbag.setConstraints(la8,c);
    pr.add(la8);
    c.gridx= 0;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.diff_freq, this.digits, this.post);
    tf8 = new TextField(wert);
    tf8.addFocusListener(this);
    tf8.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf8,c);
    pr.add(tf8);
    c.gridx= 1;
    c.gridy= row + 1;
    lau8 = new Label(this.text[25], Label.LEFT);
    lau8.setFont(this.subFont);
    gridbag.setConstraints(lau8,c);
    pr.add(lau8);
    // --------- Random Weight Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    la6 = new Label(this.text[27]);
    la6.setFont(this.subFont);
    gridbag.setConstraints(la6,c);
    pr.add(la6);
    c.gridx= 2;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatDouble(this.def.r_Weight, this.digits, this.post);
    tf6 = new TextField(wert);
    tf6.addFocusListener(this);
    tf6.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf6,c);
    pr.add(tf6);
    // --------- Iterations Textfield ------------    
    c.gridx= 4;
    c.gridy= row;
    la7 = new Label(this.text[28]);
    la7.setFont(this.subFont);
    gridbag.setConstraints(la7,c);
    pr.add(la7);
    c.gridx= 4;
    c.gridy= row+1;
    c.insets = left;
    wert = Converter.formatInt(this.def.iterations, this.digits);
    tf7 = new TextField(wert);
    tf7.addFocusListener(this);
    tf7.addActionListener(this);
    c.insets = rig;
    gridbag.setConstraints(tf7,c);
    pr.add(tf7);
    // ------------------ 3rd panel: -----------------------
    c.gridx= 0;
    c.gridy= 2;
    gridbag.setConstraints(pr,c);
    pb.add(pr);
    return pb;
}
    public void setActual() {
	String wert;
	if (def.lastPath != null) this.CustomPath = def.lastPath;
	//this.fof = def.fof;
	this.pane.remove(this.body);
	if (this.def.fof) {
	    this.body = getFOFBody();
	}
	else this.body = getBody();
	mi2_2.setEnabled(this.def.newFOF); 	// fof);
	mi2_3.setEnabled(!this.def.newFOF);	// fof);
	pane.add("Center", this.body);
	pack();

	wert = Converter.formatInt(this.def.max_freq, this.digits);
	tf.setText(wert);
	wert = Converter.formatInt(this.def.min_freq, this.digits);
	tf1.setText(wert);
	wert = Converter.formatInt(this.def.interval, this.digits);
	tf4.setText(wert);
	wert = Converter.formatInt(this.def.seed, this.digits);
	tf12.setText(wert);
	wert = Converter.formatDouble(this.def.min_tempo, this.digits, this.post);
	tf5.setText(wert);
	wert = Converter.formatDouble(this.def.max_tempo, this.digits, this.post);
	tf9.setText(wert);
	wert = Converter.formatInt(this.def.fittest, this.digits);
	tf3.setText(wert);
	wert = Converter.formatDouble(this.def.r_Weight, this.digits, this.post);
	tf6.setText(wert);
	wert = Converter.formatInt(this.def.population, this.digits);
	tf2.setText(wert);
	wert = Converter.formatInt(this.def.iterations, this.digits);
	tf7.setText(wert);
	wert = Converter.formatDouble(this.def.diff_freq, this.digits, this.post);
	tf8.setText(wert);
	if (ch != null) {
	    setLChoice(ch);
	    ch.select(def.min_amp);
	}
	if (ch1 != null) {
	    setHChoice(ch1);
	    ch1.select(def.max_amp);
	}
	if (cb1 != null) cb1.setState(this.def.stereo);
	if (cb != null) cb.setState(this.def.tonal);
	if (ch2 != null) {
	    setLVoice(ch2);
	    ch2.select(Integer.toString(def.min_voice));
	}
	if (ch3 != null) {
	    setHVoice(ch3);
	    ch3.select(Integer.toString(def.max_voice));
	}
	if (gam != null) gam.select(Double.toString(def.gamma));
    }

    /**
     * Select a file and save it as the new template
     * @return true if success
     */
    public boolean saveTemplate() {
	String filepath = selectFile(false);
	if (filepath == null) return false;
	if (!def.saveDef(filepath, this.User)) {
	    debugOut("CEditor:Can not save Template settings !", 1);
	    String txt[] = new String[2];
	    txt[0] = this.text[60]+filepath;
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);
	    al.setVisible(true);
	    return false;
	}
	State = false;
	return true;
    }
    /**
     * Select a file and load it as the new template
     * @return true if success
     */
    public boolean loadTemplate() {
	if (State) { // old values have been modified
	    String txt[] = new String[2];
	    txt[0] = this.text[61];
	    txt[1] = this.text[62];
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.CONFIRMATION);
	    al.setVisible(true);
	    if (al.OK) {
		saveTemplate();
	    }
	}
// Now load 
	String filepath = selectFile(true);
	if (filepath == null) return false;
	this.def = new Defaults();
	boolean defLoaded = this.def.loadDef(filepath);
	if (defLoaded) {
	    setActual();
	    State = false;
	    return true;
	}
	else {
	    String txt[] = new String[2];
	    txt[0] = this.text[59]+filepath;
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);
	    al.setVisible(true);
	}
	return false;
    }

    /**
     * Select a file for save or load
     * @param mode true if load, else save
     * @return path
     */
    public String selectFile(boolean mode) {
	String tit = this.text[58];
	if (mode) tit = this.text[57]; // load
	FileDialog fd = new FileDialog(this, tit);
	fd.setFile("");
	fd.setDirectory(CustomPath);
	fd.setVisible(true);
	String tmp;
	String sDir = fd.getDirectory();
	String sFile = fd.getFile();
	//System.out.println("selectFile 1:"+sDir+" "+sFile);
	if (sFile != null) {
	    tmp = sFile;
	    int i = sFile.lastIndexOf(".");
	    if (i >= 0) tmp = sFile.substring(0, i); 
	    sFile = tmp + GetEnviroment.TEMPLATEEXT;
	}
	else sDir = null;
	//System.out.println("selectFile 2:"+sDir+" "+sFile);
	if (sDir != null) CustomPath = def.lastPath = sDir;
	else sFile = null;

	String path = sDir + File.separator + sFile;
	if (path != null && mode) {
	    File fdes = new File(path);
	    if (!fdes.exists()) {
		path = null;
	    }
	}
	fd.dispose();
	//System.out.println("selectFile: path="+path);
	return path;
    }
    
    public void setLChoice(Choice c) {
	int n;
	int max = Project2.Loudness.length;
	if (this.def.newFOF) max = Project2.maxFOFLoudIndex;
	for(n = 0; n < max; n++) {
	    c.add(Project2.Loudness[n]);
	    if (def.max_amp.equals(Project2.Loudness[n])) break;
	}
    }

    public void setHChoice(Choice c) {
	int n;
	boolean flag = false;
	int max = Project2.Loudness.length;
	if (this.def.newFOF) max = Project2.maxFOFLoudIndex;
	for(n = 0; n < max; n++) {
	    if (def.min_amp.equals(Project2.Loudness[n])) flag = true;
	    if (flag) c.add(Project2.Loudness[n]);
	}
    }

    public void setLVoice(Choice c) {
	int n;
	for(n = 0; n < def.max_voice; n++) {
	    c.add(Integer.toString(n+1)); 
	}
    }

    public void setHVoice(Choice c) {
	int n;
	for(n = def.min_voice; n <= def.voiceMax; n++) {
	    c.add(Integer.toString(n));
	}
    }
    public void setValues(boolean w) {
	//tf.setEnabled(w & !this.def.tonal);
	//tf1.setEnabled(w & !this.def.tonal);
	//la.setEnabled(w & !this.def.tonal);
	//la1.setEnabled(w & !this.def.tonal);
    }

/**
 * Move Window so, that it is within the Screen !
 */
public void relocate() {
    pack();
    w = getSize().width; 	//Fensterbreite
    h = getSize().height;	//Fensterhoehe
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int maxXScreen = d.width;
    int maxYScreen = d.height;
    debugOut("Interlock: Screen="+maxXScreen+", "+maxYScreen+", loc x,y="+x+", "+y+", w,h="+w+", "+h, 3);
    if (!child) {
	x = (maxXScreen - w) / 2;
	y = (maxYScreen - h) / 2;
    }
    else {
	if ((x + w) > maxXScreen) {
	    x = maxXScreen - w;
	}
	if ((y+h) > maxYScreen) {
	    x = maxYScreen - w;
	}
    }
    super.setLocation(x,y);	// Where the Window will appear

}
    public void selectPath() {
	FileDialog fd = new FileDialog(this, this.text[15]);
	fd.setFile("");
	fd.setDirectory(CustomPath);
	fd.setVisible(true);
	String tmp;
	String sDir = fd.getDirectory();
	if (sDir != null) CustomPath = def.lastPath = sDir;
	this.sFile = fd.getFile();
	if (sFile != null) {
	    tmp = sFile;
	    int i = sFile.lastIndexOf(".");
	    if (i >= 0) tmp = sFile.substring(0, i); 
	    this.sFile = tmp + ScoAccess.SCOEXTENSION;
	    this.oFile = tmp + ScoAccess.ORCEXTENSION;
	}
	fd.dispose();
	this.State = true;
	debugOut("Path="+sDir+ "sco-File="+this.sFile+" orc-File="+this.oFile, 2);
	
    }

    public void debugOut(String tmp, int v) {
	if (v > this.Verbosity) return;
	if (this.qd != null) this.qd.put(tmp);
	else System.out.println(tmp);
    }

    private void doScoFile() {
	int n;
	setToWait(this, false);
	// Erst mal bestimme, welche Datei wir schreiben sollen (.sco) !
	if (this.sFile == null) selectPath();
	if (this.sFile == null) {
	    setToWait(this, true);
	    return;	// Tu nix
	}
	Vector sco_lines = null;
	if (!this.def.fof) sco_lines = doOszi();
	else sco_lines = doFOF();

	if (sco_lines != null) {
	    // Nun noch das EOF markieren:
	    sco_lines.addElement("e");
	    // schließlich muß der Vector in die Datei geschrieben werden 
	    if (!new ScoAccess().saveSco(CustomPath+sFile, this.User, sco_lines)) {
		String txt[] = new String[2];
		txt[0] = this.text[16]+CustomPath+sFile;
		Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);
		al.setVisible(true);
	    }
	    else {
		// Fehler
		String txt[] = new String[2];
		txt[0] = this.text[17];
		Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.MESSAGE);
		al.setVisible(true);
	    }
	}
	setToWait(this, true);

    }
    private Vector doFOF() {
	Vector sco_lines = new Vector();	
	// generate and save the orc file:
	pr3 = new Project3(this.qd, this.Verbosity, (SoundInfoListener) this);
	sco_lines = pr3.getOrc();
	//ScoAccess sc = new ScoAccess();
	if (!new ScoAccess().saveSco(CustomPath+oFile, this.User, sco_lines )) {
	    String txt[] = new String[2];
	    txt[0] = this.text[16]+CustomPath+oFile;
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);
	    al.setVisible(true);
	    return null;
	}
	// Generate and save the sco file:
	sco_lines = pr3.getHeader();	// Hier holen wir uns den Anfang der sco Datei
	// Dieser Vector (sco_lines) speichert alle Zeilen, die in die .sco datei geschrieben werden sollen
	// Nun ist der Headder der neuen sco Datei fertig
	// ------------------------------------------------------------
	// Als nächstes addieren wir mal ein paar Zeilen mit Tönen zum Vector
	// Der Vector ton speichert die Zeilen mit den Toenen
	
	ton = pr3.generateSound(this.def);
	// Der Inhalt des Vector ton wird nun in sco_lines kopiert
	for (int n= 0; n < ton.size(); n++)
	    sco_lines.addElement(ton.elementAt(n));
	return sco_lines;
    }
    private Vector doOszi() {
	Vector sco_lines = null;
	//-------------- generate and save the orc file:
	pr2 = new Project2(this.qd, this.Verbosity, (SoundInfoListener) this);
	if (this.def.newFOF) { // In fof mode check for default orc file
	    sco_lines = new ScoAccess().getFileContent(Home+File.separator+GetEnviroment.DEFAULTFOFORC);
	}
	if (sco_lines == null) {
	    sco_lines = new Vector();	
	    sco_lines = pr2.getOrc(this.def.newFOF);
	}
	if (!new ScoAccess().saveSco(CustomPath+oFile, this.User, sco_lines)) {
	    String txt[] = new String[2];
	    txt[0] = this.text[16]+CustomPath+oFile;
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);
	    al.setVisible(true);
	    return null;
	}
	sco_lines = null;
	//-------------- Generate and save the sco file:
	if (this.def.newFOF) { // In fof mode check for default sco headder file
	    sco_lines = new ScoAccess().getFileContent(Home+File.separator+GetEnviroment.DEFAULTFOFSCO);
	}
	if (sco_lines == null) {
	    sco_lines = new Vector();	
	    sco_lines = pr2.getHeader(this.def.newFOF);	// Hier holen wir uns den Anfang der sco Datei
	}
	// Dieser Vector (sco_lines) speichert alle Zeilen, die in die .sco datei geschrieben werden sollen
	// Nun ist der Headder der neuen sco Datei fertig
	// ------------------------------------------------------------
	// Als nächstes addieren wir mal ein paar Zeilen mit Tönen zum Vector
	// Der Vector ton speichert die Zeilen mit den Toenen
	ton = pr2.doProject(this.def);
	mi1_5.setEnabled(this.def.tonal & ton.size() > 0);
	play.setEnabled(this.def.tonal & ton.size() > 0);
	// Der Inhalt des Vector ton wird nun in sco_lines kopiert
	for (int n= 0; n < ton.size(); n++)
	    sco_lines.addElement(ton.elementAt(n));
	return sco_lines;
    }

/**
 * Method to react to a selection in the .len image choice
 */
    public void itemStateChanged(ItemEvent e) {
	int state = e.getStateChange();
	String item;
	Object o = (Object) e.getSource();
	if (o == cb) { 
	    if (state == e.SELECTED) {
		this.def.tonal = true;
	    }
	    else this.def.tonal = false;
	    boolean b = ton != null;
	    if (b) b = ton.size() > 0;
	    mi1_5.setEnabled(this.def.tonal & b);
	    play.setEnabled(this.def.tonal & b);
	    save.setEnabled(this.def.tonal & b & played);
	    cb2.setEnabled(this.def.tonal);
	}
	else if (o == cb1) {
	    if (state == e.SELECTED) {
		this.def.stereo = true;
	    }
	    else this.def.stereo = false;
	}
	else if (o == cb2) { // prefer lower notes (false !) 
	    if (state == e.SELECTED) {
		this.def.preferLowerNotes = true;
	    }
	    else this.def.preferLowerNotes = false;
	    System.out.println("preferLowerNotes="+this.def.preferLowerNotes);
	}
	else if (o == ch) { // min. Loudness
	    this.def.min_amp = this.ch.getSelectedItem();
	    ch1.removeAll();
	    setHChoice(ch1);
	    ch1.select(this.def.max_amp);
	}
	else if (o == ch1) { // max. Loudness
	    this.def.max_amp = this.ch1.getSelectedItem();
	    ch.removeAll();
	    setLChoice(ch);
	    //if (ch1.getSelectedIndex()  < this.def.min_amp) this.def.min_amp = this.def.max_amp;
	    ch.select(this.def.min_amp);
	}

	else if (o == ch2) { // min. Voice
	    this.def.min_voice = Integer.parseInt(this.ch2.getSelectedItem());
	    ch3.removeAll();
	    setHVoice(ch3);
	    ch3.select(Integer.toString(this.def.max_voice));
	}
	else if (o == ch3) { // max. Voice
	    this.def.max_voice = Integer.parseInt(this.ch3.getSelectedItem());
	    ch2.removeAll();
	    setLVoice(ch2);
	    if (this.def.max_voice < this.def.min_voice) this.def.min_voice = this.def.max_voice;
	    //System.out.println("Itemstatechanged: ch3:selected is:"+this.def.max_voice+" ch2.select:"+this.def.min_voice);
	    ch2.select(Integer.toString(this.def.min_voice));
	}
	else if (o == gam) { // Gamma value changed
	    item = this.gam.getSelectedItem();
	    this.def.gamma = Double.valueOf(item).doubleValue();
	}
	State = true;
	setValues(true);
    }

    public void stopChannel(int n) {
	if (isOn[n] == true  ) {
	    mid.channels[n].channel.noteOff(idx[n], ampl[n]);
	    mid.createShortEvent(mid.NOTEOFF+n, idx[n], mid.channels[n]);
	    //System.out.println("Channel "+n+" stop with "+idx[n]+" at :"+(System.currentTimeMillis()-SysStartTime));
	    //System.out.println("Channel "+n+" stop after :"+(System.currentTimeMillis()-tTime)+" akt[n]="+akt[n]+" idx[n]="+idx[n]);
	    isOn[n] = false;
	}
    }
public void actionPerformed(ActionEvent e) {
    // react to the menues
    String arg = e.getActionCommand();
    Object o = (Object) e.getSource();
    Class c = ((Object) tf).getClass();
    //System.out.println("actionPerformed e="+e+" at :"+(System.currentTimeMillis()-SysStartTime));
    if (ti != null) {
	for (int q = 0; q < 16; q++) {
	    if (ti[q] != null) {
		if (ti[q] == o)  {
		    stopChannel(q);
		    return;
		}
	    }
	}
    }
    if (o == mi1_1 ) {	// load
	mi1_1.setEnabled(false);
	// Bestimme den Pfad der Ausgabedatei
	selectPath();
	mi1_1.setEnabled(true);
    }
    else if (o == mi1_3 ) {	// load Template 
	loadTemplate();
    }
    else if (o == mi1_4 ) {	// save Template
	saveTemplate();
    }
    else if (o == mi1_2) {	// Quit
	doQuitWithInquire();
    }
    else if (o == mi2_2) {	// Oszi
	this.def.newFOF = false;	//fof = false;
	this.pane.remove(this.body);
	this.body = getBody();
	mi2_2.setEnabled(false);
	mi2_3.setEnabled(true);
	pane.add("Center", this.body);
	pack();
	State = true;
    }
    else if (o == mi2_3) {    // FOF
	this.def.newFOF=true; 	//fof = true;
	this.pane.remove(this.body);
	//this.body = getFOFBody();
	this.body = getBody();
	mi2_2.setEnabled(true);
	mi2_3.setEnabled(false);
	pane.add("Center", this.body);
	pack();
	State = true;
    }	
    else if (o == mi1_5) { // Play	/ Load	
	//doPlayMidi();
	
    }   
    else if (o == mi2_1) {	// About
	String txt[] = new String[12];
	txt[0] = this.text[34];

	txt[1] = this.text[39];
	txt[2] = this.text[40];
	txt[3] = " ";
	txt[4] = this.text[35];
	txt[5] = this.text[36];
	txt[6] = this.text[37];
	txt[7] = this.text[38];
	txt[8] = " ";
	txt[9] = this.text[66];
	txt[10] = this.text[67];
	txt[11] = this.text[68];
	
	//Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM, Alarmbox.LEFT, null);
	Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);	
	al.setVisible(true);
    }

    else if (o == stp) { // stop Button
	debugOut("------------------- Stop Button: Initiate halt --------------", 2);
	if (!playMidi) {
	    if (!this.halt) {
		this.halt = true;
		if (this.def.fof) pr3.halt = true;
		else pr2.halt = true;
		ru.stop();
	    }
	    stp.setEnabled(!this.halt);
	}
	else { // midi playing
	    if (!this.mHalt) {
		this.mHalt = true;
		pm.stop();
	    }
	    stp.setEnabled(!this.halt);
	}
	stp.setEnabled(!this.halt);
	setToWait(this, true);
    }

    else  if (c.isInstance(o) ) { // Textfield
	checkInput((TextField) e.getSource());
    }
}
    private void doPlayMidi() {
	setToWait(this, false);
	this.body.setEnabled(false);
	stp.setEnabled(true);
	start.setEnabled(false);
	save.setEnabled(false);
	mi1_5.setEnabled(false);
	play.setEnabled(false);
	this.pm = new PlayMidi();
	this.pm.start();
    }
    private void playKomposition() {
	if ( this.pr2.komposition == null | this.pr2.komposition.size() <= 0) return;
	mid.close();
	this.enableMidi = doMidiOpen();
	if (!this.enableMidi) {
	    setToWait(this, true);
	    return;
	}
	this.playMidi = true;
	Note nt;
	int sl, c;
	mid.startRecord(12); // instrument
	ti = new Timer[16]; 
	ampl = new int[16];
	idx = new int[16];
	akt = new int[16];
	double startTime = 0.0, old;
	Vector v = null, flow;
	flow = new Vector();
	v = new Vector();
	//-------- Combine the voices :
	for (int n = 0; n < this.pr2.komposition.size(); n++) {
	    old = startTime;
	    nt = (Note)  this.pr2.komposition.elementAt(n);
	    startTime = nt.start;
	    if (old != startTime) {
		// change
		if (v != null) {
		    flow.addElement(v);
		    //System.out.println("playKomposition() -------------");
		}
		v = new Vector();
		v.addElement(nt);	
		//System.out.println("playKomposition() n="+n+" start="+nt.start+" freq="+nt.freq+" nt.dauer="+nt.dauer+" nt.amplitude="+nt.amplitude);
		
	    }
	    else {
		v.addElement(nt);
		//System.out.println("playKomposition() n="+n+" start="+nt.start+" freq="+nt.freq+" nt.dauer="+nt.dauer+" nt.amplitude="+nt.amplitude);
	    }
	}
	// Instruments:
	for (int i = 0; i < mid.channels.length; i++) {
	    mid.channels[i].channel.programChange(10+i*3);
	    mid.createShortEvent(mid.PROGRAM, 10+i*3, mid.channels[i]);//Instrument change
        }
	double maxd = 0.0;
	int sleep, slp;
	// loop over komposition
	for (int n = 0; n < flow.size(); n++) {
	    maxd = 0.0; // reset max-dauer
	    v = (Vector) flow.elementAt(n);
	    // find the delay until the next note:
	    int[] drw = new int[v.size()];
	    for ( c = 0; c < v.size(); c++) {
		nt = (Note)  v.elementAt(c);
		if (nt.dauer > maxd) maxd = nt.dauer;
		drw[c] = (int) nt.freq;
	    }
	    sleep = (int) (maxd * 1000.0);
	    int millis;
	    SysStartTime = System.currentTimeMillis(); //
	    //System.out.println("playKomposition() -----> new interval n="+n+" with "+sleep+" ms");
	    this.gc.showNote(pr2.rt, drw);
	    // Loop over all notes in this time-intervall
	    for ( c = 0; c < v.size(); c++) {
		nt = (Note)  v.elementAt(c);
		sl = (int) (nt.dauer * 1000.0);
		//System.out.println("playKomposition() c="+c+" start="+nt.start+" freq="+nt.freq+" nt.dauer="+nt.dauer+" nt.amplitude="+nt.amplitude+" bal="+nt.balance);
		// init time if needed
		if (ti[c] == null) {
		    ti[c] = new Timer(sl, this);
		    ti[c].setRepeats(false);
		}
		akt[c] = idx[c];
		idx[c] = this.pr2.getNoteIndex(nt.freq);
		ampl[c] = pr2.mapAplitude(nt.amplitude);
		balance[c] = (int) ((nt.balance - 0.5) * 127.0 + 0.5) + 64;
		//----------------- Timer ---
		ti[c].setInitialDelay(sl);
		//System.out.println("Channel "+c+" start with delay="+sl+" initdely="+ti[c].getInitialDelay());
		ti[c].restart();
		//System.out.println("playKomposition() note "+c+" dauer="+sl+" ms");
		tTime = System.currentTimeMillis();    
		isOn[c] = true;
		//---------- Play it now:--------------
		// ----------- Stereo -------y
		mid.channels[c].channel.controlChange(mid.PAN, balance[c]); 	
		mid.createControlEvent(mid.PAN, balance[c], mid.channels[c]);
		//------------ Note ---------
		mid.channels[c].channel.noteOn(idx[c], ampl[c]);
		mid.createShortEvent(mid.NOTEON + c, idx[c], mid.channels[c]);
		//System.out.println("playKomposition() note="+idx[c]+" nt.amplitude="+nt.amplitude+" volume="+ampl[c]+" balance="+balance[c]);
		
	    }
	    millis = (int) (System.currentTimeMillis() - SysStartTime);
	    slp = sleep - millis;
	    try {
		java.lang.Thread.sleep(slp);
	    }	
	    catch (InterruptedException e){}
	    //System.out.println("playKomposition() ------------- end of intervall, slept "+(slp));	
	    for ( c = 0; c < v.size(); c++) stopChannel(c);
	    //System.out.println("playKomposition()  end of intervall. real time needed="+(System.currentTimeMillis() - SysStartTime));
	    if (this.mHalt) break; 
	}
	/*
	//----------- Play the midi seq.:
	try {
	    mid.sequencer.open();
	    mid.sequencer.setSequence(mid.sequence);
	} catch (Exception ex) { 
	    ex.printStackTrace(); 
	}
	mid.sequencer.start();
	setToWait(this, true);
	mid.sequencer.stop();
	*/
	setToWait(this, true);
	played = true;
    }

    public void saveMidi() {
	//------save --------
	String midf = CustomPath+oFile;
	int id = midf.lastIndexOf(".");
	midf = midf.substring(0, id);
	midf = midf + ".mid";
	mid.saveMidiFile(new File(midf));
    }
  
    public void displayRT(RandomTable rt) {
	if (this.gc != null) gc.setTable(rt, this.def.gamma);
    }

public void focusGained(FocusEvent e) {
    Object o = (Object) e.getSource();
    debugOut("focusGained: Object o="+o.toString(), 5);
    Class c = ((Object) tf).getClass();
    if (c.isInstance(o) ) ((TextField) e.getSource()).selectAll();
}

public void focusLost(FocusEvent e) {
    // treat like user hit 'Return'
    Object o = (Object) e.getSource();
    debugOut("focuslost: Object o="+o.toString(), 5);
    Class c = ((Object) tf).getClass();
    if (c.isInstance(o)) checkInput((TextField) e.getSource());
}
/** 
 * React to Start Button.
 */
    class STListener implements ActionListener {
    public void actionPerformed(ActionEvent ev) {
	if (ev.getSource() == start) { // start Button
	    body.setEnabled(false);
	    start.setEnabled(false);
	    stp.setEnabled(true);
	    save.setEnabled(false);
	    ru = new Runner();
	    ru.start();
	}
	else if (ev.getSource() == play) { // start Button
	    doPlayMidi();
	}
	else if (ev.getSource() == save) { // save Button
	    saveMidi();
	}
    }	
    }// end of inner class
/** 
 * React to Window Events.
 */
    class MDListener extends WindowAdapter {
    public void windowClosing(WindowEvent ev) {
	String arg;
	AWTEvent event = (AWTEvent) ev;
	if (event.getID() == Event.WINDOW_DESTROY) {
	    //dispose();
	    doQuitWithInquire();
	}	
    }	
    }// end of inner class



public void checkInput(TextField tfi)
    throws NumberFormatException {

    int x = 0, tmp = 0,disp = 0;
    double dTmp = 0.0, dx = 0.0, dDisp = 0.0;
    String wert;
    boolean tst = false;
    int elcode = -1;
    if (tfi == tf) elcode = 1; // maxFreq
    else if (tfi == tf1) elcode = 2; // minFeq
    else if (tfi == tf2) elcode = 3; // Population
    else if (tfi == tf3) elcode = 4; // fittest
    else if (tfi == tf4) elcode = 5; // interval
    else if (tfi == tf5) elcode = 6; // min_tempo
    else if (tfi == tf6) elcode = 7; // r_weight
    else if (tfi == tf7) elcode = 8; // iterations
    else if (tfi == tf8) elcode = 9; // diff freq
    else if (tfi == tf9) elcode = 10; // max_tempo
    else if (tfi == tf12) elcode = 11; // seed

    switch(elcode) {
    case 1: tmp = this.def.max_freq;
	break;
    case 2: tmp = this.def.min_freq;
	break;
    case 3: tmp = this.def.population;
	break;
    case 4: tmp = this.def.fittest;
	break;
    case 5: tmp = this.def.interval;
	break;
    case 6: dTmp = this.def.min_tempo;
	break;
    case 7: dTmp = this.def.r_Weight;
	break;
    case 8: tmp = this.def.iterations;
	break;
    case 9: tmp = this.def.diff_freq;
	break;
    case 10: dTmp = this.def.max_tempo;
	break;
    case 11: tmp = this.def.seed;
	break;
    }
    String t;
    t = tfi.getText();
    try { //
	if (elcode == 6 || elcode == 7 || elcode == 10) {
	    dDisp = dTmp ;
	    dx = Double.valueOf(tfi.getText()).doubleValue();
	}
	else {
	    disp = tmp;
	    x = Integer.parseInt(tfi.getText());
	}
	switch(elcode) {
	case 1: this.def.max_freq = x;
	    disp = this.def.max_freq;
	    if (disp <= 0 || disp <= this.def.min_freq) throw(new NumberFormatException());
	    break;
	case 2: this.def.min_freq = x;
	    disp = this.def.min_freq;
	    if (disp <= 0 || disp >= this.def.max_freq) throw(new NumberFormatException()); 
	    break;
	case 3: this.def.population = x;	
	    disp = this.def.population;
	    if (disp < this.def.fittest ) disp = this.def.fittest; //throw(new NumberFormatException()); 
	    break;
	case 4: this.def.fittest = x;	
	    disp = this.def.fittest;
	    //System.out.println("CheckInput: Textfield.getText()="+t+" Fittest ="+this.def.fittest+" population="+this.def.population+" disp="+disp);
	    if (disp > this.def.population ) disp = this.def.population; // throw(new NumberFormatException()); 
	    break;
	case 5: this.def.interval = x; // Hz	
	    disp = this.def.interval;
	    if (disp <= 0 || disp > this.def.max_freq) throw(new NumberFormatException()); 
	    break;
	case 6: this.def.min_tempo = dx; // time	
	    dDisp = this.def.min_tempo;
	    //System.out.println("CheckInput: Textfield.getText()="+t+" min_tempo ="+this.def.min_tempo+" dDisp="+dDisp);
	    if (dDisp <= 0.0 || dDisp > this.def.max_tempo) throw(new NumberFormatException()); 
	    break;
	case 7: this.def.r_Weight = dx; 
	    dDisp = this.def.r_Weight;
	    if (dDisp <= 0.0 ) throw(new NumberFormatException()); 
	    break; 	  
	case 8: this.def.iterations = x; // anzahl	
	    //
	    disp = this.def.iterations;
	    if (disp <= 0 ) throw(new NumberFormatException()); 
	    break;
	case 9: this.def.diff_freq = x; // how much is influenced	
	    disp = this.def.diff_freq;
	    if (disp <= 0 ) throw(new NumberFormatException()); 
	    break;
	case 10: this.def.max_tempo = dx; // time	
	    dDisp = this.def.max_tempo;
	    //System.out.println("CheckInput: Textfield.getText()="+t+" max_tempo ="+this.def.max_tempo+" dDisp="+dDisp);
	    if (dDisp <= 0.0 || dDisp < this.def.min_tempo) throw(new NumberFormatException()); 
	    break;
	case 11: this.def.seed = x; // 
	    disp = this.def.seed;
	    if (disp < 0 || disp > this.def.max_freq) throw(new NumberFormatException()); 
	    break;
	}// end of switch
	tst = true;
    } catch (java.lang.NumberFormatException e) {
	if (elcode == 6 || elcode == 7) dDisp = dTmp;
	else disp = tmp;
	switch(elcode) {
	case 1: this.def.max_freq = tmp;
	    break;
	case 2: this.def.min_freq = tmp;
	    break;
	case 3: this.def.population = tmp;
	    break;
	case 4: this.def.fittest = tmp;
	    break;
	case 5: this.def.interval = tmp;
	    break;
	case 6: this.def.min_tempo = dTmp;
	    break;
	case 7: this.def.r_Weight = dTmp;
	    break;
	case 8: this.def.iterations = tmp;
	    break;
	case 9: this.def.diff_freq = tmp;
	    break;
	case 10: this.def.max_tempo = dTmp;
	    break;
	case 11: this.def.seed = tmp;
	    break;
	}  
	tst = false;
    } // end of catch
    
    if (elcode == 6 || elcode == 7 || elcode == 10) wert = Converter.formatDouble(dDisp, this.digits, this.post);
    else wert = Converter.formatInt(disp, this.digits);
	
    debugOut("nun setze das angewaehlte Textfield mit : "+wert, 3);
    tfi.setText(wert); // update Textfield with corrected or old value
    State = State | tst;
}// end of checkInput    

public void doQuitWithInquire() {
    String txt[] = new String[2];
    txt[0] = this.text[10];
    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.CONFIRMATION);
    al.setVisible(true);
    if (!al.OK) {
	// do not abort !
	return;
    }
    doQuit();
}

public void doQuit() {
    debugOut(" Quit: State="+State, 2);
    if (State) { // just when changed
	//def.def.fof = this.fof;
	if (!def.saveDef(Home+File.separator+GetEnviroment.SAVEFILE, this.User)) {
	    debugOut("CEditor:Can not save default settings !", 1);
	}
    }
    if (child) {
	super.dispose();
    }
    else {	// leave completely
	System.exit(0);
    }
}

    public void setEnviroment(String home, String user) {
	this.Home = home;
	this.User = user;
	this.child = true;
    }

public String[] getText(String[] t) {
    t[0] = "JcSelf "+GetEnviroment.sVersionCode+" © Copyright by Nils Kay, Peter Heeren. All rights reserved (2000-2003)";
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
//t[38] = "";
//t[36] = "";


    return t;
}

/**
 * Set The Cursor to Wait or normal state and disable/enable whatever neccassary.
 */
public void setToWait(Component Co, boolean w) {
    Cursor cu;
    if (!w) cu = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    else cu = Cursor.getDefaultCursor();
    Co.setCursor(cu);
    //this.setEnabled(w);
    start.setEnabled(w);
    Color col = Color.black;
    if (!w) col = Color.red;
    this.pt.setColor(col);
    this.pr.setColor(col);
    this.body.setColor(col);
}

    class Runner implements Runnable{
	private volatile Thread evoThread = null;
	public void start() {
	    halt = false;
	    try {
		if (evoThread == null) {
		    evoThread = new Thread(this," Iterate Composition");
		}
	    if (evoThread != null) evoThread.start();
	    } catch (IllegalThreadStateException ex) {
		this.start();  // ??? or what
	    }
	}

	public void stop() {
	    stp.setEnabled(false);
	    start.setEnabled(true);
	    save.setEnabled(played);
	    debugOut("CEditor: stopping", 2);
	    body.setEnabled(true);
	    evoThread = null;
	    /*if (evoThread != null) {
		evoThread.stop();
		evoThread = null;
	    }
	    */
	    //System.out.println("CEditor: stopping done");
	}

	public void run() {
	    Thread thisThread = Thread.currentThread();
	    while(evoThread == thisThread) {
		debugOut("CEditor: RUN", 2);
		doScoFile();
		halt = true; 
		this.stop();
	    }
	}
    }

    class PlayMidi implements Runnable{
	private volatile Thread midiThread = null;
	public void start() {
	    mHalt = false;
	    try {
		if (midiThread == null) {
		    midiThread = new Thread(this," Play Composition");
		}
	    if (midiThread != null) midiThread.start();
	    } catch (IllegalThreadStateException ex) {
		this.start();  // ??? or what
	    }
	}

	public void stop() {
	    body.setEnabled(true);
	    stp.setEnabled(false);
	    start.setEnabled(true);
	    save.setEnabled(played);
	    //System.out.println("Play komp. ton="+ton);
	    mi1_5.setEnabled(def.tonal & ton.size() > 0);
	    play.setEnabled(def.tonal & ton.size() > 0);
	    midiThread = null;
	}

	public void run() {
	    Thread thisThread = Thread.currentThread();
	    while(midiThread == thisThread) {
		playKomposition();
		mHalt = true; 
		this.stop();
	    }
	}
    }

    public boolean examineOptions(String in) {
	System.out.println("OPtions = "+in);
	boolean d = false;
	if (in == null) return false;
	String tmp;
	in = in.toLowerCase();
	in = in.trim();
	if (!in.startsWith("-")) return false;
	in = in.substring(1, in.length());	// remove the - char
	tmp = in;
	if ((in.indexOf("d")) > -1) d = true;	// debug flag is set !
	int i;
	if ((i = in.indexOf("v")) > -1) { // verbosity
	    in = in.substring(i+1, in.length());
	    i = Converter.getInt(in, 1);
	    this.Verbosity = i;
	}
	/*if ((i = tmp.indexOf("f")) > -1) { // work as fof, not osci
	    this.fof = true;
	    //System.out.println("FOF flag found!");
	    }*/
	return d;
    }

public static void main(String args[]){
    String options = null;
    try {
	if (args[0] != null) options = args[0]; 
    } catch (ArrayIndexOutOfBoundsException e) {}
    
    CEditor lb = new CEditor(null, null, null);
    lb.pack();
    lb.setVisible(true);
}
}// end of class






