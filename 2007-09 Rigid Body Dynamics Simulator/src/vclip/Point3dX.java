package vclip;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.NumberFormat;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

/**
 * Implements some useful extensions to the class Point3d.
 *
 * @author <a href="http://www.cs.ubc.ca/~lloyd">John E. Lloyd</a>
 */
class Point3dX extends Point3d implements java.io.Serializable {
	//	Tuple3dIO tupleIO = null;

	//  	public static PrintfFormat defaultPrintfFormat =
	//  	   new PrintfFormat ("%9.4f");

	public Point3dX() {
		super();
	}

	public Point3dX(double x, double y, double z) {
		super(x, y, z);
	}

	public Point3dX(Tuple3d tup) {
		super(tup.x, tup.y, tup.z);
	}

	public double dot(Tuple3d tup) {
		return x * tup.x + y * tup.y + z * tup.z;
	}

	/**
	 * Gets a specific element of the vector.
	 *
	 * @param index element to get (0 for x, 1 for y, 2 for z).
	 * @return element's value
	 */
	public double get(int i) {
		switch (i) {
		case 0: {
			return x;
		}
		case 1: {
			return y;
		}
		case 2: {
			return z;
		}
		default: {
			throw new IndexOutOfBoundsException(new Integer(i).toString());
		}
		}
	}

	/**
	 * Sets a specific element of the vector.
	 *
	 * @param index element to set (0 for x, 1 for y, 2 for z).
	 * @param value value to set the element to.
	 */
	public void set(int i, double value) {
		switch (i) {
		case 0: {
			x = value;
			break;
		}
		case 1: {
			y = value;
			break;
		}
		case 2: {
			z = value;
			break;
		}
		default: {
			throw new IndexOutOfBoundsException(new Integer(i).toString());
		}
		}
	}

	/**
	 * Sorts the contents of the vector by the absolute value
	 * of its components.
	 */
	public void sortAbsolute() {
		double absx = (x < 0 ? -x : x);
		double absy = (y < 0 ? -y : y);
		double absz = (z < 0 ? -z : z);
		double tmp;

		if (absx >= absy) {
			if (absy >= absz) { // output x, y, z
				// nothing to do
			} else if (absx >= absz) { // output x, z, y
				tmp = y;
				y = z;
				z = tmp;
			} else { // ouput z, x, y
				tmp = x;
				x = z;
				z = y;
				y = tmp;
			}
		} else {
			if (absx >= absz) { // output y, x, z
				tmp = x;
				x = y;
				y = tmp;
			} else if (absy >= absz) { // output y, z, x
				tmp = x;
				x = y;
				y = z;
				z = tmp;
			} else { // output z, y, x
				tmp = x;
				x = z;
				z = tmp;
			}
		}
	}

	/**
	 * Returns the index (0, 1, or 2) of the element of v with the largest
	 * absolute value.
	 *
	 * @return index
	 */
	public int maxAbsIndex() {
		double absx = (x < 0 ? -x : x);
		double absy = (y < 0 ? -y : y);
		double absz = (z < 0 ? -z : z);

		if (absx >= absy) {
			return (absx >= absz) ? 0 : 2;
		} else {
			return (absy >= absz) ? 1 : 2;
		}
	}

	/**
	 * Returns the index (0, 1, or 2) of the element of v with the smallest
	 * absolute value.
	 *
	 * @return index
	 */
	public int minAbsIndex() {
		double absx = (x < 0 ? -x : x);
		double absy = (y < 0 ? -y : y);
		double absz = (z < 0 ? -z : z);

		if (absx <= absy) {
			return (absx <= absz) ? 0 : 2;
		} else {
			return (absy <= absz) ? 1 : 2;
		}
	}

	/**
	 * Sorts the contents of the vector by the value
	 * of its components.
	 */
	public void sort() {
		double tmp;

		if (x >= y) {
			if (y >= z) { // output x, y, z
				// nothing to do
			} else if (x >= z) { // output x, z, y
				tmp = y;
				y = z;
				z = tmp;
			} else { // ouput z, x, y
				tmp = x;
				x = z;
				z = y;
				y = tmp;
			}
		} else {
			if (x >= z) { // output y, x, z
				tmp = x;
				x = y;
				y = tmp;
			} else if (y >= z) { // output y, z, x
				tmp = x;
				x = y;
				y = z;
				z = tmp;
			} else { // output z, y, x
				tmp = x;
				x = z;
				z = tmp;
			}
		}
	}

	/**
	 * Returns the index (0, 1, or 2) of the element of v with the largest
	 * value.
	 *
	 * @return index
	 */
	public int maxIndex() {
		if (x >= y) {
			return (x >= z) ? 0 : 2;
		} else {
			return (y >= z) ? 1 : 2;
		}
	}

	/**
	 * Returns the index (0, 1, or 2) of the element of v with the smallest
	 * value.
	 *
	 * @return index
	 */
	public int minIndex() {
		if (x <= y) {
			return (x <= z) ? 0 : 2;
		} else {
			return (y <= z) ? 1 : 2;
		}
	}

	//  	public String sprintf ()
	//  	 { return sprintf (defaultPrintfFormat); 
	//  	 }

	//  	public String sprintf (String s)
	//  	 { return sprintf (new PrintfFormat (s));
	//  	 }

	//  	public String sprintf (PrintfFormat fmt)
	//  	 { 
	//  	   if (tupleIO == null)
	//  	    { tupleIO = new Tuple3dIO(); 
	//  	    }
	//  	   return tupleIO.sprintf (fmt, this);
	//  	 }

	public void scan(String s, Reader r) throws IOException, IllegalArgumentException {
		scan(new TupleFormat(s), r);
	}

	public void scan(String s, StreamTokenizer stok) throws IOException, IllegalArgumentException {
		scan(new TupleFormat(s), stok);
	}

	public void scan(TupleFormat fmt, Reader r) throws IOException, IllegalArgumentException {
		scan(fmt, new StreamTokenizer(r));
	}

	public void scan(TupleFormat fmt, StreamTokenizer stok) throws IOException, IllegalArgumentException {
		int c;

		if ((c = fmt.getPrefix()) != (char) -1) {
			stok.nextToken();
			if (stok.ttype != c) {
				stok.pushBack();
				new IOException("'" + (char) c + "' expected, line " + stok.lineno());
			}
		}
		for (int i = 0; i < 3; i++) {
			double d = TokenScanner.scanDouble(stok);
			//  	      if (stok.ttype == '+')
			//  	       { stok.nextToken();
			//  	       }
			//  	      if (stok.ttype != StreamTokenizer.TT_NUMBER)
			//  	       { stok.pushBack();
			//  		 throw new IOException (
			//  "Expected double, line " + stok.lineno());
			//  	       }
			set(i, d);
		}
		if ((c = fmt.getSuffix()) != (char) -1) {
			stok.nextToken();
			if (stok.ttype != c) {
				stok.pushBack();
				new IOException("'" + (char) c + "' expected, line " + stok.lineno());
			}
		}
	}

	public String toString(NumberFormat fmt) {
		return fmt.format(x) + " " + fmt.format(y) + " " + fmt.format(z);
	}

	public String toString(String fmtStr) {
		return toString(new FloatFormat(fmtStr));
	}
}
