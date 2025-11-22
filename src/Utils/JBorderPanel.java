package Utils;
import java.awt.*;
import javax.swing.*;

/**
 * Various graphical borders. The border itself is a Panel so that it can
 * contain other Components (i.e. it borders something). You use the
 * BorderPanel like any other Panel: you set the layout that you prefer and
 * add Components to it. Beware that a null layout does not obey the insets
 * of the panel so if you use null layouts, adjust your measurements to
 * handle the border by calling getInsets().
 *
 */
public class JBorderPanel extends JPanel {
    // Data
    private int style;
    private int thickness;
    private int gap = 2;
    private Color color = Color.black;

    private Font font;
    private String text = null;
    private int alignment;

    public boolean enabled = true;

    /**
     * Constructor. Makes default border.
     */
    public JBorderPanel() {
	this(null, 2, 1);
    }
    /**
     * Constructor. Makes default border.
     */
    public JBorderPanel(String text) {
	this(text, 2, 1);
    }
    /**
     * Constructor. Makes an etched IN border with given text caption.
     *
     * @param text  Text caption
     */
    public JBorderPanel(String text, int gap, int thickness) {
	this.text = text;
	this.thickness = thickness;
	this.gap = gap;
	setBorder(); // enabled
    }

    /**
     * Constructor. Makes SOLID border with gap given.
     * @param gap The gap of the border.
     */
    public JBorderPanel(int gap) {
        this(null, gap, 1);
    }

    public void setEnableState(boolean enabled) {
	this.enabled = enabled;
	this.setBorder();
	this.setEnabled(enabled);
    }

    public void setText(String text) {
	this.text = text;
	this.setBorder();
    }

    /**
     * Sets the thickness of the border.
     *
     * @param thickness The new thickness
     */
    public JBorderPanel setThickness(int thickness) {
        if (thickness > 0) {
            this.thickness = thickness;
            setBorder();
	}
        return this;
    }

   /**
     * Sets the font. Only applies to etched borders.
     */
    public JBorderPanel setTextFont(Font font) {
        // set font
        if (font != null) {
            this.font = font;
	    setBorder();
	}
        return this;
    }

    public void setBorder() { 
	Color col = Color.black;
	if (!this.enabled) col = Color.red;
	if (this.text != null) {
	    this.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder(
		    BorderFactory.createLineBorder(col, 1), this.text),
	    BorderFactory.createEmptyBorder(this.gap, this.gap, this.gap, this.gap)));//top, left, bot, right
	}
	else { // no Text
	    this.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(col, this.thickness),
		BorderFactory.createEmptyBorder(this.gap, this.gap, this.gap, this.gap)));
	}
    }

    /**
     * Sets the gap between the border and the contained Component.
     *
     * @param gap The new gap, in pixels.
     */
    public JBorderPanel setGap(int gap) {

        if (gap > -1) {
            this.gap = gap;
	    setBorder();
            /*doLayout();
            repaint();
	    */
            }

        return this;
        }
} // end of class





