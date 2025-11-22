package Sound;
import java.awt.*;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.awt.geom.*;
import java.lang.Math;
import javax.swing.*;
import javax.swing.event.*;
/**
 * This class creates a Panel to hold a Graph
 * @version 	$Id$
 * @author 	Nils Kay
 */
class GraphCanvas extends JPanel {
    int fontsize = 10;
    Dimension preferredSize; 	// the Canvas Size
    //BufferedImage offImage, pImage;
    Image offImage, pImage;
    Font norm;
    Point img_size;	// Imagesize
    Color bg;
    RandomTable rt;
    int height;
    double xscale;
    double alpha = Math.toRadians(20.0);
    double beta = Math.toRadians(10.0);
    double cosB = Math.cos(beta);
    double sinB = Math.sin(beta);
    double tanA = Math.tan(alpha);
    int offsetY = 0, mode = 0;
    AlphaComposite ac;
    boolean first, change;

    public GraphCanvas() {
	this(new Dimension(550, 150), 10, Color.lightGray);	 
    }
/**
 * create a Canvas for Graph display
 * @param prefSize the size of this canvas
 */

public GraphCanvas(Dimension prefSize, int fontsize, Color bg) {
    this.preferredSize = prefSize;
    this.fontsize = fontsize;
    this.setBackground(bg);
    this.bg = bg;
    norm = new Font("Helvetica", Font.PLAIN, fontsize);
    setFont(norm);
    // bg = this.getBackground();
    setSize(preferredSize);
    height = preferredSize.height;
    this.first = true;
    this.changeRule(0.4f);

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
    this.setTable(r, 1.0, 0);
}
    private FitnessTable getPolygon(RandomTable rt, double xscale, double yscale, int height, boolean log) {
	int[] xpoints, ypoints;
	xpoints = new int[rt.size * 5];
	ypoints = new int[rt.size * 5];	
	double x = 0, dx;
	int y = 0;
	int oldY = y;
	int numpoints = 0;
	int freq, xp;
	if (log) rt.debugOut("------> New Polygon----",55);
	dx = (double) rt.step *  xscale;	
	for (int n = 0; n < rt.size; n++) { // loop over all Frequencys
	    // Ein n hat die Breite von step hz
	    oldY = y;
	    freq = n * rt.step;
	    x = ((double) freq *  xscale +0.5);	// start of graph
	    y = (int) ( (rt.table[n] - 1.0) * yscale);
	    if (log) rt.debugOut("n="+n+" x="+x+" y="+y+" dx="+dx+" oldY="+oldY+" x="+x,55);		
	    if ( y != oldY) {
		xp = (int) ( x + 0.5);
		if (dx > 1.0) {
		    xpoints[numpoints] = xp; // turning point up
		    if (oldY <= 0 ) ypoints[numpoints] = 0;
		    else ypoints[numpoints] = oldY;
		    if (log) rt.debugOut("Point(b1) n="+n+" "+xpoints[numpoints]+", "+ypoints[numpoints], 55);
		    numpoints += 1;

		    xpoints[numpoints] = xp; // turning point up
		    if (y <= 0 ) ypoints[numpoints] = 0;
		    else ypoints[numpoints] = y;
		    if (log) rt.debugOut("Point(b2) n="+n+" "+xpoints[numpoints]+", "+ypoints[numpoints], 55);
		    numpoints += 1;

		    x += dx;
		    xpoints[numpoints] = xp; // turning point up	
		    ypoints[numpoints] = ypoints[numpoints - 1];
		    if (log) rt.debugOut("Point(b3) n="+n+" "+xpoints[numpoints]+", "+ypoints[numpoints], 55);
		    numpoints += 1;
		}
		else {
		    xpoints[numpoints] = xp; // turning point up
		    if (oldY <= 0 ) ypoints[numpoints] = 0;
		    else ypoints[numpoints] = oldY;
		    if (log) rt.debugOut("Point(a1) n="+n+" "+xpoints[numpoints]+", "+ypoints[numpoints], 55);
		    numpoints += 1;
		    x += dx;
		    xpoints[numpoints] = xp; // turning point up
		    if (y <= 0 ) ypoints[numpoints] = 0;
		    else ypoints[numpoints] = y;
		    if (log) rt.debugOut("Point(b2) n="+n+" "+xpoints[numpoints]+", "+ypoints[numpoints], 55);
		    numpoints += 1;

		}
	    }
	}
	xp = (int) ( x + 0.5);
	//System.out.println("last Point :x-max="+xp);
	if (y != 0 | oldY != 0) {
	    xpoints[numpoints] = xp; // last point
	    ypoints[numpoints++] = 0;
	}
	// copy to:
	int xps[] = new int[numpoints];
	int yps[] = new int[numpoints];
	for (int n = 0; n < numpoints; n++) {
	    xps[n] = xpoints[n];
	    yps[n] = ypoints[n];
	}
	return new FitnessTable(xps, yps, numpoints);
    }

