package nl.vu.cs.cn.test;

import java.io.IOException;

import junit.framework.Assert;

import nl.vu.cs.cn.ConnectionState;
import nl.vu.cs.cn.ConnectionUtils;
import nl.vu.cs.cn.Logging;
import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.Socket;
import nl.vu.cs.cn.TCP.TcpPacket;
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
	public static Socket getServerSocket(int serverIP, int serverPort) {
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
	public static Socket getClientSocket(int clientIP) {
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
				
				//verify SEQ/ACK numbers
				TcpPacket tcpPacket = serverSocket.getLastReceivedTcpPacketForTesting();
				if (tcpPacket == null)
					fail("No last received TCP packet available!");
				long SEQnum = tcpPacket.getSEQNumber();
				long ACKnum = tcpPacket.getACKNumber();
				assertEquals(0, SEQnum);
				assertEquals(0, ACKnum);
				
				long localSEQNum = serverSocket.getTcpControlBlock().getLocalSeqForTesting();
				long remoteNextSEQNum = serverSocket.getTcpControlBlock().getRemoteNextSeqForTesting();
				assertEquals(0, localSEQNum);
				assertEquals(12, remoteNextSEQNum);
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
				
				// verify SEQ/ACK numbers
				TcpPacket tcpPacket = clientSocket.getLastReceivedTcpPacketForTesting();
				if (tcpPacket == null)
					fail("No last received TCP packet available!");
				long SEQnum = tcpPacket.getSEQNumber();
				long ACKnum = tcpPacket.getACKNumber();
				assertEquals(0, SEQnum);
				assertEquals(12, ACKnum);
				
				long localSEQNum = clientSocket.getTcpControlBlock().getLocalSeqForTesting();
				long remoteNextSEQNum = clientSocket.getTcpControlBlock().getRemoteNextSeqForTesting();
				assertEquals(12, localSEQNum);
				assertEquals(0, remoteNextSEQNum);
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
	
	public void testT023SimpleCloseConnection() {
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		byte[] exptectedTextToReceive = "Established".getBytes();
				Socket serverSocket = getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] buf = new byte[1024];
				if (serverSocket.read(buf, 0, 1024) <= 0) {
					fail("Failed to read a message from the client!");
				}
				
				for(int i = 0; i<11; i++) {
					assertEquals(exptectedTextToReceive[i], buf[i]);
				}
				try {
					this.wait(5000);	// wait for close()
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				// try reading again
				serverSocket.read(buf, 0, 1024);
				
				// connection state should be CLOSED
				ConnectionState check = serverSocket.getTcpControlBlock().getConnectionStateForTesting();
				Assert.assertEquals(ConnectionState.S_CLOSED, check);
				
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
	        	String textToSend = "Established";
	        	
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
				
				// close connection
				clientSocket.close();
				
				// connection state should be CLOSED
				assertEquals(ConnectionState.S_CLOSED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
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


	public void testT024LargeReadWrite() {
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		int expectedLen = 20*1024;
	    		byte[] exptectedTextToReceive = new byte[expectedLen];
				Socket serverSocket = getServerSocket(serverIP, serverPort);
								
				StringBuilder textToSend = new StringBuilder();
	        	for (int i=0; i<expectedLen; i++)
					textToSend.append("A");
	        	exptectedTextToReceive = textToSend.toString().getBytes();
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				try {
					this.wait(1000);	// wait a bit
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] buf = new byte[expectedLen];
				if (serverSocket.read(buf, 0, expectedLen) <= 0) {
					fail("Failed to read a message from the client!"+buf.toString());
				}
				

				for(int i = 0; i<expectedLen; i++) {
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
	        	int expectedLen = 20*1024;
	        	
	        	StringBuilder textToSend = new StringBuilder();
	        	
	        	for (int i=0; i<expectedLen; i++)
					textToSend.append("A");
	        	
				Socket clientSocket = getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] textByteArray = textToSend.toString().getBytes();
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

	
	public void testT025LargeReadWriteWithMultipleRead() {
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "test server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		int expectedLen = 20*1024;
	    		byte[] exptectedTextToReceive = new byte[expectedLen];
				Socket serverSocket = getServerSocket(serverIP, serverPort);
				
				
				StringBuilder textToSend = new StringBuilder();
	        	for (int i=0; i<expectedLen; i++)
					textToSend.append("A");
	        	exptectedTextToReceive = textToSend.toString().getBytes();
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				try {
					this.wait(1000);	// wait a bit
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] buf = new byte[expectedLen];
				if (serverSocket.read(buf, 0, 5000) <= 0) {
					fail("Failed to read a message from the client!"+buf.toString());
				}
				
				if (serverSocket.read(buf, 5000, 15000) <= 0) {
					fail("Failed to read a message from the client!"+buf.toString());
				}
								
				if (serverSocket.read(buf, 20000, expectedLen) <= 0) {
					fail("Failed to read a message from the client!"+buf.toString());
				}

				for(int i = 0; i<expectedLen; i++) {
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
	        	int expectedLen = 20*1024;
	        	
	        	StringBuilder textToSend = new StringBuilder();
	        	
	        	for (int i=0; i<expectedLen; i++)
					textToSend.append("A");
	        	
				Socket clientSocket = getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] textByteArray = textToSend.toString().getBytes();
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
	
	/**
	 * Create a client and a server in a separate thread, establish the connection, send "Hello World!" and close the connection.
	 */
	public static void runClientServerCommunication() {
		// START SERVER in a thread
		Thread serverThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "Server");
	    
	        	int serverIP = 1;
	    		int serverPort = 80;
	    		byte[] exptectedTextToReceive = "Hello World!".getBytes();
				Socket serverSocket = ClientServerTest.getServerSocket(serverIP, serverPort);
				
				// listen at serverSocketListener and accept new incoming connections
				serverSocket.accept();
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, serverSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] buf = new byte[1024];
				if (serverSocket.read(buf, 0, 1024) <= 0) {
					fail("Failed to read a message from the client!");
				}
				
				for(int i = 0; i<11; i++) {
					assertEquals(exptectedTextToReceive[i], buf[i]);
				}
				try {
					this.wait(5000);	// wait for close()
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				// try reading again
				serverSocket.read(buf, 0, 1024);
				
				// connection state should be CLOSED
				ConnectionState check = serverSocket.getTcpControlBlock().getConnectionStateForTesting();
				Assert.assertEquals(ConnectionState.S_CLOSED, check);
				
	        }
	    });
		serverThread.start();
		
		
		
	    // START CLIENT in a thread
		Thread clientThread = new Thread(new Runnable() {
	        public void run() {
	        	Logging.getInstance().LogConnectionInformation(null, "Client");
	    	    
	        	int serverIP = 1;
	        	int clientIP = 2;
	        	int server_socket = 80;
	        	String textToSend = "Hello World!";
	        	
				Socket clientSocket = ClientServerTest.getClientSocket(clientIP);
				
				// create server IP address and connect to server
				IpAddress serverAddress = IpAddress.getAddress("192.168.0." + serverIP);
				if (!clientSocket.connect(serverAddress, server_socket)) {
					fail("Failure during connect to server!");
				}
				
				// connection state should be ESTABLISHED
				assertEquals(ConnectionState.S_ESTABLISHED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
				
				byte[] textByteArray = textToSend.getBytes();
				clientSocket.write(textByteArray, 0, textByteArray.length);
				
				// close connection
				clientSocket.close();
				
				// connection state should be CLOSED
				assertEquals(ConnectionState.S_CLOSED, clientSocket.getTcpControlBlock().getConnectionStateForTesting());
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
