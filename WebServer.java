/**
 * WebServer Class
 * 
 * Implements a multi-threaded web server
 * supporting non-persistent connections.
 *
 * @author Matteo Cusanelli
 * @version	2024
 * 
 */


 import java.util.*;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.*;
 import java.util.logging.*;
 import java.io.File;
 
 public class WebServer extends Thread {
	 // global logger object, configures in the driver class
	 private static final Logger logger = Logger.getLogger("WebServer");
 
	 private boolean shutdown = false; // shutdown flag
	 int ServerPort;
	 String ServerRoot;
	 int ServerTimeout;
 
	 
	 /**
	  * Constructor to initialize the web server
	 * 
	 * @param port 	Server port at which the web server listens > 1024
	 * @param root	Server's root file directory
	 * @param timeout	Idle connection timeout in milli-seconds
	 * 
	 */
	 public WebServer(int port, String root, int timeout){
		 ServerPort = port;
		 ServerRoot = root;	
		 ServerTimeout = timeout;
	 
	 }
 
	 
	 /**
	  * Main method in the web server thread.
	 * The web server remains in listening mode 
	 * and accepts connection requests from clients 
	 * until it receives the shutdown signal.
	 * 
	 */
	 public void run(){
		 ServerSocket serversocket; 
		 
		 try{
			 serversocket = new ServerSocket(ServerPort);
			 serversocket.setSoTimeout(100);
			 List<Thread> threads = new ArrayList<Thread>();
 
			 while(!shutdown){
			 
				 try{
				 Socket socket = serversocket.accept();
				 String address = socket.getInetAddress().toString();
				 int port = socket.getPort();
 
				 System.out.println("Accepted Client:\nIp Adress: "+ address +"\nPort: "+ port+"\n");
 
				 WorkerThread workerThread = new WorkerThread(socket);
				 Thread thr = new Thread(workerThread);
				 thr.start();
				 threads.add(thr);
				 } catch(SocketTimeoutException e){
 
				 }
			 }
 
			 for (int i = 0; i < threads.size(); i++){
				 threads.get(i).join();
			 }
 
		 serversocket.close(); // Close server
 
		 }
		 catch(Exception e){
			 System.out.println("Error: "+ e.getCause());
		 }
	 }
	 
 
	 /**
	  * Signals the web server to shutdown.
	 *
	 */
	 public void shutdown() {
		 shutdown = true;
	 }
 
 /**
  * Class for worker threads
 */
	 public class WorkerThread implements Runnable {
		 private Socket socket;
		 public WorkerThread(Socket socket){
			 this.socket = socket;
		 }
 
		 @Override
		 public void run(){
			 // Create variables for streams
						 
			 OutputStream outputStream = null;
			 InputStream inputStream = null;
			 FileInputStream fileInputStream = null;
 
			 try{
				 inputStream = socket.getInputStream();
				 outputStream = socket.getOutputStream();
				 Boolean timeout = true; // variable to see if client timed out
				 
				 long millis = System.currentTimeMillis(); // current time
 
				 int dataInt = 1; // data from the input stream
				 boolean error_happened = false; //flag if an error happened
				 String status_line = ""; // status line to print to console
				 byte[] getStatusBytes = status_line.getBytes(); //bytes of status line
 
 
				 if (ServerTimeout == 0){ // If the timeout variable is infinite
					 while(true){
						 dataInt = inputStream.read();
						 if (dataInt != 0){ // If the client sent data
							 timeout = false; // set the timed out variable to false
							 break;
						 }
					 }
 
				 }else{ // If a timeout variable was specified
					 long futureTime = millis + ServerTimeout;
					 while(millis > futureTime){
						 dataInt = inputStream.read();
						 if (dataInt != 0){ // If the client sent data
							 timeout = false; // set the timed out variable to false
							 break;
						 }
					 }
				 }
 
	 
				 if (timeout){ // If the client timed out and didnt send any data
 
					 // get status and header lines and change to bytes
					 status_line = ("HTTP/1.1 408 Request Timeout\r\n");
					 getStatusBytes = status_line.getBytes("US-ASCII");
					 error_happened = true;
 
 
				 }else{ // If no timeout occured
 
					 // initial variables
					 String CharResponse = "\r\n"; // Response for head of header
					 boolean containsSequence = false; // boolean to see if string contains the squence (\r\n)
					 boolean EndOfHeader = false; // boolen flag to see if header ended
					 ArrayList<String> line = new ArrayList<String>(); // Create an empty arraylist for the characters from the headers
					 boolean checkedFirst = false; // boolean to see if first byte of input streamw was read
					 boolean CheckedFirstLine = false; // boolen to see if the request line was read
					 String request_line = "";
 
					 /**
					  * Loops through input stream till header ends
					 */
					 while(!EndOfHeader){ 
 
						 if (checkedFirst){ // checks if we already got the first int from the imput stream
							 dataInt = inputStream.read(); // gets the first int from the inputstream
						 }
 
						 byte dataByte = (byte) dataInt; // Converts the integer obtained into a byte
						 byte[] dataArray = {dataByte}; // Saves the byte to an array of size 1
						 String ByteChar = new String(dataArray); // Converts the array to a string
						 line.add(ByteChar); // Add the string to the arraylist
		 
						 // Combines all the characters in the arraylist into one string
						 String lineString = String.join("", line);
		 
						 // Checks if the string contans the sequence of characters  \r\n
						 containsSequence = lineString.contains(CharResponse);
		 
						 if (containsSequence){ // If the line is a header line or a status line (contains \r\n)
							 if (line.size() == 2){ // If the line signals the end of the header lines
								 EndOfHeader = true;
							 }else{ // If line is not the end of the header lines
		 
								 // Loop through list and remove the \r\n from the lines before printing
								 for (int i = 0; i < 2; i++){ 
									 int index = line.size() - 1;
									 line.remove(index);
								 }
								 lineString = String.join("", line); // Create a new line without the \r\n
								 System.out.println(lineString);
								 line.clear(); // Clear line arraylist
 
								 if (!CheckedFirstLine){ // checks if we saved the request line
									 CheckedFirstLine = true;
									 request_line = lineString; // Saves the request line to a string
								 }
							 }
						 }
 
						 // Sets the flag that we checked the first input to true
						 if (!checkedFirst){
							 checkedFirst = true;
						 }
					 }
 
					 // Splits the request line by a " " to get the request name, file path, and HTTP
					 String[] arrOfReq = request_line.split(" ", 2);
					 String GET_part = arrOfReq[0]; // gets the GET part of the line
 
					 String withHTTP = arrOfReq[1];
					 String withoutHTTP = withHTTP.replace(" HTTP/1.1", ""); // removes the HTTP from the remaining string
					 String object_path = "";
 
					 if (withoutHTTP.equals("/")){ // If no object path specified
						 object_path = "index.html";
					 }else{
						 object_path = withoutHTTP; // If there is an object path
					 }
 
					 boolean bad_request = false; // boolean to track if request valid
					 if (!(GET_part.equals("GET"))){ // if request does not contain a get
						 bad_request = true;
					 }
					 
					 if (!(withHTTP.contains("HTTP/1.1"))){ // if request does not contain HTTP/1.1
						 bad_request = true;
					 }
 
					 if (bad_request){ // if there is a bad request
						 // get status and header lines and change to bytes
						 status_line = ("HTTP/1.1 400 Bad Request\r\n");
						 getStatusBytes = status_line.getBytes("US-ASCII");
						 error_happened = true;
					 }
 
					 String TruePath = ServerRoot+object_path; // Path of object in root directory
 
					 File f = new File(TruePath);
					 if (!(f.exists())){ // If the file does not exis
						 // get status and header lines and change to bytes
						 status_line = ("HTTP/1.1 404 Not Found\r\n");
						 getStatusBytes = status_line.getBytes("US-ASCII");
						 error_happened = true;
					 }
 
					 if (!error_happened){ // If no error occured
					 System.out.print("no error");

						 // Get the OK status as a string and in bytes
						 status_line = ("HTTP/1.1 200 OK\r\n");
						 getStatusBytes = status_line.getBytes("US-ASCII");
 
						 // Get the date as a string and bytes
						 String date = ServerUtils.getCurrentDate() + "\r\n";
						 byte[] getDatebyte = date.getBytes("US-ASCII");
 
						 // Get the server name as a string and bytes
						 String ServerName = ("Matteo Server\r\n");
						 byte[] getServerByte = ServerName.getBytes("US-ASCII");
 
						 // Get the last time file was modified as a string and in bytes
						 String LastMod = (ServerUtils.getLastModified(f) + "\r\n");
						 byte[] getLastModByte = LastMod.getBytes("US-ASCII");
 
						 // Get the file length as a string and as bytes
						 String ContentLength = (ServerUtils.getContentLength(f) + "\r\n");
						 byte[] GetContentLength = ContentLength.getBytes("US-ASCII");
 
						 // Get the content type of the file as a string and as bytes
						 String ContentType = (ServerUtils.getContentType(f) + "\r\n");
						 byte[] GetContentType = ContentType.getBytes("US-ASCII");
 
						 // Get the close request as a string and bytes
						 String CloseRequest = ("Connection: close\r\n");
						 byte[] CloseRequestByte = CloseRequest.getBytes("US-ASCII");
 
						 // Get the end of the header as a string and bytes
						 String EndOfHead = ("\r\n");
						 byte[] EndOfHeadByte = EndOfHead.getBytes("US-ASCII");
 
						 status_line = status_line.replace("\r\n", ""); // get rid of the \r\n from the status
 
						 // Send response lines to output stream
						 outputStream.write(getStatusBytes);
						 outputStream.flush();
						 System.out.println(status_line); // prints the status line
			 
						 outputStream.write(getDatebyte);
						 outputStream.flush();
						 System.out.println(ServerUtils.getCurrentDate()); // prints the date
 
						 outputStream.write(getServerByte);
						 outputStream.flush();
						 System.out.println("Matteo Server"); // prints the server name
 
						 outputStream.write(getLastModByte);
						 outputStream.flush();
						 System.out.println(ServerUtils.getLastModified(f)); // prints the last modified
 
						 outputStream.write(GetContentLength);
						 outputStream.flush();
						 System.out.println(ServerUtils.getContentLength(f)); // prints the content length
 
						 outputStream.write(GetContentType);
						 outputStream.flush();
						 System.out.println(ServerUtils.getContentType(f)); // prints the content type
			 
						 outputStream.write(CloseRequestByte);
						 outputStream.flush();
						 System.out.println("Connection: close\n"); // prints the connection: close line
			 
						 outputStream.write(EndOfHeadByte); // Write to the outputstream the end of byte header (\r\n)
						 outputStream.flush();
 
						 fileInputStream = new FileInputStream(TruePath);
 
						 //Create a buffer of bytes 
						 byte[] buffer = new byte[4096];
						 int count;
						 while((count = fileInputStream.read(buffer)) > 0){
							 outputStream.write(buffer, 0, count);
							 outputStream.flush();
						 }
					 }
				 }
 
				 if (error_happened){ // If an error happened
					 String date = ServerUtils.getCurrentDate() + "\r\n";
					 byte[] getDatebyte = date.getBytes("US-ASCII");
 
					 // Get the server name as a string and bytes
					 String ServerName = ("Matteo Server\r\n");
					 byte[] getServerByte = ServerName.getBytes("US-ASCII");
 
					 // Get the close request as a string and bytes
					 String CloseRequest = ("Connection: close\r\n");
					 byte[] CloseRequestByte = CloseRequest.getBytes("US-ASCII");
 
					 // Get the end of header as a string and bytes
					 String EndOfHead = ("\r\n");
					 byte[] EndOfHeadByte = EndOfHead.getBytes("US-ASCII");
 
					 status_line = status_line.replace("\r\n", "");
 
					 // Send response lines to output stream
					 outputStream.write(getStatusBytes);
					 outputStream.flush();
					 System.out.println(status_line); // prints the status line
		 
					 outputStream.write(getDatebyte);
					 outputStream.flush();
					 System.out.println(ServerUtils.getCurrentDate()); // prints the date
 
					 outputStream.write(getServerByte);
					 outputStream.flush();
					 System.out.println("Matteo Server"); // prints the server name
		 
					 outputStream.write(CloseRequestByte);
					 outputStream.flush();
					 System.out.println("Connection: close\n"); // prints the connection: close line
	 
					 outputStream.write(EndOfHeadByte); // Write to the outputstream the end of byte header (\r\n)
					 outputStream.flush();
				 }
 
 
				 // Close the sockets
				 outputStream.close();
				 inputStream.close();
				 socket.close();
 
			 } catch (IOException e){
				 System.out.println("Error: " + e.getCause());
			 }
		 }
	 }
 }
 