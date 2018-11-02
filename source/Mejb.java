//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.logging.Logger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;

import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.management.QueryExp;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.ejb.CreateException;

public class Mejb extends T3Connection{ 
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private ManagementHome home;
	
	//*************   Constructor *************
	public Mejb(String ip, Integer port, String username, String password){
		super(ip, port, username, password, "weblogic.jndi.WLInitialContextFactory", true);
		myLogger.fine("SQLDataSource object created");
		this.home = null;
	}
	
	public void getAllJMONames(){
		this.connection();
		if (this.isConnected() == true){
			try {
				this.home = (ManagementHome)this.getCtx().lookup("ejb.mgmt.MEJB");
				Management rhome = this.home.create();
				String string = "";
				ObjectName name = new ObjectName(string);
				QueryExp query = null;
				Set allNames = rhome.queryNames(name, query);
				Iterator nameIterator = allNames.iterator();
				while(nameIterator.hasNext()) {
					ObjectName on = (ObjectName)nameIterator.next();
					System.out.println(on.getCanonicalName() + "\n");
				}
			} 
			catch (Exception ex) {
				myLogger.severe("Error to lookup ejb.mgmt.MEJB. Perhaps you have not enough privileges: "+ex );
			}
		}
	}
	
}

