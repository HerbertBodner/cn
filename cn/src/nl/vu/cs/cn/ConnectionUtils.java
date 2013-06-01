package nl.vu.cs.cn;

/**
 * This class contains some useful functions for checking ranges of different fields (port, SEQ/ACK number)
 * @author Herbert Bodner, Alexandru Assandei
 *
 */
public class ConnectionUtils {
	
	/* the maximum unsigned 16 bit value, which is 2^16 - 1. It´s used to check the upper border of the source_port and destination_port. */
	static final int MAX16BIT_VALUE = 65535;			// AA: equivalent to Short.MAX_VALUE-Short.MIN_VALUE
	
	
	/* the maximum unsigned 32 bit value, which is 2^32 - 1. It´s used to check the upper border of the seq_nr and the ack_nr. */
	static final long MAX32BIT_VALUE = 4294967295l; 	// AA: equivalent to Integer.MAX_VALUE-Integer.MIN_VALUE
	
	
	/**
	 * Returns true, if the given port is within a valid range.
	 * @param port
	 * @return
	 */
	public static boolean isPortValid(int port) {

		if (port < 0 || port > MAX16BIT_VALUE)
		{
			Logging.getInstance().LogTcpPacketError("Destination port number only allowed between 0 and " + MAX16BIT_VALUE + ".");
			return false;
		}
		return true;
	}
	
	
	/**
	 * Returns true, if the given SEQ/ACK number is within a valid range.
	 * @param seqNr
	 * @return
	 */
	public static boolean isSEQValid(long seqNr) {
		
		if (seqNr < 0 || seqNr > MAX32BIT_VALUE)
		{
			Logging.getInstance().LogTcpPacketError("Acknowledgement number only allowed between 0 and " + MAX32BIT_VALUE + ".");
			return false;
		}
		return true;
	}
}
