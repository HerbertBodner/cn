package nl.vu.cs.cn.test;

import nl.vu.cs.cn.ConnectionState;
import nl.vu.cs.cn.Logging;
import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.Socket;
import nl.vu.cs.nc.test.PacketLossControl;
import android.test.AndroidTestCase;


public class UnreliableNetworkTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testT031SYNPacketLoss() {
		
		// SYN packet should be lost 1 time
		PacketLossControl.getInstance().SetSYNPacketLost(1);
		
		
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		

				Socket serverSocket = ClientServerTest.getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
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
	        	
				
				Socket clientSocket = ClientServerTest.getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
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

	
	
	public void testT032SYNACKPacketLoss() {
		
		// SYN/ACK packet should be lost 1 time
		PacketLossControl.getInstance().SetSYNACKPacketLost(1);
		
		
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		
	
				Socket serverSocket = ClientServerTest.getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
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
	        	
				
				Socket clientSocket = ClientServerTest.getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
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
	
	
	
	public void testT033ACKPacketLoss() {
		
		// ACK packet should be lost 1 times
		PacketLossControl.getInstance().SetACKPacketLost(1);
		
		
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		
	
				Socket serverSocket = ClientServerTest.getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
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
	        	
				
				Socket clientSocket = ClientServerTest.getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
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
