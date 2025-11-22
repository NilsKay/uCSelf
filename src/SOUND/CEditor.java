package SOUND;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*; 

import java.io.*;
import java.util.Vector;

import java.lang.Thread;
import Utils.*; 


/**
 * This class is used to generate a control file for csound
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class CEditor extends JFrame implements ItemListener, FocusListener, ActionListener, SoundInfoListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = 85632L;
	Window wi;
	
    int w = 200, h = 100;	// initial Windowsize
    Color fg,he;
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
    //Project3 pr3;
    MyMidi mid;
    Timer[] ti;
    // --------- The UI Elements: --------------
    Font font;
    Font subFont;
    private JLabel[] la, uni;
    private JTextField[] ptfs;
    private JComboBox[] combo;
    // Label laa, la1, la2,la3,la4, la5, la6, la7, la8, la9, la10, la11, la12, la13, la14, la15, la16, la17;
    //Label lau, lau1, lau2, lau3,lau4,lau5, lau6, lau8, lau9, lau12;
    //TextField tf, tf1, tf2, tf3, tf4, tf5, tf6, tf7, tf8, tf9, tf12, tf17;
    JButton start, stp, play, save, select;
    JCheckBox cb, cb1, cb2, cb3;
    //Choice ch, ch1, ch2, ch3, gam, ch4;	
    JMenuBar mb=null;		//Der Menubalken
    JMenu m1, m2;			//Das Menu im Balken
    JMenuItem   mi1_1, mi1_2, mi2_1, mi2_2, mi2_3, mi1_3, mi1_4, mi1_5, instr ;		//
    JCheckBoxMenuItem mi2_4, mi2_5;
    JPanel pane;
    JPanel body;
    JPanel pr, pt;
    //-------- L&F-------
    Color tbg = Color.white;
    Color bg = Color.lightGray;
    Color tfg = Color.black;
    Color cbg = Color.white;
    @SuppressWarnings("rawtypes")
	// ---------- variables -------------
    Vector ton;
    String options = null;
    boolean debug = false;
    boolean enableFOF = false, playMidi = false, played = false, enableMidi = true;
    int idx[], ampl[], akt[];
    boolean isOn[] = new boolean[16];
    int balance[] = new int[16];
    long SysStartTime, tTime;
    int max = 18;
    boolean busy = true, ignore;
    /**
     * Verbosity: 0= no, 1=important Messages, 2=less important, 3=low debug, 4=higher debug, 5 = high debug, 6 = crazy debug
     */
    int Verbosity = 1;
    //boolean fof = false; // if true, we work as FOF generator, not osci
    
    String CustomPath = null;
    String sFile = null;
    String oFile = null;
    public PrintWriter prs = null;
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
@SuppressWarnings("unused")
public CEditor(String home, String user, String opt) {
    
    la = new JLabel[max];
    uni = new JLabel[max];
    ptfs = new JTextField[max];
    combo = new JComboBox[max];
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
	
	//debugOut("GetEviroment got: SystemPath="+gsp.getEnviroment()+" ClassPath="+gsp.getClassPath()+" Home="+gsp.getHome()+" User="+gsp.getUser(), 2);
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
    //------------ Text debug: --------
    File log = new File(Home+File.separator+GetEnviroment.LOGFILE);
    try {
	this.prs = new PrintWriter(new FileOutputStream(log));
	// Headder Information
	this.prs.println("# Logfile File for JcSelf.");
    }
    catch (java.io.IOException e) {
	this.prs = null;
    } // End of IO Excep

    // user defaults:
    setLF();
    this.def = new Defaults();
    if (Home == null)
    	Home =  System.getProperty("user.home");
    boolean defLoaded = this.def.loadDef(Home+File.separator+GetEnviroment.SAVEFILE);
    if (def.lastPath != null) this.CustomPath = def.lastPath;
    this.mid.open(def);
 // --------- Select Button ------------
   
    select = new JButton(this.def.text[74]);
    select.setFont(this.subFont);
    select.addActionListener(new STListener());
    this.select.setEnabled(this.def.useSelectNotes);
    //this.fof = def.fof;
    //this.fof = false;	// as long as FOF is disabled.
    setFont(this.font);
    mb = new JMenuBar();
    mb.setFont(this.font); 
    setMenubar(); // erzeugt das Menu hier in guimain
    setJMenuBar(mb);
    bg = Color.lightGray;
    pane = new JPanel();
    pane.setBackground(bg);
    fg = Color.black;
    pane.setForeground(fg);
    pane.setLayout(new BorderLayout());
    JPanel top = new JPanel();
    top.setBackground(bg);
   // top.setGap(0);
    gc = new GraphCanvas(gd, this.subFontsize, this.bg);
    top.add(gc);
    pane.add("North", top);
    if (options == null) options = gsp.getOPTIONS();
    this.debug = examineOptions(options);
    if ( this.debug) {
    	this.qd = new CDebug(this, "Debug Tool, just for Nils");
    	this.qd.setVisible(true);
    }
    if (!enableFOF) this.def.fof = false; // oszi
    /* if (!this.def.fof) this.body = getBody(); // Oszi
    else this.body = getFOFBody();
    */
    this.body = getBody(); // Oszi
    pane.add("Center", this.body);
    //mi2_2.setEnabled(this.def.newFOF);	//fof);
    //mi2_3.setEnabled(!this.def.newFOF);	//.fof);
    mi1_5.setEnabled(false);
    JPanel bot = new JPanel();
    // --------- Play Button ------------
    play = new JButton(this.def.text[63]);
    play.setFont(this.subFont);
    play.addActionListener(new STListener());
    bot.add(play);
    play.setEnabled(false);
    // --------- Save Button ------------
    save = new JButton(this.def.text[64]);
    save.setFont(this.subFont);
    save.addActionListener(new STListener());
    bot.add(save);
    save.setEnabled(false);
    // --------- Start Button ------------
    start = new JButton(this.def.text[14]);
    start.setFont(this.subFont);
    start.addActionListener(new STListener());
    bot.add(start);
    start.requestFocus();
 
    // --------- Stop Button ------------
    stp = new JButton(this.def.text[31]);
    stp.setFont(this.subFont);
    stp.addActionListener(this);
    bot.add(stp);
    stp.setEnabled(false);
    
   // bot.add(select);
   
    pane.add("South", bot);	
   
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new MDListener());
    this.setContentPane(pane);
    //this.add(pane);
    setActual();
    pack();
    relocate();
    
    super.setTitle(this.def.text[0]);
    debugOut(this.def.text[0], 1);
    busy = false;
} // end of constructor

    @SuppressWarnings("unused")
	private JPanel getTextPanel(int index, String latext, String unit) {
	JPanel p = new JPanel();
	int vgap = 6;
	int hgap = 10;
	//int top, int left, int bottom, int right) 
	p.setBorder( BorderFactory.createEmptyBorder(vgap, hgap, vgap, hgap));// top, left, bottom, right
	p.setLayout(new BorderLayout());
	p.setBackground(this.bg);
	p.setForeground(this.tfg);
	la[index] = new JLabel(latext, JLabel.LEFT);
	la[index].setFont(this.subFont);
	p.add("North", la[index]);
	double d = 0.0;
	ptfs[index] = new JTextField(this.formatValue(0.0), this.digits);
	ptfs[index].setBackground(this.tbg);
	ptfs[index].addActionListener(this);
	ptfs[index].addFocusListener(this);
	//ptfs[index].addMouseListener(new OHMouse(gui.hp, otext));// For Online Help
	p.add("Center", ptfs[index]);
	if (unit != null) {
	    uni[index] = new JLabel(unit, JLabel.LEFT);
	    uni[index].setFont(this.subFont);
	    p.add("East", uni[index]);
	}
	return p;
    }
    
    
    /**
     * A Panel for a JComboBox Component
     */ 
    @SuppressWarnings({ "rawtypes", "unused" })
	public JPanel getChoicePanel(int index, String latext, Vector elements) {
	JPanel p = new JPanel();
	p.setLayout(new BorderLayout());
	int gap = 2;
	p.setBorder( BorderFactory.createEmptyBorder(gap, gap, gap, gap));
	p.setBackground(this.bg);
	la[index] = new JLabel(latext, JLabel.LEFT);
	la[index].setFont(this.subFont);
	p.add("North", la[index]);
	double d = 0.0;
	combo[index] = new JComboBox(elements);
	combo[index].setFont(this.subFont);
	/*for (int n = 0; n < elements.size(); n++) 
	    combo[index].addItem(elements.elementAt(n).toString()); 	
	*/
	combo[index].setBackground(this.cbg);
	combo[index].addActionListener(this);
	//combo[index].addMouseListener(new OHMouse(ep.hp, otext));// For Online Help
	//la[index].addMouseListener(new OHMouse(ep.hp, otext));// For Online Help
	p.add("Center", combo[index]);
	return p;
    }

    private boolean doMidiOpen() {
		this.enableMidi = this.mid.open(this.def);
		if (!enableMidi) {
		    System.out.println("MidiOpen failed, msgs="+this.def.text[69]);
		  /*  String txt[] = new String[2];
		    txt[0] = this.def.text[69];
		    Utils ut = new Utils();
		    Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM);	
		    al.setVisible(true);
		    */
		    new Utils().doMessagePane( this.def.text[69]);
		}
		return this.enableMidi;
    }
    /**
     * Adds the various Menu Items to the Menubar
     **/
