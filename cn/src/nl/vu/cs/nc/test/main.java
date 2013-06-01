package nl.vu.cs.nc.test;

public class main {
	
	public static void main()
	{
		ServerThread server = new ServerThread(1, 1083);
		server.start();
		
		ClientThread client = new ClientThread("192.168.1.1", 1083);
		//client.start();
	}
}
