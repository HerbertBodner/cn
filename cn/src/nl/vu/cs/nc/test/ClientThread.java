package nl.vu.cs.nc.test;

import java.io.IOException;
import java.math.BigInteger;

import android.widget.TextView;

import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.Socket;

public class ClientThread extends Thread {
	
	private String _ip;
	private int _port;
	private TextView _messageHistory;
	
	public ClientThread(String ip, int port, TextView messages){
		_ip = ip;
		_port = port;
		_messageHistory = messages;
	}
	
	
	
	public void run()
	{
		/////////////////////////////////////////////
		// CONNECT TO THE SERVER FROM THE CLIENT
		/////////////////////////////////////////////
		try {
		// create a new communication endpoint
		TCP tcpClient = new TCP(2);
		
		Socket clientSocket = tcpClient.socket();
		
		// create server IP address and connect to server
		IpAddress serverAddress = IpAddress.getAddress(_ip);
		clientSocket.connect(serverAddress, _port);
		
		if (!clientSocket.connect(serverAddress, _port)) {
			_messageHistory.append("\nCould not connect to "+_ip);
		}
		else {
			_messageHistory.append("\nEnetered: "+clientSocket.getTcpControlBlock().getConnectionStateForTesting());
		}
		
		clientSocket.close();
		_messageHistory.append("\nConnection closed");
		
		
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
}
