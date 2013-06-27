package nl.vu.cs.cn.test;


import java.io.IOException;

import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.TCP;
import nl.vu.cs.cn.TCP.TcpPacket;
import android.test.AndroidTestCase;


import junit.framework.Assert;

public class TcpPacketTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	public static String bytArrayToHex(byte[] a) {
	   StringBuilder sb = new StringBuilder();
	   for(byte b: a)
	      sb.append(String.format("%02x", b&0xff));
	   return sb.toString();
	}
	
	
	
	public void testT011ReceivedTcpHeaderChecksum() {
		TCP tcp = null;
		try {
			tcp = new TCP(1);
		} catch (IOException e) {
			Assert.fail("Exception during creation of TCP: " + e.getMessage());
		}
		
		// hexString copied from wireshark (empty ACK package) from specific source/destination IP
		String hexString = "dd810050334b9c1f38e0f8b65010ff37c4d20000";
		
		
		IpAddress sourceIp = IpAddress.getAddress("192.168.1.12");
		IpAddress destinationIp = IpAddress.getAddress("65.55.10.11");
		byte[] tcpData = hexStringToByteArray(hexString);
		
		TcpPacket tcpPacket = tcp.new TcpPacket(sourceIp.getAddress(), destinationIp.getAddress(), tcpData, tcpData.length);
		
		assertTrue(tcpPacket.isACK_Flag());
		assertFalse(tcpPacket.isFIN_Flag());
		assertFalse(tcpPacket.isPSH_Flag());
		assertFalse(tcpPacket.isSYN_Flag());
		assertTrue(tcpPacket.verifyChecksum());
	}
	
	
	public void testT012CreatedTcpHeaderChecksum() {
		
		TCP tcp = null;
		try {
			tcp = new TCP(1);
		} catch (IOException e) {
			Assert.fail("Exception during creation of TCP: " + e.getMessage());
		}
		
		// hexString copied from wireshark ("GET / HTTP/1.1 ....") in Hex code
		String expectedHeaderHexString = "dd8c0050e8106bb5fd72b0da501810c2b7f40000";
		String expectedPayloadHexString = "474554202f20485454502f312e310d0a486f73743a20676f6f676c652e61740d0a436f6e6e656374696f6e3a206b6565702d616c6976650d0a4163636570743a20746578742f68746d6c2c6170706c69636174696f6e2f7868746d6c2b786d6c2c6170706c69636174696f6e2f786d6c3b713d302e392c2a2f2a3b713d302e380d0a557365722d4167656e743a204d6f7a696c6c612f352e30202857696e646f7773204e5420362e313b20574f57363429204170706c655765624b69742f3533372e333620284b48544d4c2c206c696b65204765636b6f29204368726f6d652f32372e302e313435332e3934205361666172692f3533372e33360d0a582d4368726f6d652d566172696174696f6e733a20434d2b3179514549694c624a4151695974736b42434b4b3279514549714c624a4151697074736b42434c4332795145492b6f504b4151694f684d6f42434d53467967453d0d0a4163636570742d456e636f64696e673a20677a69702c6465666c6174652c736463680d0a4163636570742d4c616e67756167653a20656e2d55532c656e3b713d302e382c64652d44453b713d302e362c64653b713d302e340d0a436f6f6b69653a20505245463d49443d353363626537613534363735303931313a553d346334363061383366356364303565633a4c443d64653a544d3d313335383235373936393a4c4d3d313335383235373938353a533d396b724f6c64784f5062454a6e66486e3b20485349443d413568666139714f447742534a685755753b204150495349443d73584b734f773866475f5f67456354642f416e4a75424d576c46685f6d576f6c43473b204e49443d36373d4737487542764151664b774e35364552784c4d4e6d5f574154314138416c6e4b726645704458597971354b48687a69585656444c78574a54664e783871516c7353566c35675f714d337362564a334c346e48694734365f324652502d42564258667572625f63796b6858715a57785841574c3547594b446d676c7a2d4b4b517755575a76714b454f5349637a652d46355143545072783935797a7767557a6d6d3267395a7a4846414f66464259526268676c452d397a76315f3679332d6b574e584531563b205349443d44514141414d59414141425059396b57784c736f64487142547767546a744f7a645258715f4d4d372d4f4c542d30445f547773757a48505f694a6153505472464258436c495451555670354b334e616357464d47384c4867394c2d4d6245556a4e64743359324b4e6c326f686943764c563068523663456a4269317639385a4c316b6e79474368795a45725167417030796c5a6e746f6457425366746852777a3876775f664636394f4963564477774a47685f4a724f79595f4559417067617a444255484675386d7037587a514473426a4f4d6246386451363338624645714366597a364d4f4f354b6c434378565f462d4e657241593933466c494e6f4278707969777661554f76664e507751464a4934387a5f75796f49357939714f734e700d0a0d0a";
		byte[] payload = hexStringToByteArray(expectedPayloadHexString);
		
		
		IpAddress sourceIp = IpAddress.getAddress("192.168.1.12");
		IpAddress destinationIp = IpAddress.getAddress("173.194.66.94");
		TcpPacket tcpPacket = tcp.new TcpPacket(sourceIp.getAddress(), destinationIp.getAddress(), 56716, 80, 3893390261l, 4252152026l, payload);
		tcpPacket.setPSH_Flag(true);
		tcpPacket.setACK_Flag(true);
		tcpPacket.setWindowSize((short)4290);

		
		String expectedHexString = expectedHeaderHexString + expectedPayloadHexString;
		String actualHexString = bytArrayToHex(tcpPacket.getByteArray());
		
		// byte array of created package (=actualHexString) must be the same as the given Header plus the given payload (=expectedHexString)
		assertEquals(expectedHexString, actualHexString);
	}
	


	/*
	public void testT013BasicTCPSendRecv() {
		TCP tcp1 = null;
		TCP tcp2 = null;
		
		try {
			tcp1 = new TCP(1);
			tcp2 = new TCP(2);
			TCP.Socket sender = tcp1.socket();
			TCP.Socket receiver = tcp2.socket(80);
			
			// set the remote IP address for the sender (this has to be done manually, because there is no connection setup)
			sender.getTcpControlBlock().setRemoteIPAddressForTesting(IpAddress.getAddress("192.168.0.2").getAddress());
			receiver.getTcpControlBlock().setRemoteIPAddressForTesting(IpAddress.getAddress("192.168.0.1").getAddress());
			
			// set the connectionState to ESTABLISHED
			sender.getTcpControlBlock().setConnectionStateForTesting(ConnectionState.S_ESTABLISHED);
			receiver.getTcpControlBlock().setConnectionStateForTesting(ConnectionState.S_ESTABLISHED);
			
			byte[] payload = "test".getBytes();
			byte[] buf = ByteBuffer.allocate(4).array();
			sender.write(payload, 0, 4);
			receiver.read(buf, 0, 10);
			
			for(int i = 0; i<payload.length; i++)
				assertEquals(payload[i], buf[i]);
			
		} catch (Exception e) {
			fail(e.getMessage() + "\n\n" + e.getStackTrace());
		}
		
		
	}*/
	
}