package nl.vu.cs.nc.test;

import nl.vu.cs.cn.ConnectionState;
import nl.vu.cs.cn.TCP.TcpPacket;



/*
 * This singleton class simulates an unreliable network
 * @author Herbert Bodner, Alexandru Assandei
 */
public class PacketLossControl {
	
	// Singleton pattern
	private static PacketLossControl instance = null;
	public static PacketLossControl getInstance() {
		if (instance==null)
			instance = new PacketLossControl();
		return instance;
	}
	private PacketLossControl() {
		
	}
	
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
	
	
	
	private int _amountOfLostSYNPackets=0;
	public void SetSYNPacketLost(int amountOfLostSYNPackets) {
		_amountOfLostSYNPackets = amountOfLostSYNPackets;
	}
	
	private int _amountOfLostSYNACKPackets=0;
	public void SetSYNACKPacketLost(int amountOfLostSYNACKPackets) {
		_amountOfLostSYNACKPackets = amountOfLostSYNACKPackets;
	}
	
	private int _amountOfLostACKPackets=0;
	public void SetACKPacketLost(int amountOfLostACKPackets) {
		_amountOfLostACKPackets = amountOfLostACKPackets;
	}	
	
	
	private int _amountOfLostFINSendingPackets=0;
	public void SetFINSendingPacketLost(int amountOfLostFINSendingPackets) {
		_amountOfLostFINSendingPackets = amountOfLostFINSendingPackets;
	}
	private int _amountOfLostFINReceivingPackets=0;
	public void SetFINReceivingPacketLost(int amountOfLostFINReceivingPackets) {
		_amountOfLostFINReceivingPackets = amountOfLostFINReceivingPackets;
	}
	
	private int _amountOfLostFINACKPackets=0;
	public void SetFINACKPacketLost(int amountOfLostFINACKPackets) {
		_amountOfLostFINACKPackets = amountOfLostFINACKPackets;
	}
}
