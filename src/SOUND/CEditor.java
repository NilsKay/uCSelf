package SOUND;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*; 

import java.io.*;
import java.text.DecimalFormat;
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
    int digits = 10;	// Anzahl Stellen
    int post = 5;	// Post colon digits
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
    //Choice ch, ch1, ch2, ch3, gam, ch4;	
    JMenuBar mb=null;		//Der Menubalken
    JMenu m1, m2, lang;			//Das Menu im Balken
    JMenuItem   mi1_1, mi1_2, mi2_1, mi2_2, mi2_3, mi1_3, mi1_4, mi1_5, instr, stateM;		//
    JCheckBoxMenuItem mi2_4, mi2_5, ger, eng;
    JPanel pane;
    
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
   /* int idx[], ampl[];// akt[];
    boolean isOn[] = new boolean[16];
    int balance[] = new int[16];
    */
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
    public int build;
    
    CEditorModel model;
    CEditorUi ui;
    
    Dimension gd;
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
    
    Point p = new Point(0,0);
    xgap = 10 ;	// horiz. gap for Graidbag components
    ygap = 0 ;	// vert.gap 
    min = 0 ;	// minimum gap
    this.State = false;
    this.sFile = null; 
   // test:
    int oct = 9;
    int not = 7; // e
    OneNote nt = new OneNote(0, not, oct);
    nt.setMidiFreqency();
    System.out.println("Octave="+oct+" note="+nt.nts[not]+" midi="+nt.midiIndex+" f="+nt.freq);
    nt.getMidiNote();
    System.out.println("equals from Freq:"+nt);
    //------------ Midi init ----------
    this.mid = new MyMidi();
    
    //this.enableMidi = doMidiOpen();
    //------------------------------------
   
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
    this.build = gsp.build;
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
    boolean defLoaded = this.def.loadDef(Home+File.separator+GetEnviroment.SAVEFILE, this.build);
    if (def.lastPath != null) this.CustomPath = def.lastPath;
    this.mid.open(def);
    //------------
    this.model = new CEditorModel(this);
    this.model.addListeners(this);
    this.ui = new CEditorUi(this.model);
    
 // --------- Select Button ------------
   
    setFont(this.model.font);
    mb = new JMenuBar();
    mb.setFont(this.model.font); 
    setMenubar(); // erzeugt das Menu hier in guimain
    this.eng.setSelected(this.def.lang == 0);
    this.ger.setSelected(this.def.lang == 1);
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
    //pane.add("North", top);
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
    
    mi1_5.setEnabled(false);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new MDListener());
    
    JPanel pn = new JPanel();
    pn.setLayout(new BorderLayout());
    pn.add("North", top);
    pn.add("Center", this.ui);
    this.setContentPane(pn);
    //this.add(pane);
    model.setActual(this);
    pack();
    relocate();
    
    super.setTitle(this.def.text[0]);
    debugOut(this.def.text[0], 1);
    busy = false;
} // end of constructor

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
    m1.setFont(this.model.font); 
    mb.add(m1);				// zum Balken addieren

    mi1_1 = new JMenuItem(this.def.text[12]); // Select Path
    mi1_1.setFont(this.model.font);
    mi1_1.addActionListener(this);
    m1.add(mi1_1);				//Addiere MenuPunkt zum Menu
    mi1_1.setEnabled(true);
    m1.addSeparator();
    mi1_3 = new JMenuItem(this.def.text[56]); // Load Template
    mi1_3.setFont(this.model.font);
    mi1_3.addActionListener(this);
    m1.add(mi1_3);				//Addiere MenuPunkt zum Menu
    mi1_3.setEnabled(true);
    mi1_4 = new JMenuItem(this.def.text[55]); // Save Template
    mi1_4.setFont(this.model.font);
    mi1_4.addActionListener(this);
    m1.add(mi1_4);				//Addiere MenuPunkt zum Menu
    mi1_4.setEnabled(true);
    m1.addSeparator();
    mi1_2 = new JMenuItem(this.def.text[13]); // Quit
    mi1_2.setFont(this.model.font);
    mi1_2.addActionListener(this);
    m1.add(mi1_2);				//Addiere MenuPunkt zum Menu
    mi1_2.setEnabled(true);

    m2 = new JMenu(this.def.text[33],true);	// File Tear off Menu(muss clicken)
    m2.setFont(this.model.font); 
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
    mi2_4 = new JCheckBoxMenuItem(this.def.text[96]); // 2d
    mi2_4.setFont(this.model.font);
    mi2_4.addActionListener(this);
    m2.add(mi2_4);	
    mi2_5 = new JCheckBoxMenuItem(this.def.text[97]); // 3d
    mi2_5.setFont(this.model.font);
    mi2_5.addActionListener(this);
    m2.add(mi2_5);
    m2.addSeparator();
    //---------------
    mi1_5 = new JMenuItem(this.def.text[70]); // Play
    mi1_5.setFont(this.model.font);
    mi1_5.addActionListener(this);
    //m2.add(mi1_5);
    //m2.addSeparator();
    //--
    instr = new JMenuItem(this.def.text[89]); // Change instr.
    instr.setFont(this.model.font);
    instr.addActionListener(this);
    m2.add(instr);
    //---
    m2.addSeparator();
    stateM = new JMenuItem(this.def.text[109]); // State Machine setup
    stateM.setFont(this.model.font);
    stateM.addActionListener(this);
    m2.add(stateM);		
    //----
    m2.addSeparator();
    lang = new JMenu(this.def.text[93]); // Change instr.
    lang.setFont(this.model.font);
    lang.addActionListener(this);
    m2.add(lang);
    m2.addSeparator();
    //-------------
    eng = new JCheckBoxMenuItem(this.def.text[94]);
    eng.setFont(this.model.font);
    eng.addActionListener(this);
    lang.add(eng);
    ger = new JCheckBoxMenuItem(this.def.text[95]);
    ger.setFont(this.model.font);
    ger.addActionListener(this);
    lang.add(ger);
    
    mi2_1 = new JMenuItem(this.def.text[32]); // About
    mi2_1.setFont(this.model.font);
    mi2_1.addActionListener(this);
    m2.add(mi2_1);		
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
		boolean defLoaded = this.def.loadDef(filepath, this.build);
		if (defLoaded) {
		    model.setActual(this);
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
		int max = ProjectTools.Loudness.length;
		Vector v = new Vector();
		if (this.def.newFOF) max = ProjectTools.maxFOFLoudIndex;
		for(n = 0; n < max; n++) {
		    v.addElement(ProjectTools.Loudness[n]);
		    if (def.max_amp.equals(ProjectTools.Loudness[n])) break;
		}
		return v;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector setHChoice() {
		int n;
		boolean flag = false;
		Vector v = new Vector();
		int max = ProjectTools.Loudness.length;
		if (this.def.newFOF) max = ProjectTools.maxFOFLoudIndex;
		for(n = 0; n < max; n++) {
		    if (def.min_amp.equals(ProjectTools.Loudness[n])) flag = true;
		    if (flag) v.addElement(ProjectTools.Loudness[n]);
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
		// Erst mal bestimmen, welche Datei wir schreiben sollen (.sco) !
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
		    // schlie�lich mu� der Vector in die Datei geschrieben werden 
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
		//ProjectModel m = new ProjectModel(this.qd, this.Verbosity, (SoundInfoListener) this, this.prs); 
		pr2 = new Project2(this.model.pModel);
		if (this.def.newFOF) { // In fof mode check for default orc file
		    sco_lines = new ScoAccess().getFileContent(Home+File.separator+GetEnviroment.DEFAULTFOFORC);
		}
		if (sco_lines == null) {
		    sco_lines = new Vector();	
		    sco_lines = ScoGenerator.getOrc(this.def.newFOF, this.def);
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
		    sco_lines = ScoGenerator.getHeader(this.def.newFOF);	// Hier holen wir uns den Anfang der sco Datei
		}
		// Dieser Vector (sco_lines) speichert alle Zeilen, die in die .sco datei geschrieben werden sollen
		// Nun ist der Headder der neuen sco Datei fertig
		// ------------------------------------------------------------
		// Als n�chstes addieren wir mal ein paar Zeilen mit T�nen zum Vector
		// Der Vector ton speichert die Zeilen mit den Toenen
		this.gc.offImage = null;
		this.gc.mach();
		ton = pr2.doProject(this.def, this.Home); // komposition
		if (ton != null) {
			mi1_5.setEnabled(this.def.tonal & ton.size() > 0);
			model.bt[model.PLAY].setEnabled(this.def.tonal & ton.size() > 0);
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
		if (o == model.cb[model.USENOTES]) { 
		    boolean old = this.def.tonal;
		    if (state == e.SELECTED) {
		    	this.def.tonal = true;
		    }
		    else {
		    	this.def.tonal = false;
		    	this.model.cb[model.DOAKKORD].setSelected(false);
		    	this.def.akkord = false;
		    }
		    boolean b = ton != null;
		    if (b) b = ton.size() > 0 & old;
		    mi1_5.setEnabled(this.def.tonal & b);
		    model.bt[model.PLAY].setEnabled(this.def.tonal & b);
		    model.bt[model.SAVE].setEnabled(this.def.tonal & b & played);
		    this.model.cb[model.USESELECTION].setEnabled(this.def.tonal);
		    if (this.def.tonal) {
		    	// Notes choosen, so there is no match from freq - to Notes
		    	this.def.selectedNotes = new Vector<OneNote>();
		    }
		    else {
		    	this.def.useSelectNotes = false;
		    	this.model.cb[model.USESELECTION].setSelected(false);
		    }
		    	
		    
		}
		else if (o == model.cb[model.DOSTEREO]) {
		    if (state == e.SELECTED) {
		    	this.def.stereo = true;
		    }
		    else this.def.stereo = false;
		}
		else if (o == model.cb[model.PREFLOWERNOTES]) { // prefer lower notes (false !) 
		    if (state == e.SELECTED) {
		    	this.def.preferLowerNotes = true;
		    }
		    else this.def.preferLowerNotes = false;
		    System.out.println("preferLowerNotes="+this.def.preferLowerNotes);
		}
		else if (o == model.cb[model.USESELECTION]) {
			this.def.useSelectNotes = state == e.SELECTED;
			this.model.bt[model.SELECT].setEnabled(this.def.useSelectNotes);
		}
		else if (o == model.cb[model.DOAKKORD]) {
			this.def.akkord = state == e.SELECTED;
		}
		else if (o == model.cb[model.DOSHADOW]) {
			this.def.pedal = state == e.SELECTED;
		}
		State = true;
		model.setActual(this);
		this.ignore = false;
    }
/*
    public MidiNote stopChannel(MidiNote mn) {
    	if (mn.isOn == true  ) {
    		int n = mn.midiChannel;
    		mid.channels[n].channel.noteOff(mn.midiIndex, mn.amplitude);
    		mid.createShortEvent(mid.NOTEOFF+n, mn.midiIndex, mid.channels[n]);
    		//System.out.println("Channel "+n+" stop with "+idx[n]+" at :"+(System.currentTimeMillis()-SysStartTime));
    		//System.out.println("Channel "+n+" stop after :"+(System.currentTimeMillis()-tTime)+" akt[n]="+akt[n]+" idx[n]="+idx[n]);
    		mn.isOn = false;
    	}
    	return mn;
    }
    */
@SuppressWarnings("rawtypes")
public void actionPerformed(ActionEvent e) {
    //System.out.println("Action performed");
    if (this.busy) return;
   String item;
    Object o = (Object) e.getSource();
    if (this.ignore) return;
    this.ignore = true;
    ProjectModel p = this.model.pModel;
    if (o == model.bt[model.START]) { // start Button
	   // body.setEnabled(false);
	    model.bt[model.START].setEnabled(false);
	    model.bt[model.STOP].setEnabled(true);
	    model.bt[model.SAVE].setEnabled(false);
	    ru = new Runner();
	    ru.start();
	}
	else if (o ==  model.bt[model.PLAY]) { // start Button
	    doPlayMidi();
	}
	else if (o ==  model.bt[model.SAVE]) { // save Button
	    saveMidi();
	}
	else if (o ==  model.bt[model.SELECT]) { // select
	    doSelectNotes();
	}
	else if (o == mi1_1 ) {	// load
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
    else if (o == eng) {
    	this.def.lang = 0;
    	this.ger.setSelected(false);
    	State = true;
    	new Utils().doMessagePane(def.text[98]);
    	//doQuit();
    }
    else if (o == ger) {
    	this.def.lang = 1;
    	this.eng.setSelected(false);
    	State = true;
    	new Utils().doMessagePane(def.text[98]);
    	//doQuit();
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
    
    else if (o == mi1_5) { // Play	/ Load	
    	//doPlayMidi();
	
    }   
    else if (o == this.instr) {
    	new InstrumentDialog(this);
    }
    else if (o == this.stateM) {
    	new StateDialog(this);
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

    else if (o == model.bt[model.STOP]) { // stop Button
		//System.out.println("------------------- Stop Button: Initiate halt --------------");
		debugOut("------------------- Stop Button: Initiate halt --------------", 2);
		if (!playMidi) {
		    if (!this.halt) {
		    	this.halt = true;
		    	p.halt = true;
		    	ru.stop();
		    }
		    model.bt[model.STOP].setEnabled(!this.halt);
		}
		else { // midi playing
		    if (!this.mHalt) {
		    	this.mHalt = true;
		    	pm.stop();
		    }
		    model.bt[model.STOP].setEnabled(!this.halt);
		}
		model.bt[model.STOP].setEnabled(!this.halt);
		setToWait(this, true);
    }
    else if (o == model.combo[model.MINLOUD]) { // min. Loudness
		this.def.min_amp = model.combo[model.MINLOUD].getSelectedItem().toString();
		System.out.println("ActionPerformed min. Loudness selected: "+this.def.min_amp);
		Vector v = setHChoice();
		model.combo[model.MAXLOUD].removeAllItems();
		for (int pi = 0; pi < v.size() ; pi++) 
			model.combo[model.MAXLOUD].addItem(v.elementAt(pi));
		model.combo[model.MAXLOUD].setSelectedItem(def.max_amp);
		State = true;
    }
    else if (o == model.combo[model.MAXLOUD]) { // max. Loudness
		this.def.max_amp = model.combo[model.MAXLOUD].getSelectedItem().toString();
		Vector v = setLChoice();
		model.combo[model.MINLOUD].removeAllItems();
		for (int pi = 0; pi < v.size() ; pi++) 
			model.combo[model.MINLOUD].addItem(v.elementAt(pi));
		model.combo[model.MINLOUD].setSelectedItem(def.min_amp);  	
		State = true;
    }
    else if (o == model.combo[model.MINVOICE]) { // min. Voice
		this.def.min_voice = Integer.parseInt(model.combo[model.MINVOICE].getSelectedItem().toString());
		Vector v = setHVoice();
		model.combo[model.MAXVOICE].removeAllItems();
		for (int pi = 0; pi < v.size() ; pi++) 
			model.combo[model.MAXVOICE].addItem(v.elementAt(pi));
		model.combo[model.MAXVOICE].setSelectedItem(Integer.toString(def.max_voice));
		State = true;
    }
    else if (o == model.combo[model.MAXVOICE]) { // max. Voice
		this.def.max_voice = Integer.parseInt(model.combo[model.MAXVOICE].getSelectedItem().toString());
		Vector v = setLVoice();
		model.combo[model.MINVOICE].removeAllItems();
		for (int pi = 0; pi < v.size() ; pi++) 
			model.combo[model.MINVOICE].addItem(v.elementAt(pi));
		if (this.def.max_voice < this.def.min_voice) 
			this.def.min_voice = this.def.max_voice;
		//System.out.println("Itemstatechanged: ch3:selected is:"+this.def.max_voice+" ch2.select:"+this.def.min_voice);
		//ch2.select(Integer.toString(this.def.min_voice));
		model.combo[model.MINVOICE].setSelectedItem(Integer.toString(def.min_voice));
		State = true;
    }
    else if (o == model.combo[model.GAMMA]) { // Gamma value changed
		item = model.combo[model.GAMMA].getSelectedItem().toString();
		this.def.gamma = Double.valueOf(item).doubleValue();
		State = true;
    }
    else if (o == model.combo[model.DURSTEPS]) { // duration step
    	//System.out.println("ActionPerformed duration step ="+this.combo[5].getSelectedIndex());
    	this.def.duration_step_index = model.combo[model.DURSTEPS].getSelectedIndex();
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
		//this.body.setEnabled(false);
		model.bt[model.STOP].setEnabled(true);
		model.bt[model.START].setEnabled(false);
		model.bt[model.SAVE].setEnabled(false);
		mi1_5.setEnabled(false);
		model.bt[model.PLAY].setEnabled(false);
		this.pm = new PlayMidi();
		this.pm.start();
    }
    Vector<String> log;
    
    private void addLog(String t) {
    	this.log.addElement(t);
    	System.out.println(t);
    }
    
    Vector<MidiNote> mNotes;
    /**
     * 31.1.13: Idee von Peter: vor dem start der Melodie Note eine Verz�gerung einbauen
     * (nur ohne quantisierung...)
     */
    @SuppressWarnings({ "rawtypes", "unused" })
	private void playKomposition() {
    	/*String kompo = this.pr2.kompo;
    	if (kompo == null) 
    		return;
    		*/
    	ProjectModel p = this.model.pModel;
		if ( p.komposition == null | p.komposition.size() <= 0) return;
		mid.close();
		this.enableMidi = doMidiOpen();
		if (!this.enableMidi) {
		    setToWait(this, true);
		    return;
		}
		this.log = new Vector<String>(); 
		this.playMidi = true;
		Note nt;
		int sl, c;
		mid.startRecord(12); // instrument
		double startTime = 0.0, old;
		Vector v = null, flow;
		flow = new Vector();
		v = new Vector();
		//-------- Combine the voices :
		flow = ProjectTools.getInterval(p.komposition, false, def.duration_step_index);
		
		// Instruments:
		for (int i = 0; i < mid.channels.length; i++) { // hier die Instrumente setzen (2 pro stimme wg.akkord ?)
			int instr = def.getInstrumentForChannel(i);
		    mid.channels[i].channel.programChange(instr);
		    mid.createShortEvent(mid.PROGRAM, instr, mid.channels[i]);//Instrument change into data stream (track)
		}
		
		double maxd = 0.0;
		int slp;
		addLog("playKomposition() -----> flow.size="+flow.size());
		this.mNotes = new Vector<MidiNote>();
		activeNotes = 0;
		int intervall = 0;
		BufferedReader br = null;
		String ilog = this.Home+File.separator+GetEnviroment.IMAGELOG;
		Vector<Note> res = null;
    	try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(ilog)));
			br = new BufferedReader(isr);
    	} catch (java.io.IOException e) {
    		System.out.println("CEditor.playKomposition(): catched "+e);
    		return;
    	}
		//---------- loop over komposition
		for (int n = 0; n < flow.size(); n++) {
		    maxd = 0.0; // reset max-dauer
		    v = (Vector) flow.elementAt(n);
		    // find the delay until the next note:
		    int[] drw = new int[v.size()];
		    for ( c = 0; c < v.size(); c++) {
				nt = (Note)  v.elementAt(c);
				//maxd = nt.dauer;
				if (nt.tempo > 0)
					intervall = (int) (nt.tempo * 1000);
				if (this.def.duration_step_index > 0) 
					intervall = this.def.durations[this.def.duration_step_index];
				drw[c] = (int) nt.freq;
		    }
		   // sleep = (int) (maxd * 1000.0);
		    int millis;
		    SysStartTime = System.currentTimeMillis(); //
		    try {
		    	String res0 = br.readLine();
		    	p.rt.setTableFromString(res0);
		    	displayRT(p.rt);
		    } catch (Exception ex) {}
		    this.gc.showNote(p.rt, drw, n);
		    // Loop over all notes in this time-intervall
		    DecimalFormat format = new DecimalFormat("#.###");
		    double startIntervall = 0.0;
		    for ( int ci = 0; ci < v.size(); ci++) {
				nt = (Note)  v.elementAt(ci);
				startIntervall = nt.start * 1000;
				if (ci == 0) {
					addLog("");
					addLog("--------> New interval n="+n+" from start="+format.format(startIntervall)+" ms, \tIntervaldauer="+intervall+" ms ");
				}
				sl = (int) (nt.dauer * 1000.0);
				if (nt.note.pedal)
					sl = (int) (nt.pDauer * 1000.0);
				if (def.delay > 0)
					sl += def.delay;
				String note = ""+nt.freq;
				if (nt.note.note.length() > 0)
					note = nt.note.note+nt.note.Octave;
				if (nt.dauer > this.def.max_tempo && nt.misc != 222) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>> Fatal error: Dauer zu lang:"+nt.dauer+" <<<<<<<<<<<<<<<<<<<<<<");
					break;
				}
				if (nt.misc == 222)
					System.out.println("melody tone note="+note+ " amplitude="+nt.amplitude+" delay="+nt.delay+" dauer="+nt.dauer);
					
				if (!nt.isAkkord) {
					String txt = "--> Instrument="+nt.channel+" note="+note+"\t f="+format.format(nt.note.freq)+"\t dauer="+format.format(nt.dauer*1000)+"ms";
					if (def.delay > 0)
						txt += "\t delay="+def.delay+"ms";
					if (nt.note.pedal)
						txt += "\tpedal="+nt.note.pedal+" pdauer="+(int)(nt.pDauer*1000)+"ms";
					addLog(txt+" bal="+format.format(nt.balance));
				}
				else {
					addLog("-----> Akkord tension="+nt.note.distance+" note="+note+" f="+format.format(nt.note.freq));
				}
				int channel = nt.channel;
				int midiNote = nt.note.midiIndex; //this.pr2.getNoteIndex(nt.freq);
				//addLog("midiNote="+midiNote);
				MidiNote midN = new MidiNote(midiNote,
						(int) ((nt.balance - 0.5) * 127.0 + 0.5) + 64, 
						ProjectTools.mapAplitude(nt.amplitude), channel, sl);
				//----------------- Timer ---
				tTime = System.currentTimeMillis();  
				if (midN.dauer > 0)  {
					midN.delay = (int) (nt.delay);
					
					midN.startTimer(sl + midN.delay);
					//---------- Play it now:--------------
					// ----------- Stereo -------y
					
					mid.channels[channel].channel.controlChange(mid.PAN, midN.balance); 	
					System.out.println("pan="+mid.PAN+" bal="+midN.balance);
					mid.createControlEvent(mid.PAN, midN.balance, mid.channels[channel]);
					//------------ Note ---------
					//addLog("playKomposition() set amplitude:"+midN.amplitude);
					mid.channels[channel].velocity = midN.amplitude;
					//mid.channels[channel].channel.noteOn(midN.midiIndex, midN.amplitude);
					//addLog("playKomposition() note="+idx[c]+" nt.amplitude="+nt.amplitude+" volume="+ampl[c]+" balance="+balance[c]);
					if (midN.delay > 0 )
						midN.startChannel();
					else
						midN.setOn();
					this.mNotes.addElement(midN);
				}
		    }
		   
		    millis = (int) (System.currentTimeMillis() - SysStartTime);
		    slp = intervall - millis;
		    try {
		    	if (slp > 0)
		    		java.lang.Thread.sleep(slp);
		    }	
		    catch (InterruptedException e){}
		    //addLog("playKomposition() ------------- end of intervall, slept "+(slp));	
		    /*for ( c = 0; c < v.size(); c++) {
		    	nt = (Note)  v.elementAt(c);
		    	this.mNotes[c].stopChannel();
		    }
		    */
		    //System.out.println("playKomposition()  end of intervall. real time needed="+(System.currentTimeMillis() - SysStartTime));
		    if (this.mHalt) break; 
		    addLog(" active notes="+activeNotes);
		} // end of loop over flow
		try {
			br.close();
		} catch (Exception ex) {}
		
		String logf = Home+File.separator+GetEnviroment.PLAYLOG;
		new Utils().save(logf,  "ASCII", this.log);
		setToWait(this, true);
		 try {
		    java.lang.Thread.sleep(1000);
		   }	
		    catch (InterruptedException e){}
		mid.close();
		played = true;
    }
    
    public void testMidi() {
    	Vector<MidiNote> notes = new Vector<MidiNote>();
    	notes.addElement(new MidiNote(80, 68, 127, 0, 2000));
    	notes.addElement(new MidiNote(27, 68, 80, 0, 500));
    	notes.addElement(new MidiNote(34, 68, 80, 0, 200));
    	notes.addElement(new MidiNote(45, 68, 80, 0, 500));
    	notes.addElement(new MidiNote(33, 68, 80, 0, 200));
    	notes.addElement(new MidiNote(20, 68, 80, 0, 500));
    	
    	mid.close();
		this.enableMidi = doMidiOpen();
		if (!this.enableMidi) {
		    setToWait(this, true);
		    return;
		}
		this.log = new Vector<String>(); 
		this.playMidi = true;
		mid.startRecord(12); // instrument
		// Instruments:
    	for (int i = 0; i < mid.channels.length; i++) { // hier die Instrumente setzen (2 pro stimme wg.akkord ?)
    		int instr = def.getInstrumentForChannel(i);
    		mid.channels[i].channel.programChange(instr);
    		mid.createShortEvent(mid.PROGRAM, instr, mid.channels[i]);//Instrument change into data stream (track)
    	}
    	int max = notes.size();
    	this.mNotes = new Vector<MidiNote>();
		activeNotes = 0;
    	for (int n = 0; n < max; n++ ) {
	    	SysStartTime = System.currentTimeMillis(); //
	    	MidiNote midN = notes.elementAt(n);
	    	midN.startTimer(midN.dauer);
			mid.channels[midN.midiChannel].channel.controlChange(mid.PAN, midN.balance); 	
			mid.createControlEvent(mid.PAN, midN.balance, mid.channels[midN.midiChannel]);
			//------------ Note ---------
			mid.channels[midN.midiChannel].channel.noteOn(midN.midiIndex, midN.amplitude);
			mid.createShortEvent(mid.NOTEON + midN.midiChannel, midN.midiIndex, mid.channels[midN.midiChannel]);
			//addLog("playKomposition() note="+idx[c]+" nt.amplitude="+nt.amplitude+" volume="+ampl[c]+" balance="+balance[c]);
			this.mNotes.addElement(midN);
			/*
			long millis = (int) (System.currentTimeMillis() - SysStartTime);
			long sleep = (int) (midN.dauer);
			long slp = sleep - millis;
			*/
			long slp = 500;
			try {
				if (slp > 0)
					java.lang.Thread.sleep(slp);
			}	
			catch (InterruptedException e){}
			addLog(" active notes="+activeNotes);
    	}
    	 setToWait(this, true);
    }
    
   // int of = 0;
    public void saveMidi() {
		//------save --------
		String midf = CustomPath+oFile;
		int id = midf.lastIndexOf(".");
		midf = midf.substring(0, id);
		midf = midf + ".mid";
		mid.saveMidiFile(new File(midf));
    }
  
    public void displayRT(Soundscape rt) {
    	if (this.gc != null) gc.setTable(rt, this.def.gamma, this.def.mode);
    }

@SuppressWarnings("rawtypes")
public void focusGained(FocusEvent e) {
    Object o = (Object) e.getSource();
    debugOut("focusGained: Object o="+o.toString(), 5);
    Class c = ((Object) model.ptfs[model.MINFREQ]).getClass();
    if (c.isInstance(o) ) ((JTextField) e.getSource()).selectAll();
}

@SuppressWarnings("rawtypes")
public void focusLost(FocusEvent e) {
    // treat like user hit 'Return'
    Object o = (Object) e.getSource();
    debugOut("focuslost: Object o="+o.toString(), 5);
    Class c = ((Object)  model.ptfs[model.MINFREQ]).getClass();
    if (c.isInstance(o)) checkInput((JTextField) e.getSource());
}

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
	Project2 p = new Project2(this.model.pModel);
	this.model.pModel.def = def;
	this.model.pModel.notes = this.model.pModel.createNoteTable(this.model.pModel.note_mode, def.min_freq, def.max_freq, this.model.pModel.anfOkt); // respects the min-max freq.
	// filer die selectierten Noten aus der Quelle !
	Vector<OneNote> source = new Vector<OneNote>();
	for (int n = 0; n < this.model.pModel.notes.size(); n++) {
		OneNote no = this.model.pModel.notes.elementAt(n);
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
    if (tfi == model.ptfs[model.MAXFREQ]) elcode = 1; // maxFreq
    else if (tfi == model.ptfs[model.MINFREQ]) elcode = 2; // minFeq
    else if (tfi == model.ptfs[model.POPULATION]) elcode = 3; // Population
    else if (tfi == model.ptfs[model.NUMBERFITTEST]) elcode = 4; // fittest
    else if (tfi == model.ptfs[model.INTERVAL]) elcode = 5; // interval
    else if (tfi == model.ptfs[model.MINDUR]) elcode = 6; // min_tempo
    else if (tfi == model.ptfs[model.RANDOMWEIGHT]) elcode = 7; // r_weight
    else if (tfi == model.ptfs[model.ITERATIONEN]) elcode = 8; // iterations
    else if (tfi == model.ptfs[model.FOOTPRINT]) elcode = 9; // diff freq
    else if (tfi == model.ptfs[model.MAXDUR]) elcode = 10; // max_tempo
    else if (tfi == model.ptfs[model.SEED]) elcode = 11; // seed
    else if (tfi == model.ptfs[model.DEGRESSION]) elcode = 17; // degression
    else if (tfi == model.ptfs[model.DELAY]) elcode = 18; // iterations

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
    case 18: dTmp = this.def.delay;
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
	case 18: this.def.delay = x; // anzahl	
		disp = this.def.delay;
		if (disp <= 0 ) throw(new NumberFormatException()); 
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
	case 18: this.def.delay = tmp;
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
    this.model.enableChildren(this, w);
    model.bt[model.START].setEnabled(w);
   // this.pt.setEnabled(w);
   // this.pr.setEnabled(w);
   // this.body.setEnabled(w);
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
		 model.bt[model.STOP].setEnabled(false);
		 model.bt[model.START].setEnabled(true);
		 model.bt[model.SAVE].setEnabled(played);
	    debugOut("CEditor: stopping", 2);
	   // body.setEnabled(true);
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
	    //body.setEnabled(true);
		 model.bt[model.STOP].setEnabled(false);
		 model.bt[model.START].setEnabled(true);
		 model.bt[model.SAVE].setEnabled(played);
	    //System.out.println("Play komp. ton="+ton);
	    mi1_5.setEnabled(def.tonal & ton.size() > 0);
	    model.bt[model.PLAY].setEnabled(def.tonal & ton.size() > 0);
	    midiThread = null;
	}

	public void run() {
	    Thread thisThread = Thread.currentThread();
	    while(midiThread == thisThread) {
	    	playKomposition();
	    	//testMidi();
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
	    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // abh�ngig vom OS
	    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); // java L&F
	    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); // SUN
	} catch (Exception e) { }
    }

public int activeNotes;


class MidiNote implements ActionListener {
	int midiIndex;
	int balance;
	int amplitude, dauer, delay;
	boolean isOn;
	Timer timer;
	int midiChannel;
	long tStart = 0;
	
	public MidiNote(int i, int b, int a, int c, int dauer) {
		this.midiIndex = i; // the note we play
		this.balance = b;
		this.amplitude = a;
		this.dauer = dauer;
		midiChannel = c;
		
		
	}
	/*public void setTimer(int sleep) {
		this.timer = new Timer(sleep, this);
    	this.timer.setRepeats(false);
	}
	*/
	/**
	 * 
	 * @param sleep [ms] Dauer des Tones
	 * @param delay [ms] Verz�gerung des Tones
	 */
	public void startTimer(int sleep) {
		
		this.timer = new Timer(sleep, this);
		
    	this.timer.setRepeats(false);
		//this.timer.setInitialDelay(sleep);
		//this.timer.restart();
    	/*if (delay > 0)
    		this.timer.setInitialDelay(delay);
    		*/
    	this.timer.start();
    	this.tStart = System.currentTimeMillis();
    	addLog("              -> Start Channel Timer"+this.midiChannel+"\t note="+midiIndex+" duration:"+sleep+"ms at:"+tStart+" Systemtime");
		this.isOn = true;
		activeNotes++;
	}
	
	public void startChannel() {
		Timer ti = new Timer(this.delay, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.out.println("Now start delayed note. "+System.currentTimeMillis());
				MidiNote.this.setOn();
			}
		});
		ti.setRepeats(false);
		ti.start();
	}
	
	public void setOn() {
		addLog("              -> Start Note:"+this.midiChannel+"\t note="+this.midiIndex+" at "+System.currentTimeMillis());
		mid.channels[this.midiChannel].channel.noteOn(this.midiIndex, this.amplitude);
		mid.createShortEvent(mid.NOTEON + this.midiChannel, this.midiIndex, mid.channels[this.midiChannel]);	
	}
	
	public void stopChannel() {
		if (this.isOn) {
			int n = this.midiChannel;
			mid.channels[n].channel.noteOff(this.midiIndex, this.amplitude);
			mid.createShortEvent(mid.NOTEOFF+n, this.midiIndex, mid.channels[n]);
			//System.out.println("Channel "+n+" stop with "+idx[n]+" at :"+(System.currentTimeMillis()-SysStartTime));
			//System.out.println("Channel "+n+" stop after :"+(System.currentTimeMillis()-tTime)+" akt[n]="+akt[n]+" idx[n]="+idx[n]);
			this.isOn = false;
			this.timer.stop();
			addLog("              <- Stop Channel "+this.midiChannel+"\t note="+midiIndex+" after "+(System.currentTimeMillis()-tStart));
			activeNotes--;
		}
	}
	
	public void actionPerformed(ActionEvent ev) {
		this.stopChannel();
	}
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






