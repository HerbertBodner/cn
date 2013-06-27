package nl.vu.cs.cn;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.nc.test.PacketLossControl;

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

		/*
		 * The TCP control block contains all details (like the state) of the
		 * connection
		 */
		TcpControlBlock control;

		IP.Packet sent_IP_packet;
		IP.Packet recv_IP_packet;

		/**
		 * Construct a client socket.
		 */
		private Socket() {
			control = new TcpControlBlock();
			recv_IP_packet = control.createIPPacket();
			control.tcb_local_ip_addr = ip.getLocalAddress().getAddress();
		}

		/**
		 * Construct a server socket bound to the given local port.
		 * 
		 * @param port
		 *            the local port to use
		 * @throws IOException
		 */
		private Socket(int port) throws IOException {

			// TODO: HB: We can check if the port is not already used by another
			// TCP connection (therefore we can create a HashMap<Number,
			// TcpControlBlock> to store the currently active TCP connections)
			control = new TcpControlBlock(ip.getLocalAddress(), port);
			recv_IP_packet = control.createIPPacket();
		}

		/**
		 * Returns the TcpControlBlock. This method is only used for
		 * manipulation of the TcpControlBlock for testing purposes.
		 * 
		 * @return
		 */
		public TcpControlBlock getTcpControlBlock() {
			return control;
		}

		/**
		 * Connect this socket to the specified destination and port.
		 * 
		 * @param dst
		 *            the destination to connect to
		 * @param dst
		 *            the port to connect to
		 * @return true if the connect succeeded.
		 */
		public boolean connect(IpAddress dst, int port) {

			// check if the given parameters (dst, port) are correct
			if (dst == null || !ConnectionUtils.isPortValid(port)) {
				return false;
			}

			// get a new free local Port
			Random rand = new Random();
			int localPort = rand.nextInt(ConnectionUtils.MAX16BIT_VALUE - 1024) + 1024;

			// we have to be in state CLOSED, when connect is called, otherwise
			// something went wrong and we return false
			if (control.tcb_state != ConnectionState.S_CLOSED) {
				Logging.getInstance().LogConnectionError(control, "TCP has to be in CLOSED state, when calling method 'connect'!");
				return false;
			}

			// set local and remote IP address and port to the TcpControlBlock
			// control
			control.tcb_remote_ip_addr = dst.getAddress();
			control.tcb_local_port = localPort;
			control.tcb_remote_port = port;

			// Start with the three-way handshake here.
			// Create and send SYN packet
			TcpPacket syn_packet = control.createTcpPacket(null, 0, 0, false);
			if (!send_tcp_packet(syn_packet, true)) {
				Logging.getInstance().LogTcpPacketError(control, "Sending SYN packet failed, 'connect' aborted!");
				return false;
			}

			
			
			// Create and send ACK packet
			TcpPacket ack_packet = control.createTcpPacket(null, 0, 0, false);

			if (!send_tcp_packet(ack_packet, false)) {
				Logging.getInstance().LogTcpPacketError(control, "Sending ACK packet failed, 'connect' aborted!");
				return false;
			}

			

			/*
			// wait to receive a packet
			if (!recv_tcp_packet(true, false)) {
				Logging.getInstance().LogTcpPacketError(control, "Receiving SYN/ACK packet failed, 'connect' aborted!");
				control.resetConnection(ConnectionState.S_CLOSED);
				return false;
			}
			*/

			return true;
		}

		/**
		 * Accept a connection on this socket. This call blocks until a
		 * connection is made.
		 */
		public void accept() {

			// we have to be in state LISTEN, when accept is called, otherwise
			// something went wrong and we return
			if (control.tcb_state != ConnectionState.S_LISTEN) {
				Logging.getInstance()
						.LogConnectionError(control,
								"TCP has to be in LISTEN state, when calling method 'accept'!");
				return;
			}

			// Start with the three-way handshake here.
			if (!recv_tcp_packet(false, true)) {
				Logging.getInstance().LogTcpPacketError(control,
						"Receiving SYN packet failed, 'accept' aborted!");
				return;
			}

			// Create and send SYN/ACK package
			TcpPacket synack_packet = control
					.createTcpPacket(null, 0, 0, false);
			if (synack_packet == null) {
				control.resetConnection(ConnectionState.S_LISTEN);
				return;
			}

			if (!send_tcp_packet(synack_packet, true)) {
				Logging.getInstance().LogTcpPacketError(control,
						"Sending SYN/ACK packet failed, 'accept' aborted!");
				control.resetConnection(ConnectionState.S_LISTEN);
				return;
			}
/*
			// wait to receive a ACK packet
			if (!recv_tcp_packet(false, false)) {
				Logging.getInstance().LogTcpPacketError(control,
						"Receiving ACK packet failed, 'accept' aborted!");
				control.resetConnection(ConnectionState.S_LISTEN);
				return;
			}*/
		}

		/**
		 * Reads bytes from the socket into the buffer. This call is not
		 * required to return maxlen bytes every time it returns.
		 * 
		 * @param buf
		 *            the buffer to read into
		 * @param offset
		 *            the offset to begin reading data into
		 * @param maxlen
		 *            the maximum number of bytes to read
		 * @return the number of bytes read, or -1 if an error occurs.
		 */
		public int read(byte[] buf, int offset, int maxlen) {

			// Read from the socket here.
			if (!recv_tcp_packet(true, false))
				return -1;

			TcpPacket tcp = new TcpPacket(this.recv_IP_packet.source,
					this.recv_IP_packet.destination, this.recv_IP_packet.data,
					this.recv_IP_packet.length);
			tcp.getPayload(buf);
			return 1;
		}

		/**
		 * Writes to the socket from the buffer.
		 * 
		 * @param buf
		 *            the buffer to
		 * @param offset
		 *            the offset to begin writing data from
		 * @param len
		 *            the number of bytes to write
		 * @return the number of bytes written or -1 if an error occurs.
		 */
		public int write(byte[] buf, int offset, int len) {

			// Write to the socket here.
			TcpPacket next_packet = control.createTcpPacket(buf, offset, len,
					false);
			if (next_packet == null) {
				return -1;
			}

			send_tcp_packet(next_packet, true);
			return -1;
		}

		/**
		 * Non-blocking method that closes the current TCP connection for
		 * writing. Handles connection tear-down requested by the application,
		 * not by the peer (via FIN packet).
		 * 
		 * @return true unless no connection was open.
		 */
		public boolean close() {

			// CASE 0: "phantom" close
			// if we are in CLOSED, SYN_SENT or LISTEN state, then we go to
			// CLOSE state and return false
			if (control.tcb_state == ConnectionState.S_CLOSED
					|| control.tcb_state == ConnectionState.S_SYN_SENT
					|| control.tcb_state == ConnectionState.S_LISTEN
					|| control.tcb_state == ConnectionState.S_TIME_WAIT) {
				Logging.getInstance().LogConnectionInformation(control, "TCP connection was not established, when calling method 'close'!");
				control.resetConnection(ConnectionState.S_CLOSED);
				return false;
			}

			// CASE 1: passive close
			if (this.control.tcb_state == ConnectionState.S_CLOSE_WAIT) {
				// go to LAST_ACK state
				control.tcb_state = ConnectionState.S_LAST_ACK;
				// must send the client a FIN
				TcpPacket close_response = control.createTcpPacket(null, 0, 0, true);
				// we don't check for the result of the send because we close
				// the connection anyway
				boolean response = send_tcp_packet(close_response, true);
				// close connection
				Logging.getInstance().LogConnectionInformation(control, "TCP connection was PASSIVELY closed!");
				control.resetConnection(ConnectionState.S_CLOSED);
			}

			// CASE 2: active close (if we are in SYN_RCVD state)
			if (this.control.tcb_state == ConnectionState.S_SYN_RCVD
					|| this.control.tcb_state == ConnectionState.S_ESTABLISHED) {
				// go to FIN_WAIT1 state
				control.tcb_state = ConnectionState.S_FIN_WAIT_1;
				// must send the client a FIN
				TcpPacket close_response = control.createTcpPacket(null, 0, 0, true);
				boolean sent = send_tcp_packet(close_response, true);
				// go to TIME_WAIT state
				control.tcb_state = ConnectionState.S_TIME_WAIT;
				// must send the client an ACK
				close_response = control.createTcpPacket(null, 0, 0, false);
				send_tcp_packet(close_response, false);
				// close connection
				Logging.getInstance().LogConnectionInformation(control, "TCP connection was ACTIVELY closed!");
				control.resetConnection(ConnectionState.S_CLOSED);
			}

			return true;
		}

		private boolean send_tcp_packet(TcpPacket tcpPacketToSend, boolean waitForACKAfterSend) {

			this.sent_IP_packet = control.createIPPacket(tcpPacketToSend);

			// for a maximum of 10 retries
			int i = 0;
			while (i < 10) {
				try {
					// try sending or simulate a lost TcpPacket
					if (!PacketLossControl.getInstance().IsTcpPacketLost(tcpPacketToSend, control.tcb_state)) {
						ip.ip_send(this.sent_IP_packet);
					}

					// don´t wait for ACKs during connection setup/teardown
					if (!waitForACKAfterSend)
						return true;

					// wait for ACK
					ip.ip_receive_timeout(recv_IP_packet, 1);
					// check for "garbage" packages which do not count
					if (recv_IP_packet == null)
						continue;

					TcpPacket tcpPacket = control.verifyReceivedPacket(this.recv_IP_packet, true);
					if (tcpPacket != null) {
						if (control.tcb_state == ConnectionState.S_FIN_WAIT_1) {
							if (tcpPacket.isFIN_Flag()
									&& !tcpPacket.isACK_Flag()) {
								// active close (slow)
								control.tcb_state = ConnectionState.S_CLOSING;
								// create ACK packet
								TcpPacket reply = this.control.createTcpPacket(null, 0, 0, false);
								// make sure ACK flag is set
								reply.setACK_Flag(true);
								// create IP packet
								IP.Packet encoded = this.control.createIPPacket(reply);
								// send ACK
								ip.ip_send(encoded);
								// enter timeout mode
								control.tcb_state = ConnectionState.S_TIME_WAIT;
								try {
									// wait for ACK with double timeout
									ip.ip_receive_timeout(recv_IP_packet, 2);
								} catch (InterruptedException e) {
									// ignore timeout
								} finally {
									// we close the connection regardless of the
									// received packet
									this.close();
								}
								// send failed
								return false;
							} else if (tcpPacket.isFIN_Flag()
									&& tcpPacket.isACK_Flag()) {
								// active close (fast)
								control.tcb_state = ConnectionState.S_TIME_WAIT;
								// create ACK packet
								TcpPacket reply = this.control.createTcpPacket(
										null, 0, 0, false);
								// make sure ACK flag is set
								reply.setACK_Flag(true);
								// create IP packet
								IP.Packet encoded = this.control
										.createIPPacket(reply);
								// send ACK
								ip.ip_send(encoded);
								try {
									// wait for ACK with double timeout
									ip.ip_receive_timeout(recv_IP_packet, 2);
								} catch (InterruptedException e) {
									// ignore timeout
								} finally {
									// we close the connection regardless of the
									// received packet
									this.close();
								}
								// send failed
								return false;
							} else if (!tcpPacket.isFIN_Flag()
									&& tcpPacket.isACK_Flag()) {
								// active close (slow)
								control.tcb_state = ConnectionState.S_FIN_WAIT_2;
								// create FIN packet
								TcpPacket reply = this.control.createTcpPacket(
										null, 0, 0, false);
								// create IP packet
								IP.Packet encoded = this.control
										.createIPPacket(reply);
								// enter timeout mode
								control.tcb_state = ConnectionState.S_TIME_WAIT;
								// send FIN
								ip.ip_send(encoded);
								try {
									// wait for ACK with double timeout
									ip.ip_receive_timeout(recv_IP_packet, 2);
								} catch (InterruptedException e) {
									// ignore timeout
								} finally {
									// we close the connection regardless of the
									// received packet
									this.close();
								}
								// send failed
								return false;
							}
						}

						if (control.acceptReceivedTcpPacket(tcpPacket)) {
							return true; // packet send and ACK received successfully
						}
					}

					// bad packet received, retry => increase counter
					i++;
				} catch (IOException e) {
					e.printStackTrace();
					// sending failed, retry => increase counter
					i++;

				} catch (InterruptedException e) {
					e.printStackTrace();
					// no ACK received within 1 second, retry => increase
					// counter
					i++;
				}
			}
			// crashed one too many times, admit defeat
			if (i >= 10)
				return false;
			return true;
		}

		private boolean recv_tcp_packet(boolean sendACKAfterReceive, boolean acceptNewConnection) {

			try {
				TcpPacket tcpPacket = null;
				for (int i = 0; i<10; i++) {
					// wait for packet
					ip.ip_receive_timeout(recv_IP_packet, 10);
					// check if real packet has been received
					if (recv_IP_packet == null)
						return false;
	
					// verify the packet
					tcpPacket = control.verifyReceivedPacket(this.recv_IP_packet, acceptNewConnection);
					if (tcpPacket != null) {
						// received a valid packet, break out from the for loop (which is used to resend old packages in case of received packages
						break;
					}
					else {
						// Re-send old package
						ip.ip_send(this.sent_IP_packet);
						Logging.getInstance().LogConnectionInformation(control, "Resend packet, because received packet was not an expected packet!");
					}
				}

				if (tcpPacket == null) {
					return false;	// we didn´t get a packet we expected => return false
				}
				
				// check if the received packet has the correct flags
				if (!control.acceptReceivedTcpPacket(tcpPacket)) {
					return false; // correct, but unexpected packet -> don´t wait for resend, just return false
				}
				
				if (tcpPacket.isFIN_Flag()) {
					// check state
					if (control.tcb_state == ConnectionState.S_ESTABLISHED) {
						// passive close
						control.tcb_state = ConnectionState.S_CLOSE_WAIT;
						// create ACK packet
						TcpPacket reply = this.control.createTcpPacket(
								null, 0, 0, false);
						// make sure ACK flag is set
						reply.setACK_Flag(true);
						// create IP packet
						IP.Packet encoded = this.control.createIPPacket(reply);
						// send ACK
						ip.ip_send(encoded);
						// close connection
						this.close();
					} else if (control.tcb_state == ConnectionState.S_FIN_WAIT_1) {
						// active close (fast)
						control.tcb_state = ConnectionState.S_TIME_WAIT;
						// create FIN+ACK packet
						TcpPacket reply = this.control.createTcpPacket(null, 0,
								0, true);
						// make sure ACK flag is set
						reply.setACK_Flag(true);
						// create IP packet
						IP.Packet encoded = this.control.createIPPacket(reply);
						// send FIN+ACK
						ip.ip_send(encoded);
						// wait for ACK packet (twice longer than normal)
						try {
							ip.ip_receive_timeout(recv_IP_packet, 2);
						} catch (InterruptedException e) {
							// ignore timeout
						} finally {
							// we close the connection regardless of the
							// received packet
							this.close();
						}
					}
					// recv failed
					return false;
				}



				// don´t send ACK packets, just return true, because of a
				// successful receive
				if (!sendACKAfterReceive)
					return true;

				// create ACK packet
				TcpPacket reply = this.control.createTcpPacket(null, 0, 0,
						false);
				// make sure ACK flag is set
				reply.setACK_Flag(true);
				// create IP packet
				IP.Packet encoded = this.control.createIPPacket(reply);
				// send ACK
				ip.ip_send(encoded);
				// verify for duplicates
				/*
				 * if (this.control.tcb_remote_next_expected_SEQ_num ==
				 * decoded.getSEQNumber()) continue; // ACK was resent, listen
				 * for next packet (do not push this duplicate to application)
				 * // update control block info
				 * this.control.tcb_remote_next_expected_SEQ_num =
				 * decoded.getSEQNumber();
				 */
				// exit current waiting loop
				return true;
			} catch (IOException e) {
				// connection forcely terminated => abort
				e.printStackTrace();
				return false;
			} catch (InterruptedException e) {
				// timed out, try again
				e.printStackTrace();
			}

			// successfully received
			return false;
		}

		/**
		 * Returns the last received Tcp Packet. This method is only used for
		 * testing purposes.
		 * 
		 * @return
		 */
		public TcpPacket getLastReceivedTcpPacketForTesting() {
			if (this.recv_IP_packet == null)
				return null;

			return control.lastReceivedTcpPacket;
		}
	}

	/**
	 * 
	 * This class represents a TCP Packet
	 * 
	 */
	public class TcpPacket {

		// TODO check the max length of the payload:
		// - in the book (p. 539) they say it´s 536 by default
		// - Wikipedia says it is 1500 - 20 - 20 = 1460 Byte (20 Byte IP-Header,
		// 20 Byte TCP-Header)
		// AA: see Knowledge Sharing AA-0.1 about max payload
		static final int MAX_PAYLOAD_LENGTH = 8152;
		static final int HEADER_SIZE = 20; // AA: TCP header size is 20B

		/*
		 * the source IP address, which is needed for calculation of the
		 * checksum
		 */
		int source_ip;

		/*
		 * the destination IP address, which is needed for calculation of the
		 * checksum
		 */
		int destination_ip;

		/* contains the Tcp Packet in a raw byte format */
		ByteBuffer rawData = null;

		/* the TCP header checksum */
		byte[] checksum = null;

		/* the data for the TCP packet */
		byte[] data;

		/* the length of the TCP packet */
		int packetLength;

		/**
		 * Constructor used when a IP packet has been received (for verifying
		 * the content of the received data)
		 * 
		 * @param source_IpAddress
		 * @param destination_IpAddress
		 * @param tcpData
		 */
		public TcpPacket(int source_IpAddress, int destination_IpAddress,
				byte[] tcpData, int length) {
			source_ip = source_IpAddress;
			destination_ip = destination_IpAddress;

			rawData = ByteBuffer.wrap(tcpData, 0, length);
			packetLength = length;
		}

		/**
		 * Constructor used to create a new TCP packet (to create a TCP packet,
		 * which should be sent)
		 * 
		 * @param source_IpAddress
		 * @param destination_IpAddress
		 * @param source_port
		 * @param destination_port
		 * @param seq_nr
		 * @param ack_nr
		 * @param payload
		 */
		public TcpPacket(int source_IpAddress, int destination_IpAddress,
				int source_port, int destination_port, long seq_nr,
				long ack_nr, byte[] payload) {
			source_ip = source_IpAddress;
			destination_ip = destination_IpAddress;

			// check for valid source port
			if (!ConnectionUtils.isPortValid(source_port)) {
				throw new InvalidParameterException(
						source_port
								+ " now valid! Source port number only allowed between 0 and 65535.");
			}

			// check for valid destination port
			if (!ConnectionUtils.isPortValid(destination_port)) {
				throw new InvalidParameterException(
						destination_port
								+ " now valid! Destination port number only allowed between 0 and 65535.");
			}

			// check for valid seq_nr
			if (!ConnectionUtils.isSEQValid(seq_nr)) {

				throw new InvalidParameterException(
						"Wrong value for SEQ number!");
			}

			// check for valid ack_nr
			if (!ConnectionUtils.isSEQValid(ack_nr)) {
				throw new InvalidParameterException(
						"Wrong value for ACK number!");
			}

			if (payload != null && payload.length > MAX_PAYLOAD_LENGTH) {
				throw new InvalidParameterException(
						"Payload length is only allowed up to "
								+ MAX_PAYLOAD_LENGTH + " Bytes.");
			}

			// Reserve space for the TCP packet
			int payloadLength = 0;
			if (payload != null)
				payloadLength = payload.length;

			rawData = ByteBuffer.allocate(HEADER_SIZE + payloadLength);

			fillTcpPacket(source_port, destination_port, seq_nr, ack_nr,
					payload);
		}

		/**
		 * Fill the Tcp Packet with the given parameters and set the rest (e.g.
		 * ACK-Flag,...) to default values
		 * */
		private void fillTcpPacket(int source_port, int destination_port,
				long seq_nr, long ack_nr, byte[] payload) {
			// TODO: check whether the conversion (int to short) is done
			// correctly for e.g. 65535
			// AA: I think it's safer (and easier to control) converting to byte
			// array
			// TODO: check byte ordering (little endian vs. big endian): host
			// uses big endian
			byte[] raw_src_port = ByteBuffer.allocate(4).putInt(source_port)
					.array();
			byte[] raw_dest_port = ByteBuffer.allocate(4)
					.putInt(destination_port).array();
			byte[] raw_seq_nr = ByteBuffer.allocate(8).putLong(seq_nr).array();
			byte[] raw_ack_nr = ByteBuffer.allocate(8).putLong(ack_nr).array();

			// set the source_port, destination_port, seq_nr and ack_nr in the
			// rawData of the TCP packet
			// AA: because we are using big endian we ignore the first half of
			// each int/long when extracting short/int from them
			// (alternatively we could also shift <<2/<<4 and use
			// putShort/putInt)
			rawData.put(0, raw_src_port[2]);
			rawData.put(1, raw_src_port[3]);
			// rawData.putShort(0, (short)source_port);
			rawData.put(2, raw_dest_port[2]);
			rawData.put(3, raw_dest_port[3]);
			// rawData.putShort(2, (short)destination_port);
			rawData.put(4, raw_seq_nr[4]);
			rawData.put(5, raw_seq_nr[5]);
			rawData.put(6, raw_seq_nr[6]);
			rawData.put(7, raw_seq_nr[7]);
			// rawData.putInt(4, (int)seq_nr);
			rawData.put(8, raw_ack_nr[4]);
			rawData.put(9, raw_ack_nr[5]);
			rawData.put(10, raw_ack_nr[6]);
			rawData.put(11, raw_ack_nr[7]);
			// rawData.putInt(8, (int)ack_nr);
			// STATUS: 12 bytes set, 8 remaining in header

			/*
			 * TCP header length: in our case we have no Options, therefore the
			 * header has a fixed length of 5 32-bit words
			 */
			// STATUS: next byte is for DATA OFFSET, RESERVED and NS
			rawData.put(12, (byte) (5 << 4));

			// although follwing code has a bad performance, it is more readable
			int flags = 0 << 5 // urgent flag, which is always false in our case
			+ 0 << 4 // ack flag, which is 0 by default, but can be changed by
						// the method setACK_Flag
			+ 1 << 3 // push flag, which is always true in our case
			+ 0 << 2 // reset flag, which is always false in our case
			+ 0 << 1 // syn flag, which is 0 by default, but can be changed by
						// the method setSYN_Flag
			+ 0; // fin flag, which is 0 by default, but can be changed by the
					// method setFIN_Flag
			rawData.put(13, (byte) flags);

			// Window Size: since we implement the stop’n’go protocol, we ignore
			// the window size and set it to the maximum size
			// of one packet on all outgoing packets, which is 8KB = 8192 byte
			rawData.putShort(14, (short) 8192);

			// Checksum: is by default 0
			rawData.putShort(16, (short) 0);

			// Urgent pointer: is always 0, because we the urgent flag is not
			// supported
			rawData.putShort(18, (short) 0);

			// Set the payload
			int payloadLength = 0;
			if (payload != null) {
				payloadLength = payload.length;
				rawData.position(HEADER_SIZE);
				rawData.put(payload);
			}

			// the length of the TCP packet is the length of the payload plus
			// the 20 byte TCP header (in Bytes)
			packetLength = payloadLength + HEADER_SIZE;
		}

		/**
		 * Verify the checksum of a TCP packet and return the result (true if
		 * OK, otherwise false).
		 * 
		 * @return
		 */
		public boolean verifyChecksum() {
			return calculateChecksum(true);
		}

		/**
		 * if onlyVerify is false, then the checksum and is calculated and set
		 * it into the rawData of the TCP packet. Then true is returned. If
		 * onlyVerify is true, then the checksum is only verified and the result
		 * is returned (true if OK, otherwise false).
		 * 
		 * @param onlyVerify
		 * @return
		 */
		private boolean calculateChecksum(boolean onlyVerify) {
			if (!onlyVerify) {
				// set checksum to 0
				rawData.putShort(16, (short) 0);
			}

			byte[] buf = rawData.array();
			int leftSum = 0;
			int rightSum = 0;
			int sum = 0;

			// calculate the 16bit one´s complement sum for the left 8 bit and
			// the right 8 bit separately
			// byte is signed, therefore add "& 0xFF" to get an unsigned value
			for (int i = 0; i < packetLength - 1; i += 2) {
				leftSum += buf[i] & 0xFF;
				rightSum += buf[i + 1] & 0xFF;
			}

			// if packetLength contains odd bytes, add the last byte
			if ((packetLength & 1) != 0) {
				leftSum += buf[packetLength - 1] & 0xFF;
			}

			// form complete sum
			sum = (leftSum << 8) + rightSum;

			// ADD CHECKSUM OF PSEUDO HEADER
			// add source/destination IP address

			// following code is correct, if we would have the IP addresses in
			// big-endian (but unfortunately we have it in little-endian format)
			/*
			 * sum += source_ip & 0xFFFF; sum += (source_ip >>> 16) & 0xFFFF;
			 * sum += destination_ip & 0xFFFF; sum += (destination_ip >>> 16) &
			 * 0xFFFF;
			 */

			// for little-endian we transform it to big-endian
			sum += ((source_ip >>> 24) + ((source_ip & 0xFF0000) >>> 8)
					+ ((source_ip & 0xFF00) >>> 8) + ((source_ip & 0xFF) << 8));
			sum += ((destination_ip >>> 24)
					+ ((destination_ip & 0xFF0000) >>> 8)
					+ ((destination_ip & 0xFF00) >>> 8) + ((destination_ip & 0xFF) << 8));

			// add TCP protocol nr 6
			sum += IP.TCP_PROTOCOL;

			// add packet length
			sum += packetLength;

			// add overflow
			while ((sum >>> 16) != 0) {
				sum = (sum & 0xFFFF) + (sum >>> 16);
			}
			sum = ~((short) sum);

			if (!onlyVerify) {
				rawData.putShort(16, (short) sum);
				return true;
			}
			return sum == 0;
		}

		/**
		 * Calculates the checksum and returns the whole TCP packet as byte
		 * array
		 */
		public byte[] getByteArray() {

			// recompute the checksum
			this.calculateChecksum(false);

			return rawData.array();
		}

		/**
		 * Saves the payload of the package in the given variable 'payload'.
		 * 
		 * @param payload
		 */
		public void getPayload(byte[] payload) {
			int payloadLength = packetLength - HEADER_SIZE;
			for (int i = 0; i < payloadLength; i++) {
				payload[i] = rawData.get(HEADER_SIZE + i);
			}
		}

		/**
		 * @return the source port of the TCP packet
		 */
		public int getSourcePort() {
			return (rawData.getInt(0) >>> 16);
		}

		/**
		 * @return the destination port of the TCP packet
		 */
		public int getDestinationPort() {
			return (rawData.getInt(0) & 0xFFFF);
		}

		/**
		 * @return the SEQ number of the TCP packet
		 */
		public long getSEQNumber() {
			return (rawData.getLong(4) >>> 32);
		}

		/**
		 * @return the ACK number of the TCP packet
		 */
		public long getACKNumber() {
			return (rawData.getLong(8) >>> 32);
		}

		/**
		 * @return the length of the payload in Byte.
		 */
		public long getPayloadLength() {
			return packetLength - HEADER_SIZE;
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
		 * Set the PSH flag to true or to false should be true when sending data
		 * packets with payload (non-control packets)
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
		 * Set a flag_value at a specific position (e.g. position=1,
		 * new_flag_value=true means SYN=1)
		 * 
		 * @param position
		 * @param new_flag_value
		 */
		private void setFlag(int position, boolean new_flag_value) {
			// read all flags from the raw data
			byte flags = getFlags();

			// modify one flag
			if (new_flag_value)
				flags |= (1 << position); // combine disjunctive with 1 at
											// correct position
			else
				flags &= (0 << position); // combine conjunctive with 0 at
											// correct position

			// write all flags back to the raw data
			rawData.put(13, flags);
		}

		/**
		 * Check, whether this is a connection request (RST=0, SYN=1, FIN=0)
		 */
		public boolean isConnectRequest() {
			return (getFlags() & 7) == 2;
		}

		/**
		 * This method is only used for testing purposes
		 * 
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

		// TODO
		// private int TCB_BUF_SIZE = 8192;

		/** Our IP address. */
		int tcb_local_ip_addr;

		/** Their IP address. */
		int tcb_remote_ip_addr;

		/** Our port number. */
		int tcb_local_port;

		/** Their port number. */
		int tcb_remote_port;

		/**
		 * The SEQ number, which is used and increased during the creation of
		 * new TCP packets to send.
		 */
		long tcb_local_sequence_num;

		/**
		 * The SEQ number, which we expect them to acknowledge by an ACK packet.
		 */
		long tcb_local_expected_ack;

		/** The next SEQ number, which we expect from the other side. */
		long tcb_remote_next_expected_SEQ_num;

		/**
		 * The last SEQ number, which we expect from the other side. This is
		 * needed to verify the accepted receiving packet length.
		 */
		long tcb_remote_last_expected_SEQ_num;

		/** Static buffer for recv data. */
		byte tcb_data[];

		/** The undelivered data. */
		byte tcb_p_data[];

		/** Undelivered data bytes. */
		int tcb_data_left[];

		/** The current TCP connection state. */
		ConnectionState tcb_state;

		/**
		 * The communication side can be either 'SERVER' or 'CLIENT' (this is
		 * just a name for debugging purposes)
		 */
		String tcb_communication_side = "";

		/**
		 * Contains the last received (and successful verified) TCP packet. This
		 * is only needed for testing purposes.
		 */
		TcpPacket lastReceivedTcpPacket = null;

		/**
		 * Constructor called by client
		 */
		public TcpControlBlock() {
			tcb_state = ConnectionState.S_CLOSED;
			tcb_communication_side = "CLIENT";

			// SEQ and ACK are set to default values
			tcb_local_sequence_num = 0; // -1;
			tcb_local_expected_ack = 0; // -1;
		}

		/**
		 * Constructor called by server
		 * 
		 * @param local_ip
		 * @param local_port
		 * @throws IOException
		 */
		public TcpControlBlock(IpAddress local_ip, int local_port)
				throws IOException {
			// check IP address
			if (local_ip == null) {
				throw new IOException("Invalid empty IP address!");
			}
			tcb_local_ip_addr = local_ip.getAddress();

			// check local port
			if (!ConnectionUtils.isPortValid(local_port)) {
				throw new IOException("Invalid local port!");
			}

			tcb_communication_side = "SERVER";
			tcb_local_port = local_port;

			// SEQ and ACK are set to default values
			tcb_local_sequence_num = 0; // -1;
			tcb_local_expected_ack = 0; // -1;

			// set the connection status to LISTEN for the server
			tcb_state = ConnectionState.S_LISTEN;
		}

		/**
		 * Verifies a received Packet by checking the IP, port, SEQ/ACK number
		 * and returns a TcpPacket if everything is OK, otherwise null.
		 * 
		 * @param ipPacket
		 *            : the received IP packet
		 * @param acceptNewConnection
		 *            : this is needed when accepting a new connection at the
		 *            server, because only at that time the server knows the
		 *            address/port of the client, who connects
		 * @return a TcpPacket if everything is OK, otherwise null.
		 */
		public TcpPacket verifyReceivedPacket(IP.Packet ipPacket, boolean acceptNewConnection) {

			// set the remote_address and the remote port
			// (this is needed when accepting a new connection at the server,
			// because only at that time the server knows the address/port of
			// the client, who connects)
			if (acceptNewConnection) {
				tcb_remote_ip_addr = ipPacket.source;
			}

			// check, if the received IP packet has the expected IP addresses
			// this would also be detected by verifyChecksum of the TcpPacket,
			// but here we produce a better error message
			if (ipPacket.destination != tcb_local_ip_addr) {
				Logging.getInstance().LogTcpPacketError(
						this,
						"The received IP packet had the wrong destination IP address "
								+ "(expectedIP='"
								+ IpAddress.htoa(tcb_local_ip_addr) + "', "
								+ "packet-destination-address='"
								+ IpAddress.htoa(ipPacket.destination) + "')");
				return null;
			}
			if (ipPacket.source != tcb_remote_ip_addr) {
				Logging.getInstance().LogTcpPacketError(
						this,
						"The received IP packet had the wrong source IP address "
								+ "(expectedIP='"
								+ IpAddress.htoa(tcb_remote_ip_addr) + "', "
								+ "packet-source-address='"
								+ IpAddress.htoa(ipPacket.source) + "')");
				return null;
			}

			// verify protocol version
			if (ipPacket.protocol != 4) {	// "ipPacket.protocol != IP.Packet.IP_PROTOCOL_V4" threw a build error at the assignment submission
				Logging.getInstance().LogTcpPacketError(
						this,
						"The received IP packed had the wrong protocol version (expected='4', actual='"
								+ ipPacket.protocol + "').");
				return null;
			}

			TcpPacket tcpPacket = new TcpPacket(ipPacket.source,
					ipPacket.destination, ipPacket.data, ipPacket.length);

			// set the remote_address and the remote port
			// (this is needed when accepting a new connection at the server,
			// because only at that time the server knows the address/port of
			// the client, who connects)
			if (acceptNewConnection) {
				tcb_remote_port = tcpPacket.getSourcePort();
			}

			// TODO: verify ports (this would also be detected by verifyChecksum
			// of the TcpPacket, so it´s nice but not really necessary)

			if (!tcpPacket.verifyChecksum()) {
				Logging.getInstance().LogTcpPacketError(this,
						"The received TCP packet had the wrong checksum!");
				return null;
			}

			// VERIFY SEQ/ACK only of we are not in CLOSED or LISTEN state
			if (tcb_state != ConnectionState.S_CLOSED
					&& tcb_state != ConnectionState.S_LISTEN) {

				// don´t verify SEQ number during connection setup
				if (tcb_state != ConnectionState.S_SYN_SENT) {
					// Verify the SEQ number, which should be the
					// tcb_remote_next_expected_SEQ_num
					if (tcb_remote_next_expected_SEQ_num != tcpPacket
							.getSEQNumber()) {
						Logging.getInstance().LogTcpPacketError(
								this,
								"Wrong SEQ number. Expected was '"
										+ tcb_remote_next_expected_SEQ_num
										+ "', but was '"
										+ tcpPacket.getSEQNumber() + "'!");
						return null;
					}

					// Verify that the length of the packet is OK, that means
					// the tcb_remote_last_expected_SEQ_num <= SEQnumber +
					// payloadLength
					if (tcb_remote_last_expected_SEQ_num < tcpPacket
							.getSEQNumber() + tcpPacket.getPayloadLength()) {
						Logging.getInstance().LogTcpPacketError(
								this,
								"Packet too long. Expected last SEQ num should be <= '"
										+ tcb_remote_last_expected_SEQ_num
										+ "', but was '"
										+ tcpPacket.getSEQNumber()
										+ tcpPacket.getPayloadLength() + "'!");
						return null;
					}
				}

				// if the received packet is an ACK packet, we have to verify
				// the tcb_local_expected_ack
				if (tcpPacket.isACK_Flag()
						&& tcb_local_expected_ack != tcpPacket.getACKNumber()) {
					Logging.getInstance().LogTcpPacketError(
							this,
							"Wrong ACK number. Expected was '"
									+ tcb_local_expected_ack + "', but was '"
									+ tcpPacket.getACKNumber() + "'!");
					return null;
				}
			}

			lastReceivedTcpPacket = tcpPacket;
			return tcpPacket;
		}

		/**
		 * The method is called after a received TcpPacket was verified
		 * successfully. The method changes the state of the connection and the
		 * SEQ and/or ACK numbers of the TcpControlblock, depending on the
		 * current state and the flags in the received TCP packet.
		 * 
		 * @param tcpPacket
		 * @return true if the packet was an expected packet, otherwise false.
		 */
		public boolean acceptReceivedTcpPacket(TcpPacket tcpPacket) {

			boolean receivedPacketWasExpected = false;
			switch (tcb_state) {
			case S_CLOSED:
				Logging.getInstance().LogConnectionError(this, "Package was received while TCP was in CLOSED state!");
				break;

			case S_SYN_SENT:
				if (tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag()
						&& !tcpPacket.isFIN_Flag()) {
					receivedPacketWasExpected = true;
				} else {
					Logging.getInstance().LogTcpPacketError(this, "Expected SYN/ACK packet, but actual package had SYN=" + tcpPacket.isSYN_Flag() + ", ACK=" + tcpPacket.isACK_Flag() + ", FIN=" + tcpPacket.isFIN_Flag());
				}
				break;

			case S_LISTEN:
				if (tcpPacket.isSYN_Flag() && !tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
					// If we were in LISTEN state and received a valid SYN
					// package, we go to SYN_RCVD state and create a new
					// tcb_local_sequence_num
					tcb_state = ConnectionState.S_SYN_RCVD;
					
					receivedPacketWasExpected = true;
				} else {
					Logging.getInstance().LogTcpPacketError(this, "Expected SYN packet, but actual package had SYN=" + tcpPacket.isSYN_Flag() + ", ACK=" + tcpPacket.isACK_Flag() + ", FIN=" + tcpPacket.isFIN_Flag());
				}
				break;

			case S_SYN_RCVD:
				if (!tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
					// If we were in SYN_RCVD state and received a valid ACK
					// package, we go to ESTABLISHED state
					tcb_state = ConnectionState.S_ESTABLISHED;
					receivedPacketWasExpected = true;
				}
				// active close
				else if (tcpPacket.isFIN_Flag()) {
					// If we were in SYN_RCVD state and received a FIN package,
					// we go to FIN_WAIT_1 state
					tcb_state = ConnectionState.S_FIN_WAIT_1;

					receivedPacketWasExpected = true;
				} else {
					Logging.getInstance().LogTcpPacketError(
							this,
							"Expected ACK packet, but actual package had SYN="
									+ tcpPacket.isSYN_Flag() + ", ACK="
									+ tcpPacket.isACK_Flag() + ", FIN="
									+ tcpPacket.isFIN_Flag());
				}
				break;

			case S_ESTABLISHED:
				receivedPacketWasExpected = true;
				break;
			/*case S_FIN_WAIT_1:
				if (!tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag()) {
					receivedPacketWasExpected = true;
				}
				break;*/
			default:
				receivedPacketWasExpected = true;
				break;
			}
			

			if (receivedPacketWasExpected) {
				if (tcpPacket.isACK_Flag()) {

					tcb_local_expected_ack = tcpPacket.getACKNumber();
				}
				if (tcpPacket.isSYN_Flag() || tcpPacket.isFIN_Flag()) {
					// increase the sequence-numbers by 1
					tcb_remote_next_expected_SEQ_num = ConnectionUtils.getNextSequenceNumber(tcpPacket.getSEQNumber(), 1);
					tcb_remote_last_expected_SEQ_num = tcb_remote_next_expected_SEQ_num + TcpPacket.MAX_PAYLOAD_LENGTH;
				}
				else {
					tcb_remote_next_expected_SEQ_num = ConnectionUtils.getNextSequenceNumber(tcpPacket.getSEQNumber(), tcpPacket.getPayloadLength());
					tcb_remote_last_expected_SEQ_num = tcb_remote_next_expected_SEQ_num + TcpPacket.MAX_PAYLOAD_LENGTH;
				}
			}
			return receivedPacketWasExpected;

		}

		/**
		 * Returns a TcpPacket, which has all the flags set according to the
		 * current ConnectionState If setFIN is set, then the FIN-flag is set.
		 * If the packet could not be created, then null is returned (e.g. when
		 * you want to close a not-established connection)
		 * 
		 * @param buf
		 * @param offset
		 * @param len
		 * @param setFIN
		 * @return a new created TCP packet
		 */
		public TcpPacket createTcpPacket(byte[] buf, int offset, int len,
				boolean setFIN) {

			// sending data is only allowed, when the connection is in a
			// specific state
			// TODO: extend the possible cases (CLOSING connections) and return
			// null
			/*
			 * if (len > 0) { if (tcb_state == ConnectionState.S_CLOSED ||
			 * tcb_state == ConnectionState.S_LISTEN) {
			 * 
			 * Logging.getInstance().LogConnectionError(this,
			 * "You have to establish a connection first, before sending data is allowed!"
			 * ); return null; } }
			 */

			// set the SEQ/ACK before creating the TCP package
			switch (tcb_state) {
			case S_CLOSED:
				// SYN packet: create new SEQ number, set ACK-NR to 0
				tcb_local_sequence_num = ConnectionUtils.getNewSequenceNumber();
				tcb_remote_next_expected_SEQ_num = 0;
				break;
			case S_SYN_SENT:
				break;
			case S_SYN_RCVD:
				// create SYN/ACK package
				tcb_local_sequence_num = ConnectionUtils.getNewSequenceNumber();
				break;
			case S_TIME_WAIT:
			case S_CLOSE_WAIT:
				// create FIN/ACK packet
				break;
			case S_FIN_WAIT_1:
				// create FIN packet
				break;
			default:
				break;
			}

			TcpPacket next_packet = new TCP.TcpPacket(tcb_local_ip_addr, // local IP
					tcb_remote_ip_addr, // remote IP
					tcb_local_port, // local PORT
					tcb_remote_port, // remote PORT
					tcb_local_sequence_num, // SEQ number
					tcb_remote_next_expected_SEQ_num, // ACK number
					buf);

			// increase the tcb_local_sequence_num
			tcb_local_sequence_num = ConnectionUtils.getNextSequenceNumber(
					tcb_local_sequence_num, len);

			// set the FLAGS and the connection state after creating the TCP
			// package
			switch (tcb_state) {
			case S_CLOSED:
				// create SYN packet
				next_packet.setSYN_Flag(true);
				tcb_local_sequence_num = ConnectionUtils.getNextSequenceNumber(tcb_local_sequence_num, 1);
				tcb_local_expected_ack = tcb_local_sequence_num;
				
				// set connection state to SYN_SENT after successful sent a SYN packet
				tcb_state = ConnectionState.S_SYN_SENT;
				break;
			case S_SYN_SENT:
				// create ACK packet
				next_packet.setACK_Flag(true);
				
				// If we were in SYN_SENT state and create a new package, we are in Established state after sending it
				tcb_state = ConnectionState.S_ESTABLISHED;
				break;
			case S_SYN_RCVD:
				// create SYN/ACK packet
				next_packet.setSYN_Flag(true);
				next_packet.setACK_Flag(true);
				tcb_local_sequence_num = ConnectionUtils.getNextSequenceNumber(tcb_local_sequence_num, 1);
				tcb_local_expected_ack = tcb_local_sequence_num;
				break;
			case S_ESTABLISHED:
				tcb_local_expected_ack = tcb_local_sequence_num;
				break;
			case S_CLOSING:
			case S_CLOSE_WAIT:
				// ceate ACK packet for received FIN
				next_packet.setACK_Flag(true);
				break;
			case S_LAST_ACK:
			case S_FIN_WAIT_2:
				// create FIN packet
				next_packet.setFIN_Flag(true);
				break;
			case S_TIME_WAIT:
				// create ACK+FIN packet
				next_packet.setACK_Flag(true);
				next_packet.setFIN_Flag(true);
				break;
			}

			// check if closing connection is allowed and set FIN flag
			if (setFIN) {
				/*
				 * if (tcb_state != ConnectionState.S_ESTABLISHED) {
				 * Logging.getInstance().LogTcpPacketError(
				 * "Only established connection can be closed! (ConnectionState='"
				 * + tcb_state + "')."); return null; }
				 */
				next_packet.setFIN_Flag(true);
				tcb_local_sequence_num = ConnectionUtils.getNextSequenceNumber(tcb_local_sequence_num, 1);
				tcb_local_expected_ack = tcb_local_sequence_num;
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
				tcb_remote_next_expected_SEQ_num = 0;
				tcb_remote_last_expected_SEQ_num = 0;
				tcb_data = null;
				tcb_p_data = null;
				tcb_data_left = null;
				tcb_state = resetState;
				break;
			case S_LISTEN:
				tcb_state = resetState;
				break;
			}

		}

		/**
		 * Creates an IP packet out of the given TCP packet
		 * 
		 * @param tcpPacket
		 * @return
		 */
		public IP.Packet createIPPacket(TcpPacket tcpPacket) {
			byte[] ip_data = tcpPacket.getByteArray();
			IP.Packet ip = new IP.Packet(tcb_remote_ip_addr, 4, // TODO: IPv4
																// (should we
																// also support
																// IPv6?)
					1, // TODO: change this
					ip_data, ip_data.length);
			ip.source = tcb_local_ip_addr;
			return ip;
		}

		/**
		 * Creates an empty IP packet
		 * 
		 * @return
		 */
		public IP.Packet createIPPacket() {

			IP.Packet ip = new IP.Packet(tcb_remote_ip_addr, 4, // TODO: IPv4
																// (should we
																// also support
																// IPv6?)
					1, // TODO: change this
					new byte[] {}, 0);
			ip.source = tcb_local_ip_addr;
			return ip;
		}

		/**
		 * This method is only used for manipulation of the TcpControlBlock from
		 * outside for testing purposes.
		 * 
		 * @param ipAddress
		 */
		public void setRemoteIPAddressForTesting(int ipAddress) {
			tcb_remote_ip_addr = ipAddress;
		}

		/**
		 * This method is only used for manipulation of the TcpControlBlock from
		 * outside for testing purposes.
		 * 
		 * @param port
		 */
		public void setRemotePortForTesting(int port) {
			this.tcb_remote_port = port;
		}

		/**
		 * This method is only used for manipulation of the TcpControlBlock from
		 * outside for testing purposes.
		 * 
		 * @param connectionStatus
		 */
		public void setConnectionStateForTesting(
				ConnectionState connectionStatus) {
			tcb_state = connectionStatus;
		}

		/**
		 * This method is only used for check the state of the TcpControlBlock
		 * from outside for testing purposes.
		 */
		public ConnectionState getConnectionStateForTesting() {
			return tcb_state;
		}

		public void setLocalIPAddressForTesting(int ipAddress) {
			this.tcb_local_ip_addr = ipAddress;
		}

		public int getLocalIPAddressForTesting() {
			return this.tcb_local_ip_addr;
		}

		public void setLocalPortForTesting(int port) {
			this.tcb_local_port = port;
		}

		public int getLocalPortForTesting() {
			return this.tcb_local_port;
		}

		public void setLocalSeqForTesting(long seq_num) {
			this.tcb_local_sequence_num = seq_num;
		}

		public long getLocalSeqForTesting() {
			return this.tcb_local_sequence_num;
		}

		public void setRemoteLastSeqForTesting(long seq_num) {
			this.tcb_remote_last_expected_SEQ_num = seq_num;
		}

		public long getRemoteLastSeqForTesting() {
			return this.tcb_remote_last_expected_SEQ_num;
		}

		public void setRemoteNextSeqForTesting(long seq_num) {
			this.tcb_remote_next_expected_SEQ_num = seq_num;
		}

		public long getRemoteNextSeqForTesting() {
			return this.tcb_remote_next_expected_SEQ_num;
		}

		public void setDataForTesting(byte[] data) {
			this.tcb_data = data;
		}

		public byte[] getDataForTesting() {
			return this.tcb_data;
		}

		public void setRemoteDataLeftForTesting(int[] data_left) {
			this.tcb_data_left = data_left;
		}

		public int[] getRemoteDataLeftForTesting() {
			return this.tcb_data_left;
		}
	}

	/**
	 * Constructs a TCP stack for the given virtual address. The virtual address
	 * for this TCP stack is then 192.168.0.address.
	 * 
	 * @param address
	 *            The last octet of the virtual IP address 1-254.
	 * @throws IOException
	 *             if the IP stack fails to initialize.
	 */
	public TCP(int address) throws IOException {
		ip = new IP(address);
		// initialize some helper values
		ConnectionUtils.init();
	}

	/**
	 * @return a new socket for this stack
	 */
	public Socket socket() {
		return new Socket();
	}

	/**
	 * @return a new server socket for this stack bound to the given port
	 * @param port
	 *            the port to bind the socket to.
	 * @throws IOException
	 *             when an invalid socket is passed
	 */
	public Socket socket(int port) throws IOException {
		return new Socket(port);
	}

}
