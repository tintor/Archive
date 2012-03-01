package vclip;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamTokenizer;

/**
 * Class to help parse input using a StreamTokenizer.
 */
class TokenScanner {

	static char EOF = '\004';
	static String EOFstring = String.valueOf(EOF);

	static String scanWord(StreamTokenizer stok) throws IOException {
		return scanWord(stok, null, false);
	}

	static String scanWord(StreamTokenizer stok, String specials, boolean returnNullOnError) throws IOException {
		stok.nextToken();
		if (stok.ttype == StreamTokenizer.TT_WORD) {
			return stok.sval;
		} else if (specials != null) {
			for (int i = 0; i < specials.length(); i++) {
				char c = specials.charAt(i);
				if (stok.ttype == c) {
					return String.valueOf(c);
				}
			}
		}
		if (returnNullOnError) {
			return null;
		} else if (stok.ttype == StreamTokenizer.TT_EOF) {
			throw new EOFException("EOF, line " + stok.lineno());
		} else {
			throw new IOException("Identifier or keyword expected, line " + stok.lineno());
		}
	}

	static boolean isExponent(String s) {
		int len = s.length();
		int numdigits = 0;
		int i = 0;
		char c;

		if (len < 2) {
			return false;
		}
		c = s.charAt(i++);
		if (c != 'e' && c != 'E') {
			return false;
		}
		c = s.charAt(i++);
		if (c == '+' || c == '-') {
			if (i == len) {
				return false;
			}
			c = s.charAt(i++);
		}
		while (Character.isDigit(c)) {
			numdigits++;
			if (i == len) {
				break;
			}
			c = s.charAt(i++);
		}
		if (numdigits == 0 || i < len) {
			return false;
		} else {
			return true;
		}
	}

	static double scanDouble(StreamTokenizer stok) throws IOException {
		stok.nextToken();
		if (stok.ttype == '+') {
			stok.nextToken();
		}
		if (stok.ttype == StreamTokenizer.TT_NUMBER) {
			double nval = stok.nval;
			int lineno = stok.lineno();
			stok.nextToken();
			if (stok.ttype == StreamTokenizer.TT_WORD && stok.lineno() == lineno && isExponent(stok.sval)) {
				nval = Double.parseDouble(String.valueOf(nval) + stok.sval);
			} else {
				stok.pushBack();
			}
			return nval;
		} else if (stok.ttype == StreamTokenizer.TT_WORD) {
			try {
				return Double.parseDouble(stok.sval);
			} catch (NumberFormatException e) { // will throw exception at end of routine
			}
		}
		stok.pushBack();
		throw new IOException("Expected double, line " + stok.lineno());
	}

	static double[] scanDoubles(StreamTokenizer stok, int n) throws IOException {
		double[] vals = new double[n];
		for (int i = 0; i < n; i++) {
			vals[i] = scanDouble(stok);
		}
		return vals;
	}
}
