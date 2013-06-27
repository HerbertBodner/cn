package nl.vu.cs.cn;

/**
 * This class contains some useful functions for checking ranges of different fields (port, SEQ/ACK number).
 * @author Herbert Bodner, Alexandru Assandei
 *
 */
public class ConnectionUtils {
	
	/** the maximum unsigned 16 bit value, which is 2^16 - 1. It´s used to check the upper border of the source_port and destination_port. */
	public static final int MAX16BIT_VALUE = 65535;			// AA: equivalent to Short.MAX_VALUE-Short.MIN_VALUE
	
	
	/** the maximum unsigned 32 bit value, which is 2^32 - 1. It´s used to check the upper border of the seq_nr and the ack_nr. */
	public static final long MAX32BIT_VALUE = 4294967295l; 	// AA: equivalent to Integer.MAX_VALUE-Integer.MIN_VALUE
	
	/** the timestamp (in ms) when a sequence number was last generated */
	public static long ISN_TIMESTAMP = 0;
	
	/** If this value has a value, which is not 0, then the method "getNewSequenceNumber" will return this value (used for testing purposes) */
	public static long SEQUENCE_NR_STARTVALUE_FOR_TESTING = 0;
	
	
	/**
	 * Initialize ISN_TIMESTAMP (initial sequence number timestamp) to current time
	 * @param
	 * @return
	 */
	public static void init() {
		ISN_TIMESTAMP = System.currentTimeMillis();
	}
	
	/**
	 * Returns true, if the given port is within a valid range.
	 * @param port
	 * @return
	 */
	public static boolean isPortValid(int port) {

		if (port < 0 || port > MAX16BIT_VALUE)
		{
			Logging.getInstance().LogTcpPacketError(null, "Destination port number only allowed between 0 and " + MAX16BIT_VALUE + ".");
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
			Logging.getInstance().LogTcpPacketError(null, "Acknowledgement number only allowed between 0 and " + MAX32BIT_VALUE + ".");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a new SEQ number, depending on the current time.
	 * @return a pseudo-random ISN based on a timer incremented every 4 microseconds
	 */
	public static long getNewSequenceNumber() {
		
		// for temporary testing (do not checkin this)
		//return 0;
		
		if (SEQUENCE_NR_STARTVALUE_FOR_TESTING != 0) {
			return SEQUENCE_NR_STARTVALUE_FOR_TESTING;
		}
		
		long stored_timestamp = ISN_TIMESTAMP;
		// update timestamp
		ISN_TIMESTAMP = System.currentTimeMillis();
		
		return (System.currentTimeMillis()-stored_timestamp)*4 % ConnectionUtils.MAX32BIT_VALUE;
	}
	
	/**
	 * Returns the next valid SEQ number, depending to the current SEQ and the size of the packet (in byte)
	 * @param currentSEQ
	 * @param amountOfBytes
	 * @return
	 */
	public static long getNextSequenceNumber(long currentSEQ, long amountOfBytes) {
		return (currentSEQ + amountOfBytes) % ConnectionUtils.MAX32BIT_VALUE;
	
	}
}
