package nl.vu.cs.cn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import nl.vu.cs.cn.IP.IpAddress;

/**
 * This class represents a TCP stack. It should be built on top of the IP stack
 * which is bound to a given IP address.
 */
public class TCP {

	/** The underlying IP stack for this TCP stack. */
    private IP ip;

    /**
     * This class represents a TCP socket.
     *
     */
    public class Socket {

    	/* Hint: You probably need some socket specific data. */

    	/* The TCP control block contains all details (like the state) of the connection */
    	TcpControlBlock control;
    	
    	IP.Packet sent_IP_packet;
    	IP.Packet recv_IP_packet;

    	
    	/**
    	 * Construct a client socket.
    	 * @throws IOException 
    	 */
    	private Socket() {
			control = new TcpControlBlock();
    	}

    	/**
    	 * Construct a server socket bound to the given local port.
		 *
    	 * @param port the local port to use
    	 * @throws IOException 
    	 */
        private Socket(int port) throws IOException {
        	control = new TcpControlBlock(ip.getLocalAddress(),	port);
		}

		/**
         * Connect this socket to the specified destination and port.
         *
         * @param dst the destination to connect to
         * @param dst the port to connect to
         * @return true if the connect succeeded.
         */
        public boolean connect(IpAddress dst, int port) {
        	
        	// check if the given parameters (dst, port) are correct
        	if (dst == null || !ConnectionUtils.isPortValid(port)) {
        		return false;
        	}
        	
        	// get a new free local Port
        	IpAddress localIp = ip.getLocalAddress();
        	int localPort = TcpConnections.getFreeLocalPort(localIp, control);
        	if (localPort < 0) {
        		return false;
        	}
        	
        	// we have to be in state CLOSED, when connect is called, otherwise something went wrong and we return false
        	if (control.tcb_state != ConnectionState.S_CLOSED) {
        		Logging.getInstance().LogConnectionError(control, "TCP has to be in CLOSED state, when calling method 'connect'!");
        		return false;
        	}
        	
        	// set local and remote IP address and port to the TcpControlBlock control
        	control.tcb_local_ip_addr = localIp.getAddress();
        	control.tcb_remote_ip_addr = dst.getAddress();
        	control.tcb_local_port = localPort; 
        	control.tcb_remote_port = port;
        	

        	// Start with the three-way handshake here.
        	// Create and send SYN packet
        	TcpPacket syn_packet = control.createTcpPacket(null, 0, 0, false);
        	if (syn_packet == null) {
    			return false;
    		}
        	this.sent_IP_packet = control.createIPPacket(syn_packet);
        	if (!send_tcp_packet()) {
        		Logging.getInstance().LogTcpPacketError("Sending SYN packet failed, 'connect' aborted!");
        		return false;
        	}
        	
        	// set connection state to SYN_SENT after successful sent a SYN packet
        	control.tcb_state = ConnectionState.S_SYN_SENT;
        	
        	// wait to receive a packet
    		if (!recv_tcp_packet()){
        		Logging.getInstance().LogTcpPacketError("Receiving SYN/ACK packet failed, 'connect' aborted!");
        		control.resetConnection(ConnectionState.S_CLOSED);
        		return false;
        	}
        	
    		// check if the received packet has the correct flags (if everything is OK, then the ESTABLISHED state is set within the method)
    		if (!control.verifyReceivedPacket(this.recv_IP_packet)) {
    			Logging.getInstance().LogTcpPacketError("Invalid received SYN/ACK packet, 'connect' aborted!");
    			control.resetConnection(ConnectionState.S_CLOSED);
    			return false;
    		}
        	
            return true;
        }

