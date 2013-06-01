package nl.vu.cs.cn.test;

import java.io.IOException;

import nl.vu.cs.cn.Logging;
import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.Socket;
import android.test.AndroidTestCase;

public class ClientServerTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testT021ClientServerConnection() {
		
		
		
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		String exptectedTextToReceive = "Hello world!";
	    		

				TCP tcpServer = null;
				Socket serverSocket = null;
				try {
		        	// create a new communication endpoint
					tcpServer = new TCP(serverIP);
				
					// bind (attach a local address to the socket)
					serverSocket = tcpServer.socket(serverPort);
				} catch (IOException e) {
					e.printStackTrace();
					fail("Error during server initialization: " + e.getMessage());
				}
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				
				byte[] buf = new byte[1024];
				if (serverSocket.read(buf, 0, 1027) <= 0) {
					fail("Failed to read a message from the client!");
				}
				
				assertEquals(exptectedTextToReceive.getBytes(), buf);
				
	        }
	    });
		serverThread.start();
		
		
		
	    // START CLIENT in a thread
		Thread clientThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test client");
	    	    
	        	int serverIP = 1;
	        	int clientIP = 2;
	        	int server_socket = 80;
	        	String textToSend = "Hello world!";
	        	
				TCP tcpClient = null;
				try {
					// create a new communication endpoint
					tcpClient = new TCP(clientIP);
				} catch (IOException e) {
					
					e.printStackTrace();
					fail("Error during client initialization: " + e.getMessage());
				}
				
				Socket clientSocket = tcpClient.socket();
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.1." + serverIP);
				clientSocket.connect(serverAddress, server_socket);
				
				byte[] textByteArray = textToSend.getBytes();
				clientSocket.write(textByteArray, 0, textByteArray.length);
				
	        }
		});
		clientThread.start();
		
		
		try {
			clientThread.join();
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Exception when joining the client and server thread: " + e.getMessage());
		}
	}
	
}
