package Utils;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math.*;
/**
 * This class handles an AlarmBox Window
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class Alarmbox extends Dialog implements MouseListener, KeyListener, ActionListener {

/** Alignment of text
 */
public final static int CENTERED = 0;
public final static int LEFT = 1;
public final static int RIGHT = 2;

    Label la, la1, la2 = null;
    TextField tf;
public static String result = null;	// Where the result at an Input box goes
    String def = "";
    Window wi;
    int w = 300, h = 150;	// initial Windowsize
    int syw = 50;
    int syh = 50;	// width and height of symbol when no images defined
    int paw,pah;	// panel Dimensions
    final int MAX = 32; // Maximal number of entry in list
    int type;	// the alarmbox type
    int align;	// Text alignment
    Color fg,bg;
    int x,y;	// this windowPosition
    int parent_w, parent_h;	// The size of the parent Window
    Font fon;
    int fonth;
    int textHeight;	// Height of all TextLines [Pixel]
    Button bt, bt1, bt2;
    Image symbol;
    final int DefFont = 12 ;	// Default Fontsize
    //String imagePath;		// path where to find the symbols
    alPanel ap;
    Panel pan;	// to wrap the Button
//public static boolean Center = true;	// If true, text is centered, else leftbound
public static boolean OK = false;	//The state of a confirmation box
    public int bt_nr = 0;	// the number of the button which was pressed
    String text[];
    String button_label[] = new String[3];
public static final int ALARM = 0;	// To create an Alarmbox with a warning
public static final int MESSAGE = 1;	// To create a MessageBox
public static final int CONFIRMATION = 2;	// A Confirmation Box
public static final int INPUT = 3;	// An Input Box
    // To be continued ...

    //public static String deftitel = "Alaram";
/**
 * Creates an Alarmbox Window
 * @param dw the owner of the Window
 * @param tit the window title
 * @param text an array of Strings to be displayed as Alarmbox text
 */
public Alarmbox(Frame dw, String tit, String text[]) {
    this(dw, tit, text, -1, -1,  CENTERED, null, null);
}
public Alarmbox(Frame dw, String tit, String text[], int ti, int type) {
    this(dw, tit, text, ti, type, CENTERED, null, null);
}

public Alarmbox(Frame dw, String tit, String text[], int ti, int type, String def) {
    this(dw, tit, text, ti, type, CENTERED, null, def);
}

public Alarmbox(Frame dw, String tit, String text[], int ti, int type, int align) {
    this(dw, tit, text, ti, type, align, null, null);
}    
/**
 * Creates an Alarmbox Window
 * @param dw the owner of the Window
 * @param tit the window title
 * @param text an array of Strings to be displayed as Alarmbox text
 * @param ti the fontsize of this Box
 * @param type determines what kind of Dialog this will be
 * @param align the Alignment of the Text
 * @param butt[] an array of strings for labeling the buttons
 * @param def a default value String for INPUT mode
 */
public Alarmbox(Frame dw, String tit, String text[], int ti, int type, int align, String butt[], String def) {
    super(dw,tit,true);
    this.text = text;
    this.def = def;
    if (ti == -1) ti = DefFont;
    if (type == -1) type = ALARM;
    if (butt == null) {
	button_label[0] = "OK";
	button_label[1] = "CANCEL";
	button_label[2] = null; 
    }
    else button_label = butt;
    Point p;
    this.type = type;
    this.align = align;
    try {
	p = dw.getLocationOnScreen(); // Absolute Pos. on Screen
	parent_w = dw.getSize().width;
	parent_h = dw.getSize().height;
    }catch (IllegalComponentStateException e) {
	// Parent is not showing, so use defaults
	p = new Point(0,0);
	parent_w = 600;
	parent_h = 400;
    }
    x = p.x; // Offset of Alarmbox Window to Parent Window
    y = p.y;
    super.setLocation(x,y);	// Where the Window will appear

    //fon = new Font("Helvetica", Font.PLAIN, ti);
    fon = new Font("TimesNewRoman", Font.PLAIN, ti);
    Panel pane = new Panel();
    pane.setFont(fon);
    this.bg = Color.lightGray;
    pane.setBackground(bg);
    fg = Color.black;
    pane.setForeground(fg);
    pane.setLayout(new BorderLayout());
    //pane.addMouseListener(this);
    symbol = loadSymbol(type);	// The image
    ap = new alPanel();
    ap.getPreferredSize();
    ap.setSize(paw,pah);
    pane.add("North",ap);
    ap.addKeyListener(this);
    ap.addMouseListener(this);
    pan = new Panel();
    if (type != INPUT) {
	bt = new Button(button_label[0]);
	pan.add(bt);
	bt.addKeyListener(this);
	bt.addMouseListener(this);
	if (type == CONFIRMATION) {
	    bt1 = new Button(button_label[1]);
	    pan.add(bt1);
	    bt1.addMouseListener(this);
	    try {
		if (button_label[2] != null) {
		    bt2 = new Button(button_label[2]);
		    pan.add(bt2);
		    bt2.addMouseListener(this);
		}
	    } catch (Exception ex) {}
	}
    }
    else {
	// Input box !
	tf = new TextField(this.def, 8);
	tf.addActionListener(this);
	pan.add(tf);
    }
    pan.addMouseListener(this);
    pan.addKeyListener(this);
    pane.add("South",pan);
    h = getPreferredSize().height;
    setSize(w,h);
    this.validate();
    this.addWindowListener(new MDListener());
    pane.addKeyListener(this);
    this.addKeyListener(this);
    pane.addMouseListener(this);
    this.add(pane);
    relocate();
    //doBreak();
} // end of Alarmbox()

