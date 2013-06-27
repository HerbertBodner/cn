package nl.vu.cs.cn.test;

import nl.vu.cs.nc.test.PacketLossControl;
import android.test.AndroidTestCase;


public class UnreliableNetworkTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testT031SYNPacketLoss() {
		
		// SYN packet should be lost 1 time
		PacketLossControl.getInstance().SetSYNPacketLost(1);
		
		ClientServerTest.runClientServerCommunication();
	}

	
	
	public void testT032SYNACKPacketLoss() {
		
		// SYN/ACK packet should be lost 1 time
		PacketLossControl.getInstance().SetSYNACKPacketLost(1);
		
		ClientServerTest.runClientServerCommunication();
	}
	
	
	
	public void testT033ACKPacketLoss() {
		
		// ACK packet should be lost 1 times
		PacketLossControl.getInstance().SetACKPacketLost(1);
		
		ClientServerTest.runClientServerCommunication();
	}
		
		
		
		
	public void testT034FINSendingPacketLoss() {
		
		// Sending FIN packet should be lost 1 times
		PacketLossControl.getInstance().SetFINSendingPacketLost(1);
	
		ClientServerTest.runClientServerCommunication();
	}

	public void testT035FINReceivingPacketLoss() {
		
		// Receiving FIN packet should be lost 1 times
		PacketLossControl.getInstance().SetFINReceivingPacketLost(1);
	
		ClientServerTest.runClientServerCommunication();
	}
	
	public void testT036FINACKPacketLoss() {
		
		// FIN/ACK packet should be lost 1 times
		PacketLossControl.getInstance().SetFINACKPacketLost(1);
	
		ClientServerTest.runClientServerCommunication();
	}

	
	
}
