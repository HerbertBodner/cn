package nl.vu.cs.cn;

import nl.vu.cs.cn.TCP.TcpControlBlock;


/**
 * This singleton class is used for logging several errors and warnings to LogCat
 * @author Herbert Bodner, Alexandru Assandei
 *
 */
public class Logging {
	
	// Singleton pattern
	private static Logging instance = null;
	public static Logging getInstance() {
		if (instance==null)
			instance = new Logging();
		return instance;
	}
	private Logging() {
		
	}
	
	
	
	private boolean detailedLogging = false;
	
	/**
	 * Switch on/off detailed logging (if it is on, then the current connection state with some additional information is written to LogCat) 
	 * @param detailedLoggingOn
	 */
	public void setDetailedLogging(boolean detailledLoggingOn) {
		detailedLogging = detailledLoggingOn;
	}
	
	/**
	 * Log a TCPPacket Error with a given msg to LogCat
	 * @param msg
	 */
	public void LogTcpPacketError(String msg) {
		Log(null, "TCPPacketError", msg);
	}
	
	/**
	 * Log a TCP Connection Error with a  given msg to LogCat
	 * @param msg
	 */
	public void LogConnectionError(TcpControlBlock control, String msg) {
		Log(control, "TCPConnectionError", msg);
	}
	
	/**
	 * Log a TCPPacket Information with a given msg to LogCat
	 * @param msg
	 */
	public void LogConnectionInformation(TcpControlBlock control, String msg) {
		Log(control, "TCPConnectionInformation", msg);
	}
	

	/**
	 * Writes the message with the given type and additional TcpControlBlock information to LogCat 
	 * @param control
	 * @param type
	 * @param msg
	 */
	private void Log(TcpControlBlock control, String type, String msg) {
		if (control != null && detailedLogging) {
			msg += "; ConnectionState=" + control.tcb_state 
					+ ", Local-SEQ=" + control.tcb_local_sequence_num 
					+ ", Remote-SEQ=" + control.tcb_remote_sequence_num 
					+ ", Expected-ACK=" + control.tcb_local_expected_ack; 
		}
		android.util.Log.w(type, msg);
	}
}
