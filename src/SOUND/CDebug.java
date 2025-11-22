package Sound;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.text.*;
import Utils.*; 
/**
 * This class creates a Dialog to display the debug info
 * @author 	Nils Kay
 */
public class CDebug extends Dialog implements ActionListener {
    Color bg,fg,tbg,bbg1,bbg2,xbg,ybg;
    int no;
    Font font;
    Label la, la1, la2;
    List ta1;
    Button stop, go, slow, clear, stp;
    public boolean halt = false;
    boolean langsam = false;
    public boolean stepp = false;
    String la_text = "Queue. Last:";
    GregorianCalendar greg;
    SimpleDateFormat datef;
    long startmillis;
    String dummy = "                        .";
    
/**
 * Creates the Debug Dialog Window 
 * @param dw the owner of the dialog
 * @param text the title
 */
public CDebug(Frame dw, String text) {
    super(dw, text, false);
    super.setLocation(0, 10);
    this.font = new Font("Helvetica", Font.PLAIN, 10);
    String wert;   
    halt = false;
    langsam = false;
    int i,row = 0;
    int d;
    double d1;
    int ygap = 2;
    greg = new GregorianCalendar();
    datef = new SimpleDateFormat("HH:mm:ss");
    Panel pane = new BorderPanel();
    pane.setFont(this.font);
    pane.setLayout(new BorderLayout());
    //Panel body = new Panel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    bg = Color.lightGray;
    fg = Color.black;
    tbg = Color.white;
    xbg = tbg;
    Panel top = new Panel();
    top.setBackground(bg);
    top.setForeground(fg);
    top.setLayout(gridbag);

    c.fill = GridBagConstraints.NONE;
    c.weightx =	1.0; // Gewichtung fuer freien Raum
    c.weighty = 1.0;
    c.gridwidth = 1;
    c.gridheight = 1;
    int xgap = 5;

    // -------------------- Messages in/out
    c.gridx= 1;
    c.gridy= row;
    la1 = new Label("Debug - Output:", Label.LEFT);
    c.insets = new Insets(0, 0, 0, xgap); // top, left, bottom, right;
    gridbag.setConstraints(la1,c);
    top.add(la1);
    c.gridx= 1;
    c.gridy= row+1;
    ta1 = new List(40, false);
    ta1.add("Messages:                                                                   .");
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0, 0, 0, xgap); // top, left, bottom, right;
    gridbag.setConstraints(ta1,c);
    top.add(ta1);
    Panel bts = new Panel();

    // -------------------- Button stop
    stop = new Button("Stop");
    c.insets = new Insets(ygap, 0, 0, xgap); // top, left, bottom, right;
    bts.add(stop);
    stop.addActionListener(this);
    // ------------------- Button go
    go = new Button("Go");
    bts.add(go);
    go.addActionListener(this);
    // -------------------- Button slow
    slow = new Button("Slow");
    bts.add(slow);
    slow.addActionListener(this);
    //top.add(bts);
    // -------------------- Button step
    stp = new Button("Step");
    bts.add(stp);
    stp.addActionListener(this);
    //top.add(stp);
    // -------------------- Button clear
    clear = new Button("Clear");
    bts.add(clear);
    clear.addActionListener(this);
    //top.add(bts);
    // -------------- The Panel itself -------------
    // this.addMouseListener(new OHMouse(gui.hp, gui.blang.ohelp[24]));// For Online Help
    pane.add("North", top);
    pane.add("South", bts);
    //pane.add(body);
    this.add(pane);
    pack();
    setValues();
    startmillis = new Date().getTime();
} // end of SetupDialog()


/**
 * Stop the communication !
 */
    public void setStopped() {
	this.halt = true;
	setValues();
    }

/**
 * A message has been added to the queue !
 */
public void put(String param) {
    addComItem(param);
}

private void addComItem(String tmp) {
    /*long millis = new Date().getTime();
    String wert = datef.format(new Date(millis)); // actualTime
    long m = millis / 1000;
    int i = (int) (millis - m * 1000);
    wert+= ","+Integer.toString(i / 100);
    ta1.add(wert+tmp);	// Communication
    */
    ta1.add(tmp);
    int count = ta1.getItemCount();
    ta1.makeVisible(count - 1);
}

/**
 * Sets all Values up to date, following the instance values like unit etc.
 * Actually, this diplayes all variables set in this class which affect the
 * outlook.
 */
public void setValues() {    
    // Textentrys of the Double Values
    String wert;
    go.setEnabled(this.halt | this.langsam | this.stepp);
    stop.setEnabled(!this.halt);
    slow.setEnabled(!this.langsam);
}

public void actionPerformed(ActionEvent ev) {
    // check the Buttons with this ActionListener
    //System.out.println("actionPerformed !");
    if (ev.getSource() == stop) { // stop
	this.halt = true;
	this.stepp = false;
	this.langsam = false;
    }
    else if (ev.getSource() == go) { // go
	this.halt = false;
	this.langsam = false;
	this.stepp = false;
    }
    else if (ev.getSource() == slow) { // slow
	this.langsam = true;
	this.stepp = false;
	this.halt = false;
    }
    else if (ev.getSource() == stp) { // step
	this.halt = !this.halt;
	this.stepp = true;
	this.langsam = false;
    }
    else if (ev.getSource() == clear) { // clear
	ta1.removeAll();
    }
    setValues();

}


/** React on Window Events.
 *
 */
    class WDListener extends WindowAdapter {
    public void windowClosing(WindowEvent ev) {
	String arg;
	halt = false;
	//log.writeLog("Close WindowEvent="+ev+", Source="+ev.getSource());
	AWTEvent event = (AWTEvent) ev;
	if (event.getID() == Event.WINDOW_DESTROY) {
	    //log.writeLog("WindowDestroy="+event.getID());
	    setVisible(false);
	}
    }
    public void windowIconified(WindowEvent ev) {
	String arg;
    }
    }// end of inner class
} // end of class 



