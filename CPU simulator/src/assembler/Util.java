package assembler;

public class Util {

	public static byte BIGBYTE(int a) {
		return (new Integer((a >> 8) % 0x100)).byteValue();
	}

	public static byte SMALLBYTE(int a) {
		return (new Integer(a % 0x100)).byteValue();
	}

	public static final char indirChar1 = '[';
	public static final char indirChar2 = ']';
	public static final char pcRelChar = '&';
	public static final char neposrChar = '#';
	public static final char commentChar = ';';

	public static String skipWhitespaces(String s) {
		int i = 0;
		if(s.length() == 0) return s;
		while((i < s.length()) && Character.isWhitespace(s.charAt(i)))
			i++;
		if(i > 0) s = s.substring(i);
		return s;
	}

	public static int findWhitespace(String s) {
		int i = 0;
		if(s.length() == 0) return 0;
		while(!Character.isWhitespace(s.charAt(i))) {
			i++;
			if(i == s.length()) return -1;
		}
		return i;
	}

	public static String removeWhitespaces(String s) {
		int i = 0;
		if(s.length() == 0) return s;
		while((i < s.length()) && Character.isWhitespace(s.charAt(i)))
			i++;
		int j = s.length() - 1;
		while(Character.isWhitespace(s.charAt(j)) && (j >= i))
			j--;
		if(j < i) return "";
		return s.substring(i, j + 1);
	}

	public static String removeComment(String s) {
		int pos;
		pos = s.indexOf(Util.commentChar);
		if(pos != -1) s = s.substring(0, pos);
		return s;
	}
}
