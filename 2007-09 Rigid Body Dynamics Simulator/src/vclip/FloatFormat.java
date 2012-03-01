package vclip;

import java.text.DecimalFormat;
import java.text.FieldPosition;

class FloatFormat extends DecimalFormat {
	private int width;
	private int precision;

	private void parseFormatString(String s) {
		int i = 0;
		int k;
		int len;
		char c = ' ';

		precision = 0;
		width = 0;

		len = s.length();
		if (i < len && s.charAt(i) == '%') {
			i++;
		}
		k = i;
		while (i < len && Character.isDigit((c = s.charAt(i)))) {
			width = width * 10 + Character.getNumericValue(c);
			i++;
		}
		if (i == k) {
			throw new IllegalArgumentException("Missing field width in format string");
		}
		if (c == '.') {
			i++;
			k = i;
			while (i < len && Character.isDigit((c = s.charAt(i)))) {
				precision = precision * 10 + Character.getNumericValue(c);
				i++;
			}
			if (i == k) {
				throw new IllegalArgumentException("Missing precision following '.' in format string");
			}
		}
	}

	public static String format(String s, double val) {
		return new FloatFormat(s).format(val);
	}

	public FloatFormat(String s) throws IllegalArgumentException {
		super();
		parseFormatString(s);
		setMinimumFractionDigits(precision);
		setMaximumFractionDigits(precision);
		setGroupingUsed(false);
	}

	private void padWithBlanks(StringBuffer res, int offset, int n, FieldPosition pos) {
		char[] blanks = new char[n];
		for (int i = 0; i < n; i++) {
			blanks[i] = ' ';
		}
		res.insert(offset, blanks);
		pos.setBeginIndex(pos.getBeginIndex() + n);
		pos.setEndIndex(pos.getEndIndex() + n);
	}

	public StringBuffer format(double number, StringBuffer res, FieldPosition pos) {
		int offset = res.length();
		super.format(number, res, pos);
		int w = res.length() - offset;
		if (w < width) {
			padWithBlanks(res, offset, width - w, pos);
		}
		return res;
	}

	public StringBuffer format(long number, StringBuffer res, FieldPosition pos) {
		int offset = res.length();
		super.format(number, res, pos);
		int w = res.length() - offset;
		if (w < width) {
			padWithBlanks(res, offset, width - w, pos);
		}
		return res;
	}
}
