package nl.vu.cs.nc.test;

import java.io.IOException;
import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.TCP.Socket;

public class ServerThread extends Thread {
	
	private int _address;
	private int _port;
	public ServerThread(int address, int port) {
		_address = address;
		_port = port;
	}
	
	
	public void run()
	{
		/////////////////////////////////////////////
		// CREATE A SERVER
		/////////////////////////////////////////////
		try {
			// create a new communication endpoint
			TCP tcpServer = new TCP(_address);
			
			// bind (attach a local address to the socket)
			Socket serverSocket = tcpServer.socket(_port);
			
			// listen at serverSocketListener and accept new incoming connections
			serverSocket.accept();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		
	}
}
