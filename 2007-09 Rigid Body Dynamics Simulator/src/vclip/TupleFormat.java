package vclip;

/**
 * Contains format codes for scanning vectors and matrices.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>
 */
class TupleFormat {
	private char prefix = (char) -1;
	private char suffix = (char) -1;
	private char code;

	public TupleFormat(String fmt) throws IllegalArgumentException {
		set(fmt);
	}

	public TupleFormat(char prefix, char code, char suffix) {
		this.prefix = prefix;
		this.code = code;
		this.suffix = suffix;
	}

	public void set(String fmt) {
		int idx = 0;
		int len = fmt.length();

		if (idx < len && fmt.charAt(idx) != '%') {
			prefix = fmt.charAt(idx);
			idx++;
		}
		if (idx >= len || fmt.charAt(idx) != '%') {
			throw new IllegalArgumentException("Missing '%' character in format string");
		}
		idx++;
		if (idx == len) {
			throw new IllegalArgumentException("Premature end of format string");
		}
		code = fmt.charAt(idx++);
		if (idx < len) {
			suffix = fmt.charAt(idx);
		}
	}

	public char getPrefix() {
		return prefix;
	}

	public char getSuffix() {
		return suffix;
	}

	public char getCode() {
		return code;
	}
}
