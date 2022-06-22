//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.logging.Logger;
import javax.sql.DataSource;
import java.sql.*;
import javax.naming.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.net.*;
//For SQL requester
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
//For the listener
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

//Connection to a datasource
public class SQLDataSource extends T3Connection {
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private Connection connection;
	private String dataSource;
	
	public SQLDataSource(String ip, Integer port, String username, String password){
		super(ip, port, username, password, "weblogic.jndi.WLInitialContextFactory", true);
		myLogger.fine("SQLDataSource object created");
		this.connection = null;
		this.dataSource = "";
	}
	
	//Returns True if no error. Otherwise returns False
	private boolean initDataSourceConnection(String dataSource){
		String databaseType = "";
		myLogger.info("SQL connection through the datasource '"+dataSource+"'");
		try {
			this.connection = ((DataSource)this.getCtx().lookup(dataSource)).getConnection();
		} catch (Exception e) {
			myLogger.severe("Impossible to get a DataSource connection: "+e);
			return false;
		}
		databaseType = this.connection.toString().toUpperCase();
		if (databaseType.contains("MYSQL")){
			this.print("Connected to a MYSQL database");
		}else if(databaseType.contains("ORACLE")){
			this.print("Connected to an ORACLE database");
		}else if(databaseType.contains("SYBASE")){
			this.print("Connected to a SYSBASE database");
		}else{
			this.print("Connected to a UNKNOWN database: "+databaseType);
		}
		return true;
	}
	
	//Returns true if all is OK. Otherwise return False
	public boolean SQLshell (String dataSource){
		boolean connected = false;
		this.dataSource = dataSource;
		this.connection();
		if (this.isConnected() == true){
			try {
				connected = this.initDataSourceConnection(dataSource);
				if (connected==true){
					generateSQLShell();
				}
				else {
					return false;
				}
			} catch (Exception e) {
				myLogger.severe("Impossible to get a DataSource connection: "+e);
				return false;
			}
			return true;
		}
		else {
			myLogger.severe("Impossible to get a SQL shell because we can't establish a connection: "+this.getLastConnectionErrorDescription());
			return false;
		}
	}
	

	private void generateSQLShell(){
		Vector<String> columnNames= new Vector<String>();
		try {
			//source: http://jeszysblog.wordpress.com/2012/04/14/readline-style-command-line-editing-with-jline/
			ConsoleReader console = new ConsoleReader();
			console.setPrompt(this.dataSource+"> ");
			String sql = null;
			while ((sql = console.readLine()) != null) {
				columnNames= new Vector<String>();
				try{
					Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					DBTablePrinter.printResultSet(rs);
				} catch (SQLException e) {
					myLogger.severe("Error with the SQL request '"+sql+"':"+e);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				TerminalFactory.get().restore();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String letHimSelectDatasource(){
		int pos = 0;
		Integer dataSourceNb = -1;
		String datasourceToUse = "";
		myLogger.fine("Let the user choose the datasource from jndi list");
		JndiListing jndiListing = new JndiListing(this.getIp(),this.getPort(),this.getUser(),this.getPassword());
		jndiListing.searchJndi();
		ArrayList datasources = jndiListing.getDatasources();
		if (datasources.toArray().length>0){
			while (dataSourceNb < 0 || dataSourceNb >= datasources.toArray().length){
				System.out.println("Choose the number of the datasource to use:");
				for (pos = 0;pos<datasources.toArray().length; pos = pos+1){
					System.out.println(pos+". "+datasources.get(pos));
				}
				try {
					BufferedReader is = new BufferedReader(
					new InputStreamReader(System.in));
					dataSourceNb = Integer.parseInt(is.readLine());
				} 
				catch (NumberFormatException ex) {
					myLogger.severe("Not a good value");
				}
				catch (Exception e){
					myLogger.severe("Unexpected IO ERROR");
				}
			}
			datasourceToUse = ""+datasources.get(dataSourceNb);
			System.out.println("You have chosen the datasource '"+datasources.get(dataSourceNb)+"'");
		}
		else {
			myLogger.severe("Not one datasource in jndi list, cancelation...");
		}
		return datasourceToUse;
	}
	
	/*<Results>
		<Row>
			<Column>
				<Name>USER</Name>
				<Value>ORCL_MDS</value>
			</Column>
		</Row>
	</Results>*/
	public String resultSetToXML (ResultSet rs) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SQLException{
		myLogger.finest("Parsing SQL results for XML output started");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();

		Element results = doc.createElement("Results");
		doc.appendChild(results);

		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();

		while (rs.next()){
			Element row = doc.createElement("Row");
			results.appendChild(row);

			myLogger.finest("Paring a new line");
			for (int i = 1; i <= colCount; i++){
				String columnName = rsmd.getColumnName(i);
				Object value = rs.getObject(i);
				myLogger.finest("value = '"+value.toString()+"'");
				//Element node = doc.createElement(columnName);
				//node.appendChild(doc.createTextNode(value.toString()));
				//row.appendChild(node);
				myLogger.finest("Node appened in xml");
				
				//
				Element column = doc.createElement("Column");
				row.appendChild(column);
				Element nameNode = doc.createElement("Name");
				nameNode.appendChild(doc.createTextNode(columnName));
				column.appendChild(nameNode);
				Element valueNode = doc.createElement("Value");
				valueNode.appendChild(doc.createTextNode(value.toString()));
				column.appendChild(valueNode);
				//
			}
		}
		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		myLogger.finest("Parsing SQL results for XML output stopped");
		return sw.toString();
	}
	
	public Integer listenFromRequest(int port, String dataSource){
		myLogger.fine("Listening on the port "+port+" for SQL requests");
		ServerSocket s = null;
		Socket soc = null;
		BufferedReader bReader = null;
		PrintWriter bWriter = null;
		this.connection();
		if (this.isConnected() == true){
			this.initDataSourceConnection(dataSource);
			try{
				s = new ServerSocket(port);
				soc = s.accept();
				myLogger.finer("A new connection has been established");
			} catch (IOException e) {
				myLogger.severe("Could not listen on port "+port);
				return -1;
			}
			try{
				bReader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
				bWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())),true);
			} catch (IOException e) {
				myLogger.severe("Impossible to get a read or write buffer");
				return -2;
			}
			while (true) {
				boolean executionError = false;
				String request = "";
				String response = "";
				ResultSet rs = null;
				Statement stmt = null;
				try{
					myLogger.finer("Waiting a SQL request...");
					request = bReader.readLine();
					if (request == null) break;
				} catch (IOException e) {
					myLogger.severe("Impossible to get data from client");
					return -3;
				}
				myLogger.finer("Received from client: '"+request+"'");
				if (request.equals("END")) break;
				try{
					stmt = connection.createStatement();
					rs = stmt.executeQuery(request);
					executionError = false;
				} catch(SQLException e) {
					response = "<error>"+e+"</error>";
					executionError = true;
					myLogger.severe("SQL Error with the request '"+request+"':"+e);
				} catch (Exception e) {
					executionError = true;
					response = "<error>server side error during XML paring: '"+e+"'</error>";
					myLogger.severe("Error with the SQL request '"+request+"':"+e);
				}
				if (executionError==false){
					try{
						response = resultSetToXML(rs);
					} catch (Exception e) {
						myLogger.severe("Error with the response '"+response+"':"+e);
					}
				}
				myLogger.finer("Sending response to client: '"+response+"'");
				bWriter.println(response);
			}
		}
		return 0;
	}
}