    public void oldMethod(Graphics2D off, RandomTable rt, double xscale, double yscale, int height) {
	// ---------- Old Method ----------------
	int freq, x, y;
	int dx = (int) ((double) rt.step * xscale);
	if (dx < 1) dx = 1;
	for (int n= 0; n < rt.size; n++) { // loop over all Frequencys
	    freq = n * rt.step;
	    x = (int) ((double) freq *  xscale +0.5);	// start of graph
	    dx = (int) ((double) ((freq + rt.step) *  xscale +0.5));	
	    dx -= x;
	    y = (int) ( (rt.table[n] - 1.0) * yscale);
	    if( y > 0 && dx > 0) {
		off.fillRect(x, this.height - y, dx, y);
		rt.debugOut("GraphCanvas.setTable() index n="+n+" freq="+freq+" draw x="+x+" draw y="+y+" dx="+dx, 6);
	    }	
	    
	}
    }

/**
 * Malt den Graphen
 */
public void setTable(RandomTable r, double gamma, int mode) {
    this.rt = r;
    this.mode = mode;
    change = false;
    mode = mode;
    if (this.offImage == null) {
	return;
    }
    //Utils.Converter.doBreak();
    //Graphics2D off = (Graphics2D) this.offImage.getGraphics();
    Graphics2D off = (Graphics2D) this.pImage.getGraphics();
    if ( off == null) return;
    //--------first clear the background
    off.setColor(Color.white);
    off.fillRect(0,0, preferredSize.width - 1, this.height);
    //--------------
    int x, y, n;	// Draw positions in pixel
    off.setColor(Color.blue);
    //--------- Scaling: --------------
    double max = rt.getMaxx();
    //double gamm = Math.toRadians(5.0);
    int hei = this.height;
    int width = this.preferredSize.width;
    int wid = width;
    offsetY = 0;
    int minH = 40;
    int minW = 20;
    int depth = 0;
    int back_w = width, dw = 0, dh = 0, redw = 0;
    if (mode == 0) {
	offsetY = 20;
	hei = this.height - 35 - offsetY;
    }
    else if (mode == 1) {
	hei = 100;
	offsetY = 50;
	//width = (int) ((double) (this.height - hei - offsetY) * ( Math.cos(beta) / Math.sin(beta)) + 0.5);
	width = (int) ((double) (this.height - hei - offsetY) / Math.sin(beta) + 0.5);
	width -= (int) ((double) (hei + offsetY) * Math.tan(beta));
	depth = (int) ((double) (hei + offsetY - minH) / Math.tan(alpha+beta) + 0.5);
    }
    else if (mode == 2) {
	hei = 100;
	wid = (int) ((double) width * Math.cos(beta) + 0.5);	
	offsetY = (int) ((double) width * Math.sin(beta) + 0.5);	
	depth = (int) ((double) (this.height - minH - offsetY) / Math.tan(alpha) + 0.5);
	double tanB = Math.tan(beta);
	//double tanG = Math.tan(gamm);
	double h = (double) this.height;
	double w = width;
	double m = h / (double) (w - wid);
	double ddw = m * (double) wid + (double) (h - minH) + tanB * (double) depth;
	ddw = ddw / ( tanB + m);
	double ddh = m * (ddw - (double) wid);
	redw = (int) (((double) (h - minH) - ddh) / Math.sin(beta) +0.5); // reduced width
	//System.out.println("This.height="+this.height+" width="+width+" ddw="+ddw+" ddh="+ddh+" redw="+redw);
	dh = (int) (ddh +0.5);
	dw = (int) (ddw +0.5);
    }
    //System.out.println("GraphCanvas.setTable() width="+width);
    double yscale = (double) (hei) / max;
    this.xscale = (double) width / (double) (rt.stop - rt.start);
    //---------------------------------
    //System.out.println("GraphCanvas.setTable() width of rect="+dx);
    //oldMethod(off, rt, xscale, yscale, hei); // for tests this may be in !
    
    FitnessTable fitness = getPolygon(this.rt, xscale, yscale, hei, false);
    this.rt.history.addElement(fitness); // the most-recent Polygon
    if (mode == 0) {
	do2D(off, rt, fitness, wid, hei, offsetY); 
    }
    else if (mode == 1) {
	do3DDraw(off, hei, width, depth, minH);
    }
    else if (mode == 2) {
	doMy3DDraw(off, hei, width, depth, minH, dh, dw, redw);
    }
    if (this.offImage != null) {
	Graphics g = this.offImage.getGraphics();
	g.drawImage(pImage, 0, 0, this); // display buffered Image
    }
    change = true;
    repaint();
    
}

