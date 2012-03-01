package assembler.instruction;

public class Number {
	int ival;
	private boolean isNumber;
	private final static String binNumberPattern = "[01]+b";
	private final static String octNumberPattern = "0[0-7]+";
	private final static String decNumberPattern = "[0-9]+";
	private final static String hexNumberPattern = "(0x[0-9a-fA-F]+)|([0-9a-fA-F]+[hH])";

	public Number(String s) {
		// :)
		boolean negative = false;
		if(s.charAt(0) == '-') {
			negative = true;
			s = s.substring(1);
		}
		int len = s.length();
		int val = 0;

		int radix = 1;
		if(s.matches(binNumberPattern)) {
			radix = 2;
			len -= 1;
		}
		else if(s.matches(octNumberPattern))
			radix = 8;
		else if(s.matches(decNumberPattern))
			radix = 10;
		else if(s.matches(hexNumberPattern)) {
			radix = 16;
			if(s.charAt(1) == 'x') {
				s = s.substring(2);
				len -= 2;
			} // da podesim za lepo parsovanje u ova slucaja
			if(Character.toUpperCase(s.charAt(len - 1)) == 'H') {
				s.substring(0, len - 2);
				len -= 1;
			}
		}
		if(radix != 1) {
			for(int i = 0; i < len; i++)
				val = val * radix + Character.digit(s.charAt(i), radix);
			isNumber = true;
		} else
			isNumber = false;

		if(negative) val = -val;
		ival = val;
	}

	public boolean isNumber() {
		return isNumber;
	}

	public int val() {
		return ival;
	}
}