public void setMenubar() {
    // erzeuge das Menu
    m1 = new JMenu(this.def.text[11],true);	// File Tear off Menu(muss clicken)
    m1.setFont(this.font); 
    mb.add(m1);				// zum Balken addieren

    mi1_1 = new JMenuItem(this.def.text[12]); // Select Path
    mi1_1.setFont(this.font);
    mi1_1.addActionListener(this);
    m1.add(mi1_1);				//Addiere MenuPunkt zum Menu
    mi1_1.setEnabled(true);
    m1.addSeparator();
    mi1_3 = new JMenuItem(this.def.text[56]); // Load Template
    mi1_3.setFont(this.font);
    mi1_3.addActionListener(this);
    m1.add(mi1_3);				//Addiere MenuPunkt zum Menu
    mi1_3.setEnabled(true);
    mi1_4 = new JMenuItem(this.def.text[55]); // Save Template
    mi1_4.setFont(this.font);
    mi1_4.addActionListener(this);
    m1.add(mi1_4);				//Addiere MenuPunkt zum Menu
    mi1_4.setEnabled(true);
    m1.addSeparator();
    mi1_2 = new JMenuItem(this.def.text[13]); // Quit
    mi1_2.setFont(this.font);
    mi1_2.addActionListener(this);
    m1.add(mi1_2);				//Addiere MenuPunkt zum Menu
    mi1_2.setEnabled(true);

    m2 = new JMenu(this.def.text[33],true);	// File Tear off Menu(muss clicken)
    m2.setFont(this.font); 
    mb.add(m2);				// zum Balken addieren
    /*
    mi2_2 = new JMenuItem(this.def.text[48]); // Oszi
    mi2_2.setFont(this.font);
    mi2_2.addActionListener(this);
    if ( enableFOF ) m2.add(mi2_2);	
    mi2_3 = new JMenuItem(this.def.text[47]); // FOF
    mi2_3.setFont(this.font);
    mi2_3.addActionListener(this);
    if ( enableFOF ) m2.add(mi2_3);
    m2.addSeparator();
    */
    mi2_4 = new JCheckBoxMenuItem("Display 2D"); // 2d
    mi2_4.setFont(this.font);
    mi2_4.addActionListener(this);
    m2.add(mi2_4);	
    mi2_5 = new JCheckBoxMenuItem("Display 3D"); // 3d
    mi2_5.setFont(this.font);
    mi2_5.addActionListener(this);
    m2.add(mi2_5);
    m2.addSeparator();
    //---------------
    mi1_5 = new JMenuItem(this.def.text[70]); // Play
    mi1_5.setFont(this.font);
    mi1_5.addActionListener(this);
    //m2.add(mi1_5);
    //m2.addSeparator();
    //--
    instr = new JMenuItem(this.def.text[89]); // Change instr.
    instr.setFont(this.font);
    instr.addActionListener(this);
    m2.add(instr);
    m2.addSeparator();
    mi2_1 = new JMenuItem(this.def.text[32]); // About
    mi2_1.setFont(this.font);
    mi2_1.addActionListener(this);
    m2.add(mi2_1);		
}

