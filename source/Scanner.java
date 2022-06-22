//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.lang.Integer;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Scanner extends T3Connection{

	private static Logger myLogger = Logger.getLogger("JNDIAT");
	List<Integer> openedPorts;//you can access to JNDI through these ports

	/*Constructor*/
	public Scanner(){
		super("weblogic.jndi.WLInitialContextFactory", false);
		myLogger.fine("Scanner object created");
		this.openedPorts = new ArrayList<Integer>();
	}
	
	/* To scan ports of a server
	 * Compute openedPorts */
	public void scan (String ip, String ports, String username, String password){
		myLogger.info("Scanning ports '"+ports+"' of "+ip+" with password '"+username+"' and password '"+password+"'");
		int nb;
		boolean connected = false;
		String[] portsList = new String[] {};
		if (ports.contains(",")==true){
			myLogger.fine("There is a ',' in ports, split ports to scan each port");
			portsList = ports.split(",");
			myLogger.fine("Ports to scan:"+Arrays.toString(portsList));
		}
		else if (ports.contains("-")==true){
			myLogger.fine("There is a '-' in ports");
			String[] limits = new String[] {};
			limits = ports.split("-");
			int port = 0;
			List<String> tempPorts = new ArrayList<String>();
			for( port = Integer.parseInt(limits[0]); port <= Integer.parseInt(limits[1]); port++){
				tempPorts.add(Integer.toString(port));
			}
			portsList = tempPorts.toArray(new String[tempPorts.size()]);
		}
		else {
			//ports contains a port only
			portsList = new String[] {ports};
		}
		for (nb=0; nb<portsList.length; nb++) {
			myLogger.fine("Scanning the port "+portsList[nb]);
			int portToTest = Integer.valueOf(portsList[nb]);
			connected = this.connection(ip, portToTest, username, password);
			if (connected == true){
				myLogger.fine("Target "+ip+":"+portToTest+" : T3 connection establish :)");
				this.openedPorts.add(portToTest);
				this.deconnection();
			}
			else {
				myLogger.fine("Target "+ip+":"+portToTest+" : T3 connection impossible");
			}
		}
	}
	
	/*return opened ports from this.openedPorts*/
	public List<Integer> getOpenedPorts(){
		return this.openedPorts;
	}
	
	/*Print opened ports from this.openedPorts*/
	public void printOpenedPorts(){
		if (this.openedPorts.isEmpty()) {
			this.printBadNews("No opened port has been found to connect with T3 protocol");
		}
		else {
			this.printGoodNews("You can use the T3 protocol to connect to these ports: "+this.openedPorts.toString());
		}
	}
}



