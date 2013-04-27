package nl.vu.cs.cn.test;

import java.io.IOException;




import junit.framework.TestCase;

public class TcpPacketTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testABitAround() {
		int i = 0;
		assertEquals(0, i);
	}
	
	public void testTcpPacketCreation()
	{/*
		TCP tcp;
		IP ip_source, ip_dest;
		try {
			ip_source = new IP(1);
			ip_dest = new IP(2);
			tcp = new TCP(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Creation of TCP instance threw a IOException: " + e.getMessage() + "\n\n" + e.getStackTrace());
			return;
		}
		
		
		IpAddress source_ip = ip_source.getLocalAddress();
		IpAddress dest_ip = ip_dest.getLocalAddress();
		
		TcpPacket packet = tcp.new TcpPacket(source_ip.getAddress(), dest_ip.getAddress(), 65534, 65535, 4294967294l, 4294967295l, new byte[] {});
		
		assertFalse(packet.isACK_Flag());
		assertFalse(packet.isRST_Flag());
		assertFalse(packet.isSYN_Flag());
		assertFalse(packet.isFIN_Flag());*/
	}
}
