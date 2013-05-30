package nl.vu.cs.nc.test;

import java.io.IOException;
import java.math.BigInteger;

import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.Socket;

public class ClientThread extends Thread {
	
	private String _ip;
	private int _port;
	public ClientThread(String ip, int port){
		_ip = ip;
		_port = port;
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
		
		// send number 3
		//TODO: add '\0' at the end
		byte[] three = BigInteger.valueOf(3).toByteArray();
		clientSocket.write(three, 0, 4);
		
		// send number 4
		//TODO: add '\0' at the end
		byte[] four = BigInteger.valueOf(4).toByteArray();
		clientSocket.write(four, 0, 4);
		
		// receive the sum of the two numbers
		byte[] buffer = new byte[5];
		while (clientSocket.read(buffer, 0, 5) > 0)
		{
		
		}
		
		
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
}
