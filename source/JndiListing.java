//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import javax.naming.*;
import java.util.logging.Logger;
import java.util.ArrayList;

public class JndiListing extends T3Connection{

	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private ArrayList<String[]> paths;
	private ArrayList lastPath;
	
	final String JDBC_CLASS_DATASOURCE_ID = ".jdbc.";

	public JndiListing(String ip, Integer port, String username, String password){
		super(ip, port, username, password, "weblogic.jndi.WLInitialContextFactory", true);
		myLogger.fine("JndiListing object created");
		this.paths = new ArrayList<String[]>();
		this.lastPath = new ArrayList();
	}
	
	public boolean printJndi(){
		myLogger.fine("Loading JNDI accessible with print");
		return loadJndi(true);
	}
	
	public boolean searchJndi(){
		myLogger.fine("Loading JNDI accessible without print");
		return loadJndi(false);
	}
	
	private boolean loadJndi(boolean printGoodNews){
		myLogger.fine("Loading JNDI accessible ...");
		this.connection();
		if (this.isConnected() == true){
			this.listContext(this.getCtx(), "", 0, printGoodNews, "");
			return true;
		}
		else {
			myLogger.severe("Impossible to list JNDI because we can't establish a connection: "+this.getLastConnectionErrorDescription());
			return false;
		}
	}
	
	private void listContext(Context ctx, String indent, int pos, boolean printGoodNews, String appellant) {
		String className = "";
		String name = "";
		myLogger.finest("Run the fonction listContext(). Pos="+pos+", lastPath="+this.lastPath+", paths="+this.getPrintableJndiList());
		try {
			if (pos==0){this.lastPath.clear();};
			NamingEnumeration list = ctx.listBindings("");
			while (list.hasMore()) {
				Binding item = (Binding) list.next();
				className = item.getClassName();
				name = item.getName();
				if (printGoodNews==true) {printGoodNews(indent + className + " " + name);}
				myLogger.finer("new JNDI found:"+indent + className + " " + name);
				this.lastPath.add(name);
				Object o = item.getObject();
				if (o instanceof javax.naming.Context) {
					listContext((Context) o, indent + "   ", pos + 1, printGoodNews, className+" "+name);
				}
				else {
					this.addCurrentPathToPaths(className);
					this.deleteLastElementOfCurrentPath();
					if (pos - 1 ==0){this.lastPath.clear();};
				}
			}
		}catch (javax.naming.NoPermissionException e) {
			myLogger.warning("Current user does not have permission on '"+appellant+"':'"+e+"'");
			this.addCurrentPathToPaths(className);
			this.deleteLastElementOfCurrentPath();
		} catch (NamingException ex) {
			myLogger.fine("JNDI failure: "+ex);
		}
		if (lastPath.toArray().length>=1) {this.deleteLastElementOfCurrentPath();};
		myLogger.finest("Stop the fonction listContext(). Pos="+pos+", lastPath="+this.getPrintableJndiList());
	}
	
	private void addCurrentPathToPaths(String className){
		String aPath = this.lastPath.toString().replaceAll("\\[|\\]", "").replaceAll(", ","/");
		myLogger.finest("Adding the path '"+aPath+"' to "+this.getPrintableJndiList());
		String[] arrayPath = new String[2];
		arrayPath[0] = aPath;
		arrayPath[1] = className;
		this.paths.add(arrayPath);
	}
	
	private void deleteLastElementOfCurrentPath(){
		int lastPos = lastPath.toArray().length - 1;
		myLogger.finest("Deleting the element "+lastPos+" ('"+this.lastPath.get(lastPos)+"') of "+this.lastPath);
		this.lastPath.remove(lastPos);
	}
	
	private void clearCurrentPath(){
		myLogger.finest("Clearing the path "+this.lastPath);
		this.lastPath.clear();
	}
	
	public String getPrintableJndiList(){
		String line="[";
		int pos = 0;
		for (pos = 0;pos<paths.toArray().length; pos = pos+1){
				line = line + ", "+paths.get(pos)[0]+ " ("+paths.get(pos)[1]+")";
		}
		return line+"]";
	}
	
	public ArrayList getDatasources(){
		myLogger.finest("Clearing the path "+this.lastPath);
		ArrayList datasources = new ArrayList();
		int pos = 0;
		for (pos = 0;pos<paths.toArray().length; pos = pos+1){
			if (paths.get(pos)[1].contains(JDBC_CLASS_DATASOURCE_ID)){
				datasources.add(paths.get(pos)[0]);
			}
		}
		return datasources;
	}
}