    private void do2D(Graphics2D off, RandomTable rt, FitnessTable fitness, int wid, int hei, int offsetY) {
	int x, y;
	GradientPaint redtowhite = new GradientPaint(0, 0, 
						     new Color(160, 250, 100), 
						     0, hei, 
						     new Color(255, 120, 50));
	Polygon poly = fitness.getPolygon(this.height, offsetY);
	off.setPaint(redtowhite);
	off.fill(poly);
	off.setColor(Color.black);
	off.draw(poly);
	//------------------
	//----- actual freq marker:
	int freq, df, dy,dx;
	dx = (int) ((double) (rt.centerFreq - rt.start) * xscale);
	Point p = new Point(dx, offsetY);
	rt.debugOut("GraphCanvas.setTable() mark-freq="+(rt.centerFreq),5);
	off.setColor(Color.red);
	dy = this.height - p.y;
	off.drawLine(p.x, dy , p.x, dy + 10);
	off.drawLine(p.x, dy , p.x - 2, dy + 3);
	off.drawLine(p.x, dy , p.x + 2, dy + 3);
	//----- Freq. Scale --------------
	double fm = (double) wid / (double) (rt.stop - rt.start);
	off.setColor(Color.black);
	off.setFont(norm);
	for (int n=0; n <= rt.stop; n += 1000) { // loop over all Frequencys
	    dx = (int) ((double) (n - rt.start) * fm +0.5);
	    dy = this.height - offsetY;
	    off.drawLine(dx, dy , dx, dy + 5);
	    if (n/1000 > 0) 
		off.drawString(Integer.toString(n/1000)+"k", dx-3, dy + 15);
	}
	//System.out.println("Scale last Point="+dx+" transformed="+p.x);
	// zero line:
	off.setColor(Color.black);
	dx = wid;
	dy = this.height - offsetY;
	//System.out.println("Scale width = "+wid+" transformed="+dx);
	off.drawLine(0, this.height - offsetY, dx, dy);
	off.drawLine(0, this.height - offsetY, 0, this.height - offsetY - hei);
	
	//----- Iteration
	off.setColor(Color.black);
	off.drawString(Integer.toString(rt.iteration+1), 1, 20); //draw the title
	//------ Seed ---
	off.setColor(Color.green);
	dx = (int) ((double) (rt.seed - rt.start) * fm + 0.5);
	//System.out.println("GraphCanvas.setTable() rt.seed="+rt.seed+" dx="+dx);
	dy = this.height - offsetY;
	off.drawLine(dx, dy , dx, dy + 10);
	off.drawLine(dx, dy , dx - 2, dy + 3);
	off.drawLine(dx, dy , dx + 2, dy + 3);
/*
	//----- actual freq marker:
	x = (int) ((double) (rt.centerFreq - rt.start) * xscale);
	rt.debugOut("GraphCanvas.setTable() mark-freq="+(rt.centerFreq),5);
	off.setColor(Color.red);
	off.drawLine(x, 10 , x, this.height);
	//----- Iteration
	off.setColor(Color.black);
	off.setFont(norm);
	off.drawString(Integer.toString(rt.iteration+1), 1, 20); //draw the title
	//------ Seed ---
	off.setColor(Color.green);
	x = (int) ((double) (rt.seed - rt.start) * xscale);
	//System.out.println("GraphCanvas.setTable() rt.seed="+rt.seed+" x="+x);
	off.drawLine(x, 0 , x, 9);
*/
    }


