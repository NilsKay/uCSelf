package Utils;
import java.io.*;
import java.net.*;
import java.awt.Color;
import java.awt.event.MouseEvent;
/**
 * This class returns where the Grapholasnt System files are located
 * @version 	$Id$
 * @author 	Nils Kay
 */
public class GetEnviroment {
    public static String sVersionCode="Version V1.06 alpha4";
    public final static Color DisplayBackColor = new Color(0xe0e0e0); // Display Background color
    public final static String SAVEFILE = "jcself.sav";	// here we store the user default settings
    public final static String TEMPLATEEXT = ".jct";	// 

    String Home; 	// the users Home dir
    String User; 	// the user name
    String Host;	// the local hostname
    String Ip_address;	// the ip address of the local host
    String SystemPath;	// where the system files are
    String ClassPath;	// the ClassPath enviroment variable
    String OPTIONS;     // Otions for the Program
    String Platform;	// the platform we run on !
    int left_Button;
    int right_Button;
    // --------- internal --------
private boolean error;

/**
 * Return the SystemPath. This is relying on a directory path for the Toplevel dir:
 *  where a folder System is located !
 * @return the SystemPath
 */
public GetEnviroment() {
    this(null);
}
/**
 * Return the SystemPath. This is relying on a directory path for the Toplevel dir:
 *  where a folder System is located !
 * @return the SystemPath
 */
public GetEnviroment(String rootDir) {
    this.error = false; // no error
    this.Home = System.getProperty("HOME");
    this.ClassPath = System.getProperty("java.class.path");
    this.OPTIONS = System.getProperty("OPTIONS");
    this.User = System.getProperty("user.name");
    this.Platform = System.getProperty("os.arch");
    this.Host = "Unknown";
    try {
	InetAddress iadr = InetAddress.getLocalHost();
	this.Host = iadr.getHostName();
	this.Ip_address = iadr.getHostAddress();
    } catch (UnknownHostException ue) {}

    if(Platform.startsWith("x86")) {
	this.left_Button = MouseEvent.BUTTON1_MASK;
	this.right_Button = MouseEvent.BUTTON3_MASK;
    }
    else { // on sun ?
	this.left_Button = 0;
	this.right_Button = 2;	// ???
    }
}

public boolean getPathes(){
    String StartPath = System.getProperty("user.dir") + File.separator;
    //Check for v1.2x
    String version = System.getProperty("java.version"); 
    int n;
    return error;
}

public int getLeftButton() {
    return this.left_Button;
}
public int getRightButton() {
    return this.right_Button;
}
public String getEnviroment() {
    return this.SystemPath;
}
public String getPlatform() {
    return this.Platform;
}
public String getOPTIONS() {
    return this.OPTIONS;
}
public String getUser() {
    return this.User;
}
public String getHome() {
    return this.Home;
}
public String getHost() {
    return this.Host;
}
public String getIpAddress() {
    return this.Ip_address;
}
public String getClassPath() {
    return this.ClassPath;
}
public boolean getError() {
    return this.error;
}
public static boolean copyFile(File in, File out) {
    byte buf[] = new byte[0x2000];
    int n;
    boolean error = false;

    FileInputStream dis = null;
    FileOutputStream fos = null;

    //System.out.println("File Data Reader!");
    boolean done = false;
//    bytesRead = 0;	// bytes read on transfer
    try {
	//File of = new File(outpath);
	dis = new FileInputStream(in);
	fos = new FileOutputStream(out);
	while( true && !error && !done) {
	    n = dis.read(buf, 0, 0x2000);
	    //this.bytesRead = this.bytesRead + n;
	    if (n == -1) {
		done = true;
	    }
	    else fos.write(buf, 0, n);
	}
    }
    catch (EOFException e) {
	System.out.println("GetEnviroment.copyFile: Exception"+e);
    }
    catch (IOException ex) {
	error = true;
	System.out.println("GetEnviroment.copyFile: Exception"+ex);
    }

    try {
	fos.close();
	dis.close();
    } catch(java.io.IOException e) {
	error = true;
	System.out.println("file.close: IOException: "+e);
    }
    return error;
}
} // end of class