/**
 * Oszi panel 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
private JPanel getBody() {
    int row = 0;
    JPanel pb = new JPanel();
    pb.setFont(this.font); 
    this.pr = new JPanel();	//Random related
    this.pt = new JPanel();	//Tone related
    pb.setBackground(this.bg);
    pr.setBackground(this.bg);
    pt.setBackground(this.bg);
    //Font Tfont = new Font("TimesRoman", Font.PLAIN, 10);
    //int gap = 0;
    //pr.setTextFont(this.subFont); 
    pr.setBorder(BorderFactory.createTitledBorder(this.def.text[50]));
   // pr.setGap(gap);
   // pt.setTextFont(this.subFont); 
    pt.setBorder(BorderFactory.createTitledBorder(this.def.text[51]));
    //pr.setGap(gap);
    if (this.def.newFOF) pb.setBorder(BorderFactory.createTitledBorder(this.def.text[47])); // fof
    else pb.setBorder(BorderFactory.createTitledBorder(this.def.text[48]));
    //pb.setGap(gap);
    debugOut(this.def.text[48], 1);
   // String wert;
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
    JPanel minFreq = getTextPanel(0, this.def.text[19], this.def.text[25]); // Hz
    gridbag.setConstraints(minFreq,c);
    pt.add(minFreq);
// --------- max_freq Textfield ------------
    c.gridx= 1;
    c.gridy= row;
    JPanel maxFreq = getTextPanel(1, this.def.text[18], this.def.text[25]); // Hz
    gridbag.setConstraints(maxFreq,c);
    pt.add(maxFreq);
    // --------- Seed Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    JPanel seedP = getTextPanel(2, this.def.text[46], this.def.text[25]); // Hz
    gridbag.setConstraints(seedP,c);
    pt.add(seedP);
    //Panel pk = new Panel();
    // --------- Use notes Checkbox ------------
    c.gridx= 3;
    c.gridy= row;
    cb = new JCheckBox(this.def.text[30]);
    cb.setBackground(this.bg);
    cb.setFont(this.subFont);
    cb.addItemListener(this);
    cb.setSelected(this.def.tonal);
    c.insets = rig;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(cb,c);
    //pk.add(cb);
    pt.add(cb);
    c.gridwidth = 1;
    // --------- Min Duration Textfield ------------
    row += 1;
    c.gridx= 0;
    c.gridy= row;
    JPanel minDurP = getTextPanel(3, this.def.text[24], this.def.text[26]); // sec
    gridbag.setConstraints(minDurP,c);
    pt.add(minDurP);
// --------- Max Duration Textfield ------------
    c.gridx= 1;
    c.gridy= row;
    JPanel maxDurP = getTextPanel(4, this.def.text[42], this.def.text[26]); // sec
    gridbag.setConstraints(maxDurP,c);
    pt.add(maxDurP);
    // --------- duration steps: ------------------------
    c.gridx= 2;
    c.gridy= row;
    JPanel dup = new JPanel();
    dup.setBackground(this.bg);
    Vector elements = new Vector();
    for(int n = 0; n < Project2.dura.length; n++) 
	elements.addElement(Project2.dura[n]);
    JPanel durP = getChoicePanel(5, this.def.text[71], elements);
    combo[5].setSelectedIndex(def.duration_step_index);	
    dup.add(durP);
    gridbag.setConstraints(dup,c);
    pt.add(dup);
    // --------- Prefer lower Notes ------------
    c.gridx= 3;
    c.gridy= row;
    c.insets = left;
    cb2 = new JCheckBox(this.def.text[65]);
    cb2.setBackground(this.bg);
    cb2.setFont(this.subFont);
    cb2.addItemListener(this);
    cb2.setSelected(this.def.preferLowerNotes);
    c.insets = rig;
    gridbag.setConstraints(cb2,c);
    pt.add(cb2);
    // --------- Min. loudness Choice ------------
    row += 1;
    c.gridx= 0;
    c.gridy= row;
    elements = setLChoice();
    JPanel minLoudP = getChoicePanel(6, this.def.text[44], elements);
    combo[6].setSelectedItem(def.min_amp);	
    gridbag.setConstraints(minLoudP,c);
    pt.add(minLoudP);
    // --------- Max. loudness Choice ------------
    c.gridx= 1;
    c.gridy= row;
    elements = setHChoice();
    JPanel maxLoudP = getChoicePanel(7, this.def.text[45], elements);
    combo[5].setSelectedItem(def.max_amp);	
    gridbag.setConstraints(maxLoudP,c);
    pt.add(maxLoudP);
    // --------- Use stereo effects Checkbox ------------
    c.gridx= 2;
    c.gridy= row;
    c.insets = left;
    //wert = Converter.formatInt(this.def.iterations, this.digits);
    cb1 = new JCheckBox(this.def.text[43]);
    cb1.setBackground(this.bg);
    cb1.setFont(this.subFont);
    cb1.addItemListener(this);
    cb1.setSelected(this.def.stereo);
    c.insets = rig;
    gridbag.setConstraints(cb1,c);
    pt.add(cb1);
    // --------- Use selected freq ------------
    c.gridx= 3;
    c.gridy= row;
    c.insets = left;
    cb3 = new JCheckBox(this.def.text[79]);
    cb3.setBackground(this.bg);
    cb3.setFont(this.subFont);
    cb3.addItemListener(this);
    cb3.setSelected(this.def.useSelectNotes);
    c.insets = rig;
    gridbag.setConstraints(cb3,c);
    pt.add(cb3);
    // --------- Min. Voices ------------
    row += 2;
    c.gridx= 0;
    c.gridy= row;
    elements = setLVoice();
    JPanel minVoiP = getChoicePanel(8, this.def.text[52], elements);
    combo[8].setSelectedItem(Integer.toString(def.min_voice));	
    gridbag.setConstraints(minVoiP,c);
    pt.add(minVoiP);
    // --------- Max. Voices ------------
    c.gridx= 1;
    c.gridy= row;
    elements = setLVoice();
    JPanel maxVoiP = getChoicePanel(9, this.def.text[53], elements);
    combo[9].setSelectedItem(Integer.toString(def.max_voice));	
    gridbag.setConstraints(maxVoiP,c);
    pt.add(maxVoiP);
    // --------- Gamma ------------
    c.gridx= 2;
    c.gridy= row;
    elements = new Vector();
    for (int qz = 0; qz < Project2.GAMMAT.length; qz++) 
	elements.addElement(Double.toString(Project2.GAMMAT[qz]));
    JPanel gamP = getChoicePanel(10, this.def.text[54], elements);
    combo[10].setSelectedItem(Double.toString(def.gamma));	
    gridbag.setConstraints(gamP,c);
    pt.add(gamP);
    
    // --------- Change selection------------
    c.gridx= 3;
    c.gridy= row;
    //c.insets = left;
    /*cb3 = new JCheckBox(this.def.text[79]);
    cb3.setBackground(this.bg);
    cb3.setFont(this.subFont);
    cb3.addItemListener(this);
    cb3.setSelected(this.def.useSelectNotes);
    */
    c.insets = rig;
    JPanel bt = new JPanel();
    bt.add(select);
    bt.setBackground(this.bg);
    gridbag.setConstraints(bt,c);
    pt.add(bt);
    // ------------------ 2nd panel: -----------------------
    c.gridx= 0;
    c.gridy= 0;
    gridbag.setConstraints(pt,c);
    pb.add(pt);
