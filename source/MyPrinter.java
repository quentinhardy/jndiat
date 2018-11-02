//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.logging.Logger;

public class MyPrinter {

	private static Logger myLogger = Logger.getLogger("JNDIAT");
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BOLD_ON = "\u001B[1m";
	public static final String ANSI_BOLD_OFF = "\u001B[22m";
	
	public static boolean enableColor;
	public static int titlePos;
	public static int subtitlePos;
	
	//CONSTANTS (ERRORS)
	public static final String ERROR_STREAM_CLOSED = "java.io.IOException: Stream closed";
	public static final String ERROR_CONNECTION_RESET = "java.net.SocketException: Connection reset";

	public MyPrinter(){
		myLogger.fine("MyPrinter object created");
		this.enableColor = true;
		this.titlePos = 0;
		this.subtitlePos = 0;
	}
	
	public void printTitle (String message){
		this.titlePos += 1;
		this.subtitlePos = 1;
		if (this.enableColor==true){System.out.println("\n" + ANSI_WHITE + ANSI_BOLD_ON + "["+this.titlePos+"] " + message + ANSI_BOLD_OFF + ANSI_RESET);}
		else {System.out.println("\n["+this.titlePos+"] "+ message);}
	}
	
	public void printSubtitle (String message){
		this.subtitlePos += 1;
		if (this.enableColor==true){System.out.println("\n" + ANSI_WHITE + ANSI_BOLD_ON + "["+this.subtitlePos+"] " + message + ANSI_BOLD_OFF + ANSI_RESET);}
		else {System.out.println("\n["+this.titlePos+"] "+ message);}
	}
	
	public void printBadNews (String message){
		if (this.enableColor==true){System.out.println(ANSI_RED + message + ANSI_RESET);}
		else {System.out.println(message);}
	}
	
	public void printGoodNews (String message){
		if (this.enableColor==true){System.out.println(ANSI_GREEN + message + ANSI_RESET);}
		else {System.out.println(message);}
	}
	
	public void disableColor (){
		this.enableColor = false;
	}
	
	public void print(String message){
		System.out.println(message);
	}
	
	/*
	public void printUnknownNews (String message){
		
	}
	*/
}

