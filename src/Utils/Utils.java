package Utils;
import java.awt.*;
import java.awt.image.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;

public class Utils {

    JFrame parnt;

    public Utils(JFrame p) {
    	this.parnt = p;
    }

    public Utils() {
    	this.parnt = null;
    }

    public BufferedImage loadImage(String pathname) {
    	BufferedImage img = null;
    	//System.out.println("Load image from hd");
    	try {
    	    img = ImageIO.read(new File(pathname));
    	} catch (IOException e) {
    		img = null;
    	}
    	return img;
    }
    
    public BufferedImage loadImage(String pathname, Dimension d) {
    	try {
    		return subsampleImage(ImageIO.createImageInputStream(new File(pathname)),
    				d.width, d.height, null);
    	}
    	 catch (IOException e) {
     		
     	}
     	return null;
    }
    
    public static BufferedImage subsampleImage(
    		ImageInputStream inputStream,
    	    int x,
    	    int y,
    	    IIOReadProgressListener progressListener) throws IOException {
    	BufferedImage resampledImage = null;

    	Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
    	
    	if(!readers.hasNext()) {
    		throw new IOException("No reader available for supplied image stream.");
    	}
    	
    	ImageReader reader = readers.next();
    	
    	ImageReadParam imageReaderParams = reader.getDefaultReadParam();
    	reader.setInput(inputStream);
    	
    	Dimension d1 = new Dimension(reader.getWidth(0), reader.getHeight(0));
    	Dimension d2 = new Dimension(x, y);
    	int subsampling = (int)scaleSubsamplingMaintainAspectRatio(d1, d2);
    	imageReaderParams.setSourceSubsampling(subsampling, subsampling, 0, 0);
    	
    	reader.addIIOReadProgressListener(progressListener);
    	resampledImage = reader.read(0, imageReaderParams);
    	reader.removeAllIIOReadProgressListeners();
    	
    	return resampledImage;
    }

    public static long scaleSubsamplingMaintainAspectRatio(Dimension d1, Dimension d2) {
    	long subsampling = 1;
    	
    	if(d1.getWidth() > d2.getWidth()) {
    		subsampling = Math.round(d1.getWidth() / d2.getWidth());
    	} else if(d1.getHeight() > d2.getHeight()) {
    		subsampling = Math.round(d1.getHeight() / d2.getHeight());
    	}

    	return subsampling;
    }

    public String addSeparator(String path) {
    	if (!path.endsWith(File.separator))
			path += File.separator;
    	return path;
    }
    
    public String getHomePath() {
    	String p = System.getProperty("user.home");
    	if (!p.endsWith(File.separator))
    		p += File.separator;
    	return p;
    }
    
    /**
     * Return the path of a path+file-name combination:
     * @param in the path+file to examine
     * @return the path
     */
    public String getPath(String in) {
		String path = "";
		try {
		    int n = in.lastIndexOf(File.separator);
		    if (n > 0) path = in.substring(0, n+1);
		}
		catch (Exception e) {
		    //System.out.println("Exception "+e);
		    //e.printStackTrace();
		    path ="";
		}
		return path;
    }
    /**
     * Return the file-name of path+file-name combination:
     * @param in the file to examine
     * @return the filename
     */
    public String getFileName(String in) {
		String file = in;
		try {
		    int n = in.lastIndexOf(File.separator);
		    if (n > 0) file = in.substring(n+1, in.length());
		}
		catch (Exception e) {}
		return file;
    }
    
    public int doConfirmationPane(String text) {
		Object[] options = {
		    "Yes",
		    "No"};
		return JOptionPane.showOptionDialog(parnt,
						     text,
						     "Confirmation Box",
						     JOptionPane.YES_NO_CANCEL_OPTION,
						     JOptionPane.QUESTION_MESSAGE,
						     null,
						     options,
						     options[0]);
    }
    public int doConfirmationPane(String text, String thopt) {
		Object[] options = {
		    "Yes",
		    "No", thopt};
		return JOptionPane.showOptionDialog(parnt,
						     text,
						     "Confirmation Box",
						     JOptionPane.YES_NO_CANCEL_OPTION,
						     JOptionPane.QUESTION_MESSAGE,
						     null,
						     options,
						     options[0]);
    }
    public int doMessagePane(String text) {
    	return doMessagePane(text, null);
    }
    
