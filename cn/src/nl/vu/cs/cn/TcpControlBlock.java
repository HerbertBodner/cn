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
	static int TCP_BUF_SIZE = 8192;
	
	/** Our IP address. */
	IpAddress tcp_local_ip_addr;
	
	/** Their IP address. */
	IpAddress tcp_remote_ip_addr;
	
	/** Our port number. */
	int tcp_local_port;
	
	/** Their port number. */
	int tcp_remote_port;
	
	/** What we know they know. */
	int tcp_local_sequence_num;
	
	/** What we want them to ack. */
	int tcp_local_expected_ack;
	
	/** What we think they know we know. */
	int tcp_remote_sequence_num;
	
	/** Static buffer for recv data. */
	byte tcp_data[];
	
	/** The undelivered data. */
	byte tcp_p_data; 
	
	/** Undelivered data bytes. */
	int tcp_data_left; 
	
	/** The current TCP connection state. */
	ConnectionState tcp_state; 
	
	/** constructor */
	public TcpControlBlock(IpAddress local_ip, IpAddress remote_ip, int local_port, int remote_port) throws InvalidParameterException
	{
		// check local ip
		if (local_ip == null)
		{
			throw new InvalidParameterException("Invalid local IP address.");
		}
		tcp_local_ip_addr = local_ip;
		
		// check remote ip
		if (remote_ip == null)
		{
			throw new InvalidParameterException("Invalid remote IP address.");
		}
		tcp_remote_ip_addr = remote_ip;
	}
	
	

}
