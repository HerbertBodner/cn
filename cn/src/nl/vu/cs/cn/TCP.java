package nl.vu.cs.cn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import android.R.raw;

import nl.vu.cs.cn.IP.IpAddress;

/**
 * This class represents a TCP stack. It should be built on top of the IP stack
 * which is bound to a given IP address.
 */
public class TCP {

	/** The underlying IP stack for this TCP stack. */
    private IP ip;

    // AA: we can just use a fixed default port since we always have just one connection
    private static final int PORT = 12345;
    /**
     * This class represents a TCP socket.
     *
     */
    public class Socket {

    	/* Hint: You probably need some socket specific data. */
    	TcpControlBlock control;
    	IP.Packet sent_IP_packet;
    	IP.Packet recv_IP_packet;
    	/**
    	 * Construct a client socket.
    	 * @throws IOException 
    	 */
    	private Socket() throws IOException {
    		// TODO: check byte order
    		control = new TcpControlBlock(ip.getLocalAddress(),	ip.getLocalAddress(), TCP.PORT, TCP.PORT);
    		control.tcp_state = ConnectionState.S_LISTEN;
    	}

    	/**
    	 * Construct a server socket bound to the given local port.
		 *
    	 * @param port the local port to use
    	 * @throws IOException 
    	 */
        private Socket(int port) throws IOException {
        	// TODO: check byte order
        	control = new TcpControlBlock(ip.getLocalAddress(),	ip.getLocalAddress(), port, TCP.PORT);
        	control.tcp_state = ConnectionState.S_LISTEN;
		}

		/**
         * Connect this socket to the specified destination and port.
         *
         * @param dst the destination to connect to
         * @param dst the port to connect to
         * @return true if the connect succeeded.
         */
        public boolean connect(IpAddress dst, int port) {

            // Implement the connection side of the three-way handshake here.
        	ip.getLocalAddress();

            return false;
        }

        /**
         * Accept a connection on this socket.
         * This call blocks until a connection is made.
         */
        public void accept() {

            // Implement the receive side of the three-way handshake here.

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
         * @throws InterruptedException 
         * @throws IOException 
         */
        public int read(byte[] buf, int offset, int maxlen) throws IOException, InterruptedException {

            // Read from the socket here.
        	recv_tcp_packet();
        	buf = control.tcp_data;
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
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        	ObjectOutput out = new ObjectOutputStream(bos);
        	TcpPacket next_packet = new TcpPacket(
        			control.tcp_local_ip_addr.getAddress(),		// local IP
        			control.tcp_remote_ip_addr.getAddress(), 	// remote IP
        			65000, 										// local PORT
        			65000, 										// remote PORT
        			4294967294l, 								// SEQ number
        			4294967295l, 								// ACK number
        			buf);
        	out.writeObject(next_packet);
        	byte[] ip_data = bos.toByteArray();		
        	this.sent_IP_packet = new IP.Packet (control.tcp_remote_ip_addr.getAddress(),
        			IP.TCP_PROTOCOL,
        			1,	// TODO: change this
        			ip_data,
        			ip_data.length);		// check this later
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

            // Close the socket cleanly here.

            return false;
        }
        
        
        private void send_tcp_packet() throws IOException {
        	ip.ip_send(this.sent_IP_packet);
        }
        
        private void recv_tcp_packet() throws IOException, InterruptedException {
        	ip.ip_receive_timeout(recv_IP_packet, 1000);
        	control.tcp_data = recv_IP_packet.data;
        }
    }

    
    
    /**
     * 
     * This class represents a TCP Packet 
     *
     */
    public class TcpPacket implements Serializable {
    	
    	/* the maximum unsigned 32 bit value, which is 2^32 - 1. It큦 used to check the upper border of the seq_nr and the ack_nr. */
    	static final long MAX32BIT_VALUE = 4294967295l; 	// AA: equivalent to Integer.MAX_VALUE-Integer.MIN_VALUE
    	
    	
    	/* the maximum unsigned 16 bit value, which is 2^16 - 1. It큦 used to check the upper border of the source_port and destination_port. */
    	static final int MAX16BIT_VALUE = 65535;			// AA: equivalent to Short.MAX_VALUE-Short.MIN_VALUE
    	
