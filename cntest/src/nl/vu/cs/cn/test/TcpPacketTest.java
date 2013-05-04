package nl.vu.cs.cn.test;


import java.io.IOException;
import java.nio.ByteBuffer;



import android.test.AndroidTestCase;


import junit.framework.TestCase;
import nl.vu.cs.cn.*;
import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP.TcpPacket;

// AA: Section 2.4.1 says it should extend AndroidTestCase (for later)
public class TcpPacketTest extends AndroidTestCase {

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
	

	/*
	public void testTcpPacketCreationBeforeSend()
	{
		
		TCP tcp = null;
		
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
		
		
		TcpPacket packet = tcp.new TcpPacket(1, 2, 65534, 65535, 4294967294l, 4294967295l, new byte[] {});
		
		assertFalse(packet.isACK_Flag());
		assertFalse(packet.isRST_Flag());
		assertFalse(packet.isSYN_Flag());
		assertFalse(packet.isFIN_Flag());
		
	}
	*/
	
	public void testBasicTCPsendRecv() {
		TCP tcp1 = null;
		TCP tcp2 = null;
		
		IP ip_source, ip_dest;
		try {
			tcp1 = new TCP(1);
			tcp2 = new TCP(2);
			TCP.Socket sender = tcp1.socket();
			TCP.Socket receiver = tcp2.socket();
			
			byte[] buf = ByteBuffer.allocate(10).array();
			sender.write("test".getBytes(), 0, 4);
			receiver.read(buf, 0, 10);
			assertEquals("test".getBytes(), buf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.getMessage() + "\n\n" + e.getStackTrace());
			return;
		}
		
	}

	public void testTcpPacketCreationAfterReceive()
	{
		
	}

}
