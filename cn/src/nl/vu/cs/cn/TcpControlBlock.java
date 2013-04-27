package nl.vu.cs.cn;

import java.security.InvalidParameterException;

import nl.vu.cs.cn.IP.IpAddress;


/**
 * This enum represents a the connection states of a TCP connection
 *
 * @author Alexandru Asandei, Herbert Bodner;
 *
 */
public class TcpControlBlock {

	//TODO
	static int TCB_BUF_SIZE = 1024;
	
	/** Our IP address. */
	IpAddress tcb_local_ip_addr;
	
	/** Their IP address. */
	IpAddress tcb_remote_ip_addr;
	
	/** Our port number. */
	int tcb_local_port;
	
	/** Their port number. */
	int tcb_remote_port;
	
	/** What we know they know. */
	int tcb_local_sequence_num;
	
	/** What we want them to ack. */
	int tcb_local_expected_ack;
	
	/** What we think they know we know. */
	int tcb_remote_sequence_num;
	
	/** Static buffer for recv data. */
	byte tcb_data[];
	
	/** The undelivered data. */
	byte tcb_p_data; 
	
	/** Undelivered data bytes. */
	int tcb_data_left; 
	
	/** The current TCP connection state. */
	ConnectionState tcb_state; 
	
	/** constructor */
	public TcpControlBlock(IpAddress local_ip, IpAddress remote_ip, int local_port, int remote_port) throws InvalidParameterException
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
	}
	
	

}
