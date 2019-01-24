package com.ibm.cognos.auth.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DBAuthLogger {
	private static String logMode = "DEBUG";
	private static String filePath = "../logs/JDBC_AUTH_LIB.log";
    private static String br = System.getProperty("line.separator");
	
    public static void setLogMode(String mode) {
    	logMode = mode;
    }
    
    public static void setLogPath(String path) {
    	filePath = path;
    }
    
    public static void debug(Object obj) {
    	if("DEBUG".equals(logMode)) {
	        try {
	            FileWriter fw = new FileWriter(new File(filePath), true);
	            fw.write("[" + getCurDate() + "] [DEBUG] " + obj + br);
	            fw.close();
	        } catch(IOException ioe) {
	            ioe.printStackTrace();
	        }
    	}
    }

    public static void error(Object obj) {
        try {
            FileWriter fw = new FileWriter(new File(filePath), true);
            fw.write("[" + getCurDate() + "] [ERROR] " + obj + br);
            fw.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
	
    public static String getCurDate() {
        String formatter = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat fmt = new SimpleDateFormat(formatter);
        fmt.setTimeZone(TimeZone.getDefault());
        return fmt.format(new Date(System.currentTimeMillis()));
    }
}
