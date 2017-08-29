package sonarqube;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	
	public static String getDate(String pattern) {
		Date d = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
	    return sdf.format(d);
	}
}
