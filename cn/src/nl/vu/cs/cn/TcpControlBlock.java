package nl.vu.cs.cn;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Random;

import nl.vu.cs.cn.IP.IpAddress;


/**
 * This enum represents a the connection states of a TCP connection
 *
 * @author Alexandru Asandei, Herbert Bodner;
 *
 */
class TcpControlBlock {

	//TODO
	private static int TCB_BUF_SIZE = 1024;
	
	/** Contains all current TcpConnections. Key is the local port.
	 * When a new TCP connection is set up, then we will search in this HashMap to check, which local port is available and can be used  */
	private static HashMap<Number, TcpControlBlock> currentTcpConnections = new HashMap<Number, TcpControlBlock>();
	
	/* the maximum (exclusive) MAX_PORT_NR unsigned 16 bit value, which is 2^16 - 1. It´s used to check the upper boundary of the source_port and destination_port. */
	static final int MAX_PORT_NR = 65535;
	
	/** Our IP address. */
	private IpAddress tcb_local_ip_addr;
	
	/** Their IP address. */
	private IpAddress tcb_remote_ip_addr;
	
	/** Our port number. */
	private int tcb_local_port;
	
	/** Their port number. */
	private int tcb_remote_port;
	
	/** What we know they know. */
	private int tcb_local_sequence_num;
	
	/** What we want them to ack. */
	private int tcb_local_expected_ack;
	
	/** What we think they know we know. */
	private int tcb_remote_sequence_num;
	
	/** Static buffer for recv data. */
	private byte tcb_data[];
	
	/** The undelivered data. */
	private byte tcb_p_data; 
	
	/** Undelivered data bytes. */
	private int tcb_data_left; 
	
	/** The current TCP connection state. */
	private ConnectionState tcb_state; 
	
	
	
	
	
	/** constructor called by client */
	public TcpControlBlock() {
		tcb_state = ConnectionState.S_CLOSED;
	}
	
	
	
	public void SetSynSentState(IpAddress local_ip, IpAddress remote_ip, int remote_port) throws InvalidParameterException
	{			
		// check local ip
		if (local_ip == null)
		{
			throw new InvalidParameterException("Invalid local IP address.");
		}
		tcb_local_ip_addr = local_ip;
		
		// check remote ip
		if (remote_ip == null)
		{
			throw new InvalidParameterException("Invalid remote IP address.");
		}
		tcb_remote_ip_addr = remote_ip;
		
		// check remote port
		if (remote_port < 0 || remote_port >= MAX_PORT_NR)
		{
			throw new InvalidParameterException("Invalid remote port, only between 0 and " + MAX_PORT_NR + "!");
		}
		
		// find a local port, which is free
		int local_port = getUnusedLocalPort();
		if (local_port == -1) {
			throw new InvalidParameterException("No free source port available. There are too many opened TCP connections, only " + MAX_PORT_NR + " allowed.");
		}
		tcb_local_port = local_port;
	}

	
	/**
	 * This method returns a random port between 0 and MAX_PORT_NR 
	 * and checks in the HashMap "currentTcpConnections", which local port is available and can be used
	 * If no unused port was found, then -1 is returned */
	private static int getUnusedLocalPort() {
		
		// try to find a new unused port randomly
		for(int i=0; i<MAX_PORT_NR; i++) {
			Random rand = new Random();
			int new_port = rand.nextInt(MAX_PORT_NR);
			if (!currentTcpConnections.containsKey(new_port)) 
				return new_port;
		}
		
		// if the random method does not succeed, we use a systematic way
		for(int i=0; i<MAX_PORT_NR; i++) {
			if (!currentTcpConnections.containsKey(i)) 
				return i;
		}
		
		// if no unused port is found, return -1
		return -1;
	}
	
}
