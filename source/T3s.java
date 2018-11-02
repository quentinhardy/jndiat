//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.logging.Logger;
import java.io.*;
//To get a remote certificate
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;

public class T3s {
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private String ip;
	private int port;
	private String TEMP_CERT_FILE;
	private String TEMP_KEYSTORE_FILE;
	
	/*Constructor*/
	public T3s(String ip, int port){
		myLogger.fine("T3s object created");
		this.ip=ip;
		this.port=port;
		this.TEMP_CERT_FILE = this.ip+"-"+this.port+".cert";
		this.TEMP_KEYSTORE_FILE = this.ip+"-"+this.port+".jks";
	}
	
	/*Make the T3 configuration to establish a T3 connection after.
	 * Get the remote certificate. Create a keystore file. 
	 * Load weblogic parameters to use the keystore.
	 * Returns true if no error. Otherwise returns false*/
	public boolean makeT3sConfig(){
		myLogger.fine("Making the T3s configuration (t3 over SSL)");
		this.initWeblogicPropertiesForKeyStore();
		if (this.isKeyStoreFileExist()==false){
			if (this.saveTheRemoteCertificate()==true){
				boolean status = this.createAValidKeyStore();
				if (status == false){
					return false;
				}
				else {
					myLogger.fine("I'm trying to connect trough the T3s protocol");
				}
			}
			else {
				myLogger.severe("We can't get the remote certificate. There is a certificate on this port?");
				return false;
			}
		}
		else {
			myLogger.info("The file "+TEMP_KEYSTORE_FILE+" exists: The tool will use it to establish the T3s connection.");
		}
		return true;
	}
	
	
	/* Create a valid keystore TEMP_KEYSTORE_FILE which contains the TEMP_CERT_FILE certificat
	 * Returns Truue if no error. Otherwise return False */
	public boolean createAValidKeyStore(){
		KeyStore theKeyStore = null;
		try {
			theKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			theKeyStore.load(null , "".toCharArray());
		}
		catch (Exception e){
			myLogger.fine("Impossible to generate an empty KeyStore:"+e);
			return false;
		}
		// Store away the keystore.
		try {
			InputStream inStreamToCertFile = new FileInputStream(TEMP_CERT_FILE);
			BufferedInputStream inBufStreamToCertFile = new BufferedInputStream(inStreamToCertFile);
			FileOutputStream outFileStreamToKeyStore = new FileOutputStream(TEMP_KEYSTORE_FILE);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			while (inBufStreamToCertFile.available() > 0) {
				myLogger.fine("Loading in the keystore a certificat stored in "+TEMP_CERT_FILE);
				Certificate cert = cf.generateCertificate(inBufStreamToCertFile);
				theKeyStore.setCertificateEntry("fiddler"+inBufStreamToCertFile.available(), cert);
			}
			theKeyStore.store(outFileStreamToKeyStore,"".toCharArray());
			myLogger.fine("The KeyStore "+TEMP_KEYSTORE_FILE+" has been created locally.");
			outFileStreamToKeyStore.close();
			inStreamToCertFile.close();
		}catch (Exception e){
			myLogger.fine("Impossible to create the new KeyStore:"+e);
			return false;
		}
		deleteFile(this.TEMP_CERT_FILE);
		return true;
	}
	
	/*Store the certificate of the remote server in TEMP_CERT_FILE
	 * Return true if no error. Otherwise return False */
	public boolean saveTheRemoteCertificate(){
		BufferedWriter writer = null;
		// create custom trust manager to ignore trust paths
		TrustManager trm = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		};
		SSLSocket socket = null;
		try {
			SSLContext sc = SSLContext.getInstance("TLSv1");
			sc.init(null, new TrustManager[] { trm }, null);
			SSLSocketFactory factory = sc.getSocketFactory();
			socket =(SSLSocket)factory.createSocket(this.ip, this.port);
			socket.startHandshake();
			SSLSession session = socket.getSession();
			java.security.cert.Certificate[] servercerts = session.getPeerCertificates();
			writer = new BufferedWriter(new FileWriter(this.TEMP_CERT_FILE));
			for (int i = 0; i < servercerts.length; i++) {
				writer.write("-----BEGIN CERTIFICATE-----\n");
				writer.write(new sun.misc.BASE64Encoder().encode(servercerts[i].getEncoded()));
				writer.write("\n-----END CERTIFICATE-----\n");
			}
			writer.close();
			socket.close();
		} catch (Exception e) {
			myLogger.severe("Impossible to write the cer file "+this.TEMP_CERT_FILE+": "+e);
			return false;
		}
		myLogger.fine("The file "+this.TEMP_CERT_FILE+" has been created to stored the remote certificate");
		return true;
	}
	
	/*Inialize weblogic variable to use a keystore to bypass certificat errors with T3s*/
	public void initWeblogicPropertiesForKeyStore(){
		myLogger.fine("The KeyStore "+TEMP_KEYSTORE_FILE+" will be used to establish the T3s connection in order don't have a certificate error");
		System.setProperty("weblogic.security.SSL.ignoreHostnameVerification","true");
		System.setProperty("weblogic.security.TrustKeyStore","CustomTrust");
		System.setProperty("weblogic.security.CustomTrustKeyStoreFileName", this.TEMP_KEYSTORE_FILE);
		System.setProperty("weblogic.security.CustomTrustKeyStorePassPhrase",""); 
		System.setProperty("weblogic.security.CustomTrustKeyStoreType","JKS");
	}
	
	/* Returns true if the current keystore file exists. Otherwise return false*/
	public boolean isKeyStoreFileExist(){
		return new File(TEMP_KEYSTORE_FILE).exists();
	}
	
	/* Delete the file fileName
	 * Returns true if the file has been deleted. Otherwise return false*/
	public boolean deleteFile(String fileName){
		try{
			File file = new File(fileName);
			if(file.delete()){
				myLogger.fine("The file "+fileName+" has been removed");
				return true;
			}
			else {
				myLogger.fine("The file "+fileName+" has NOT been removed");
				return false;
			}
		}catch(Exception e){
			myLogger.fine("The file "+fileName+" has NOT been removed: "+e);
			return false;
		}
	}
}
