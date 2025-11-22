package Utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;




public class CountBuild extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9872436L;
	JTextArea ta, tb;
	String ver, sb;
	
	public CountBuild(String ver, String sb, int no) {
		//JFrame f = new JFrame();
		
		this.ver = ver;
		this.sb = sb;
		
		this.tb = new JTextArea(8, 60);
		tb.setLineWrap(true);
		tb.setWrapStyleWord(true);
		tb.setBackground(Color.LIGHT_GRAY);
		JScrollPane scrollP = new JScrollPane(tb); 
		tb.setEditable(false);
		//tb.setResizable(false);
		if (no == 1) {
			File fi = new File(ver);
			if (fi.exists())
				fi.delete();
		}
		else {
			load(tb);
			//tb.setCaretPosition(tb.getText().length() - 1);
			
		}
		scrollP.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
			public void adjustmentValueChanged(AdjustmentEvent e){
				tb.select(tb.getHeight()+1000,0);
			}});
		this.ta = new JTextArea(20, 60);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(ta); 
		JPanel p = new JPanel();
		JButton bt = new JButton("Close");
		bt.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (ta != null)
					save();
				CountBuild.this.dispose();
			}
		});
		p.add(bt);
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add("North",scrollP );
		pane.add("Center",scrollPane );
		pane.add("South", p);
		this.getContentPane().add(pane);
		this.pack();
		this.setTitle("Enter Change-Info for build "+sb);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (ta != null)
					save();
				CountBuild.this.dispose();
			}
		});
		
	}
	

	private void load(JTextArea ta) {
		Utils ut = new Utils();
		try {
			@SuppressWarnings("rawtypes")
			Vector b = ut.readTextFile(this.ver,  true);
			for (int n = 0; n < b.size(); n++) {
				ta.append(b.elementAt(n)+"\n");
				ta.setCaretPosition(ta.getText().length() - 1);
			}
		} catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
	}
	private void save() {
		try{
			String tx = ta.getText();
			
            FileWriter fstream = new FileWriter(CountBuild.this.ver, true);
            BufferedWriter fbw = new BufferedWriter(fstream);
            
            fbw.write(CountBuild.this.sb+":"+tx);
            fbw.newLine();
            fbw.close();
		} catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
		ta = null;
	}
	
	/**
	 * used to increase the build number every time we create a new Grapholasnt.jar file
	 * Saved in L:\workspace\Grapholasnt\build.txt
	 * @param args
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		String sp = null, tp = null;
		String ver = null;
		String sb=null;
		try {
			
			Properties prop = System.getProperties();
			String path = prop.getProperty("java.class.path", null);
			System.out.println("ClassPath="+path);
			Utils ut = new Utils(); 
			String[] list = ut.getSeparatedValues(path, ';');
			if (list != null && list.length > 0)
				path = list[0];
			int idx = path.lastIndexOf("\\");
			if (idx > 0)
				path = path.substring(0, idx);
			sp = path+"\\build.txt";
			System.out.println("Build source ="+sp);
			tp = path +"\\bin\\Utils\\build.txt";
			System.out.println("Build target ="+tp);
			ver = path+"\\version.txt";
			if (sp != null && tp != null) {
				int bu = -1; // invalid
				Vector b = ut.readTextFile(sp,  true);
				//System.out.println("b[0]="+b.elementAt(0));
				if (b != null) {
					if (b.size() > 0) {
						System.out.println("b[0]="+b.elementAt(0));
						String t = (String) b.elementAt(0);
						/*int id = -1;
						while((id = t.indexOf('0')) >= 0 )
							t = t.substring(id+1, t.length());
						System.out.println("Cleaned number string t="+t);
						*/
						bu = Converter.getInt(t, 0);
						System.out.println("As integer: bu="+bu);
					}
				}
				if (bu < 0)
					System.out.println("Not possible to read the build number !");
				else {
					bu++;
					NumberFormat nf=NumberFormat.getInstance(); // Get Instance of NumberFormat
					nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 5
					nf.setMaximumIntegerDigits(3); // The maximum Digits required is 5
					sb=(nf.format(bu));
					System.out.println("Java: New build number ="+sb);
					Vector<String> t = new Vector<String>();
					t.addElement("# Build file for Grapholas");
					t.addElement(sb);
					//t.addElement("# end of file ");
					ut.save(sp, GetEnviroment.CHARSET_ASCII, t); // source
					ut.save(tp, GetEnviroment.CHARSET_ASCII, t); // target
					// call version input ?
					
					
						
						
			        
				}
				CountBuild cb = new CountBuild(ver, sb, bu);
				cb.setVisible(true);
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Invalid parameter !");
		}
		
		
    }
}