// --------- Population Textfield ------------
    row = 0;
    c.gridx= 0;
    c.gridy= row;
    JPanel popP = getTextPanel(11, this.def.text[20], null);
    gridbag.setConstraints(popP,c);
    pr.add(popP);

// --------- Number of fittest Textfield ------------
    c.gridx= 1;
    c.gridy= row;
    JPanel nofP = getTextPanel(12, this.def.text[21], null);
    gridbag.setConstraints(nofP,c);
    pr.add(nofP);
    // --------- Interval Textfield ------------
    c.gridx= 2;
    c.gridy= row;
    JPanel interP = getTextPanel(13, this.def.text[23], this.def.text[25]);
    gridbag.setConstraints(interP,c);
    pr.add(interP);
    // --------- Diff. Freq. Textfield ------------
    c.gridx= 3;
    c.gridy= row;
    JPanel diffFP = getTextPanel(14, this.def.text[29], this.def.text[25]);
    gridbag.setConstraints(diffFP,c);
    pr.add(diffFP);
    // --------- Random Weight Textfield ------------
    row += 1;
    c.gridx= 0;
    c.gridy= row;
    JPanel randWP = getTextPanel(15, this.def.text[27], null);
    gridbag.setConstraints(randWP,c);
    pr.add(randWP);
    // --------- Degression Textfield ------------
    c.gridx= 1;
    c.gridy= row;
    JPanel degP = getTextPanel(16, this.def.text[72], null);
    gridbag.setConstraints(degP,c);
    pr.add(degP);
    // --------- Iterations Textfield ------------    
    //row += 1;
    c.gridx= 2;
    c.gridy= row;
    JPanel iterP = getTextPanel(17, this.def.text[28], null);
    gridbag.setConstraints(iterP,c);
    pr.add(iterP);
    // ------------------ 3rd panel: -----------------------
    c.gridx= 0;
    c.gridy= 1;
    gridbag.setConstraints(pr,c);
    pb.add(pr);
    return pb;
}

    public void enableChildren(boolean b) {
	ptfs[0].setEnabled(b);
	ptfs[1].setEnabled(b);
	ptfs[13].setEnabled(b);
	ptfs[2].setEnabled(b);
	ptfs[3].setEnabled(b);
	ptfs[4].setEnabled(b);
	ptfs[12].setEnabled(b);
	ptfs[15].setEnabled(b);
	ptfs[16].setEnabled(b);
	ptfs[11].setEnabled(b);
	ptfs[17].setEnabled(b); 
	ptfs[14].setEnabled(b);
	cb2.setEnabled(b);
	cb3.setEnabled(b);
	cb1.setEnabled(b);
	cb.setEnabled(b);
	combo[6].setEnabled(b);
	combo[7].setEnabled(b);
	combo[8].setEnabled(b);
	combo[9].setEnabled(b);
	combo[10].setEnabled(b);
	combo[5].setEnabled(b);
	m1.setEnabled(b);
	m2.setEnabled(b);
    }

    @SuppressWarnings("rawtypes")
	public void setActual() {
	String wert;
	if (def.lastPath != null) this.CustomPath = def.lastPath;
	//mi2_2.setEnabled(this.def.newFOF); 	// fof);
	//mi2_3.setEnabled(!this.def.newFOF);	// fof);
	mi2_4.setSelected(this.def.mode == 0);
	mi2_5.setSelected(this.def.mode > 0);
	wert = Converter.formatInt(this.def.max_freq, this.digits);
	ptfs[1].setText(wert);
	wert = Converter.formatInt(this.def.min_freq, this.digits);
	ptfs[0].setText(wert);
	wert = Converter.formatInt(this.def.interval, this.digits);
	ptfs[13].setText(wert);
	wert = Converter.formatInt(this.def.seed, this.digits);
	ptfs[2].setText(wert);
	wert = Converter.formatDouble(this.def.min_tempo, this.digits, this.post);
	ptfs[3].setText(wert);
	wert = Converter.formatDouble(this.def.max_tempo, this.digits, this.post);
	ptfs[4].setText(wert);
	wert = Converter.formatInt(this.def.fittest, this.digits);
	ptfs[12].setText(wert);
	wert = Converter.formatDouble(this.def.r_Weight, this.digits, this.post);
	ptfs[15].setText(wert);
	wert = Converter.formatDouble(this.def.degression, this.digits, this.post);
	ptfs[16].setText(wert);
	wert = Converter.formatInt(this.def.population, this.digits);
	ptfs[11].setText(wert);
	wert = Converter.formatInt(this.def.iterations, this.digits);
	ptfs[17].setText(wert);
	wert = Converter.formatDouble(this.def.diff_freq, this.digits, this.post);
	ptfs[14].setText(wert);
	if (combo[6] != null) {
	    Vector v = setLChoice();
	    combo[6].removeAllItems();
	    for (int pi = 0; pi < v.size() ; pi++) 
		combo[6].addItem(v.elementAt(pi));
	    combo[6].setSelectedItem(def.min_amp);
	}
	if (combo[7] != null) {
	    Vector v = setHChoice();
	    combo[7].removeAllItems();
	    for (int pi = 0; pi < v.size() ; pi++) 
		combo[7].addItem(v.elementAt(pi));
	    combo[7].setSelectedItem(def.max_amp);
	}
	if (cb1 != null) cb1.setSelected(this.def.stereo);
	if (cb != null) cb.setSelected(this.def.tonal);
	this.select.setEnabled(this.def.useSelectNotes);
	if (cb3 != null) cb3.setSelected(this.def.useSelectNotes);
	if (combo[8] != null) {
	    Vector v = setLVoice();
	    combo[8].removeAllItems();
	    for (int pi = 0; pi < v.size() ; pi++) 
		combo[8].addItem(v.elementAt(pi));
	    combo[8].setSelectedItem(Integer.toString(def.min_voice));
	}
	if (combo[9] != null) {
	    Vector v = setHVoice();
	    combo[9].removeAllItems();
	    for (int pi = 0; pi < v.size() ; pi++) 
		combo[9].addItem(v.elementAt(pi));
	    combo[9].setSelectedItem(Integer.toString(def.max_voice));
	}
	if (combo[10] != null) combo[10].setSelectedItem(Double.toString(def.gamma));
	if (combo[5] != null) 	
	    this.combo[5].setSelectedIndex(this.def.duration_step_index);
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
	    txt[0] = this.def.text[60]+filepath;
	    //Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM);
	    //al.setVisible(true);
	    new Utils().doMessagePane( txt[0]);
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
	    /*String txt[] = new String[2];
	    txt[0] = this.def.text[61];
	    txt[1] = this.def.text[62];
	    Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.CONFIRMATION);
	    al.setVisible(true);
	    */
	    int i = new Utils().doConfirmationPane(this.def.text[61]+"\n"+this.def.text[62]);
	    if (i == 0) {
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
	    txt[0] = this.def.text[59]+filepath;
	    //Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM);
	    //al.setVisible(true);
	    new Utils().doMessagePane(txt[0]);
	}
	return false;
    }

    /**
     * Select a file for save or load
     * @param mode true if load, else save
     * @return path
     */
    public String selectFile(boolean mode) {
	String tit = this.def.text[58];
	if (mode) tit = this.def.text[57]; // load
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
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector setLChoice() {
	int n;
	int max = Project2.Loudness.length;
	Vector v = new Vector();
	if (this.def.newFOF) max = Project2.maxFOFLoudIndex;
	for(n = 0; n < max; n++) {
	    v.addElement(Project2.Loudness[n]);
	    if (def.max_amp.equals(Project2.Loudness[n])) break;
	}
	return v;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector setHChoice() {
	int n;
	boolean flag = false;
	Vector v = new Vector();
	int max = Project2.Loudness.length;
	if (this.def.newFOF) max = Project2.maxFOFLoudIndex;
	for(n = 0; n < max; n++) {
	    if (def.min_amp.equals(Project2.Loudness[n])) flag = true;
	    if (flag) v.addElement(Project2.Loudness[n]);
	}
	return v;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector setLVoice() {
	int n;
	Vector v = new Vector();
	for(n = 0; n < def.max_voice; n++) {
	    v.addElement(Integer.toString(n+1)); 
	}
	return v;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector setHVoice() {
	int n;
	Vector v = new Vector();
	for(n = def.min_voice; n <= def.voiceMax; n++) {
	    v.addElement(Integer.toString(n));
	}
	return v;
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
	FileDialog fd = new FileDialog(this, this.def.text[15]);
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
	if (v == 55 && this.prs != null) { // print !
	    prs.println(tmp);
	    /*}
	    catch (java.io.IOException e) {
		this.prs = null;
		return;
	    }
	    */
	}
	else { 	   
	    if (v > this.Verbosity) return;
	    if (this.qd != null) this.qd.put(tmp);
	    else System.out.println(tmp);
	}
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void doScoFile() {
	//int n;
	setToWait(this, false);
	// Erst mal bestimme, welche Datei wir schreiben sollen (.sco) !
	if (this.sFile == null) selectPath();
	if (this.sFile == null) {
	    setToWait(this, true);
	    return;	// Tu nix
	}
	Vector sco_lines = null;
	if (!this.def.fof) sco_lines = doOszi();
	//else sco_lines = doFOF();

	if (sco_lines != null) {
	    // Nun noch das EOF markieren:
	    sco_lines.addElement("e");
	    // schließlich muß der Vector in die Datei geschrieben werden 
	    if (!new ScoAccess().saveSco(CustomPath+sFile, this.User, sco_lines)) {
			String txt[] = new String[2];
			txt[0] = this.def.text[16]+CustomPath+sFile;
			//Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM);
			//al.setVisible(true);
			new Utils().doMessagePane(txt[0]);
	    }
	    else {
		// Fehler
			String txt[] = new String[2];
			txt[0] = this.def.text[17];
			//Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.MESSAGE);
			//al.setVisible(true);
			new Utils().doMessagePane(txt[0]);
	    }
	}
	setToWait(this, true);

    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector doOszi() {
	Vector sco_lines = null;
	//-------------- generate and save the orc file:
	pr2 = new Project2(this.qd, this.Verbosity, (SoundInfoListener) this, this.prs);
	if (this.def.newFOF) { // In fof mode check for default orc file
	    sco_lines = new ScoAccess().getFileContent(Home+File.separator+GetEnviroment.DEFAULTFOFORC);
	}
	if (sco_lines == null) {
	    sco_lines = new Vector();	
	    sco_lines = pr2.getOrc(this.def.newFOF);
	}
	if (!new ScoAccess().saveSco(CustomPath+oFile, this.User, sco_lines)) {
	    String txt[] = new String[2];
	    txt[0] = this.def.text[16]+CustomPath+oFile;
	    //Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM);
	   // al.setVisible(true);
	    new Utils().doMessagePane(txt[0]);
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
	this.gc.offImage = null;
	this.gc.mach();
	ton = pr2.doProject(this.def); // komposition
	if (ton != null) {
		mi1_5.setEnabled(this.def.tonal & ton.size() > 0);
		play.setEnabled(this.def.tonal & ton.size() > 0);
		// Der Inhalt des Vector ton wird nun in sco_lines kopiert
		Object nt;
		for (int n= 0; n < ton.size(); n++) {
		    nt = ton.elementAt(n);
		    sco_lines.addElement(nt);
		}
	}
	return sco_lines;
    }

/**
 * Method to react to a selection in the .len image choice
 */
    @SuppressWarnings("static-access")
	public void itemStateChanged(ItemEvent e) {
	if (this.busy | this.ignore) return; 
	this.ignore = true;
	int state = e.getStateChange();
	//String item;
	Object o = (Object) e.getSource();
	if (o == cb) { 
	    boolean old = this.def.tonal;
	    if (state == e.SELECTED) {
	    	this.def.tonal = true;
	    }
	    else this.def.tonal = false;
	    boolean b = ton != null;
	    if (b) b = ton.size() > 0 & old;
	    mi1_5.setEnabled(this.def.tonal & b);
	    play.setEnabled(this.def.tonal & b);
	    save.setEnabled(this.def.tonal & b & played);
	    cb2.setEnabled(this.def.tonal);
	    if (this.def.tonal) {
	    	// Notes choosen, so there is no match from freq - to Notes
	    	this.def.selectedNotes = new Vector<OneNote>();
	    }
	    	
	    
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
	else if (o == cb3) {
		this.def.useSelectNotes = state == e.SELECTED;
		this.select.setEnabled(this.def.useSelectNotes);
	}
	State = true;
	setActual();
	this.ignore = false;
    }

    public void stopChannel(int n) {
	if (isOn[n] == true  ) {
	    mid.channels[n+of].channel.noteOff(idx[n], ampl[n]);
	    mid.createShortEvent(mid.NOTEOFF+n, idx[n], mid.channels[n+of]);
	    //System.out.println("Channel "+n+" stop with "+idx[n]+" at :"+(System.currentTimeMillis()-SysStartTime));
	    //System.out.println("Channel "+n+" stop after :"+(System.currentTimeMillis()-tTime)+" akt[n]="+akt[n]+" idx[n]="+idx[n]);
	    isOn[n] = false;
	}
    }
@SuppressWarnings("rawtypes")
public void actionPerformed(ActionEvent e) {
    //System.out.println("Action performed");
    if (this.busy) return;
    // react to the menues
   // String arg = e.getActionCommand();
    String item;
    Object o = (Object) e.getSource();
    //System.out.println("Action performed on"+o);
    //Class c = ((Object) tf).getClass();
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
    if (this.ignore) return;
    this.ignore = true;
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
    else if (o == mi2_4) { // mode = 2d
	System.out.println("actionPerformed 2d");
	this.def.mode = 0;
	this.mi2_4.setSelected(this.def.mode == 0);
	this.mi2_5.setSelected(this.def.mode != 0);
    }
    else if (o == mi2_5) { // mode = 2d
	this.def.mode = 2;
	this.mi2_4.setSelected(this.def.mode == 0);
	this.mi2_5.setSelected(this.def.mode != 0);
    }
    /*else if (o == mi2_2) {	// Oszi
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
    */
    else if (o == mi1_5) { // Play	/ Load	
	//doPlayMidi();
	
    }   
    else if (o == this.instr) {
    	new InstrumentDialog(this);
    }
    else if (o == mi2_1) {	// About
	String txt[] = new String[12];
	txt[0] = this.def.text[34];

	txt[1] = this.def.text[39];
	txt[2] = this.def.text[40];
	txt[3] = " ";
	txt[4] = this.def.text[35];
	txt[5] = this.def.text[36];
	txt[6] = this.def.text[37];
	txt[7] = this.def.text[38];
	txt[8] = " ";
	txt[9] = this.def.text[66];
	txt[10] = this.def.text[67];
	txt[11] = this.def.text[68];
	
	//Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM, Alarmbox.LEFT, null);
	//Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.ALARM);	
	//al.setVisible(true);
	String t = "";
	for (int n = 0; n < txt.length; n++) {
		if (txt[n] != null)
			t += txt[n] + "\n";
	}
	new Utils().doMessagePane(t);
    }

    else if (o == stp) { // stop Button
	//System.out.println("------------------- Stop Button: Initiate halt --------------");
	debugOut("------------------- Stop Button: Initiate halt --------------", 2);
	if (!playMidi) {
	    if (!this.halt) {
		this.halt = true;
		/*if (this.def.fof) pr3.halt = true;
		else pr2.halt = true;
		*/
		pr2.halt = true;
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
    else if (o == combo[6]) { // min. Loudness
	this.def.min_amp = this.combo[6].getSelectedItem().toString();
	System.out.println("ActionPerformed min. Loudness selected: "+this.def.min_amp);
	Vector v = setHChoice();
	combo[7].removeAllItems();
	for (int pi = 0; pi < v.size() ; pi++) 
	    combo[7].addItem(v.elementAt(pi));
	combo[7].setSelectedItem(def.max_amp);
	State = true;
    }
    else if (o == combo[7]) { // max. Loudness
	this.def.max_amp = this.combo[7].getSelectedItem().toString();
	Vector v = setLChoice();
	combo[6].removeAllItems();
	for (int pi = 0; pi < v.size() ; pi++) 
	    combo[6].addItem(v.elementAt(pi));
	combo[6].setSelectedItem(def.min_amp);  	
	State = true;
    }
    else if (o == combo[8]) { // min. Voice
	this.def.min_voice = Integer.parseInt(this.combo[8].getSelectedItem().toString());
	Vector v = setHVoice();
	combo[9].removeAllItems();
	for (int pi = 0; pi < v.size() ; pi++) 
	    combo[9].addItem(v.elementAt(pi));
	combo[9].setSelectedItem(Integer.toString(def.max_voice));
	State = true;
    }
    else if (o == combo[9]) { // max. Voice
	this.def.max_voice = Integer.parseInt(this.combo[9].getSelectedItem().toString());
	Vector v = setLVoice();
	combo[8].removeAllItems();
	for (int pi = 0; pi < v.size() ; pi++) 
	    combo[8].addItem(v.elementAt(pi));
	if (this.def.max_voice < this.def.min_voice) this.def.min_voice = this.def.max_voice;
	//System.out.println("Itemstatechanged: ch3:selected is:"+this.def.max_voice+" ch2.select:"+this.def.min_voice);
	//ch2.select(Integer.toString(this.def.min_voice));
	combo[8].setSelectedItem(Integer.toString(def.min_voice));
	State = true;
    }
    else if (o == combo[10]) { // Gamma value changed
	item = this.combo[10].getSelectedItem().toString();
	this.def.gamma = Double.valueOf(item).doubleValue();
	State = true;
    }
    else if (o == combo[5]) { // duration step
	//System.out.println("ActionPerformed duration step ="+this.combo[5].getSelectedIndex());
	this.def.duration_step_index = this.combo[5].getSelectedIndex();
	State = true;
    }
    else  { //if (c.isInstance(o) ) { // Textfield
	try {
	    checkInput((JTextField) e.getSource());
	} catch (Exception ex) {}
    }
    this.ignore = false;
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
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
			int instr = def.getInstrumentForChannel(i);
		    mid.channels[i].channel.programChange(instr);
		    mid.createShortEvent(mid.PROGRAM, instr, mid.channels[i]);//Instrument change into data stream (track)
		}
		
		double maxd = 0.0;
		int sleep, slp;
		System.out.println("playKomposition() -----> flow.size="+flow.size());
		//---------- loop over komposition
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
		    this.gc.showNote(pr2.rt, drw, n);
		    // Loop over all notes in this time-intervall
		    for ( c = 0; c < v.size(); c++) {
				nt = (Note)  v.elementAt(c);
				sl = (int) (nt.dauer * 1000.0);
				System.out.println("playKomposition() c="+c+" start="+nt.start+" freq="+nt.freq+" nt.dauer="+nt.dauer+" nt.amplitude="+nt.amplitude+" bal="+nt.balance);
				// init time if needed
				try {
				    if (ti[c] == null) {
					ti[c] = new Timer(sl, this);
					ti[c].setRepeats(false);
				    }
				} catch (Exception ex) {
				    ex.printStackTrace();
				    System.out.println("playKomposition() c="+c+" start="+nt.start+" freq="+nt.freq+" nt.dauer="+nt.dauer+" nt.amplitude="+nt.amplitude+" bal="+nt.balance);
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
				
				mid.channels[c+of].channel.controlChange(mid.PAN, balance[c]); 	
				mid.createControlEvent(mid.PAN, balance[c], mid.channels[c+of]);
				//------------ Note ---------
				mid.channels[c+of].channel.noteOn(idx[c], ampl[c]);
				mid.createShortEvent(mid.NOTEON + (c+of), idx[c], mid.channels[c+of]);
				//System.out.println("playKomposition() note="+idx[c]+" nt.amplitude="+nt.amplitude+" volume="+ampl[c]+" balance="+balance[c]);
			
		    }
		    millis = (int) (System.currentTimeMillis() - SysStartTime);
		    slp = sleep - millis;
		    try {
		    	java.lang.Thread.sleep(slp);
		    }	
		    catch (InterruptedException e){}
		    //System.out.println("playKomposition() ------------- end of intervall, slept "+(slp));	
		    for ( c = 0; c < v.size(); c++) 
		    	stopChannel(c);
		    //System.out.println("playKomposition()  end of intervall. real time needed="+(System.currentTimeMillis() - SysStartTime));
		    if (this.mHalt) break; 
		} // end of loop over flow
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
    int of = 0;
    public void saveMidi() {
		//------save --------
		String midf = CustomPath+oFile;
		int id = midf.lastIndexOf(".");
		midf = midf.substring(0, id);
		midf = midf + ".mid";
		mid.saveMidiFile(new File(midf));
    }
  
    public void displayRT(RandomTable rt) {
	if (this.gc != null) gc.setTable(rt, this.def.gamma, this.def.mode);
    }

@SuppressWarnings("rawtypes")
public void focusGained(FocusEvent e) {
    Object o = (Object) e.getSource();
    debugOut("focusGained: Object o="+o.toString(), 5);
    Class c = ((Object) ptfs[0]).getClass();
    if (c.isInstance(o) ) ((JTextField) e.getSource()).selectAll();
}

@SuppressWarnings("rawtypes")
public void focusLost(FocusEvent e) {
    // treat like user hit 'Return'
    Object o = (Object) e.getSource();
    debugOut("focuslost: Object o="+o.toString(), 5);
    Class c = ((Object) ptfs[0]).getClass();
    if (c.isInstance(o)) checkInput((JTextField) e.getSource());
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
	else if (ev.getSource() == select) { // select
	    doSelectNotes();
	}
    }	
    }// end of inner class
/** 
 * React to Window Events.
 */
    class MDListener extends WindowAdapter {
    public void windowClosing(WindowEvent ev) {
	//String arg;
	AWTEvent event = (AWTEvent) ev;
	if (event.getID() == Event.WINDOW_DESTROY) {
	    //dispose();
	    doQuitWithInquire();
	}	
    }	
    }// end of inner class

   

public void doSelectNotes() {
	Project2 p = new Project2(this.qd, this.Verbosity, (SoundInfoListener) this, this.prs);
	p.def = def;
	p.createNoteTable(p.note_mode); // respects the min-max freq.
	// filer die selectierten Noten aus der Quelle !
	Vector<OneNote> source = new Vector<OneNote>();
	for (int n = 0; n < p.notes.size(); n++) {
		OneNote no = p.notes.elementAt(n);
		if (!contains(def.selectedNotes, no)) // is not in the selected list
			source.addElement(no);
	}
	Vector<OneNote> sel  = def.clipNotes(def.selectedNotes);
	String text = null;
	if (sel.size() != def.selectedNotes.size()) {
		text = def.text[80];
		new Utils().doMessagePane(text);
	}
	new SelectDialog(this, source, sel);
	if (sel.size() <= 1) {
		text = def.text[85];
		new Utils().doMessagePane(text);
	}
}

public boolean contains(Vector<OneNote> source, OneNote in) {
	for (int n = 0; n < source.size(); n++) {
		if (source.elementAt(n).equals(in))
			return true;
	}
	return false;
}

@SuppressWarnings("unused")
public void checkInput(JTextField tfi)
    throws NumberFormatException {

    int x = 0, tmp = 0,disp = 0;
    double dTmp = 0.0, dx = 0.0, dDisp = 0.0;
    String wert;
    boolean tst = false;
    int elcode = -1;
    if (tfi == ptfs[1]) elcode = 1; // maxFreq
    else if (tfi == ptfs[0]) elcode = 2; // minFeq
    else if (tfi == ptfs[11]) elcode = 3; // Population
    else if (tfi == ptfs[12]) elcode = 4; // fittest
    else if (tfi == ptfs[13]) elcode = 5; // interval
    else if (tfi == ptfs[3]) elcode = 6; // min_tempo
    else if (tfi == ptfs[15]) elcode = 7; // r_weight
    else if (tfi == ptfs[17]) elcode = 8; // iterations
    else if (tfi == ptfs[14]) elcode = 9; // diff freq
    else if (tfi == ptfs[4]) elcode = 10; // max_tempo
    else if (tfi == ptfs[2]) elcode = 11; // seed
    else if (tfi == ptfs[16]) elcode = 17; // degression

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
    case 17: dTmp = this.def.degression;
	break;
    }
    String t;
    t = tfi.getText();
    try { //
	if (elcode == 6 || elcode == 7 || elcode == 10 || elcode == 17) {
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
	case 17: this.def.degression = dx; // 	degression
	    dDisp = this.def.degression;
	    //if (dDisp <= 0.0 || dDisp < this.def.min_tempo) throw(new NumberFormatException()); 
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
	case 17: this.def.degression = tmp;
	    break;
	}  
	tst = false;
    } // end of catch
    
    if (elcode == 6 || elcode == 7 || elcode == 10 || elcode == 17) 
	wert = Converter.formatDouble(dDisp, this.digits, this.post);
    else wert = Converter.formatInt(disp, this.digits);
	
    debugOut("nun setze das angewaehlte Textfield mit : "+wert, 3);
    tfi.setText(wert); // update Textfield with corrected or old value
    State = State | tst;
}// end of checkInput    

public void doQuitWithInquire() {
    String txt[] = new String[2];
    txt[0] = this.def.text[10];
    //Alarmbox al = new Alarmbox(this, this.def.text[1], txt, 12, Alarmbox.CONFIRMATION);
    //al.setVisible(true);
    int i = new Utils().doConfirmationPane(txt[0]);
    if (i == 1) {
    	// do not abort !
    	return;
    }
    doQuit();
}

public void doQuit() {
    debugOut(" Quit: State="+State, 2);
    if (State) { // just when changed
	//def.def.fof = this.fof;
    	 if (Home == null)
    	    	Home =  System.getProperty("user.home");
    	 if (!def.saveDef(Home+File.separator+GetEnviroment.SAVEFILE, this.User)) {
    		 debugOut("CEditor:Can not save default settings !", 1);
		}
    }
    try {
    	prs.close();
	}
    catch (Exception e) {
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



/**
 * Set The Cursor to Wait or normal state and disable/enable whatever neccassary.
 */
public void setToWait(Component Co, boolean w) {
    Cursor cu;
    if (!w) cu = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    else cu = Cursor.getDefaultCursor();
    Co.setCursor(cu);
    //this.setEnabled(w);
    this.enableChildren(w);
    start.setEnabled(w);
    this.pt.setEnabled(w);
    this.pr.setEnabled(w);
    this.body.setEnabled(w);
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

    @SuppressWarnings("unused")
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
public String formatValue(double v) {
	return formatDValue(v);
    }
    /**
     * Convert double Value to a formatted Display Value
     */
    public String formatDValue(double v) {
	return Converter.formatDouble(v, this.digits, this.post);
    }

    public String formatIValue(int v) {
	return Converter.formatInt(v, this.digits);
    }

public void setLF() {
	try {
	    //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); // java l&F
	    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // abhängig vom OS
	    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); // java L&F
	    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); // SUN
	} catch (Exception e) { }
    }

@SuppressWarnings("unused")
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