        /**
         * Accept a connection on this socket.
         * This call blocks until a connection is made.
         */
        public void accept() {

        	// we have to be in state LISTEN state, when accept is called, otherwise something went wrong and we return
        	if (control.tcb_state != ConnectionState.S_LISTEN) {
        		Logging.getInstance().LogConnectionError(control, "TCP has to be in LISTEN state, when calling method 'accept'!");
        		return;
        	}

            // Start with the three-way handshake here.
        	if (!recv_tcp_packet()) {
        		Logging.getInstance().LogTcpPacketError("Receiving SYN packet failed, 'accept' aborted!");
        		return;
        	}
        	
        	// check if the received packet has the correct flags (if everything is OK, then the SYN_RCVD state is set within the method)
    		if (!control.verifyReceivedPacket(this.recv_IP_packet)) {
    			Logging.getInstance().LogTcpPacketError("Invalid received SYN packet, 'accept' aborted!");
    			return;
    		}
        	
    		// Create and send SYN/ACK package
    		TcpPacket synack_packet = control.createTcpPacket(null, 0, 0, false);
    		if (synack_packet == null) {
    			control.resetConnection(ConnectionState.S_LISTEN);
    			return;
    		}
        	this.sent_IP_packet = control.createIPPacket(synack_packet);
        	if (!send_tcp_packet()) {
        		Logging.getInstance().LogTcpPacketError("Sending SYN/ACK packet failed, 'accept' aborted!");
        		control.resetConnection(ConnectionState.S_LISTEN);
        		return;
        	}
        	
        	// wait to receive a ACK packet
    		if (!recv_tcp_packet()){
        		Logging.getInstance().LogTcpPacketError("Receiving ACK packet failed, 'accept' aborted!");
        		control.resetConnection(ConnectionState.S_LISTEN);
        		return;
        	}
        	
    		// check if the received packet has the correct flags (if everything is OK, then the ESTABLISHED state is set within the method)
    		if (!control.verifyReceivedPacket(this.recv_IP_packet)) {
    			Logging.getInstance().LogTcpPacketError("Invalid received ACK packet, 'accept' aborted!");
    			control.resetConnection(ConnectionState.S_LISTEN);
    			return;
    		}
        }

        /**
         * Reads bytes from the socket into the buffer.
         * This call is not required to return maxlen bytes
         * every time it returns.
         *
         * @param buf the buffer to read into
         * @param offset the offset to begin reading data into
         * @param maxlen the maximum number of bytes to read
         * @return the number of bytes read, or -1 if an error occurs.
         */
        public int read(byte[] buf, int offset, int maxlen) {

            // Read from the socket here.
        	recv_tcp_packet();
        	buf = control.tcb_data;
            return -1;
        }

        /**
         * Writes to the socket from the buffer.
         *
         * @param buf the buffer to
         * @param offset the offset to begin writing data from
         * @param len the number of bytes to write
         * @return the number of bytes written or -1 if an error occurs.
         * @throws IOException 
         */
        public int write(byte[] buf, int offset, int len) throws IOException {
            // Write to the socket here.       	
        	TcpPacket next_packet = control.createTcpPacket(buf, offset, len, false);
        	if (next_packet == null) {
        		return -1;
        	}
        	
        	this.sent_IP_packet = control.createIPPacket(next_packet);
        	
        	
        	send_tcp_packet();
            return -1;
        }
        
