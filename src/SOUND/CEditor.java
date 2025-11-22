package Sound;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.text.*;
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
    boolean halt = false;
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
    // --------- Other classes: ---------------
    Defaults def;
    GraphCanvas gc;
    CDebug qd;
    Project2 pr2;
    Project3 pr3;
    // --------- The UI Elements: --------------
    Font font;
    Font subFont;
    Label la, la1, la2,la3,la4, la5, la6, la7, la8, la9, la10, la11, la12, la13, la14, la15;
    Label lau, lau1, lau2, lau3,lau4,lau5, lau6, lau8, lau9, lau12;
    TextField tf, tf1, tf2, tf3, tf4, tf5, tf6, tf7, tf8, tf9, tf12;
    Button start, stp;
    Checkbox cb, cb1;
    Choice ch, ch1, ch2, ch3, gam;	
    MenuBar mb=null;		//Der Menubalken
    Menu m1, m2;			//Das Menu im Balken
    MenuItem   mi1_1, mi1_2, mi2_1, mi2_2, mi2_3;		//
    Panel pane;
    BorderPanel body;
    // ---------- variables -------------
    String options = null;
    boolean debug = false;
    /**
     * Verbosity: 0= no, 1=important Messages, 2=less important, 3=low debug, 4=higher debug, 5 = high debug, 6 = crazy debug
     */
    int Verbosity = 1;
    boolean fof = false; // if true, we work as FOF generator, not osci
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
    ygap = 1 ;	// vert.gap 
    min = 2 ;	// minimum gap
    this.State = false;
    this.sFile = null; 
    Toolkit tool = Toolkit.getDefaultToolkit();
    Dimension d = tool.getScreenSize();
    Dimension gd;
    System.out.println("Screensize="+d);
    if (d.width <= 800) {
	this.font = new Font("Helvetica", Font.PLAIN, fontsize);
	this.subFont = new Font("Helvetica", Font.PLAIN, subFontsize);
	gd = new Dimension(550, 150);
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
    this.text = getText(this.text);
    // user defaults:
    this.def = new Defaults();
    boolean defLoaded = this.def.loadDef(Home+File.separator+GetEnviroment.SAVEFILE);
    if (def.lastPath != null) this.CustomPath = def.lastPath;
    //this.fof = def.fof;
    this.fof = false;	// as long as FOF is disabled.
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
    if (!this.fof) this.body = getBody(); // Oszi
    else this.body = getFOFBody();
    pane.add("Center", this.body);
    mi2_2.setEnabled(this.fof);
    mi2_3.setEnabled(!this.fof);
    // --------- Start Button ------------
    Panel bot = new Panel();
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

    /**
     * Adds the various Menu Items to the Menubar
     **/

public void setMenubar() {
    // erzeuge das Menu
    m1 = new Menu(this.text[11],true);	// File Tear off Menu(muss clicken)
    m1.setFont(this.font); 
    mb.add(m1);				// zum Balken addieren

    mi1_1 = new MenuItem(this.text[12]); // Load
    mi1_1.setFont(this.font);
    mi1_1.addActionListener(this);
    m1.add(mi1_1);				//Addiere MenuPunkt zum Menu
    mi1_1.setEnabled(true);
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
    m2.add(mi2_2);	
    mi2_3 = new MenuItem(this.text[47]); // FOF
    mi2_3.setFont(this.font);
    mi2_3.addActionListener(this);
    //m2.add(mi2_3);
    m2.addSeparator();
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
    BorderPanel pr = new BorderPanel();	//Random related
    BorderPanel pt = new BorderPanel();	//Tone related
    //Font Tfont = new Font("TimesRoman", Font.PLAIN, 10);
    int gap = 0;
    pr.setTextFont(this.subFont); 
    pr.setText(this.text[50]);
    pr.setGap(gap);
    pt.setTextFont(this.subFont); 
    pt.setText(this.text[51]);
    pr.setGap(gap);
    pb.setText(this.text[48]);
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
    public void setLChoice(Choice c) {
	int n;
	for(n = 0; n < Project2.Loudness.length; n++) {
	    c.add(Project2.Loudness[n]);
	    if (def.max_amp.equals(Project2.Loudness[n])) break;
	}
    }

    public void setHChoice(Choice c) {
	int n;
	boolean flag = false;
	for(n = 0; n < Project2.Loudness.length; n++) {
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
	if (!this.fof) sco_lines = doOszi();
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
	
	Vector ton = pr3.generateSound(this.def);
	// Der Inhalt des Vector ton wird nun in sco_lines kopiert
	for (int n= 0; n < ton.size(); n++)
	    sco_lines.addElement(ton.elementAt(n));
	return sco_lines;
    }
    private Vector doOszi() {
	Vector sco_lines = new Vector();	
	// generate and save the orc file:
	pr2 = new Project2(this.qd, this.Verbosity, (SoundInfoListener) this);
	sco_lines = pr2.getOrc();
	//ScoAccess sc = new ScoAccess();
	if (!new ScoAccess().saveSco(CustomPath+oFile, this.User, sco_lines)) {
	    String txt[] = new String[2];
	    txt[0] = this.text[16]+CustomPath+oFile;
	    Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM);
	    al.setVisible(true);
	    return null;
	}
	// Generate and save the sco file:
	sco_lines = pr2.getHeader();	// Hier holen wir uns den Anfang der sco Datei
	// Dieser Vector (sco_lines) speichert alle Zeilen, die in die .sco datei geschrieben werden sollen
	// Nun ist der Headder der neuen sco Datei fertig
	// ------------------------------------------------------------
	// Als nächstes addieren wir mal ein paar Zeilen mit Tönen zum Vector
	// Der Vector ton speichert die Zeilen mit den Toenen
	Vector ton = pr2.doProject(this.def);
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
	}
	else if (o == cb1) {
	    if (state == e.SELECTED) {
		this.def.stereo = true;
	    }
	    else this.def.stereo = false;
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

public void actionPerformed(ActionEvent e) {
    // react to the menues
    String arg = e.getActionCommand();
    Object o = (Object) e.getSource();
    Class c = ((Object) tf).getClass();
    if (o == mi1_1 ) {	// load
	mi1_1.setEnabled(false);
	// Bestimme den Pfad der Ausgabedatei
	selectPath();
	mi1_1.setEnabled(true);
    }// end of
    else if (o == mi1_2) {	// Quit
	doQuitWithInquire();
    }
    else if (o == mi2_2) {	// Oszi
	this.fof = false;
	this.pane.remove(this.body);
	this.body = getBody();
	mi2_2.setEnabled(false);
	mi2_3.setEnabled(true);
	pane.add("Center", this.body);
	pack();
	State = true;
    }
    else if (o == mi2_3) {    // FOF
	this.fof = true;
	this.pane.remove(this.body);
	this.body = getFOFBody();
	mi2_2.setEnabled(true);
	mi2_3.setEnabled(false);
	pane.add("Center", this.body);
	pack();
	State = true;
    }	
    else if (o == mi2_1) {	// About
	String txt[] = new String[8];
	txt[0] = this.text[34];
	txt[1] = this.text[35];
	txt[2] = this.text[36];
	txt[3] = this.text[37];
	txt[4] = this.text[38];
	txt[5] = this.text[39];
	txt[6] = this.text[40];
	txt[7] = this.text[41];
	Alarmbox al = new Alarmbox(this, this.text[1], txt, 12, Alarmbox.ALARM, Alarmbox.LEFT, null);
	al.setVisible(true);
    }

    else if (o == stp) { // stop Button
	debugOut("------------------- Stop Button: Initiate halt --------------", 2);
	if (!this.halt) {
	    this.halt = true;
	    if (this.fof) pr3.halt = true;
	    else pr2.halt = true;
	    ru.stop();
	}
	stp.setEnabled(!this.halt);
	setToWait(this, true);
    }

    else  if (c.isInstance(o) ) { // Textfield
	checkInput((TextField) e.getSource());
    }
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
	    start.setEnabled(false);
	    stp.setEnabled(true);
	    ru = new Runner();
	    ru.start();
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
	    if (disp < this.def.fittest ) throw(new NumberFormatException()); 
	    break;
	case 4: this.def.fittest = x;	
	    disp = this.def.fittest;
	    //System.out.println("CheckInput: Textfield.getText()="+t+" Fittest ="+this.def.fittest+" population="+this.def.population+" disp="+disp);
	    if (disp > this.def.population ) throw(new NumberFormatException()); 
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
	def.fof = this.fof;
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
    t[0] = "JcSelf Version 1.06 alpha2 © Copyright by Nils Kay, Peter Heeren. All rights reserved (2000-2001)";
    t[1] = "Alarmbox"; 
    t[10] = "Do you really want to quit ?";
    t[11] = "File";
    t[12] = "Select Path";
    t[13] = "Quit";
    t[14] = "Start";
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
    t[35] = "This program is used to generate an 'orchestra file' and a 'score file'for CSound.";
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
}

    class Runner implements Runnable{
	private Thread evoThread = null;
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
	    debugOut("CEditor: stopping", 2);
	    if (evoThread != null) {
		evoThread.stop();
		evoThread = null;
	    }
	    //System.out.println("CEditor: stopping done");
	}

	public void run() {
	    debugOut("CEditor: RUN", 2);
	    doScoFile();
	    halt = true; 
	    this.stop();
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






