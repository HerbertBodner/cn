package nl.vu.cs.nc.test;

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
	
	public boolean IsTcpPacketLost(TcpPacket tcpPacket) {
		if (_amountOfLostSYNPackets > 0 && tcpPacket.isSYN_Flag() && !tcpPacket.isACK_Flag()) {
			// loose SYN packet
			_amountOfLostSYNPackets--;
			return true;
		}
		if (_amountOfLostSYNACKPackets > 0 && tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag()) {
			// loose SYN/ACK packet
			_amountOfLostSYNACKPackets--;
			return true;
		}
		if (_amountOfLostACKPackets > 0 && !tcpPacket.isSYN_Flag() && tcpPacket.isACK_Flag()) {
			// loose SYN/ACK packet
			_amountOfLostACKPackets--;
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
}
