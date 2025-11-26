package SOUND.automat;

import java.awt.*;
import java.awt.event.*;

/**
 * Applikation code for the visualization
 * Homepage of orig. Author: http://www.csc.fi/math_topics/Movies/Java_ca1d.html
2,5,354
*/

public class CCA extends Frame implements ActionListener, ItemListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 875232L;
	private int default_k = 4;
    private int default_r = 2;
    private long default_nr = 36;
    private int k, r;
    private long nr;
    private boolean layout_ready = false;
    private Thread drawing_thread = null;
    private CA_Canvas ca_canvas;
    Choice states_choice, radius_choice;
    TextField rule_field;
    Label states_label;
    Button stop_button, restart_button;

    boolean isChanging = true;

  public CCA(String[] args) {

      if (drawing_thread != null) 
	  drawing_thread = null;
      String tmp;
      k = default_k;
      r = default_r;
      nr = default_nr;
      for (int n = 0 ; n < args.length; n++) {
	  if (args[n].startsWith("-k")) {
	      tmp = args[n].substring(2, args[n].length()).trim();
	      try { 
		  k = Integer.parseInt(tmp); }
	      catch (NumberFormatException e) { k = default_k; }
	  }
	  else if (args[n].startsWith("-r")) {
	      tmp = args[n].substring(2, args[n].length()).trim();
	      try { r = Integer.parseInt(tmp); }
	      catch (NumberFormatException e) { r = default_r; }
	  }
	  else if (args[n].startsWith("-nr")) {
	      tmp = args[n].substring(3, args[n].length()).trim();
	      try { nr = Long.parseLong(tmp); }
	      catch (NumberFormatException e) { nr = default_nr; }
	  }
      }
      if (k < 2) k = default_k;
      if (r < 1) r = default_r;
      if (nr < 0) nr = default_nr;

    if (layout_ready == false) {
      GridBagLayout grid_layout = new GridBagLayout();
      GridBagConstraints constr;
      setLayout(grid_layout);

      Panel ca_panel = new Panel();
      ca_panel.setLayout(new GridLayout(1,0));
      ca_canvas = new CA_Canvas(k, r, nr);
      ca_panel.add("Center",ca_canvas);
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(0, 0, 0, 0);
      constr.weightx = 1.0;
      constr.weighty = 1.0;
      constr.gridwidth = GridBagConstraints.REMAINDER; 
      grid_layout.setConstraints(ca_panel, constr);
      add(ca_panel);

      states_label = new Label("States:");
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.1;
      constr.weighty = 0.0;
      constr.gridx = 1;
      constr.gridy = 2;
      grid_layout.setConstraints(states_label, constr);
      add(states_label);

      states_choice = new Choice();
      states_choice.addItem("2");
      states_choice.addItem("3");
      states_choice.addItem("4");
      states_choice.addItem("5");
      states_choice.addItem("6");
      states_choice.addItem("7");
      states_choice.addItem("8");
      states_choice.addItem("9");
      states_choice.addItem("10");
      states_choice.select(k-2);
      states_choice.addItemListener(this);
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.3;
      constr.weighty = 0.0;
      constr.gridx = 2;
      constr.gridy = 2;
      grid_layout.setConstraints(states_choice, constr);
      add(states_choice);

      Label radius_label = new Label("Radius:");
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.1;
      constr.weighty = 0.0;
      constr.gridx = 3;
      constr.gridy = 2;
      grid_layout.setConstraints(radius_label, constr);
      add(radius_label);

      radius_choice = new Choice();
      radius_choice.addItem("1");
      radius_choice.addItem("2");
      radius_choice.addItem("3");
      radius_choice.addItem("4");
      radius_choice.addItem("5");
      radius_choice.addItem("6");
      radius_choice.select(r-1);
      radius_choice.addItemListener(this);
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.3;
      constr.weighty = 0.0;
      constr.gridx = 4;
      constr.gridy = 2;
      grid_layout.setConstraints(radius_choice, constr);
      add(radius_choice);

      Label rule_label = new Label("nr:");
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.1;
      constr.weighty = 0.0;
      constr.gridx = 5;
      constr.gridy = 2;
      grid_layout.setConstraints(rule_label, constr);
      add(rule_label);

      rule_field = new TextField(""+nr, 6);
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      rule_field.addActionListener(this);
      constr.weightx = 0.3;
      constr.weighty = 0.0;
      constr.gridx = 6;
      constr.gridy = 2;
      constr.gridwidth = GridBagConstraints.REMAINDER;
      grid_layout.setConstraints(rule_field, constr);
      add(rule_field);

      stop_button = new Button("Stop");
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.3;
      constr.weighty = 0.0;
      constr.gridx = 2;
      constr.gridy = 3;
      constr.gridwidth = 2; 
      stop_button.addActionListener(this);
      grid_layout.setConstraints(stop_button, constr);
      add(stop_button);

      restart_button = new Button("Restart");
      constr = new GridBagConstraints();
      constr.fill = GridBagConstraints.BOTH;
      constr.insets = new Insets(2, 4, 2, 4);
      constr.weightx = 0.3;
      constr.weighty = 0.0;
      constr.gridx = 4;
      constr.gridy = 3;
      constr.gridwidth = 2; 
      restart_button.addActionListener(this);
      grid_layout.setConstraints(restart_button, constr);
      add(restart_button);

      validate();
      layout_ready = true;	
      this.addWindowListener(new MDListener());
      pack();
    }

    drawing_thread = new Thread(ca_canvas);
    drawing_thread.setPriority(Thread.MIN_PRIORITY);
    drawing_thread.start();
    isChanging = false;
  }

  @SuppressWarnings("deprecation")