    	//TODO check the max length of the payload: 
		//  - in the book (p. 539) they say it큦 536 by default 
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
    	
    	 
    	/**Constructor*/
    	public TcpPacket(int source_IpAddress, int destination_IpAddress, int source_port, int destination_port, long seq_nr, long ack_nr, byte[] payload)
    	{
    		source_ip = source_IpAddress;
    		destination_ip = destination_IpAddress;

    		// Reserved space for the TCP packet
    		rawData = ByteBuffer.allocate(MAX_PAYLOAD_LENGTH+HEADER_SIZE);
    		
    		// check for valid source port
			if (source_port < 0 || source_port > MAX16BIT_VALUE)
			{
				throw new InvalidParameterException("Source port number only allowed between 0 and 65535.");
			}
			
			
			// check for valid destination port
			if (destination_port < 0 || destination_port > MAX16BIT_VALUE)
			{
				throw new InvalidParameterException("Destination port number only allowed between 0 and 65535.");
			}

			
			// check for valid seq_nr
			if (seq_nr < 0 || seq_nr > MAX32BIT_VALUE)
			{
				throw new InvalidParameterException("Sequence number only allowed between 0 and " + MAX32BIT_VALUE + ".");
			}
			
			// check for valid ack_nr
			if (ack_nr < 0 || ack_nr > MAX32BIT_VALUE)
			{
				throw new InvalidParameterException("Acknowledgement number only allowed between 0 and " + MAX32BIT_VALUE + ".");
			}
			
			
			if (payload.length > MAX_PAYLOAD_LENGTH)
			{
				throw new InvalidParameterException("Payload length is only allowed up to " + MAX_PAYLOAD_LENGTH + " Bytes.");
			}
			
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
        	
    		int flags =	0 << 5		// urgent flag, which is always false in our case 
    					+ 0 << 4	// ack flag, which is 0 by default, but can be changed by the method setACK_Flag
    					+ 1 << 3	// push flag, which is always true in our case
    					+ 0 << 2	// reset flag, which is 0 by default, but can be changed by the method setRST_Flag
    					+ 0 << 1	// syn flag, which is 0 by default, but can be changed by the method setSYN_Flag
    					+ 0;		// fin flag, which is 0 by default, but can be changed by the method setFIN_Flag
    		rawData.put(13, (byte)flags);
    		
    		// Window Size: since we implement the stop뭤뭛o protocol, we ignore the window size and set it to the maximum size 
        	// of one packet on all outgoing packets, which is 8KB = 8192 byte
    		rawData.putShort(14, (short)8192);
    		
    		// Checksum: is by default 0
    		rawData.putShort(16, (short)0);

    		// Urgent pointer: is always 0, because we the urgent flag is not supported
    		rawData.putShort(18, (short)0);
    		
    		// Payload, set the payload
    		
    		rawData.put(payload);
    		// TODO: debug put methods, they do not seem to work ok
    		
    		// the length of the TCP packet is the length of the payload plus the 20 byte TCP header (in Bytes)
    		packetLength = payload.length + HEADER_SIZE;
    	}
    	
    	/**
    	 * Calculate the checksum and set it into the rawData of the TCP packet
    	 * */
    	public void calculateChecksum()
    	{
    		// set checksum to 0
    		rawData.putShort(16, (short)0);
    		
    		byte[] buf = rawData.array();
    		int leftSum = 0;
			int rightSum = 0;
			int sum = 0;
			

			// calculate the 16bit one큦 complement sum for the left 8 bit and the right 8 bit separately
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
			sum += source_ip & 0xFFFF;
			sum += (source_ip >>> 16) & 0xFFFF;
			sum += destination_ip & 0xFFFF;
			sum += (destination_ip >>> 16) & 0xFFFF;
			
			// add IP protocol nr 6
			sum += IP.TCP_PROTOCOL;
			
			// add packet length
			sum += packetLength;
									
			// add overflow
			while( (sum>>>16) != 0 ) {
				sum = (sum & 0xFFFF) + (sum >>> 16);
			}
			sum = ~sum;
			rawData.putShort(16, (short) sum );
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
		 * Get the value of the RST flag
		 */
		public boolean isRST_Flag() {
			return (getFlags() & 4) != 0;
		}

		/**
		 * Set the RST flag to true or to false
		 */
		// AA: RST flag should always be cleared (Section 2: Flags)
		public void setRST_Flag(boolean rst_flag) {
			setFlag(2, rst_flag);
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
			setFlag(0, psh_flag);
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
		}
		
		/**
		 * Check, whether this is a connection request (RST=0, SYN=1, FIN=0)
		 */
		public boolean isConnectRequest() {
			return (getFlags() & 7) == 2;
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
