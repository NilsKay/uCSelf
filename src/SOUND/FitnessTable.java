package SOUND;

import java.awt.*;
/**
 * This class holds the values for one FitnessTable entry
 * @version 	$Id$
 * @author 	Nils Kay
 */
class FitnessTable  {
 
    int[] xp;
    int[] yp;
    int max;

/**
 * Store the Polygon points for one entry of the RandomTable.
 */
    public FitnessTable() {
	this(null, null, 0);
    }
public FitnessTable(int[] xp, int[] yp, int max) {
    this.xp = xp;
    this.yp = yp;
    this.max = max;
}
    
    public void setPoints(int[] xp, int[] yp) {
		this.xp = xp;
		this.yp = yp;
		this.max = this.xp.length;
    }
    
    @SuppressWarnings("unused")
	public Point translatePoint(int x, int y, double cos, double sin) {
		int[] res = new int[2];
		Point pt = new  Point((int) ((double) x * cos +0.5),
				      y - (int) ((double) x * sin +0.5));
		return pt;
    }

    public Polygon getPolygon(int height, int offsetY) {
		int[] y = new int[this.max];
		for (int n = 0; n < this.max; n++) 
		    y[n] = height - yp[n] - offsetY;
		return new Polygon(this.xp, y, this.max);
    }
   
    public Polygon getPolygon(int height, double scaleX, double scaleY, int tmpx, int tmpy, double cosB, double sinB) {
		// the real point in java coords !
		int[] xs, ys;
		xs = new int[this.max];
		ys = new int[this.max];
		Point p;
		int px, py;
		for (int n = 0; n < this.max; n++) {
		    // first scale:
		    px = (int) ((double) xp[n] * scaleX +0.5);
		    py = (int) ((double) yp[n] * scaleY +0.5);
		    p = translatePoint(px, py, cosB, sinB);
		    p.x += tmpx;
		    p.y += tmpy;
		    xs[n] = p.x;
		    ys[n] = height - p.y;
		}
		return new Polygon(xs, ys, this.max);
    }
}