public void start() {
    if (drawing_thread != null) 
      drawing_thread.resume();
  }
  
  @SuppressWarnings("deprecation")
public void stop() {
    if (drawing_thread != null) 
      drawing_thread.suspend();
  }
/*
    public boolean mouseDown(Event evt, int x, int y) {
	if (drawing_thread != null) 
	    drawing_thread = null;
	drawing_thread = new Thread(ca_canvas);
	drawing_thread.setPriority(Thread.MIN_PRIORITY);
	drawing_thread.start();
	return true;
    }
*/
      @SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
	  Object o = e.getSource();
	  if (o == restart_button) {
	      if (drawing_thread != null) 
		  drawing_thread = null;
	      drawing_thread = new Thread(ca_canvas);
	      drawing_thread.setPriority(Thread.MIN_PRIORITY);
	      drawing_thread.start();
	  }
	  else if (o == stop_button) {
	      if (drawing_thread != null) {
		  drawing_thread.stop();
		  drawing_thread = null;
	      }
	  }			  
	  else if (o == rule_field) {
	      long new_nr;
	      new_nr = Long.parseLong(rule_field.getText());
	      if (new_nr != nr) {
		  if (drawing_thread != null) {
		      drawing_thread.stop();
		      drawing_thread = null;
		  }
		  nr = new_nr;
		  ca_canvas.reinit(k, r, nr);
		  drawing_thread = new Thread(ca_canvas);
		  drawing_thread.setPriority(Thread.MIN_PRIORITY);
		  drawing_thread.start();
	      }
	  }
      }

@SuppressWarnings({ "unused", "deprecation" })
public void itemStateChanged(ItemEvent e) {
    if (isChanging) return;
    int state = e.getStateChange();
    String item;
    Object o = (Object) e.getSource();
    if (o == states_choice) {
	int new_k;
	new_k = Integer.parseInt(states_choice.getSelectedItem());
	if (new_k != k) {
	    if (drawing_thread != null) {
		drawing_thread.stop();
		drawing_thread = null;
	    }
	    k = new_k;
	    ca_canvas.reinit(k, r, nr);
	    drawing_thread = new Thread(ca_canvas);
	    drawing_thread.setPriority(Thread.MIN_PRIORITY);
	    drawing_thread.start();
	}
    }	
    else if (o == radius_choice) {
      int new_r;
      new_r = Integer.parseInt(radius_choice.getSelectedItem());
      if (new_r != r) {
	if (drawing_thread != null) {
	  drawing_thread.stop();
	  drawing_thread = null;
	}
	r = new_r;
	ca_canvas.reinit(k, r, nr);
	drawing_thread = new Thread(ca_canvas);
	drawing_thread.setPriority(Thread.MIN_PRIORITY);
	drawing_thread.start();
      }
    }
}

// Container for the CA image data
@SuppressWarnings("serial")
class CA_Canvas extends Canvas implements Runnable {

  private int width = -1;
  private int height = -1;
  private TotalisticCA CA = null;
  private int[] config = null;
  private Color[] coloring = null;
  private Image ca_picture = null;

  public CA_Canvas(int k, int r, long nr) {
    CA = new TotalisticCA(k, r, nr);
    coloring = get_colors(k);
    this.setSize(400, 300);
  }

  public void reinit(int k, int r, long nr) {
    CA = new TotalisticCA(k, r, nr);
    coloring = get_colors(k);
  }

  private Color[] get_colors(int n) {
    Color[] result = new Color[n];
    float hue;
    int i;
    result[0] = Color.white;
    result[1] = Color.black;
    if (n > 2) {
      for (i = 2; i < n; i++) {
        hue = (i-2.0F)/(n-1.0F);
        result[i] = Color.getHSBColor(hue,1.0F,1.0F);
      }
    }
    return result;
    }

  public void run() {
    Graphics g;
    Dimension d = getSize();
    int[] ca_line;

    width = d.width;
    height = d.height;

    config = CA.config_init(width);
    ca_picture = createImage(width,height);
    g = ca_picture.getGraphics();

    // Draw the background
    g.setColor(coloring[0]);
    g.fillRect(0, 0, width, height);

    // Draw the initial configuration
    for (int i = 0; i < width; i++) {
      g.setColor(coloring[config[i]]);
      g.fillRect(i, 0, 1, 1);
    }
    show_picture();

    // Compute and draw rest of the picture
    ca_line = config;
    for (int j = 1; j < height; j++) {
      ca_line = CA.ca_next(ca_line);
      for (int i = 0; i < width; i++) {
        g.setColor(coloring[ca_line[i]]);
        g.fillRect(i, j, 1, 1);
      }
      show_picture();
    }
    
    // Start sliding the picture up
    try {
      while (true) {
	g.copyArea(0,0,width,height,0,-1);
	ca_line = CA.ca_next(ca_line);
	for (int i = 0; i < width; i++) {
	  g.setColor(coloring[ca_line[i]]);
	  g.fillRect(i, height-1, 1, 1);
	}
	show_picture();
	Thread.sleep(10);
      }
    }
    catch (InterruptedException e) { }
  }

  synchronized void show_picture() {
    Graphics gp = this.getGraphics();
    if (gp != null && ca_picture != null) {
      // Dimension d = size();
      // Image picture;
      // picture = ca_picture.getScaledInstance(d.width,d.height,1);
      Image picture = ca_picture;

      gp.drawImage(picture,0,0,this);
      gp.dispose();
    }
  }

  public void paint(Graphics g) {
    show_picture();
  }

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
	    dispose();
	    System.exit(0);	
	}
    }	
    }// end of inner class

    public static void main(String args[]){
	CCA lb = new CCA(args);
	lb.setVisible(true);
    }
}