/*
    public static int getALARMMode() { return ALARM; }
    public static int getMESSAGEMode() { return MESSAGE; }
    public static int getCONFIRMATIONMode() { return CONFIRMATION; }
    public static int getINPUTMode() { return INPUT; }
*/
public void doBreak() {
	try {
	    String str=null;
	    int l = str.length();
	}catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
/**
 * Calculate the size needed to display this window !
 */
private void getWinsize(String text[]) {
    int n = 0, wi = 50, hi = 0, tmp;
    FontMetrics f = getFontMetrics(fon);
    fonth = f.getHeight();
    for(n = 0; n < text.length; n++) { // Loop over all possible elements
	if (text[n] != null) {
	    tmp = f.stringWidth(text[n]);
	    if (tmp > wi) wi = tmp;	// set window to max. width of text
	    hi = hi + fonth;	// for each line, add to windowheight
	}
    }
    textHeight = hi;
    if (symbol != null) {
	if ((2*symbol.getHeight(this)) > hi) hi = 2 * symbol.getHeight(this)+ 4;
	wi = wi + 2*symbol.getWidth(this) + 10;
    }
    else {
	if ((2*syh) > hi) hi = 2 * syh+ 4;
	wi = wi + 2*syw + 10;
    }
    pah = hi = hi +10;
    paw = wi;
}

class alPanel extends Panel{

public void paint(Graphics g) {
    Color bg;
//    String text;
    int px,py; // Paint position
    int i, fontw;
    int pw,ph;
    int centerOffset;

    pw = this.getSize().width; 	//Fensterbreite
    ph = this.getSize().height;	//Fensterhoehe
    //System.out.println("WindowSize="+pw+", "+ph);
//    FontMetrics f = g.getFontMetrics();
    FontMetrics f = getFontMetrics(fon);
    fonth = f.getHeight();
    bg = this.getBackground();

    g.setColor(bg);
    g.fillRect(0, 0, pw, ph);
    int h;
    int w;
    // draw the image:
    if (symbol != null) { // the image
	h = symbol.getHeight(this);
	w = symbol.getWidth(this);
    }
    else {
	h = syh;
	w = syw;
    }
    px = w / 2;
    py = (ph - h) / 2;
    if (symbol != null) g.drawImage(symbol, px , py, this);
    //else createImages(type, g, px, py); // draw own images
    g.setColor(Color.black);
    px = px + w + w/2;
    py = (ph - textHeight) / 2;
    //System.out.println("Paint start y="+py);
    g.setFont(fon);
    int dx;
    for (i = 0; i < text.length; i++) {
	if (text[i] != null) {
	    fontw = f.stringWidth(text[i]);
	    if (align == CENTERED) dx = px + (pw - px - fontw) / 2;
	    else if (align == LEFT) dx = px;
	    else if (align == RIGHT) dx = pw - fontw - 3;
	    else dx = px; // left as default
	    g.drawString(text[i], dx, py + fonth);
	    //else g.drawString(text[i], px, py + fonth);
	    //System.out.println("Alarmbox:text["+i+"] ="+text[i]);
	    py = py + fonth;
	}
    }
    //requestFocus();
}
public Dimension getMinimumSize() {
    getWinsize(text);
    return new Dimension(paw, pah );
}

public Dimension getPreferredSize() {
    return getMinimumSize();
}

} // end of inner class


/**
 * Move Window so, that it is within the Screen !
 */
private void relocate() {
    int q,r;
    pack();
    w = getSize().width; 	//Fensterbreite
    h = getSize().height;	//Fensterhoehe
    //System.out.println("Alarmbox: Screen="+md.dProp.maxXScreen+", "+md.dProp.maxYScreen+", loc x,y="+x+", "+y+", w,h="+w+", "+h);
    //System.out.println("Alarmbox: gibt loc x,y="+x+", "+y);
    q = parent_w - w;
    r = parent_h - h;
    super.setLocation(x + q/2, y + r/2);// Where this Window will appear
    if (tf != null) tf.requestFocus();
    //else if (bt != null) bt.requestFocus();
    else this.requestFocus();
}

/**
 * Load the symbolImage for this Alarmbox
 * @param type the type of Box that we want to create
 * @return the symbol-image
 */
public Image loadSymbol(int type) {
    Image i;
    MediaTracker tracker = new MediaTracker(this);

    switch(type) {
    case ALARM: i = Toolkit.getDefaultToolkit().createImage(Tools_G.warn); //getImage(imagePath+"warn.gif");
	break;
    case MESSAGE: i = Toolkit.getDefaultToolkit().createImage(Tools_G.info); //getImage(imagePath+"info.gif");
	break;
    case CONFIRMATION:
    case INPUT:
    default:
	i = Toolkit.getDefaultToolkit().createImage(Tools_G.frage); //getImage(imagePath+"frage.gif");
	break;
    }
   
    try {
	if (i != null) {
	    tracker.addImage(i, 0);
	    try {
		tracker.waitForAll();
	    }	
	    catch (InterruptedException e) {
		System.out.println("Alarmbox:error waiting for Image "+e);
	    }
	}
    } catch (Exception ex) {
	System.out.println("Catched:");
	ex.printStackTrace();
	i = null;
    }

    return i;
}
/*    
private void createImages(int type, Graphics g, int px, int py) {
    Font fo = new Font("Helvetica", Font.BOLD, 20);
    g.setFont(fo);
    FontMetrics f = g.getFontMetrics();

    switch(type) {
    case ALARM:
	drawAlarm(g, f, "!", px, py);
	break;
    case MESSAGE:
	drawBubble(g, f, "i", px, py);
	break;
    case CONFIRMATION:
    case INPUT:
    default:
	drawBubble(g, f, "?", px, py);
	break;
    }
}
*/  
/**
 * draws the Alarm icon.
 * syw, syh is the width and height of the symbol, set at the Alarmbox constructor
 * @param g the graphics object
 * @param f FontMetrics to respect the sizes of the font
 * @param sign the string to be displayed in the Icon
 * @param px, py the position, where to draw the image (upper left corner)
 */
/*private void drawAlarm(Graphics g, FontMetrics f, String sign, int px, int py) {
    int x[], y[], no, yh, yo, xh, xo;
    int z, tw; // value for triangle at bubble z = height, w = half of the width
    int fonth = f.getHeight();
    int fontw;
    syw = syh;
    yh = (syh * 3) / 4; // height of symbol
    yo = py + (syh - yh) / 2; // offset in y
    xh = (syw * 3) / 4;	// width of symbol
    xo = px + ( syw - xh ) / 2;
    x = new int[3];
    y = new int[3];
    fontw = f.stringWidth(sign);
    x[0] = xo + xh/2;	// Triangle
    y[0] = yo;
    x[1] = xo;
    y[1] = yo + yh;
    x[2] = xo + xh;
    y[2] = y[1];
    g.setColor(Color.yellow);
    g.fillPolygon(x,y, 3);
    g.setColor(Color.black);
    g.drawPolygon(x,y, 3);
    g.drawString(sign, xo + (xh - fontw)/2, yo + (yh - fonth) / 2 + fonth); // Titel anzeigen
    }
    */
/**
 * draws the bubble icon.
 * syw, syh is the width and height of the symbol, set at the Alarmbox constructor
 * @param g the graphics object
 * @param f FontMetrics to respect the sizes of the font
 * @param sign the string to be displayed in the Icon
 * @param px, py the position, where to draw the image (upper left corner)
 * syw, syh is the width and height of the symbol, set at the Alarmbox constructor
 */
/*private void drawBubble(Graphics g, FontMetrics f, String sign, int px, int py) {
    int x[], y[], no, yh, yo, xh, xo;
    int z, tw; // value for triangle at bubble z = height, w = half of the width
    int fonth = f.getHeight();
    int fontw;

    tw = 4;
    syw = (syh * 4) / 3; 
    yh = (syh * 3) / 4; // height of symbol
    z = (syh - yh) ; // height of band at bottom
    yo = py + (syh - yh - z) / 2; // offset in y
    xh = (syw * 3) / 4;	// width of symbol
    xo = py + ( syw - xh ) / 2;
    fontw = f.stringWidth(sign);
    no = 3;
    //System.out.println("yh="+yh+" xh="+xh+" xo="+xo+" yo="+yo+" fonth="+fonth+" syh="+syh+" syw="+syw+" z="+z+"  tw="+tw);
    x = new int[no];
    y = new int[no];
    x[0] = xo + xh/2 - tw;	// Triangle
    y[0] = yo + yh - 1;
    x[1] = x[0] + tw + tw/2;
    y[1] = y[0] + z;
    x[2] = x[0] + 2* tw;
    y[2] = y[0];
    g.setColor(Color.white);
    g.fillOval(xo, yo, xh, yh);
    g.setColor(Color.black);
    g.drawOval(xo, yo, xh, yh);
    // now the triangle:
    g.setColor(Color.white);
    g.fillPolygon(x,y, 3);
    g.setColor(Color.black);
    g.drawPolyline(x,y, 3);
    //g.fillRect(xo + xh /2, yo + yh, 2, z);
    g.setColor(Color.blue);
    g.drawString(sign, xo + (xh - fontw)/2, yo + (yh - fonth) / 2 + fonth); // Titel anzeigen
    }
    */

    public String getResult() { return this.result; }
    public void setResult(String r) { 
	this.result = r;
	tf.setText(r);
    }
/**
 * To satisfy the layout manager.
 */
public Dimension getMinimumSize() {
    w = ap.getSize().width;

    if (bt != null || tf != null) h = ap.getSize().height + 30 + 2*fonth;
    else h = ap.getSize().height + 30;
    //  System.out.println("Alarmbox.preferredSize: von ap pah=:"+ap.getSize().height+" button h:"+bt.getSize().height);

    return new Dimension(w, h );
} // End of getMinimumSize()

    //If we don't specify this, the canvas might not show up at all
    //(depending on the layout manager).

/**
 * To satisfy the layout manager.
 */
public Dimension getPreferredSize() {
    return getMinimumSize();
} // End of get PreferredSize()

public void mousePressed(MouseEvent e) {
    //Point p;
    // p = e.getPoint(); // relative MousePosition (rel. to Source)
    //System.out.println("Alarmbox: Mouse Pressed");

}

public void mouseReleased(MouseEvent e) {
    //System.out.println("Alarmbox: Mouse Released");
    bt_nr = -1;
    if (e.getSource() == bt) {
	OK = true;
	bt_nr = 0;
    }
    else if (e.getSource() == bt1) { 
	OK = false;
	bt_nr = 1;
    }
    else if (e.getSource() == bt2){
	OK = false;
	bt_nr = 2;
    }
    if (bt_nr >= 0) this.setVisible(false);
}
public void mouseEntered(MouseEvent e) {
    //System.out.println("Alarmbox: Mouse entered");
    //requestFocus();
}
public void mouseExited(MouseEvent e) {
    //System.out.println("Alarmbox: Mouse exited");
    //this.setVisible(false);

}

public void mouseClicked(MouseEvent e) {
    int cnt = e.getClickCount();
    //System.out.println("Alarmbox: Mouse clicked, "+cnt+" times");
    if (cnt > 1) {
	OK = true;
	bt_nr = 0;
	//dispose();
	this.setVisible(false);
    }
}

public void keyTyped(KeyEvent e) {
    // System.out.println("MotorDialog:Key was pressed:"+e.getKeyCode());
}

public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    //System.out.println("Alarmbox:Key was pressed:"+keyCode+" on Component:"+e.getSource());
    if (keyCode == KeyEvent.VK_ENTER ) {
	OK = true;
	bt_nr = 0;
	dispose();
    }
}

public void keyReleased(KeyEvent e) {
    //System.out.println("MotorDialog:Key was released:"+e.getKeyCode());
}
public void actionPerformed(ActionEvent e) {
    if (e.getSource() == tf) {
	// textfield entry
	this.result = tf.getText();
	this.setVisible(false); // end of alarmbox
    }
}
/** 
 * React to Window Events.
 */
    class MDListener extends WindowAdapter {
    public void windowClosing(WindowEvent ev) {
	String arg;
	AWTEvent event = (AWTEvent) ev;
	if (event.getID() == Event.WINDOW_DESTROY) {
	    // When this window is closed, set the values in it
	    //System.out.println("WindowDestroy="+event.getID());
	    // gui.mi2_2.setState(false);		// Window is closed
	    dispose();
	}	
    }	
    }// end of inner class
}// end of class