    // Resets the alpha and composite rules with selected items.    
	public void changeRule(float a) {
	    float alpha = a;	//Float.valueOf(a).floatValue();
	    ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	    repaint();
	}

    /**
     * @param off 
     * @param hei the height of the graph
     * @param wid the width of the graph
     * @param alpha angle up	
     * @param beta angle down	
     * @param offsetY position of left, front corner
     * @param depth x position of left, back corner
     * @param minH position of left , back corner
     * @param dh position of right, back corner
     * @param dw position of right, back cornerint[] xpts = new int[4];
     * @param redw reduced with at last line !
     */
    private void doMy3DDraw(Graphics2D off, int hei, int wid, int depth, 
			    int minH, int dh, int dw, int redw) {
	
	off.setColor(Color.black);	
	//AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);  
	//float alpha = 0.3f; 
	//--------------- test;
	int[] xpts = new int[4];
	int[] ypts = new int[4];
	Point p;
	if (this.first) {
	    xpts[0] = 0; // the initial start-point !
	    ypts[0] = 0;
	    xpts[1] = xpts[0];
	    ypts[1] = hei;
	    xpts[2] = wid;
	    ypts[2] = ypts[1];
	    xpts[3] = xpts[2];
	    ypts[3] = 0;
	    //------- First --------
	    Point pts[] = new Point[xpts.length];
	    int tmp;
	    for (int n = 0; n < xpts.length; n++) {
		tmp = ypts[n] + offsetY;
		pts[n] = new FitnessTable().translatePoint(xpts[n], tmp, cosB, sinB);
		//pts[n] = new Point(xpts[n], tmp);
	    }
	    /*off.drawLine(pts[0].x, this.height - pts[0].y, pts[1].x, this.height - pts[1].y);
	      off.drawLine(pts[1].x, this.height - pts[1].y, pts[2].x, this.height - pts[2].y);
	      off.drawLine(pts[2].x, this.height - pts[2].y, pts[3].x, this.height - pts[3].y);
	      off.drawLine(pts[3].x, this.height - pts[3].y, pts[0].x, this.height - pts[0].y);
	      //---- 
	      off.setColor(Color.blue);
	      off.drawLine(0, this.height - offsetY, depth, minH);
	      //---- 
	      off.setColor(Color.green);
	      off.drawLine(pts[3].x, this.height, dw , this.height - dh);
	    */
	    //------------------------
	    //int[] xpts = new int[4];
	    //int[] ypts = new int[4];
	    p = new FitnessTable().translatePoint(wid, offsetY, cosB, sinB);
	    xpts[0] = 0; // the initial start-point !
	    ypts[0] = offsetY;
	    xpts[1] = depth;
	    ypts[1] = this.height - minH;
	    xpts[2] = dw;
	    ypts[2] = dh;
	    xpts[3] = pts[3].x;
	    ypts[3] = 0;
	    for (int n = 0; n < xpts.length; n++) 
		ypts[n] = this.height - ypts[n];
	    Polygon pg = new Polygon(xpts, ypts, 4);
	    off.setColor(Color.yellow);
	    off.fill(pg);
	    //this.first = false;
	}
	//--------- Draw all Polygons: ---------
	Polygon pg;
	int step = 7;
	int amount = (int) ((double) depth / (double) step +0.5);
	int start = 0;
	int dx = depth;
	if (this.rt.history.size() > amount) {
	    this.rt.history.removeElementAt(0);
	    start = this.rt.history.size() - amount;
	    if (start < 0 ) start = 0;
	    dx = amount * step  - step;
	}
	else if (this.rt.history.size() < amount) {
	    start = 0;
	    dx = this.rt.history.size() * step - step; // weniger als depth
	}
	FitnessTable fit = null;
	GradientPaint redtowhite = new GradientPaint(0, 0, 
						     new Color(160, 250, 100), 
						     0, hei, 
						     new Color(255, 120, 50));
	//System.out.println("this.rt.history.size()="+this.rt.history.size()+" depth="+depth+" dx="+dx);
	double x = (double) dx;
	double scaleX, scaleY;
	int redH, redW, tmpx, tmpy;
	//System.out.println("-------------------------------");
	//off.setComposite(ac);
	redtowhite = new GradientPaint(0, 0, 
				       new Color(160, 250, 100), 
				       0, this.height, 
				       new Color(255, 120, 50));
	for (int n = start; n < this.rt.history.size(); n++) {
	    redH = (int) ((double) (hei) - x * (double) (hei - minH) / (double) depth +0.5);
	    redW = (int) (((double) (wid) - x * (double) (wid - redw) / (double) depth) +0.5);
	    scaleX = (double) redW / (double) wid;
	    scaleY = (double) redH / (double) hei;
	    tmpx = (int) (x +0.5); // offset to left, front corner
	    tmpy = offsetY + (int) (tanA * x +0.5);
	    //System.out.println("n="+n+" x="+x+" red="+redW+", "+redH+" delta="+tmpx+", "+tmpy);
	    fit = (FitnessTable) this.rt.history.elementAt(n);
	    pg = fit.getPolygon(this.height, scaleX, scaleY, tmpx, tmpy, cosB, sinB); // the real point in java coords !
	    off.setPaint(redtowhite);
	    off.fill(pg);	
	    off.setColor(Color.white);
	    off.draw(pg);
	    x -= (double) step; 
	}
	//----- actual freq marker:
	dx = (int) ((double) (rt.centerFreq - rt.start) * xscale);
	p = fit.translatePoint(dx, offsetY, cosB, sinB);
	rt.debugOut("GraphCanvas.setTable() mark-freq="+(rt.centerFreq),5);
	off.setColor(Color.red);
	int dy = this.height - p.y;
	off.drawLine(p.x, dy , p.x, dy + 10);
	off.drawLine(p.x, dy , p.x - 2, dy + 3);
	off.drawLine(p.x, dy , p.x + 2, dy + 3);
	//----- Freq. Scale --------------
	int freq, df;
	double fm = (double) wid / (double) (rt.stop - rt.start);
	off.setColor(Color.black);
	off.setFont(norm);
	for (int n=0; n <= rt.stop; n += 1000) { // loop over all Frequencys
	    dx = (int) ((double) (n - rt.start) * fm +0.5);
	    p = fit.translatePoint(dx, offsetY, cosB, sinB);
	    dy = this.height - p.y;
	    off.drawLine(p.x, dy , p.x, dy + 5);
	    if (n/1000 > 0) 
		off.drawString(Integer.toString(n/1000)+"k", p.x-3, dy + 15);
	}
	//System.out.println("Scale last Point="+dx+" transformed="+p.x);
	// zero line:
	p = fit.translatePoint(wid, offsetY, cosB, sinB);
	off.setColor(Color.black);
	dy = this.height - p.y;
	//System.out.println("Scale width = "+wid+" transformed="+p.x);
	off.drawLine(0, this.height - offsetY, p.x, dy);
	off.drawLine(0, this.height - offsetY, 0, this.height - offsetY - hei);
	
	//----- Iteration
	off.setColor(Color.black);
	off.drawString(Integer.toString(rt.iteration+1), 1, 20); //draw the title
	//------ Seed ---
	off.setColor(Color.green);
	dx = (int) ((double) (rt.seed - rt.start) * fm + 0.5);
	//System.out.println("GraphCanvas.setTable() rt.seed="+rt.seed+" dx="+dx);
	p = fit.translatePoint(dx, offsetY, cosB, sinB);
	dy = this.height - p.y;
	off.drawLine(p.x, dy , p.x, dy + 10);
	off.drawLine(p.x, dy , p.x - 2, dy + 3);
	off.drawLine(p.x, dy , p.x + 2, dy + 3);
	
/* // test Algo
	//------------- Last (use x,ypts and calculate last rect.
	Point[] pts = new Point[xpts.length];
	int tmpy, tmpx, tmp;
	int redy, redx;
	double x = (double) depth;
	double step = 10.0;
	for ( x = 0.0; x <= depth; x+= step) { // loop over the available depth
	    off.setColor(Color.red);
	    int[] x_pts = new int[4];
	    int[] y_pts = new int[4];
	    tmpx = (int) ((double) xpts[0] + x +0.5); // offset to left, front corner
	    tmpy = offsetY + (int) (tanA * x +0.5);
	    x_pts[0] = xpts[0];
	    y_pts[0] = ypts[0];
	    redy = (int) ((double) (ypts[1]) - x * (double) (ypts[1] - minH) / (double) depth +0.5);
	    x_pts[1] = x_pts[0];
	    y_pts[1] = y_pts[0] + redy;
	    redx = (int) (((double) (xpts[2]) - x * (double) (xpts[2] - redw) / (double) depth) +0.5 );
	    x_pts[2] = x_pts[1] + redx;
	    y_pts[2] = y_pts[1];
	    x_pts[3] = x_pts[2];
	    y_pts[3] = y_pts[0];
	    for (int n = 0; n < xpts.length; n++) {
		tmp = y_pts[n] + offsetY;
		pts[n] = translatePoint(x_pts[n], y_pts[n], cosB, sinB);
		//pts[n] = new Point(x_pts[n] + tmpx, y_pts[n] + tmpy);
		pts[n].x = pts[n].x + tmpx;
		pts[n].y = pts[n].y + tmpy;
	    }
	    off.drawLine(pts[0].x, this.height - pts[0].y, pts[1].x, this.height - pts[1].y);
	    off.drawLine(pts[1].x, this.height - pts[1].y, pts[2].x, this.height - pts[2].y);
	    off.drawLine(pts[2].x, this.height - pts[2].y, pts[3].x, this.height - pts[3].y);
	    off.drawLine(pts[3].x, this.height - pts[3].y, pts[0].x, this.height - pts[0].y);
	}
*/
	
    }



