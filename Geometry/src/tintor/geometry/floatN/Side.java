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
package tintor.geometry.floatN;

public enum Side {
	Positive, Zero, Negative;

	public static final ThreadLocal<Float> eps = new ThreadLocal<Float>() {
		@Override protected Float initialValue() {
			return 0f;
		}
	};

	public static Side classify(final float a) {
		return classify(a, eps.get());
	}

	public static Side classifySqr(final float a) {
		final float e = eps.get();
		return classify(a, e * e);
	}

	public static Side classifyMax(final float... values) {
		boolean zero = false;
		for (final float v : values)
			switch (classify(v)) {
			case Positive:
				return Positive;
			case Zero:
				zero = true;
				break;
			case Negative:
			}
		return zero ? Zero : Negative;
	}

	public static Side classifyMin(final float... values) {
		boolean zero = false;
		for (final float v : values)
			switch (classify(v)) {
			case Negative:
				return Negative;
			case Zero:
				zero = true;
				break;
			case Positive:
			}
		return zero ? Zero : Positive;
	}

	public static Side classify(final float a, final float e) {
		if (a > e) return Positive;
		if (a < -e) return Negative;
		return Zero;
	}
}