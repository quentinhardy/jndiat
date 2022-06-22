//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.lang.Integer;
import java.lang.Thread;
import java.util.logging.Logger;
import java.io.*;
import weblogic.deploy.api.tools.*;  //SesionHelper
import weblogic.deploy.api.spi .*;  //WebLogicDeploymentManager
import weblogic.deploy.api.spi.DeploymentOptions;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import java.io.File;


public class Deployer extends MyPrinter{

	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private static ModuleType[] moduleTypeTable = new ModuleType[] {ModuleType.EAR,ModuleType.WAR,ModuleType.EJB,ModuleType.RAR,ModuleType.CAR};
	private String ip;
	private String port;
	private String username;
	private String password;
	private WebLogicDeploymentManager deployManager;
	private String target;
	private T3s t3s;

	/*Constructor*/
	public Deployer(String ip, int port, String username, String password, String target){
		myLogger.fine("Deployer object created");
		this.ip=ip;
		this.port=Integer.toString(port);
		this.username=username;
		this.password=password;
		this.deployManager=null;
		this.target = target;
		this.t3s = null;
	}
	
	//deploy an application. Returns False if error
	public boolean deploy (String applicationFile, String applicationDisplayName){
		this.getRemoteDeploymentManager();
		if (this.deployManager == null) { return false;};
		DeploymentOptions options = new DeploymentOptions();
		options.setName(applicationDisplayName);
		myLogger.info("Inital options for the deployment:"+options);
		Target deployTargets[] = askTheTarget ("Application can be deployed in following targets:");
		myLogger.info("The application named "+applicationFile+" will be deployed");
		ProgressObject processStatus = this.deployManager.distribute(deployTargets, new File(applicationFile), null,options);
		try {
			processStatus = this.deployManager.deploy(deployTargets, new File(applicationFile), null,options);
		}catch (Exception e) {
			myLogger.severe("Impossible to deploy in to t3://"+this.ip+":"+this.port+"/"+this.username+":"+this.password+" :"+e);
			return false;	
		}
		DeploymentStatus deploymentStatus=processStatus.getDeploymentStatus() ;
		myLogger.info("Deployement status: "+deploymentStatus.getState());
		myLogger.info("Sleeping for Seconds. The application takes some time to get distributed in the remotre server...");
		this.sleepMscds (5000);
		boolean started =this.startThisApplication(applicationDisplayName, deployTargets);
		if (started==false){return false;};
		myLogger.info("The application is now deployed on the remote server");
		return true;
	}
	
	//undeploy an application. Returns False if error
	public boolean undeploy (String applicationDisplayName){
		this.getRemoteDeploymentManager();
		if (this.deployManager == null) { return false;};
		Target deployTargets[] = askTheTarget ("Application can be undeployed from following targets:");
		boolean stopped =this.stopThisApplication(applicationDisplayName, deployTargets);
		if (stopped==false){return false;};
		myLogger.info("Sleeping for Seconds. The application takes some time to be stopped in the remotre server...");
		this.sleepMscds (5000);
		boolean undeployed = undeployFromModuleID(applicationDisplayName, deployTargets);
		if (stopped==false){return false;};
		myLogger.info("The application is now removed from the remote server");
		return true;
	}
	
	public boolean listApplications(){
		this.getRemoteDeploymentManager();
		if (this.deployManager == null) { return false;};
		Target deployTargets[] = askTheTarget ("Application can be listed from following targets:");
		myLogger.info("We are listing applications");
		TargetModuleID[] targetModuleID = new TargetModuleID[1];
		for (int i=0;i<this.moduleTypeTable.length;i++){
			TargetModuleID[] targetModuleIDs = this.getAvailableModules(this.moduleTypeTable[i], deployTargets);
			if (targetModuleIDs!=null) {
				this.print("["+i+"] "+this.moduleTypeTable[i]+" application(s):");
				for (int j=0;j<targetModuleIDs.length;j++) {
					if(targetModuleIDs[j]!=null && targetModuleIDs[j].getModuleID() != null){
						this.print("  |-"+j+"-> "+targetModuleIDs[j].getModuleID());
					}
				}
			}
		}
		return true;
	}
	
	//Ask the target
	private Target[] askTheTarget (String askMsg){
		int target = -1;
		Target targets[] = this.deployManager.getTargets();
		while (target < 0 || target >= targets.length){
			System.out.println(askMsg);
			int i=0;
			for (i=0;i<targets.length;i++){
				System.out.println(i+". "+targets[i]);
			}
			System.out.println("Choose a target:");
			try {
				BufferedReader is = new BufferedReader(
				new InputStreamReader(System.in));
				target = Integer.parseInt(is.readLine());
			} 
			catch (NumberFormatException ex) {
				myLogger.severe("Not a good value");
			}
			catch (Exception e){
				myLogger.severe("Unexpected IO ERROR");
			}
		}
		Target deployTargets[] = new Target[1];
		deployTargets[0]=targets[target];
		return deployTargets;
	}
	
	//Sleep during t milliseconds. If error, return false
	private boolean sleepMscds (int t){
		try {
			Thread.sleep(t);
		}catch (Exception e) {
			myLogger.severe("Impossible to sleep "+t+" milliseconds :"+e);
			return false;	
		}
		return true;
	}
	
