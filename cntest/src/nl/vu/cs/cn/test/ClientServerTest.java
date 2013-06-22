package nl.vu.cs.cn.test;

import java.io.IOException;

import nl.vu.cs.cn.ConnectionState;
import nl.vu.cs.cn.ConnectionUtils;
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

	
	/**
	 * Returns a ServerSocket
	 * @param serverIP
	 * @param serverPort
	 * @return
	 */
	private Socket getServerSocket(int serverIP, int serverPort) {
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
		return serverSocket;
	}
	
	
	/**
	 * Returns a client socket.
	 * @param clientIP
	 * @return
	 */
	private Socket getClientSocket(int clientIP) {
		TCP tcpClient = null;
		try {
			// create a new communication endpoint
			tcpClient = new TCP(clientIP);
		} catch (IOException e) {
			
			e.printStackTrace();
			fail("Error during client initialization: " + e.getMessage());
		}
		
		return tcpClient.socket();
	}
	
	
	
	
	public void testT021ClientServerConnection() {
		
		
		
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		byte[] exptectedTextToReceive = "Hello world!".getBytes();
	    		

				Socket serverSocket = getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] buf = new byte[1024];
				if (serverSocket.read(buf, 0, 1024) <= 0) {
					fail("Failed to read a message from the client!");
				}
				
				for(int i = 0; i<12; i++) {
					assertEquals(exptectedTextToReceive[i], buf[i]);
				}
				
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
	        	
				
				Socket clientSocket = getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] textByteArray = textToSend.getBytes();
				clientSocket.write(textByteArray, 0, textByteArray.length);
				
	        }
		});
		clientThread.start();
		
		
		try {
			serverThread.join();
			clientThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Exception when joining the client and server thread: " + e.getMessage());
		}
	}
	
	
	
	public void testT022SEQOverflow() {
		
		// set SEQ number to the max-value minus 1 to achieve a SEQ-number overflow
		ConnectionUtils.SEQUENCE_NR_STARTVALUE_FOR_TESTING = ConnectionUtils.MAX32BIT_VALUE - 1; 
		
		
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		byte[] exptectedTextToReceive = "Hello world!".getBytes();
	    		

				Socket serverSocket = getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] buf = new byte[1024];
				if (serverSocket.read(buf, 0, 1024) <= 0) {
					fail("Failed to read a message from the client!");
				}
				
				for(int i = 0; i<12; i++) {
					assertEquals(exptectedTextToReceive[i], buf[i]);
				}
				
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
	        	
				
				Socket clientSocket = getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] textByteArray = textToSend.getBytes();
				clientSocket.write(textByteArray, 0, textByteArray.length);
				
	        }
		});
		clientThread.start();
		
		
		try {
			serverThread.join();
			clientThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Exception when joining the client and server thread: " + e.getMessage());
		}
		
	}
	
}
