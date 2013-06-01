package nl.vu.cs.cn;

import java.util.HashMap;
import java.util.Random;

import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.TcpControlBlock;

/**
 * This class manages all current opened TCP connections for different Interfaces/IpAddresses
 * @author Herbert Bodner, Alexandru Assandei
 *
 */
public class TcpConnections {
	
	/**
	 * Contains all current TcpConnections for each interface/IpAddress. The key of the HashMap is the IpAddress and its value is another HashMap, which contains all the used Port with the TcpControlBlock
	 * When a new TCP connection for a SourceIpAddress is set up, then we will search in this HashMap to check, which local port is available and can be used. 
	 */
	private static HashMap<Number, HashMap<Number, TcpControlBlock>> currentInterfaces = new HashMap<Number, HashMap<Number, TcpControlBlock>>();
	
	/* the maximum (exclusive) MAX_PORT_NR unsigned 16 bit value, which is 2^16 - 1. It´s used to check the upper boundary of the source_port and destination_port. */
	private static final int MAX_PORT_NR = 65535;

	
	/**
	 * This method returns a random port between 0 and MAX_PORT_NR 
	 * and checks in the HashMap "currentTcpConnections", which local port is available and can be used
	 * If no unused port was found, then -1 is returned 
	*/
	public static int getFreeLocalPort(IpAddress ipAddress, TcpControlBlock tcpControlBlock) {
		
		// create a new entry for the ipAddress, if it does not exist
		if (!currentInterfaces.containsKey(ipAddress.getAddress())) {
			currentInterfaces.put(ipAddress.getAddress(), new HashMap<Number, TcpControlBlock>());
		}
		
		// get all the opened connections of the given IpAddress
		HashMap<Number, TcpControlBlock> currentTcpConnections = currentInterfaces.get(ipAddress.getAddress());
		
		// try to find a new unused port randomly
		for(int i=0; i<MAX_PORT_NR; i++) {
			Random rand = new Random();
			int new_port = rand.nextInt(MAX_PORT_NR);
			if (!currentTcpConnections.containsKey(new_port)) { 
				currentTcpConnections.put(new_port, tcpControlBlock);
				return new_port;
			}
		}
		
		// if the random method does not succeed, we use a systematic way
		for(int i=0; i<MAX_PORT_NR; i++) {
			if (!currentTcpConnections.containsKey(i)) {
				currentTcpConnections.put(i, tcpControlBlock);
				return i;
			}
		}
		
		// if no unused port is found, return -1
		Logging.getInstance().LogTcpPacketError("No free source port available. There are too many opened TCP connections, only " + MAX_PORT_NR + " allowed.");
		return -1;
	}
	
	
	
	/**
	 * Returns true, if the given port for the given IpAddress is already in use, otherwise false.
	 * @param ipAddress
	 * @param port
	 * @return
	 */
	public static boolean isPortFree(IpAddress ipAddress, int port) {
		if (currentInterfaces.containsKey(ipAddress.getAddress())) {
			
			// get all the opened connections of the given IpAddress
			HashMap<Number, TcpControlBlock> currentTcpConnections = currentInterfaces.get(ipAddress.getAddress());
		
			if (currentTcpConnections.containsKey(port)) {
				Logging.getInstance().LogTcpPacketError("Port '" + port + "' for IpAddress '" + ipAddress.toString() + "' is already in use!");
				return false;
			}
		}
		return true;
	}
}
