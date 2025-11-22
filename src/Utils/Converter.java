package Utils;

/**
 * This class converts number values to and from given Units
 * It also return a Color Object, generated from a String
 * @version 	$Id: Converter.java,v 1.1 1997/05/23 09:11:39 nik Exp nik $
 * @author 	Nils Kay
 */
public class Converter {

/** Simple formatting of the String to a number of digits.
 *  @param input this is the input String
 *  @param digits the Number of digits
 **/    

@SuppressWarnings({ "null", "unused" })
public static void doBreak() {
    try {
	String str=null;
	int l = str.length();
    } catch (Exception ex) {
	ex.printStackTrace();
    }
}

/**
 * Formatting of a double to a number of digits, where per default 3 digits
 * behind the colon will be used.
 *  @param input this is the input double
 *  @param digits the Number of digits to display
 **/    
public static String formatDouble(double input, int digits) {
    return Converter.formatDouble(input, digits, 3);
}

/**
 * Formatting of a double to a number of digits, where the number
 * of postcolon digits is also set.
 *  @param input this is the input double
 *  @param digits the Number of digits to display
 *  @param post the Number of digits after the colon
 **/    
@SuppressWarnings("unused")
public static String formatDouble(double input, int digits, int post)
    {
	int len;
	double tmp, orig;
	long res;
	String wert, wt;
	// Check if input is 0:
	//System.out.println("formatDouble: was:"+input);
	if (input == 0.0) return Double.toString(0.0);
	orig = input;
        // to round after the post digit:
	post = java.lang.Math.abs(post); // prevent errors, make it positive
	tmp = java.lang.Math.pow(10.0,(double)post);
	input = input * tmp; // blow the Number up
	// Now round it:
	res = java.lang.Math.round(input); //
	input = (double) res;
	if (input == 0.0) return Double.toString(input);
	input = input / tmp; // bring the value down again, now rounded
	// Now convert to String:
	wert = Double.toString(input);
	//System.out.println("formatDouble: now is:"+input);
	if ((len = wert.indexOf("E")) >= 0 ){ // java goes to exponential display. we have to avoid that !
	    tmp = (double) Integer.parseInt((wert.substring(len+2, wert.length())));// exponent
	    input = input * java.lang.Math.pow(10.0,tmp);
	    //System.out.println("formatDouble: orig="+orig+" 1.step: input="+input+" exponent="+tmp);
	    wert = Double.toString(input);
	    len = wert.indexOf(".");
	    if (len >= 0) { // remove  a '.' char
		String li = wert.substring(0, len);
		wert = li + wert.substring(len+1, wert.length());
	    }
	    wt = "0.";
	    for (len = 0; len < (int) (tmp -1); len++) wt +="0";
	    wert = wt + wert;
	    //ySystem.out.println("formatDouble: orig="+orig+" converted to:"+wert);
	}
	len = wert.length();
	//System.out.println("formatDouble:as String:"+wert);
	if (len <= digits) return wert;
	// Now , the rounded numberstring is longer than the displaydigits,
	//so cut off after that.
	// better go to exponential Display !! (May be later)    
	//System.out.println("formatDouble:ResultString is:"+wert.substring(0,digits));
	return wert.substring(0,digits);// cut off
    }// end of formatDouble

/**
 * Formatting of an int to a number of digits
 *  @param input this is the input value as int
 *  @param digits the Number of digits to display
 **/    
@SuppressWarnings("unused")
public static String formatInt(int input, int digits)
    {
	int len;
	double tmp, nw;
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
 * Compare two strings, respecting case or not
 * @param first String
 * @param second String
 * @param true if case has to match; FALSE IGNORES CASE 1	
 */
public static boolean compare(String first, String second, boolean how) {
    if (how) return first.equals(second);
    else return first.equalsIgnoreCase(second);
}

/**
 * See if String first starts with String second, respecting case or not
 * @param first String
 * @param second String
 * @param true if case has to match; FALSE IGNORES CASE 1	
 */
public static boolean startWith(String first, String second, boolean how) {
    boolean st = true;
    if (how) return first.startsWith(second);
    else if (first != null && second != null) {
	char[] f,s;
	int len,n;
	s = (second.toLowerCase()).toCharArray();
	len = s.length;
	f = (first.toLowerCase()).toCharArray();
	if ( len <= f.length) {
	    for (n = 0; n < len; n++) {
		if (f[n] != s[n]) {
		    st = false;
		    break;
		}
	    }
	}
	else st = false;
    }
    else st = false;
    return st;
}

    public static int intModulo( int in, int mod) {
	if (mod <= 0) return -1;	// error
	while(in > 0) in = in - mod;
	return in +mod;
    }


/**
 * See if String first ends with String second, respecting case or not
 * @param name String
 * @param start String
 * @param true if case has to match; FALSE IGNORES CASE 1	
 */
public static boolean startEndWith(String name, String start, String end, boolean how) {
    String tmp = name;
    if ( how) {
	tmp = name.toLowerCase();
	start = start.toLowerCase();
	end = end.toLowerCase();
    }
    if (tmp.startsWith(start) && tmp.endsWith(end)) return true;
    return false;
}

/**
 * See if String first ends with String second, respecting case or not
 * @param first String
 * @param second String
 * @param true if case has to match; FALSE IGNORES CASE 1	
 */
public static boolean endWith(String first, String second, boolean how) {
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
/**
 * Check the boolean state of this property String
 * @param tm the string containing the boolean value 'on' or 'true' or 
 * whatever we declare as the state 'true'
 * @return true, if the string in is 'on' or 'true'
 */
    public static boolean checkState(String tm) {
	tm = tm.toLowerCase();
	if (tm.equals("true") || tm.equals("on") || tm.equals("yes")) return true;
	return false;
    }
/**
 * Converts a String into an int, when an Exception occurs, the default Value will be used
 * @param the int as a String
 * @param the default value
 * @return the parsed int
 */
    public static int getInt(String in, int def) {
    	//in = in.trim();
    	//System.out.println("in="+in);
    	in = in.toLowerCase();
    	if (in.startsWith("0x")) {
			try {
			    return Integer.decode(in).intValue();
			} catch (Exception e){
			    return def;
			}
    	}
    	else {
    		try {
			    return Integer.parseInt(in);
			} catch (Exception e){
			    return def;
			}
    	}
    }
/**
 * Converts a String into a double, when an Exception occurs, the default Value will be used
 * @param the double as a String
 * @param the default value
 * @return the parsed double
 */
    public static double getDouble(String in, double def) {
	try {
	    return Double.valueOf(in).doubleValue();
	} catch (NumberFormatException e){
	    return def;
	}
    }
}// end of Converter class







