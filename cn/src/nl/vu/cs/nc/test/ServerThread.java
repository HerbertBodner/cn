package nl.vu.cs.nc.test;

import java.io.IOException;

import android.widget.TextView;
import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.TCP.Socket;

public class ServerThread extends Thread {
	
	private int _address;
	private int _port;
	private TextView _messageHistory;
	
	public ServerThread(int address, int port, TextView messages) {
		_address = address;
		_port = port;
		_messageHistory = messages; 
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
			
			_messageHistory.append("\nListening for connections...");
			// listen at serverSocketListener and accept new incoming connections
			serverSocket.accept();
			
			//byte[] readBuffer = new byte[8152];
			//serverSocket.read(readBuffer, 0, 8152);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