    public int doMessagePane(String text, Object[] options) {
		if (options == null) {
			options = new String[1];
		    options[0] = "OK";
		}
		return JOptionPane.showOptionDialog(parnt,
						     text,
						     "Message Box",
						     JOptionPane.DEFAULT_OPTION,
						     JOptionPane.ERROR_MESSAGE,
						     null,
						     options,
						     options[0]);
    }
    public int doInfoPane(String text) {
		Object[] options = {
		    "OK"};
		return JOptionPane.showOptionDialog(parnt,
						     text,
						     "Info Box",
						     JOptionPane.DEFAULT_OPTION,
						     JOptionPane.INFORMATION_MESSAGE,
						     null,
						     options,
						     options[0]);
    }

    public String doInputPane(String text) {
		String inputValue = JOptionPane.showInputDialog(text);
		return inputValue;
    }
    public int doMessagePane(String[] text) {
    	String t = "";
    	for (int n = 0; n < text.length; n++) {
    		t += text[n];
    		if (n < (text.length -1))
    			t += "\n";
    	}
    	return doMessagePane(t);
    }
    //---- load images: --------
    public  ImageIcon getIcon(Object parent, String name){
    	try {
    		return new ImageIcon(loadResourceImage(parent, name));
    	} catch (Exception ex) {
    		return null;
    	}
    }
    /**
     * Load an image 
     * @param parent the parent frame that calls this method
     * @param image the image-name to be loaded
     * @return the images
     */
    public Image loadResourceImage(Object parent, String image) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		MediaTracker tracker = new MediaTracker((Component) parent);
		Image im = null;
		java.net.URL url = getResourceURL(image);
		//System.out.println("image url ="+url);
		if (url != null) {
		    im = toolkit.getImage(url);
		    //System.out.println("image ="+im);
		    tracker.addImage(im, 0);	
		}
		try {
		    tracker.waitForAll();
		}
		catch (InterruptedException e) {
		    System.out.println("error waiting for Images !");
		}
		return im;
    }
    
    /**
     * Load an array of images 
     * @param the parent frame that calls this method
     * @param the array of image-names to be loaded
     * @return the array of images
     */
    public Image[] loadResourceImages(Object parent, String[] images) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		MediaTracker tracker = new MediaTracker((Component) parent);
		Image im[] = new Image[images.length];
		// Now load the images:
		for (int i = 0; i < im.length; i++) {
		    im[i] = null;
		    java.net.URL url = getResourceURL(images[i]);
		    //System.out.println("Converter.loadResourceImages() try image="+images[i]);
		    if (url != null) {
			im[i] = toolkit.getImage(url);
			tracker.addImage(im[i], 0);
		    }
		}
		try {
		    tracker.waitForAll();
		}
		catch (InterruptedException e) {
		    System.out.println("error waiting for Images !");
		}
		return im;
    }
    /**
     * Get the URL of a Resource to be loaded from Package System
     * @param the filename of the file to be loaded
     */
    public java.net.URL getResourceURL(String file) {
		java.net.URL imageURL = null;
		//String pt = new GetSystemPath().getSystemDir()+file;
		String pt = "Utils/"+file;
		try {
		    //System.out.println("image="+pt);
		    imageURL = ClassLoader.getSystemResource(pt);
		    //System.out.println("SystemPath+image="+pt+" URL zum BILd="+imageURL.getFile());
		} catch (Exception e) {
		    System.out.println("Utils.getResourceURL() Couldn't load resource:"+pt);
		    imageURL = null;
		    e.printStackTrace();
		}
		return imageURL;
    }
    /**
     * Get the URL of a Resource to be loaded from Package System
     * @param the filename of the file to be loaded
     */
    public java.net.URL getResourceOldURL(String file) {
		java.net.URL imageURL = null;
		//String pt = new GetSystemPath().getSystemDir()+file;
		String pt = file; //"Raumschlacht/res/"+file;
		try {
		    //System.out.println("image="+pt);
		    imageURL = ClassLoader.getSystemResource(pt);
		    //System.out.println("SystemPath+image="+pt+" URL zum BILd="+imageURL.getFile());
		} catch (Exception e) {
		    System.out.println("Converter.getResourceURL() Couldn't load resource:"+pt);
		    imageURL = null;
		    e.printStackTrace();
		}
		return imageURL;
    }

    @SuppressWarnings("rawtypes")
	public Vector readTextFile(String path, boolean mode) {
    	File fd = new File(path);
    	return readTextFile(fd, mode);
    }
    /**
     * Just a small Method to load a text file line by line into a vector
     * @param path the path+file name of the file
     * @param mode if true, discard all comment lines
     * @return the Vector or null on error
     */
    @SuppressWarnings({ "rawtypes" })
	public Vector readTextFile(File fd, boolean mode) {
		if (!fd.exists()) {
		    System.out.println("Utils.readTextFile() : file:"+fd.getPath()+"  does not exist !");
		    return null;
		}
		try {    
		    InputStreamReader isr = new InputStreamReader(new FileInputStream(fd));
		    return readTextFile(isr, mode);
		}
		catch (java.io.FileNotFoundException ex) {
		}
		return null;
    }

    /**
     * Just a small Method to load a text file line by line into a vector
     * @param re a Reader Object
     * @param mode if true, discard all comment lines
     * @return the Vector or null on error
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector readTextFile(Reader re, boolean mode) {
		BufferedReader br;
		br = new BufferedReader(re);
		Vector res = new Vector();
		String res0;
		try {
		    while((res0 = br.readLine()) != null) {
			if (mode) {
			    if (!res0.startsWith("#"))
				res.addElement(res0);
			}
			else 
			    res.addElement(res0);
		    }
		    br.close();	// close the Buffered Reader
		} catch (java.io.IOException e) {
		    System.out.println("Utils.readTextFile(): catched "+e);
		    return null;
		}
		return res;
    }

    
    public String getFileType(String in) {
    	return getFileType(in, false);
        }
        /**
         * Return the file-type of an image:
         * @param in the file to examine
         * @param mode = true -> do lower case conversion too
         * @return the used extension
         */
    public String getFileType(String in, boolean mode) {
    	if (mode) in = in.toLowerCase();
    	String ext = in;
    	try {
    	    int n = in.lastIndexOf(".");
    	    if (n > 0) ext = in.substring(n, in.length());
    	}
    	catch (Exception e) {}
    	return ext;
    }
    
    public String getBaseName(String in) {
		String base = in;
		try {
		    int n = in.lastIndexOf(".");
		    if (n > 0) base = in.substring(0, n);
		}
		catch (Exception e) {}
		return base;
    }
    
    public String getHost(String addr) {
    	URL url;
    	String host = null;
    	try {
    		String address;
		    url = new URL(addr);
		    int z = addr.lastIndexOf("/");
		    if (z > 0) address = addr.substring(0, z);
		    else address = addr;
		    if (!address.endsWith("/")) address = address + "/";
		    //System.out.println("readPage() base address="+address+" host="+url.getHost()+" protokoll="+url.getProtocol()+" ref="+url.getRef());
		    host = url.getHost();
    	} catch (MalformedURLException e) {
		    e.printStackTrace();
		    return null;
		}
		return host;
    }
    @SuppressWarnings("null")
	public static void doBreak() {
        try {
    	System.out.println("Converter.doBreak() at "+new Date().getTime());
    	String str=null;
    	@SuppressWarnings("unused")
		int l = str.length();
        }catch (Exception ex) {
    	ex.printStackTrace();
        }
    }
    public String getProtokoll(String addr) {
    	URL url;
    	String host = null;
    	try {
    		String address;
		    url = new URL(addr);
		    int z = addr.lastIndexOf("/");
		    if (z > 0) address = addr.substring(0, z);
		    else address = addr;
		    if (!address.endsWith("/")) address = address + "/";
		    //System.out.println("readPage() base address="+address+" host="+url.getHost()+" protokoll="+url.getProtocol()+" ref="+url.getRef());
		    host = url.getProtocol();
    	} catch (MalformedURLException e) {
		    e.printStackTrace();
		    return null;
		}
		return host+"://";
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector readPage(String addr) {
    	// a new address to look for:
		String address = addr;
		URL url;
		URLConnection urc;
		BufferedReader in = null;
		try {
		    url = new URL(addr);
		    int z = addr.lastIndexOf("/");
		    if (z > 0) address = addr.substring(0, z);
		    else address = addr;
		    if (!address.endsWith("/")) address = address + "/";
		    //System.out.println("readPage() base address="+address+" host="+url.getHost()+" protokoll="+url.getProtocol()+" ref="+url.getRef());
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		    return null;
		}
		String inputLine;
		Vector pagecontent = null;
		try {
		    urc = url.openConnection();
		    // FileNameMap fnm = urc.getFileNameMap();
		    in = new BufferedReader(
			new InputStreamReader(
			    urc.getInputStream()));
		    
		    pagecontent = new Vector();
		    while ((inputLine = in.readLine()) != null) {
				//System.out.println(inputLine);
		    	// this input-line may contain multiple links, so split into a couple of lines:
		    	pagecontent.addElement(inputLine);
		    }
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		try {
			if (in != null) in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return pagecontent;
    }
    /** 
     * Return a Vector with all files ending with the file type
     * @param path
     * @param filetype
     * @return
     */
    public Vector<String> findFileTypes(String path, String filetype) {
    	File dfi = new File(path);
    	String[] list = dfi.list();
    	Vector<String> v = new Vector<String>();
    	for (int n = 0; n < list.length; n++) {
    		File ffi = new File(path, list[n]);
    		if (!ffi.isDirectory()) {
    			String name = ffi.getName();
    			String tmp = this.getFileType(name).toLowerCase();
    			if (tmp.equalsIgnoreCase(filetype))
    				v.addElement(name);
    		}
    	}
    	return v;
    }
    
    /**
     * 
     * @param targetPath the path where the file is stored (shall be stored)
     * @param hfile the base-name of the file (no extension, no path)
     * @return a new name if needed, e.g. file_1 or file_2 etc 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String findNewFileVersion(String targetPath, String hfile) {
    	//System.out.println("findNewFileVersion targetPath="+targetPath+" hfile="+hfile);
    	File dfi = new File(targetPath);
    	String[] list = dfi.list();
    	Vector v = new Vector();
    	for (int n = 0; n < list.length; n++) {
    		File ffi = new File(targetPath, list[n]);
    		if (ffi.isFile()) {
    			if (list[n].startsWith(hfile))
    				v.addElement(list[n]);
    		}
    	}
    	String newName = hfile;
    	String tmp;
    	// now we have all versions of this jobs html files
    	if (v.size() > 0 ) { // we have multiple files of this name
    		// next find the highest one:
    		int max = 0;
    		for (int n = 0; n < v.size(); n++) {
    			tmp = (String) v.elementAt(n);
    			tmp = this.getBaseName(tmp);
    			//System.out.println("Check this:"+tmp);
    			int id = tmp.lastIndexOf("_");
    			if (id >= 0) {
    				String t = tmp.substring(id+1, tmp.length());
    				System.out.println("Number string="+t);
    				int z = this.getInt(t, 0);
        			if (z > max) 
        				max = z;
    			}
    		}
    		max += 1; // next item
    		newName = hfile+"_"+max;
    	}
    	//System.out.println("Result="+newName);
		return newName;
	}
    
    /**
     * The file 'in' exists, so create a new name as follows: base_no.ext
     * No is counted to the max. if base_23 is existing, so will create base_24  
     * @param in the double name
     * @return a new name
     */
    public String countStringUp(String in) {
    	String path = this.getPath(in);
    	String name = this.getFileName(in);
	    String base = this.getBaseName(name);
	    base = findNewFileVersion(path, base);
		String ext = this.getFileType(in);
		//System.out.println("countStringUp="+path + base + ext);
		return path + base + ext;
    }
     public boolean checkState(String tm) {
    		boolean b = false;
    		try {
    		    tm = tm.toLowerCase();
    		    if (tm.equals("true") || tm.equals("on") || tm.equals("yes") || tm.equals("1")) b = true;
    		} catch (NullPointerException e) {
    		    //System.out.print("Catched Exception:");
    		    //e.printStackTrace();
    		}
    		return b;
    	    }
     
     /**
      * A Method to read a line of char separated Values into a String array
      * @param line the input line
      * @param sepchar the separator character
      * @return an array of string values
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public String[] getSeparatedValues(String line, int sepchar) {
 		Vector res = new Vector();
 		line = removeFirstSepchar(line, sepchar);
 		int index = line.indexOf(sepchar);
 		//System.out.println("getSeparatedValues() index="+index);
 		
 		while (index > 0) {
 		    res.addElement(line.substring(0, index));
 		    line = line.substring(index+1, line.length());
 		    line = removeFirstSepchar(line, sepchar);
 		    index = line.indexOf(sepchar);
 		}
 		if (line.length() > 0)
 		    res.addElement(line);
 		String[] t = new String[res.size()];
 		res.toArray(t);
 		return t;
     }
    /**
     * A Method to read a line of char separated Values into a String array
     * @param line the input line
     * @param sepchar the separator character
     * @return an array of string values
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String[] getSeparatedValues(String line, int sepchar, boolean allowEmpty) {
		Vector res = new Vector();
		line = removeFirstSepchar(line, sepchar);
		int index = line.indexOf(sepchar);
		//System.out.println("getSeparatedValues() index="+index);
		
		while (index >= 0) {
		    res.addElement(line.substring(0, index));
		    line = line.substring(index+1, line.length());
		    if (!allowEmpty)
		    	line = removeFirstSepchar(line, sepchar);
		    index = line.indexOf(sepchar);
		}
		if (line.length() > 0)
		    res.addElement(line);
		String[] t = new String[res.size()];
		res.toArray(t);
		return t;
    }

    public String removeFirstSepchar(String in, int sepchar) {
		int index = in.indexOf(sepchar);
		while (index == 0) {
		    in = in.substring(index+1, in.length());
		    index = in.indexOf(sepchar);
		}
		return in;
    }
    
    /**
     * Formatting of a double to 8 digits, where per default 3 digits
     * behind the colon will be used.
     *  @param input this is the input double
     **/    
    public static String formatDouble(double input) {
        return formatDouble(input, 8, 3);
    }
    /**
     * Formatting of a double to a number of digits, where per default 3 digits
     * behind the colon will be used.
     *  @param input this is the input double
     *  @param digits the Number of digits to display
     **/    
    public static String formatDouble(double input, int digits) {
        return formatDouble(input, digits, 3);
    }
    /**
     * Formatting of a double to a number of digits, where the number
     * of post-colon digits is also set.
     *  @param input this is the input double
     *  @param digits the Number of digits to display
     *  @param post the Number of digits after the colon
     **/    
    public static String formatDouble(double input, int digits, int post)
        {
    	int len;
    	double tmp;
    	long res;
    	String wert;
    	// Check if input is 0:
    	//System.out.println("formatDouble: is:"+input);
    	if (input == 0.0) return Double.toString(0.0);
    	//doBreak();
            // to round after the post digit:
    	post = java.lang.Math.abs(post); // prevent errors, make it positive
    	tmp = java.lang.Math.pow(10.0,(double)post);
    	input = input * tmp; // blow the Number up
    	//System.out.println("formatDouble: now:"+input+" post ="+post+" tmp="+tmp);
    	// Now round it:
    	res = java.lang.Math.round(input); //
    	input = (double) res;
    	if (input == 0.0) return Double.toString(input);
    	input = input / tmp; // bring the value down again, now rounded
    	//System.out.println("formatDouble: now is:"+input);
    	// Now convert to String:
    	wert = Double.toString(input);
    	len = wert.length();
    	//System.out.println("formatDouble:as String:"+wert);
    	if (len <= digits) return wert;
    	// Now , the rounded numberstring is longer than the displaydigits,
    	//so cut off after that.
    	// better go to exponential Display !! (May be later)    
    	//System.out.println("formatDouble:ResultString is:"+wert.substring(0,digits));
    	return wert.substring(0,digits);// cut off
        }// end of formatDouble
    
    public double getDouble(String in, double def) {
    	try {
    	    in = in.replace(',', '.'); // allow both notations
    	    return Double.valueOf(in).doubleValue();
    	} catch (Exception e){
    	    return def;
    	}
    }
    /**
     * Formatting of an int to a number of digits
     *  @param input this is the input value as int
     *  @param digits the Number of digits to display
     **/    
    public String formatInt(int input, int digits)
        {
    	int len;
    	//double tmp, nw;
    	String wert;
    	
    	// Check if input is 0:
    	//System.out.println("formatInt: was:"+input);
    	if (input == 0) return Integer.toString(input);
    	// Now convert to String:
    	wert = Integer.toString(input);
    	len = wert.length();
    	//System.out.println("formatInt:as String:"+wert);
    	if (len <= digits) return wert;
    	// Now , the rounded numberstring is longer than the displaydigits,
    	//so cut off after that.
    	// better go to exponential Display !! (May be later)    
    	//System.out.println("formatInt:ResultString is:"+wert.substring(0,digits));
    	return wert.substring(0,digits);// Simply cut off
        }// end of formatInt
    
    /**
     * Converts a String into an int, when an Exception occurs, the default Value will be used
     * @param in the int as a String
     * @param def the default value
     * @return the parsed int
     */
    public int getInt(String in, int def) {
    	try {
    	    return Integer.decode(in).intValue();
    	} catch (Exception e){
    	    return def;
    	}
    }
    /**
     * See if String first ends with String second, respecting case or not
     * @param first String
     * @param second String
     * @param how true if case has to match; FALSE IGNORES CASE 1	
     */
    public boolean endWith(String first, String second, boolean how) {
        boolean st = true;
        //System.out.println("Converter.endWith("+first+" ,"+second+" ,"+how+" )");
        if (how) {
    	//System.out.println("String:"+first+" endWith String:"+second+" ?"+first.endsWith(second));
    	return first.endsWith(second);
        }
        else if (first != null && second != null) {
    	//System.out.println("Ignore Case :String:"+first+" endWith String:"+second+" ?");
    	char[] f,s;
    	int len,n, m;
    	s = (second.toLowerCase()).toCharArray();
    	len = s.length;
    	f = (first.toLowerCase()).toCharArray();
    	if ( len <= f.length) {
    	    //System.out.println("Converter.endWith len is <= f.length !");
    	    m = f.length - len;
    	    for (n = 0; n < len; n++) {
    		if (f[m] != s[n]) {
    		    st = false;
    		    //System.out.println("Chars: "+f[m]+" != "+s[n]);
    		    break;
    		}
    		//else System.out.println("Index von second:"+n+" ,"+f[m]+" == "+s[n]);
    		m++;
    	    }
    	}
    	else st = false;
        }
        else {
    	st = false;
        }
        return st;
    }
    
    public Rectangle getViewPortFromImage(BufferedImage img, Dimension pref) {
    	return this.getViewPortFromImage(img, pref, false, 1.0, 0, 1.0);
    }
    
    /**
     * 
     * @param img
     * @param pref
     * @param scaleup
     * @param maxScale
     * @param mode 0 = full view, 1 = scale mode
     * @param sc the given scale for zooming in mode == 1
     * @return
     */
    public Rectangle getViewPortFromImage(BufferedImage img, 
			Dimension pref, boolean scaleup, double maxScale, int mode, double sc) {
		Rectangle r = null;
		double scale;
		if (img != null) {
			int iw = img.getWidth();
			int ih = img.getHeight();
			//int usablew = this.minSize.width - 2 * this.frame;
			//int usableh = this.minSize.height - 2 * this.frame;
			int dispw, disph;
			dispw = iw;
			disph = ih;
			
			if (mode == 0) { // full view
				scale = 1.0;	
				// 1st scale down in Full View mode
				if (iw > pref.width || ih > pref.height) {
					if (iw > pref.width) { // image to wide
						scale = (double) pref.width / (double) iw;
						dispw = pref.width;
						disph = (int) ((double) ih * scale + 0.5 );
					}
					if (disph > pref.height) {
						scale = (double) pref.height / (double) disph;
						disph = pref.height;
						dispw = (int) ((double) dispw * scale + 0.5 );
					}
				}
				else if (scaleup){ // 2nd scale up
					if (iw < pref.width) {
						scale = (double) pref.width / (double) iw;
						if (scale > maxScale)
							scale = maxScale;
						dispw = (int) ((double) iw * scale + 0.5 );
						disph = (int) ((double) ih * scale + 0.5 );
						//System.out.println("Scale from x="+scale);
					}
					if (disph > pref.height) {
						scale = (double) pref.height / (double) disph;
						disph = pref.height;
						dispw = (int) ((double) pref.width * scale + 0.5 );
					}
				}
				int dx = (pref.width - dispw) / 2;
				int dy = (pref.height - disph) / 2;
				r = new Rectangle(dx, dy, dispw, disph);
			}
			else {
				scale = sc;
				int dx = 0;
				int dy = 0;
				dispw = (int) ((double) dispw * scale + 0.5 );
				disph = (int) ((double) ih * scale + 0.5 );
				if (dispw < pref.width)
					dx = (pref.width - dispw) / 2;
				if (disph < pref.height)	
					dy = (pref.height - disph) / 2;
				r = new Rectangle(dx, dy, dispw, disph);
			}
		}
		return r;
	}
    
    /**
     * Read a vector of valid images from a File Object
     * @param f use the path from this file Object 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String[] loadImages(File f) {
    	
    	//String path = this.getPath(f.getPath());
    	String path = f.getPath();
        if (!path.endsWith(File.separator)) {
        	path += File.separator;
        }
        //System.out.println("Path ="+path);
        String name = getFileName(f.getPath());
        File nf = new File(path);
        String[] filelist = nf.list();
        Vector v = null;
        try {
        	v = new Vector();
            for (int n = 0; n < filelist.length; n++) {
            	nf = new File(path, filelist[n]);
            	//System.out.println("File="+filelist[n]);
            	if (istValidImage(filelist[n])) {
            		v.addElement(filelist[n]);
            		//System.out.println("Image added:"+filelist[n]);
            	}
            }
            filelist = new String[v.size()];
        	v.copyInto(filelist);
        	
        } catch (Exception ex) {
        	System.out.println("Exception:"+ ex);
        	ex.printStackTrace();
        	filelist = new String[1];
        	filelist[0] = name;
        }
        //System.out.println("Filelist="+filelist.length);
        return filelist;
    }
    
    public boolean istValidImage(String im) {
    	im = im.toLowerCase();
    	return im.endsWith(".jpg") ||
    	im.endsWith(".jpeg") ||
    	im.endsWith(".gif") ||
    	im.endsWith(".bmp") ||
    	im.endsWith(".png");
    }
    
    public boolean copyFile(File srcFile, File newFile, boolean move) {
    	// Copy the data and close file.
        BufferedInputStream is = null;
        BufferedOutputStream os = null;
        //System.out.println("Utils.copyFile() src="+srcFile.getPath()+" traget="+newFile.getPath());
        try {
          is = new BufferedInputStream(
                new FileInputStream(srcFile));
          os = new BufferedOutputStream(
                new FileOutputStream(newFile));
          int size = 4096;
          byte[] buffer = new byte[size];
          int len;
          while ((len = is.read(buffer, 0, size)) > 0) {
            os.write(buffer, 0, len);
          }
        } catch (IOException e) {
          JOptionPane.showMessageDialog(this.parnt, 
              "Failed to copy file\n  " +
              srcFile.getPath() + "\nto directory\n  " +
              newFile.getAbsolutePath(),
              "File Copy Failed",
              JOptionPane.ERROR_MESSAGE);
          return false;
        } finally {
          try {
            if (is != null) {
              is.close();
            }
            if (os != null) {
              os.close();
            }
          } catch (IOException e) {
          }      
        }
        newFile.setLastModified(srcFile.lastModified());
        if (move) {
        	srcFile.delete();
        }
        return true;
    }
    
    public Vector<String> sortStringVector(Vector<String> in) {
    	Collections.sort(in, new Comparator<String>() {
    		   public int compare(String o1, String o2){
    		      return o1.compareTo(o2);
    		   }
    		});
    	return in;
    }
}



