package Sound;
import java.awt.*;
import java.awt.image.*;
/**
 * This class creates a Canvas to hold a Graph
 * @version 	$Id$
 * @author 	Nils Kay
 */
class GraphCanvas extends Canvas {
    int fontsize = 10;
    Dimension preferredSize; 	// the Canvas Size
    Image offImage;
    Font norm;
    Point img_size;	// Imagesize
    Color bg;
    RandomTable rt;
    int height;
    public GraphCanvas() {
	this(new Dimension(550, 150), 10);	 
    }
/**
 * create a Canvas for Graph display
 * @param prefSize the size of this canvas
 */

public GraphCanvas(Dimension prefSize, int fontsize) {
    this.preferredSize = prefSize;
    this.fontsize = fontsize;
    norm = new Font("Helvetica", Font.PLAIN, fontsize);
    setFont(norm);
    bg = this.getBackground();
    setSize(preferredSize);
    height = preferredSize.height;

}

public Dimension getMinimumSize() {
    return new Dimension(10, 10);
}

public Dimension getPreferredSize() {
    return preferredSize;
}

/**
 * Malt den Graphen
 */
public void setTable(RandomTable r) {
    this.setTable(r, 1.0);
}

/**
 * Malt den Graphen
 */
public void setTable(RandomTable r, double gamma) {
    this.rt = r;
    double max = rt.getMaxx();
    double yscale = (double) (this.height - 9) / max;
    double xscale = (double) this.preferredSize.width / (double) (rt.stop - rt.start);
    if (this.offImage == null) {
	return;
    }
    Graphics off = this.offImage.getGraphics();
    if ( off != null) {
	// first clear the background
	off.setColor(Color.white);
	off.fillRect(0,0, preferredSize.width - 1, this.height);
	int x, y, dx, dy, n;	// Draw positions in pixel
	int freq;
	/*off.setColor(Color.red);
	x = (int) ((double) rt.start *  xscale +0.5);	// start
	off.fillRect(0, 0, x, this.height);// left border
	x = (int) ((double) rt.stop *  xscale +0.5);	// start
	off.fillRect(x, 0, preferredSize.width - 1, this.height);// right border
	*/
	dx = (int) ((double) rt.step * xscale);
	if (dx < 1) dx = 1;
	off.setColor(Color.blue);
	//System.out.println("GraphCanvas.setTable() width of rect="+dx);
	for (n= 0; n < rt.size; n++) {
	    freq = n * rt.step;
	    x = (int) ((double) freq*  xscale +0.5);	// start of graph
	    dx = (int) ((double) ((freq + rt.step) *  xscale +0.5));	
	    dx -= x;
	    y = (int) ( (rt.table[n] - 1.0) * yscale);
	    if( y > 0 && dx > 0) {
		off.fillRect(x, this.height - y, dx, y);
		rt.debugOut("GraphCanvas.setTable() index n="+n+" freq="+freq+" draw x="+x+" draw y="+y+" dx="+dx, 6);
	    }
	}
	// actual freq marker
	x = (int) ((double) (rt.centerFreq - rt.start) * xscale);
	rt.debugOut("GraphCanvas.setTable() mark-freq="+(rt.centerFreq),5);
	off.setColor(Color.red);
	off.drawLine(x, 10 , x, this.height);
	off.setColor(Color.black);
	off.setFont(norm);
	off.drawString(Integer.toString(rt.iteration+1), 1, 20); //draw the title
	off.setColor(Color.green);
	x = (int) ((double) (rt.seed - rt.start) * xscale);
	off.drawLine(x, 0 , x, 9);
	/*if (rt.qd != null) { // Scale for debug
	    off.setColor(Color.green);
	    for (n = 1000; n <= rt.stop; n += 1000) {
		x = (int) ((double) n *  xscale + 0.5);	
		off.drawLine(x, this.height , x, this.preferredSize.height);
		System.out.println("GraphCanvas.setTable() ruler: freq="+n+" x="+x+" von width="+(preferredSize.width - 1));
	    }
	    }*/
	// Draw Gamma Curce:
	
	int h = this.height - 9;
	double ox = -1;
	int oy = h, ooy = h, oooy = h;
	int ste = preferredSize.width / 100;
	double st;
	for ( st = 0; st < preferredSize.width; st += ste) {
	    off.setColor(Color.green);
	    y = (int) ((double) h * java.lang.Math.pow( st / (double) preferredSize.width, gamma)+0.5);
	    //if (ox >= 0) off.drawLine((int) ox, oy, (int) st, h-y);
	    oy = h -y;
	    y = (int) ((double) h * java.lang.Math.pow( ((double) preferredSize.width - st) / (double) preferredSize.width, gamma) + 0.5);
	    //if (ox >= 0) off.drawLine((int) ox, ooy, (int) st, h-y);
	    ooy = h -y;
	    off.setColor(Color.yellow);
	    y = java.lang.Math.abs(oy - ooy);
	    if (ox >= 0) off.drawLine((int) ox, oooy, (int) st, h-y);
	    oooy = h -y;
	    ox = st;
	    } 
	
    }
    //System.out.println("GraphCanvas.setTable() next is display():"); 
    if (this.offImage != null) {
	Graphics g = this.getGraphics();
	g.drawImage(offImage, 0, 0, this); // display buffered Image
	//System.out.println("Canvas setTable displays now the offImage !");
    }
}

public void paint(Graphics g) {
    //System.out.println("Canvas paint() : wurde aufgerufen !");
    update(g);
}

public void update(Graphics g) {
    if (offImage != null) {
	g.drawImage(offImage, 0, 0, this); // display buffered Image
	//System.out.println("Canvas update displays now the offImage !");
    }
    else mach();
}

public void mach() {
    //System.out.println("Canvas Mach was called !");
    Point gap;
    try {
	offImage = createImage(preferredSize.width, preferredSize.height);
    } catch (OutOfMemoryError e) {
	System.out.println("Memory:"+e);
	return;
    }
    Graphics off = offImage.getGraphics();
    if ( off != null) {
	// first clear the background
	off.setColor(Color.white);
	off.fillRect(0,0, preferredSize.width - 1, preferredSize.height -1);
	off.setColor(Color.red      );
	off.drawLine(0, 0, preferredSize.width, this.height);
	off.drawLine(0, this.height, preferredSize.width, 0);
    }
    repaint();	// display the offImage
}

} // end of  class





