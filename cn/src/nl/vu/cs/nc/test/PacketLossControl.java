package nl.vu.cs.nc.test;

import nl.vu.cs.cn.ConnectionState;
import nl.vu.cs.cn.TCP.TcpPacket;



/**
 * This singleton class simulates an unreliable network
 * @author Herbert Bodner, Alexandru Assandei
 */
public class PacketLossControl {
	
	/**
	 * Singleton pattern
	 */
	private static PacketLossControl instance = null;
	public static PacketLossControl getInstance() {
		if (instance==null)
			instance = new PacketLossControl();
		return instance;
	}
	private PacketLossControl() {
		
	}
	
	// following variables hold the amount of different types of packets, which should be lost.
	// e.g. if _amountOfLostSYNPackets is 1, then the method 'IsTcpPacketLost' returns true for the first packet, which has SYN=1 and afterwards decreases _amountOfLostSYNPackets by 1
	private int _amountOfLostSYNPackets=0;
	private int _amountOfLostSYNACKPackets=0;
	private int _amountOfLostACKPackets=0;
	private int _amountOfLostFINSendingPackets=0;
	private int _amountOfLostFINReceivingPackets=0;
	private int _amountOfLostFINACKPackets=0;
	
	
	/**
	 * This method is called before an IP packet is sent. 
	 * When the method returns true, the IP packet is NOT sent and therefore the loss of a IP packet can be simulated.
	 * @param tcpPacket
	 * @param connectionState
	 * @return
	 */
	public boolean IsTcpPacketLost(TcpPacket tcpPacket, ConnectionState connectionState) {
		if (_amountOfLostSYNPackets > 0 && tcpPacket.isSYN_Flag() && !tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
			// loose SYN packet
			_amountOfLostSYNPackets--;
			return true;
		}
		if (_amountOfLostSYNACKPackets > 0 && tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
			// loose SYN/ACK packet
			_amountOfLostSYNACKPackets--;
			return true;
		}
		if (_amountOfLostACKPackets > 0 && !tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag() && !tcpPacket.isFIN_Flag()) {
			// loose SYN/ACK packet
			_amountOfLostACKPackets--;
			return true;
		}
		
		if (connectionState == ConnectionState.S_FIN_WAIT_1 && _amountOfLostFINSendingPackets > 0 && !tcpPacket.isSYN_Flag() && !tcpPacket.isACK_Flag() && tcpPacket.isFIN_Flag()) {
			// loose FIN packet
			_amountOfLostFINSendingPackets--;
			return true;
		}
		if (connectionState == ConnectionState.S_LAST_ACK && _amountOfLostFINReceivingPackets > 0 && !tcpPacket.isSYN_Flag() && !tcpPacket.isACK_Flag() && tcpPacket.isFIN_Flag()) {
			// loose FIN packet
			_amountOfLostFINReceivingPackets--;
			return true;
		}
		if (_amountOfLostFINACKPackets > 0 && !tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag() && tcpPacket.isFIN_Flag()) {
			// loose FIN/ACK packet
			_amountOfLostFINACKPackets--;
			return true;
		}
		
		
		return false;
	}
	
	
	
	
	/**
	 * Specifies how many SYN packets should be lost.
	 * @param amountOfLostSYNPackets
	 */
	public void SetSYNPacketLost(int amountOfLostSYNPackets) {
		_amountOfLostSYNPackets = amountOfLostSYNPackets;
	}
	
	/**
	 * Specifies how many SYN-ACK packets should be lost.
	 * @param amountOfLostSYNACKPackets
	 */
	public void SetSYNACKPacketLost(int amountOfLostSYNACKPackets) {
		_amountOfLostSYNACKPackets = amountOfLostSYNACKPackets;
	}
	
	/**
	 * Specifies how many ACK packets should be lost.
	 * @param amountOfLostACKPackets
	 */
	public void SetACKPacketLost(int amountOfLostACKPackets) {
		_amountOfLostACKPackets = amountOfLostACKPackets;
	}	
	
	
	/**
	 * Specifies how many sending FIN packets should be lost.
	 * @param amountOfLostFINSendingPackets
	 */
	public void SetFINSendingPacketLost(int amountOfLostFINSendingPackets) {
		_amountOfLostFINSendingPackets = amountOfLostFINSendingPackets;
	}
	
	
	/**
	 * Specifies how many receiving FIN packets should be lost.
	 * @param amountOfLostFINReceivingPackets
	 */
	public void SetFINReceivingPacketLost(int amountOfLostFINReceivingPackets) {
		_amountOfLostFINReceivingPackets = amountOfLostFINReceivingPackets;
	}
	
	
	/**
	 * Specifies how many FIN-ACK packets should be lost.
	 * @param amountOfLostFINACKPackets
	 */
	public void SetFINACKPacketLost(int amountOfLostFINACKPackets) {
		_amountOfLostFINACKPackets = amountOfLostFINACKPackets;
	}
}