        /**
         * Closes the connection for this socket.
         * Blocks until the connection is closed.
         *
         * @return true unless no connection was open.
         */
        public boolean close() {
        	
        	// if we are in CLOSED, SYN_SENT or LISTEN state, then we go to CLOSE state and return false
        	if (control.tcb_state == ConnectionState.S_CLOSED ||
        			control.tcb_state == ConnectionState.S_SYN_SENT ||
        			control.tcb_state == ConnectionState.S_LISTEN) {
        		Logging.getInstance().LogConnectionInformation(control, "TCP connection was not established, when calling method 'close'!");
        		control.resetConnection(ConnectionState.S_CLOSED);
        		return false;
        	}

        	
        	// if we are in SYN_RCVD or ESTABLISHED state, then we sent a FIN-package
        	// TODO
        	
            return false;
        }
        
        
        private boolean send_tcp_packet() {
        	
        	// TODO: try several times, if there is an IOException at the first time? (Read the specification!)
        	try {
				ip.ip_send(this.sent_IP_packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return false;
			}
        	
        	return true;
        }
        

        private boolean recv_tcp_packet() {
        	try {
				ip.ip_receive_timeout(recv_IP_packet, 1000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
        	return true;
        }
    }

    
    
    /**
     * 
     * This class represents a TCP Packet 
     *
     */
    public class TcpPacket {
    	
    	//TODO check the max length of the payload: 
		//  - in the book (p. 539) they say it´s 536 by default 
		//  - Wikipedia says it is 1500 - 20 - 20 = 1460 Byte (20 Byte IP-Header, 20 Byte TCP-Header)
    	// AA: see Knowledge Sharing AA-0.1 about max payload
		static final int MAX_PAYLOAD_LENGTH = 8152;
		static final int HEADER_SIZE = 20;	// AA: TCP header size is 20B
    	
    	/* the source IP address, which is needed for calculation of the checksum*/
    	int source_ip;
    	
    	/* the destination IP address, which is needed for calculation of the checksum*/
    	int destination_ip;
    	
    	/* contains the Tcp Packet in a raw byte format*/
    	ByteBuffer rawData = null;
    	
    	/* the TCP header checksum*/
    	byte[] checksum = null;
    	
    	/* the data for the TCP packet*/
    	byte[] data;
    	
    	/* the length of the TCP Packet */
    	int packetLength;
    	
    	
    	/**
    	 * Constructor used when a TCP packet has been received (for verifying the content of the received data)
    	 * @param source_IpAddress
    	 * @param destination_IpAddress
    	 * @param tcpData
    	 */
    	public TcpPacket(int source_IpAddress, int destination_IpAddress, byte[] tcpData, int length)
    	{
    		source_ip = source_IpAddress;
    		destination_ip = destination_IpAddress;
    		
    		rawData = ByteBuffer.wrap(tcpData, 0, length);
    		packetLength = length;
    	}
    	
    	
    	/**
    	 * Constructor used to create a new TCP packet (to create a TCP packet, which should be sent)
    	 * @param source_IpAddress
    	 * @param destination_IpAddress
    	 * @param source_port
    	 * @param destination_port
    	 * @param seq_nr
    	 * @param ack_nr
    	 * @param payload
    	 */
    	public TcpPacket(int source_IpAddress, int destination_IpAddress, int source_port, int destination_port, long seq_nr, long ack_nr, byte[] payload)
    	{
    		source_ip = source_IpAddress;
    		destination_ip = destination_IpAddress;
   		
    		// check for valid source port
			if (!ConnectionUtils.isPortValid(source_port))
			{
				throw new InvalidParameterException("Source port number only allowed between 0 and 65535.");
			}
			
			
			// check for valid destination port
			if (!ConnectionUtils.isPortValid(destination_port))
			{
				throw new InvalidParameterException("Destination port number only allowed between 0 and 65535.");
			}

			
			// check for valid seq_nr
			if (!ConnectionUtils.isSEQValid(seq_nr))
			{
				
				throw new InvalidParameterException("Wrong value for SEQ number!");
			}
			
			// check for valid ack_nr
			if (!ConnectionUtils.isSEQValid(ack_nr))
			{
				throw new InvalidParameterException("Wrong value for ACK number!");
			}
			
			
			if (payload.length > MAX_PAYLOAD_LENGTH)
			{
				throw new InvalidParameterException("Payload length is only allowed up to " + MAX_PAYLOAD_LENGTH + " Bytes.");
			}

    		// Reserved space for the TCP packet
    		rawData = ByteBuffer.allocate(HEADER_SIZE + payload.length);

			fillTcpPacket(source_port, destination_port, seq_nr, ack_nr, payload);
    	}
    	
    	
    	
    	
    	/**
    	 * Fill the Tcp Packet with the given parameters and set the rest (e.g. ACK-Flag,...) to default values
    	 * */
    	private void fillTcpPacket(int source_port, int destination_port, long seq_nr, long ack_nr, byte[] payload)
    	{   		
    		// TODO: check whether the conversion (int to short) is done correctly for e.g. 65535 
    		// AA: I think it's safer (and easier to control) converting to byte array 
    		// TODO: check byte ordering (little endian vs. big endian): host uses big endian
    		byte[] raw_src_port = ByteBuffer.allocate(4).putInt(source_port).array();
    		byte[] raw_dest_port = ByteBuffer.allocate(4).putInt(destination_port).array();
    		byte[] raw_seq_nr = ByteBuffer.allocate(8).putLong(seq_nr).array();
    		byte[] raw_ack_nr = ByteBuffer.allocate(8).putLong(ack_nr).array();
    		
    		// set the source_port, destination_port, seq_nr and ack_nr in the rawData of the TCP packet
    		// AA: because we are using big endian we ignore the first half of each int/long when extracting short/int from them
    		// (alternatively we could also shift <<2/<<4 and use putShort/putInt)
    		rawData.put(0, raw_src_port[2]);
    		rawData.put(1, raw_src_port[3]);
    		//rawData.putShort(0, (short)source_port);
    		rawData.put(2, raw_dest_port[2]);
    		rawData.put(3, raw_dest_port[3]);
    		//rawData.putShort(2, (short)destination_port);
    		rawData.put(4, raw_seq_nr[4]);
    		rawData.put(5, raw_seq_nr[5]);
    		rawData.put(6, raw_seq_nr[6]);
    		rawData.put(7, raw_seq_nr[7]);
    		//rawData.putInt(4, (int)seq_nr);
    		rawData.put(8, raw_ack_nr[4]);
    		rawData.put(9, raw_ack_nr[5]);
    		rawData.put(10, raw_ack_nr[6]);
    		rawData.put(11, raw_ack_nr[7]);
    		//rawData.putInt(8, (int)ack_nr);
    		// STATUS: 12 bytes set, 8 remaining in header    		
    		
    		/* TCP header length: in our case we have no Options, therefore the header has a fixed length of 5 32-bit words*/
    		// STATUS: next byte is for DATA OFFSET, RESERVED and NS 
    		rawData.put(12, (byte)(5 << 4));

    		// although follwing code has a bad performance, it is more readable
    		int flags =	0 << 5		// urgent flag, which is always false in our case 
    					+ 0 << 4	// ack flag, which is 0 by default, but can be changed by the method setACK_Flag
    					+ 1 << 3	// push flag, which is always true in our case
    					+ 0 << 2	// reset flag, which is always false in our case
    					+ 0 << 1	// syn flag, which is 0 by default, but can be changed by the method setSYN_Flag
    					+ 0;		// fin flag, which is 0 by default, but can be changed by the method setFIN_Flag
    		rawData.put(13, (byte)flags);
    		
    		// Window Size: since we implement the stop’n’go protocol, we ignore the window size and set it to the maximum size 
        	// of one packet on all outgoing packets, which is 8KB = 8192 byte
    		rawData.putShort(14, (short)8192);
    		
    		// Checksum: is by default 0
    		rawData.putShort(16, (short)0);

    		// Urgent pointer: is always 0, because we the urgent flag is not supported
    		rawData.putShort(18, (short)0);
    		
    		// Payload, set the payload
    		
    		rawData.position(HEADER_SIZE);
    		rawData.put(payload);
    		// TODO: debug put methods, they do not seem to work ok
    		
    		// the length of the TCP packet is the length of the payload plus the 20 byte TCP header (in Bytes)
    		packetLength = payload.length + HEADER_SIZE;
    	}
    	
    	
    	
    	/**
    	 * Verify the checksum of a TCP packet and return the result (true if OK, otherwise false).
    	 * @return
    	 */
    	public boolean verifyChecksum() {
    		return calculateChecksum(true);
    	}
    	
    	
    	
    	/**
    	 * if onlyVerify is false, then the checksum and is calculated and set it into the rawData of the TCP packet. Then true is returned.
    	 * If onlyVerify is true, then the checksum is only verified and the result is returned (true if OK, otherwise false).
    	 * @param onlyVerify
    	 * @return
    	 */
    	private boolean calculateChecksum(boolean onlyVerify)
    	{
    		if (!onlyVerify) {
    			// set checksum to 0
    			rawData.putShort(16, (short)0);
    		}
    		
    		byte[] buf = rawData.array();
    		int leftSum = 0;
			int rightSum = 0;
			int sum = 0;
			

			// calculate the 16bit one´s complement sum for the left 8 bit and the right 8 bit separately
			// byte is signed, therefore add "& 0xFF" to get an unsigned value
			for(int i=0 ; i<packetLength-1; i+=2 ) {
				leftSum += buf[i] & 0xFF;
				rightSum += buf[i+1] & 0xFF;			
			}
			
			// if packetLength contains odd bytes, add the last byte
			if( (packetLength&1) != 0) {
				leftSum += buf[packetLength-1] & 0xFF;
			}
			
			// form complete sum
			sum = (leftSum << 8) + rightSum;
					
			// ADD CHECKSUM OF PSEUDO HEADER
			// add source/destination IP address
			
			//following code is correct, if we would have the IP addresses in big-endian (but unfortunately we have it in little-endian format)
			/*sum += source_ip & 0xFFFF;
			sum += (source_ip >>> 16) & 0xFFFF;
			sum += destination_ip & 0xFFFF;
			sum += (destination_ip >>> 16) & 0xFFFF;*/
			
			// for little-endian we have calculate a bit, but it works
			sum += ((source_ip >>> 24) + ((source_ip & 0xFF0000) >>> 8) + ((source_ip & 0xFF00) >>> 8) + ((source_ip & 0xFF) << 8));
			sum += ((destination_ip >>> 24) + ((destination_ip & 0xFF0000) >>> 8) + ((destination_ip & 0xFF00) >>> 8) + ((destination_ip & 0xFF) << 8));
			
			
			// add IP protocol nr 6
			sum += IP.TCP_PROTOCOL;
			
			// add packet length
			sum += packetLength;
									
			// add overflow
			while( (sum>>>16) != 0 ) {
				sum = (sum & 0xFFFF) + (sum >>> 16);
			}
			sum = ~((short)sum);
			
			if (!onlyVerify) {
				rawData.putShort(16, (short) sum );
				return true;
			}
			return sum==0;
    	}
    	
    	
    	/**
    	 * Returns the whole TCP packet as byte array
    	 */
    	public byte[] getByteArray()
    	{
    		return rawData.array();
    	}
    	
    	/**
		 * Get the value of the ACK flag
		 */
    	public boolean isACK_Flag() {
			return (getFlags() & 16) != 0;
		}

    	
    	/**
		 * Set the ACK flag to true or to false
		 */
		public void setACK_Flag(boolean ack_flag) {
			setFlag(4, ack_flag);
		}


		/**
		 * Get the value of the SYN flag
		 */
		public boolean isSYN_Flag() {
			return (getFlags() & 2) != 0;
		}

		/**
		 * Set the SYN flag to true or to false
		 */
		public void setSYN_Flag(boolean syn_flag) {
			setFlag(1, syn_flag);
		}

		/**
		 * Get the value of the FIN flag
		 */
		public boolean isFIN_Flag() {
			return (getFlags() & 1) == 1;
		}

		/**
		 * Set the PSH flag to true or to false
		 * should be true when sending data packets with payload (non-control packets)
		 */
		public void setPSH_Flag(boolean psh_flag) {
			setFlag(3, psh_flag);
		}
		
		/**
		 * Get the value of the PSH flag
		 */
		public boolean isPSH_Flag() {
			return (getFlags() & 3) == 1;
		}

		/**
		 * Set the FIN flag to true or to false
		 */
		public void setFIN_Flag(boolean fin_flag) {
			setFlag(0, fin_flag);
		}
		
    	
		/** 
		 * Get the flags from the TCP packet 
		 * */
		private byte getFlags() {
			return rawData.get(13);
		}
		
		/**
		 * Set a flag_value at a specific position (e.g. position=1, new_flag_value=true means SYN=1)
		 * @param position
		 * @param new_flag_value
		 */
		private void setFlag(int position, boolean new_flag_value)
		{
			// read all flags from the raw data
			byte flags = getFlags();
			
			// modify one flag
			if (new_flag_value)
				flags |= (1 << position);	// combine disjunctive with 1 at correct position
			else
				flags &= (0 << position);	// combine conjunctive with 0 at correct position
			
			// write all flags back to the raw data
			rawData.put(13, flags);
			
			// recompute the checksum
			this.calculateChecksum(false);
		}
		
		/**
		 * Check, whether this is a connection request (RST=0, SYN=1, FIN=0)
		 */
		public boolean isConnectRequest() {
			return (getFlags() & 7) == 2;
		}
		
		/**
		 * This method is only used for testing purposes
		 * @param windowSize
		 */
		public void setWindowSize(short windowSize) {
			rawData.putShort(14, windowSize);
			this.calculateChecksum(false);
		}
    }
    
    
    
    
    
    
    /**
     * This enum represents a the connection states of a TCP connection
     *
     * @author Alexandru Asandei, Herbert Bodner;
     *
     */
    public class TcpControlBlock {

    	//TODO
    	//private int TCB_BUF_SIZE = 8192;
    	
    	
    	/** Our IP address. */
    	int tcb_local_ip_addr;
    	
    	/** Their IP address. */
    	int tcb_remote_ip_addr;
    	
    	/** Our port number. */
    	int tcb_local_port;
    	
    	/** Their port number. */
    	int tcb_remote_port;
    	
    	/** What we know they know. */
    	long tcb_local_sequence_num;
    	
    	/** What we want them to ack. */
    	long tcb_local_expected_ack;
    	
    	/** What we think they know we know. */
    	long tcb_remote_sequence_num;
    	
    	/** Static buffer for recv data. */
    	byte tcb_data[];
    	
    	/** The undelivered data. */
    	byte tcb_p_data[]; 
    	
    	/** Undelivered data bytes. */
    	int tcb_data_left[]; 
    	
    	/** The current TCP connection state. */
    	ConnectionState tcb_state; 
    	
    	
    	
    	
    	
    	/** constructor called by client */
    	public TcpControlBlock() {
    		tcb_state = ConnectionState.S_CLOSED;
    		
    		//TODO set the SEQ and ACK
    		tcb_local_sequence_num = 4294967294l;
    		tcb_local_expected_ack = 4294967293l;
    	}
    	
    	
    	/**
    	 * Constructor called by server 
    	 * @param local_ip
    	 * @param local_port
    	 * @throws InvalidParameterException
    	 */
    	public TcpControlBlock(IpAddress local_ip, int local_port) throws IOException
    	{			
    		// check IP address
    		if (local_ip == null) {
    			throw new IOException("Invalid empty IP address!");
    		}
    		tcb_local_ip_addr = local_ip.getAddress();
    		
    		// check local port
    		if (ConnectionUtils.isPortValid(local_port)) {
    			throw new IOException("Invalid local port!");
    		}

    		// check if local port is not already used by another TCP connection
    		if (!TcpConnections.isPortFree(local_ip, local_port)) {
    			throw new IOException("Port '" + local_port + "' for IpAddress '" + local_ip.toString() + "' is already in use!");
    		}
    		tcb_remote_port = local_port;
    		   		
    		//TODO set the SEQ and ACK (randomly?)
    		tcb_local_sequence_num = 4294967294l;
    		tcb_local_expected_ack = 4294967293l;
    		
    		// set the connection status to LISTEN for the server
    		tcb_state = ConnectionState.S_LISTEN;
    	}
    	
    	/**	
    	 * Verifies a received TcpPacket by checking the flags, SEQ/ACK number and changing the state of the connection.
    	 * It returns true if the packet was successful verified and a ACK packet can be sent back or a ACK packet was received.
    	 * @param tcpPacket
    	 * @return
    	 */
    	public boolean verifyReceivedPacket(IP.Packet ipPacket) {

    		// check, if the received IP packet has the expected IP addresses 
    		// this would also be detected by verifyChecksum of the TcpPacket, but here we produce a better error message 
    		if (ipPacket.destination != tcb_local_ip_addr) {
    			Logging.getInstance().LogTcpPacketError("The received IP packet had the wrong destination IP address "
    					+ "(expectedIP='" + IpAddress.htoa(tcb_local_ip_addr) + "', "
    					+ "packet-destination-address='" + IpAddress.htoa(ipPacket.destination) + "')");
    			return false;
    		}
    		if (ipPacket.source != tcb_remote_ip_addr) {
    			Logging.getInstance().LogTcpPacketError("The received IP packet had the wrong source IP address "
    					+ "(expectedIP='" + IpAddress.htoa(tcb_remote_ip_addr) + "', "
    					+ "packet-source-address='" + IpAddress.htoa(ipPacket.source) + "')");
    			return false;
    		}
    			
    		// verify protocol version
    		if (ipPacket.protocol != 4) {
    			Logging.getInstance().LogTcpPacketError("The received IP packed had the wrong protocol version (expected='2', actual='" + ipPacket.protocol +"').");
    			return false;
    		}
    		
    		//TODO: verify the ID of the IP packet?
    		
    		//TODO: verify ports (this would also be detected by verifyChecksum of the TcpPacket, so it´s nice but not really necessary)
    		
    		TcpPacket tcpPacket = new TcpPacket(ipPacket.source, ipPacket.destination, ipPacket.data, ipPacket.length);
    		
    		if (!tcpPacket.verifyChecksum()) {
    			Logging.getInstance().LogTcpPacketError("The received IP packed had the wrong checksum!");
    			return false;
    		}
    		
    		// TODO check the SEQ and ACK number
    		
    		
    		switch(tcb_state) {
    			case S_CLOSED:
    				Logging.getInstance().LogConnectionError(this, "Package was received during TCP was in CLOSED state!");
    				return false;
    				
    			case S_SYN_SENT:
    				if (tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
    					// If we were in SYN_SENT state and received a valid SYN-ACK package, we go to ESTABLISHED state
    					tcb_state = ConnectionState.S_ESTABLISHED;
    					return true;
    				}
    				else {
    					Logging.getInstance().LogTcpPacketError("Expected SYN/ACK packet, but actual package had SYN=" + tcpPacket.isSYN_Flag() + ", ACK=" + tcpPacket.isACK_Flag() + ", FIN=" + tcpPacket.isFIN_Flag());
    				}
    				break;
    			
    			case S_LISTEN:
    				if (tcpPacket.isSYN_Flag() && !tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
    					// If we were in LISTEN state and received a valid SYN package, we go to SYN_RCVD state
    					tcb_state = ConnectionState.S_SYN_RCVD;
    					return true;
    				}
    				else {
    					Logging.getInstance().LogTcpPacketError("Expected SYN packet, but actual package had SYN=" + tcpPacket.isSYN_Flag() + ", ACK=" + tcpPacket.isACK_Flag() + ", FIN=" + tcpPacket.isFIN_Flag());
    				}
    				break;
    				
    			case S_ESTABLISHED:
    				break;
    			
    		}
    		return false;
    	}
    	
    	/**
    	 * Returns a TcpPacket, which has all the flags set according to the current ConnectionState
    	 * If setFIN is set, then the FIN-flag is set.
    	 * If the packet could not be created, then null is returned (e.g. when you want to close a not-established connection)
    	 * @param buf
    	 * @param offset
    	 * @param len
    	 * @param setFIN
    	 * @return
    	 * @throws IOException
    	 */
    	public TcpPacket createTcpPacket(byte[] buf, int offset, int len, boolean setFIN) {
            
    		// sending data is only allowed, when the connection is in a specific state
    		// TODO: extend the possible cases (CLOSING connections) and return null
    		if (len > 0) {
    			if (tcb_state == ConnectionState.S_CLOSED || 
    				tcb_state == ConnectionState.S_LISTEN)  {
    				
    				Logging.getInstance().LogConnectionError(this, "You have to establish a connection first, before sending data is allowed!");
    				return null;
    			}
    		}
    			
    		
        	TcpPacket next_packet = new TCP.TcpPacket(
        			tcb_local_ip_addr,		// local IP
        			tcb_remote_ip_addr, 	// remote IP
        			tcb_local_port,			// local PORT
        			tcb_remote_port, 		// remote PORT
        			tcb_local_sequence_num,	// SEQ number
        			tcb_local_expected_ack,	// ACK number
        			buf);
        	
        	switch(tcb_state) {
        		case S_CLOSED:
        			next_packet.setSYN_Flag(true);
        		case S_LISTEN:
        			next_packet.setSYN_Flag(true);
        			next_packet.setACK_Flag(true);
        	}
        	
        	// check if closing connection is allowed and set FIN flag
        	if (setFIN) {
        		if (tcb_state != ConnectionState.S_ESTABLISHED) {
        			Logging.getInstance().LogTcpPacketError("Only established connection can be closed! (ConnectionState='" + tcb_state + "').");
        			return null;
        		}
        		next_packet.setFIN_Flag(true);
        	}
        		
        	
        	return next_packet;
        }
    	
    	
    	/**
    	 * Resets the connection to state CLOSED and resets all variables
    	 */
    	public void resetConnection(ConnectionState resetState) {
    		switch (resetState) {
    			case S_CLOSED:
		    		tcb_local_ip_addr = 0;
		    		tcb_remote_ip_addr = 0;
		    		tcb_local_port = 0;
		    		tcb_remote_port = 0;
		    		tcb_local_sequence_num = 0;
		        	tcb_local_expected_ack = 0;
		        	tcb_remote_sequence_num = 0;
		        	tcb_data = null;
		        	tcb_p_data = null; 
		        	tcb_data_left= null;
		        	tcb_state = resetState;
		    		break;
    			case S_LISTEN:
    				tcb_state = resetState;
    				break;
    		}
    		
    	}
    	
    	/**
    	 * Creates an IP packet out of the given TCP packet
    	 * @param tcpPacket
    	 * @return
    	 */
    	public IP.Packet createIPPacket(TcpPacket tcpPacket) {
    		byte[] ip_data = tcpPacket.getByteArray();
    		IP.Packet ip = new IP.Packet (tcb_remote_ip_addr,
        			IP.TCP_PROTOCOL,
        			1,	// TODO: change this
        			ip_data,
        			ip_data.length);		// TODO: check this later
    		
    		return ip;
    	}
    	
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * Constructs a TCP stack for the given virtual address.
     * The virtual address for this TCP stack is then
     * 192.168.1.address.
     *
     * @param address The last octet of the virtual IP address 1-254.
     * @throws IOException if the IP stack fails to initialize.
     */
    public TCP(int address) throws IOException {
        ip = new IP(address);
    }

    /**
     * @return a new socket for this stack
     * @throws IOException 
     */
    public Socket socket() throws IOException {
        return new Socket();
    }

    /**
     * @return a new server socket for this stack bound to the given port
     * @param port the port to bind the socket to.
     * @throws IOException 
     */
    public Socket socket(int port) throws IOException {
        return new Socket(port);
    }

}
