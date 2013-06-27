package nl.vu.cs.cn;

/**
 * This enum represents the failure codes for the verifyReceivedPacket method 
 * of the control block
 *
 * @author Alexandru Asandei, Herbert Bodner;
 *
 */

public enum PacketVerifyFailure {
	F_UNDEFINED,
	F_CORRUPT,
	F_WRONG_IP,
	F_WRONG_PORT,
	F_WRONG_SEQ,
	F_WRONG_ACK,
	F_WRONG_PROTO
}
