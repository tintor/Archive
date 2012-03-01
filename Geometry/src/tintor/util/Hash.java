/*
Copyright (C) 2007 Marko Tintor <tintor@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package tintor.util;

public class Hash {
	public static int hash(final boolean a) {
		return a ? 1 : 0;
	}

	public static int hash(final float a) {
		return Float.floatToIntBits(a);
	}

	public static int hash(final double a) {
		return hash(Double.doubleToLongBits(a));
	}

	public static int hash(final long a) {
		return (int) (a >> 32) * 37 ^ (int) a;
	}

	public static int hash(final int a, final int b) {
		return a + b * 211;
	}

	public static int hash(final Class<?> a, final int b, final int c) {
		return hash(a.hashCode(), b, c);
	}

	public static int hash(final int a, final int b, final int c) {
		return hash(hash(a, b), c);
	}

	public static int hash(final int a, final int b, final int c, final int d) {
		return hash(hash(a, b, c), d);
	}

	public static int hash(final int a, final int b, final int c, final int d, final int e) {
		return hash(hash(a, b, c, d), e);
	}
}