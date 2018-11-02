//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import javax.naming.*;
import java.util.logging.Logger;
import java.util.ArrayList;
import weblogic.common.T3ServicesDef;

public class FileSystem extends T3Connection{
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	
	public FileSystem (String ip, Integer port, String username, String password){
		super(ip, port, username, password, "weblogic.jndi.WLInitialContextFactory", true);
		myLogger.fine("FileSystem object created");
	}
	
	//Returns True if no error. Otherwise returns False
	private boolean initService(){
		return true;
	}
	
}
