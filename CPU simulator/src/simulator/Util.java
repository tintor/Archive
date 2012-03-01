package simulator;

public class Util {
	public static char hex(int a) {
		switch(a) {
		case 0:
			return '0';
		case 1:
			return '1';
		case 2:
			return '2';
		case 3:
			return '3';
		case 4:
			return '4';
		case 5:
			return '5';
		case 6:
			return '6';
		case 7:
			return '7';
		case 8:
			return '8';
		case 9:
			return '9';
		case 10:
			return 'A';
		case 11:
			return 'B';
		case 12:
			return 'C';
		case 13:
			return 'D';
		case 14:
			return 'E';
		case 15:
			return 'F';
		default:
			throw new RuntimeException("" + a);
		}
	}

	//	public static String bytesToHex(byte[] a, int start, int length) {
	//		char[] c = new char[length*2];
	//		for(int i = 0; i < length; i++) {
	//			c[i*2] = hex((a >>> 4) & 0xF);
	//		}
	//	}

	public static String byteToHex(int a) {
		return new String(new char[] { hex((a >> 4) & 0xF), hex(a & 0xF) });
	}

	public static String shortToHex(int a) {
		return new String(new char[] { hex((a >> 12) & 0xF), hex((a >> 8) & 0xF), hex((a >> 4) & 0xF),
			hex(a & 0xF) });
	}

	static {
		assert byteToHex(0x4A).equals("4A");
	}
}