    private void do3DDraw(Graphics2D off, int hei, int wid, int depth, int minH) {
	//---------------
	AffineTransform saveXform = off.getTransform();
	// ------ 3D - Effect: ---------------
	double dscale = 1.0;
	double scaley = 1.0;
	double tx = 0, ty = hei;
	AffineTransform at;
	AffineTransform toCenterAt;
	Polygon pg;
	//tx = (double) (hei + offsetY) * Math.sin(beta);
	//tx = (double) (hei) * Math.sin(beta);
	double dy = 0.0;
	double tanA = Math.tan(alpha+beta);
	//--------- Draw all Polygons: ---------
	System.out.println("this.rt.history.size()="+this.rt.history.size()+" tx="+tx+" hei="+hei);
	int step = 4;
	int amount = depth / step;
	int start = 0;
	int dx = depth;
	if (this.rt.history.size() >= amount) {
	    this.rt.history.removeElementAt(0);
	    start = this.rt.history.size() - amount;
	    if (start < 0 ) start = 0;
	}
	else if (this.rt.history.size() < amount) {
	    start = 0;
	    dx = this.rt.history.size() * step;
	}
	// -------------- Show koordinate-system: --- orig, not rotated
	off.setColor(Color.black);
	int xmax = (int) ((double) (this.height - hei - offsetY) / Math.tan(beta) +0.5);
	//off.drawLine(0, hei + offsetY, depth, minH); 	// Oben
	off.setColor(Color.green);
	off.drawLine(0, hei + offsetY, xmax, this.height); // unten
	//------------------------------------------------------
	System.out.println("height="+height+" width="+this.preferredSize.width+" Depth="+depth+" step="+step+" amount="+amount+" start="+start+" dx="+dx);
	// --- scaleing:
	double quo = (double) minH / (double) hei;
	FitnessTable fit;
	for (int n = start; n < this.rt.history.size(); n++) {
	    dy = tanA * (double) dx;
	    dx -= step;
	    System.out.println("n="+n+" dx="+dx+" dy="+dy+" scaley="+scaley);
	    fit = (FitnessTable) this.rt.history.elementAt(n);
	    //pg = new Polygon(fit.xp, fit.yp, fit.max);
	    pg = fit.getPolygon(this.height, 0);
	    //System.out.println("n % amount="+(n % amount));
	    scaley = 1.0 - (1.0 - quo) * (double) (n % amount) / (double) amount; 
	    at = new AffineTransform();
	    at.rotate(beta);
	    // at.scale(dscale, scaley);
	    tx = (double) (hei + offsetY - dy) * Math.sin(beta);
	    at.translate(tx + dx, offsetY - dy);
	    off.transform(at); // at
	    off.setColor(Color.white);
	    off.draw(pg);
	    //GradientPaint redtowhite = new GradientPaint(0, 0, new Color(255, 30, 50), 
	    //					 0, hei, new Color(250, 250, 200));
	    
	    GradientPaint redtowhite = new GradientPaint(0, 0, 
							 new Color(160, 250, 100), 
							 0, hei, 
							 new Color(255, 120, 50));
	    
	    off.setPaint(redtowhite);
	    //off.setColor(Color.blue);
	    off.fill(pg);
	    
	    off.setColor(Color.red);
	    //off.drawLine(0, hei, wid, hei );
	    //off.drawRect(0, 0, wid, hei);
	    off.setTransform(saveXform);
	}
    
	/*scaley = 0.3;
	tx = (double) (hei+offsetY) * Math.sin(beta);
	off.setColor(Color.black);
	at = new AffineTransform();
	//at.scale(dscale, scaley);
	at.rotate(beta);
	at.translate(tx, offsetY);
	off.transform(at);
	off.drawRect(0, 0, wid/2, hei);
	off.setTransform(saveXform);
	
	ty = (double) (hei+offsetY) ; //+ (double) hei - (double) hei * scaley;
	tx = (double) (hei+offsetY) * Math.sin(beta);
	off.setColor(Color.yellow);
	at = new AffineTransform();
	at.rotate(beta);
	at.scale(dscale, scaley);
	at.translate(tx, ty);
	off.transform(at);
	off.drawRect(0, 0, wid/2, hei);
	off.setTransform(saveXform);
	*/
	/*
	tx = (double) (hei+offsetY) * Math.sin(beta);
	off.setColor(Color.blue);
	at = new AffineTransform();
	//at.translate(tx, offsetY);
	at.rotate(beta);
	at.translate(tx, offsetY);
	off.transform(at);
	off.drawRect(0, 0, wid, hei);
	off.setTransform(saveXform);

	scaley = 0.4;
	tx = (double) ((hei+offsetY)*scaley) * Math.sin(beta);
	off.setColor(Color.red);
	at = new AffineTransform();
	at.rotate(beta);
	at.scale(dscale, scaley);
	at.translate(tx, offsetY + (hei -hei*scaley));
	off.transform(at);
	off.drawRect(0, 0, wid, hei);
	off.setTransform(saveXform);

	tx = (double) (hei + offsetY) * Math.sin(beta);
	off.setColor(Color.red);
	at = new AffineTransform();
	at.rotate(Math.toRadians(15.0));
	at.translate(tx, offsetY);
	off.transform(at);
	off.drawRect(0, 0, wid, hei);
	off.setTransform(saveXform);

	off.setColor(Color.yellow);
	at = new AffineTransform();
	at.rotate(Math.toRadians(45.0));
	off.transform(at);
	off.drawRect(0, 0, wid, hei);
	off.setTransform(saveXform);
	*/
	
    }


/**
 * @param r the RandomTable
 * @param freq an array of voices
 */
public void showNote(RandomTable r, int[] freq, int index) {
    if (this.offImage != null) {
	Graphics g = this.getGraphics();
	g.drawImage(offImage, 0, 0, this); // display buffered Image
	int x, dy;
	g.setColor(Color.blue);
	for (int n= 0; n < freq.length; n++) {
	    x = (int) ((double) (freq[n] - rt.start) * this.xscale);
	    //System.out.println("Canvas setTable displays now one note ! freq="+freq+" x="+x);
	    Point p;
	    if (this.mode == 0) 
		p = new Point(x, offsetY);
	    else 
		p = new FitnessTable().translatePoint(x, offsetY, cosB, sinB);
	    dy = this.height - p.y;
	    g.drawLine(p.x, dy , p.x, dy + 10);
	    g.drawLine(p.x, dy , p.x - 2, dy + 3);
	    g.drawLine(p.x, dy , p.x + 2, dy + 3);
	}
	g.setColor(Color.black);
	g.setFont(norm);
	g.drawString(Integer.toString(index), 1, 30); //draw the title
    }
}

/*public void paintComponent(Graphics g) {
    //super.paintComponent(g);
    update(g);
}
*/

public void paint(Graphics g) {
    update(g);
}

public void update(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    //if (!change) return;
    if (offImage != null ) {
	g2.drawImage(offImage, 0, 0, this); // display buffered Image
	//System.out.println("Canvas update displays now the offImage !");
	//Utils.Converter.doBreak();
	//change = false;
    }
    else if (offImage == null) 	
	mach();
}

public void mach() {
    System.out.println("------------Canvas Mach was called !");
    Point gap;
    //big = bi.createGraphics();
    change = true;
    if ( offImage == null) {
	try {
	    offImage = (BufferedImage) createImage(preferredSize.width, preferredSize.height);
	    pImage = (BufferedImage) createImage(preferredSize.width, preferredSize.height);
	} catch (OutOfMemoryError e) {
	    System.out.println("Memory:"+e);
	    return;
	}
	Graphics2D off = (Graphics2D) offImage.getGraphics();
	if ( off != null) {
	    // first clear the background
	    off.setColor(Color.white);
	    off.fillRect(0,0, preferredSize.width - 1, preferredSize.height -1);
	    off.setColor(Color.red);
	    off.drawLine(0, 0, preferredSize.width, this.height);
	    off.drawLine(0, this.height, preferredSize.width, 0);
	}
	repaint();	// display the offImage
    }
}

} // end of  class





