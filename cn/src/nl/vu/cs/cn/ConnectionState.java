package nl.vu.cs.cn;

/**
 * This enum represents a the connection states of a TCP connection
 *
 * @author Alexandru Asandei, Herbert Bodner;
 *
 */
public enum ConnectionState {
	S_CLOSED, 
	S_LISTEN, 
	S_SYN_SENT, 
	S_SYN_RCVD, 
	S_ESTABLISHED,
	S_FIN_WAIT_1, 
	S_FIN_WAIT_2, 
	S_CLOSE_WAIT,
	S_LAST_ACK,
	S_CLOSING,
	S_TIME_WAIT,
}
