package utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = 	"\u001B[91m";
	public static final String ANSI_DARKR = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW ="\u001B[93m";
	public static final String ANSI_BLUE = 	"\u001B[96m";
	public static final String ANSI_PURPLE ="\u001B[95m";
	public static final String ANSI_CYAN = 	"\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private static enum type {
		INFO, WARNING, CRITICAL, ERROR, DEBUG
	};
	
	//Error
	public static void error(Object... log) {
		out(type.ERROR, log);
	}
	
	//Critical
	public static void crit(Object... log) {
		out(type.CRITICAL, log);
	}
	
	//Debug
	public static void debug(Object... log) {
		out(type.DEBUG, log);
	}
	
	//Warning
	public static void warn(Object... log) {
		out(type.WARNING, log);
	}
	
	//Info
	public static void info(Object... log) {
		out(type.INFO, log);
	}
	
	
	//Main method
	private static void out(type output_type, Object... log) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar date = Calendar.getInstance();
		

		String logString = "";
		for (int i = 0; i < log.length; i++) {
			logString += log[i] + ", ";			
		}
		logString = logString.substring(0, logString.length() - 2);
		
		switch (output_type) {
		case DEBUG:
			System.out.println(ANSI_PURPLE + "[DEBUG] [" + ANSI_BLUE +  dateFormat.format(date.getTime()).toString() + ANSI_PURPLE + "]" + ANSI_WHITE + " -> " + logString);
			break;
		case INFO:
			System.out.println(ANSI_WHITE + "[INFO] [" + ANSI_BLUE + dateFormat.format(date.getTime()).toString() + ANSI_WHITE + "]" + ANSI_WHITE + " -> " + logString);
			break;
		case WARNING:
			System.out.println(ANSI_YELLOW + "[WARNING] [" + ANSI_BLUE + dateFormat.format(date.getTime()).toString() + ANSI_YELLOW + "]" + ANSI_WHITE + " -> " + logString);
			break;
		case CRITICAL:
			System.out.println(ANSI_RED + "[CRITICAL] [" + ANSI_BLUE + dateFormat.format(date.getTime()).toString() + ANSI_RED + "]" + ANSI_WHITE + " -> " + logString);
			break;
		case ERROR:
			System.err.println(ANSI_DARKR + "[ERROR] [" + ANSI_BLUE + dateFormat.format(date.getTime()).toString() + ANSI_DARKR + "]" + ANSI_WHITE + " -> " + logString);
			System.exit(-1);
			break;

		default:
			break;
		}
	}
	
}