	//Returns a WebLogicDeploymentManager object or null if there is a problem
	private WebLogicDeploymentManager getRemoteDeploymentManager(){
		try {
			myLogger.fine("Trying to establish a T3 connection (without SSL/TLS)");
			this.deployManager = SessionHelper.getRemoteDeploymentManager("t3", this.ip, this.port , this.username, this.password);
			myLogger.info("Connection to "+this.ip+":"+this.port+"established throuth T3 protocol, good news:)");
			return deployManager;
		}catch (Exception e) {
			if (this.getStackTrace(e).contains(this.ERROR_CONNECTION_RESET)){
				myLogger.fine("Trying to connect with t3s (t3 over SSL) because there is a reset with t3");
				this.t3s = new T3s (this.ip, Integer.parseInt(this.port));
				if (t3s.makeT3sConfig() == false){
					myLogger.severe("Impossible to make the T3s configuration");
					return null;
				}
				else {
					try {
						myLogger.fine("T3s configuration made");
						this.deployManager = SessionHelper.getRemoteDeploymentManager("t3s", this.ip, this.port , this.username, this.password);
						myLogger.info("Connection to "+this.ip+":"+this.port+"established throuth T3s protocol, good news:)");
						return deployManager;
					}catch (Exception e2) {
						myLogger.severe("Impossible to connect to t3s://"+this.ip+":"+this.port+"/"+this.username+":"+this.password+" :"+e);
						return null;
					}
				}
			}else {
				myLogger.severe("Impossible to connect to t3://"+this.ip+":"+this.port+"/"+this.username+":"+this.password+" :'"+e+"'");
				return null;
			}
		}
	}
	
	//Returns a TargetModuleID table object or null if there is a problem
	private TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] deployTargets){
		TargetModuleID[] targetModuleIDs;
		try {
			targetModuleIDs = this.deployManager.getAvailableModules(moduleType, deployTargets);
			return targetModuleIDs;
		}catch (Exception e) {
			myLogger.severe("Impossible to get available modules :"+e);
			return null;	
		}
	}

	//Returns the good TargetModuleID from a application name ()
	private TargetModuleID[] getTheGoodTargetModule (String applicationDisplayName, Target deployTargets[]){
		myLogger.info("We will search the application named "+applicationDisplayName+" on the remote server");
		TargetModuleID[] targetModuleID = new TargetModuleID[1];
		for (int i=0;i<this.moduleTypeTable.length;i++){
			TargetModuleID[] targetModuleIDs = this.getAvailableModules(this.moduleTypeTable[i], deployTargets);
			if (targetModuleIDs!=null) {
				myLogger.info("Searching the application ID of the application named "+applicationDisplayName+" in "+this.moduleTypeTable[i]+" applications ("+i+"/"+(this.moduleTypeTable.length-1)+")");
				myLogger.fine("Searching in "+targetModuleIDs.length+" applications");
				for (int j=0;j<targetModuleIDs.length;j++) {
					if(targetModuleIDs[j]!=null && targetModuleIDs[j].getModuleID() != null && targetModuleIDs[j].getModuleID().equals(applicationDisplayName)){
						myLogger.info("We have found the application named "+applicationDisplayName+" in the remote server");
						targetModuleID[0] = targetModuleIDs[j];
						return targetModuleID;
					}
				}
			}
		}
		return null;
	}
	
	//Start an application. Returns False if error
	private boolean startThisApplication (String applicationDisplayName, Target deployTargets[]){
		TargetModuleID[] targetModuleID = getTheGoodTargetModule(applicationDisplayName, deployTargets);
		if(targetModuleID[0] != null){
			myLogger.info("We starting the application named "+targetModuleID[0].getModuleID());
			this.deployManager.start(targetModuleID);
		}
		else {
			myLogger.severe("The application named "+applicationDisplayName+" has not been found in the remote server, we can't start this application");
			return false;
		}
		myLogger.info("Application started");
		return true;
	}
	
	//Stop an application. Returns False if error
	private boolean stopThisApplication (String applicationDisplayName, Target deployTargets[]){
		TargetModuleID[] targetModuleID = getTheGoodTargetModule(applicationDisplayName, deployTargets);
		if(targetModuleID != null && targetModuleID[0] != null){
			myLogger.info("We are stopping the application named "+targetModuleID[0].getModuleID());
			this.deployManager.stop(targetModuleID);
		}
		else {
			myLogger.severe("The application named "+applicationDisplayName+" has not been found in the remote server, we can't stop this application");
			return false;
		}
		myLogger.info("Application "+applicationDisplayName+" stopped");
		return true;
	}
	
	//Undeploy an application. Returns False if error
	private boolean undeployFromModuleID (String applicationDisplayName, Target deployTargets[]){
		TargetModuleID[] targetModuleID = getTheGoodTargetModule(applicationDisplayName, deployTargets);
		if(targetModuleID[0] != null){
			myLogger.info("We are undeploing the application named "+targetModuleID[0].getModuleID());
			this.deployManager.undeploy(targetModuleID);
		}
		else {
			myLogger.severe("The application named "+applicationDisplayName+" has not been found in the remote server, we can't undeploy this application");
			return false;
		}
		myLogger.info("Application "+applicationDisplayName+" undeployed");
		return true;
	}
	
	//Returns the file extension
	private String getFileExtension(String name) {
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return name.substring(lastIndexOf).toUpperCase();
	}
	
	//Return the ModuleType from the extension of the filename
	private ModuleType getModuleTypeFromExtension (String filename){
		switch(this.getFileExtension(filename)) {
			case "EAR": return ModuleType.EAR;
			case "wAR": return ModuleType.WAR;
			case "EJB": return ModuleType.EJB;
			case "RAR": return ModuleType.RAR;
			case "CAR": return ModuleType.CAR;
			default: return null;
		}
	}
	
	private String getStackTrace (Exception e){
	 	StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
}
