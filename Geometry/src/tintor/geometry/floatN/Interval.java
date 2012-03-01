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

import tintor.geometry.GMath;
import tintor.util.Hash;

public final class Interval {
	// Constants
	public final static Interval Empty = new Interval(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);

	// Fields
	public final float min, max;

	// Constructors
	public Interval(final float min, final float max) {
		this.min = min;
		this.max = max;
	}

	public Interval(final Vector3 normal, final Vector3[] points) {
		float mini = Empty.min, maxi = Empty.max;
		for (final Vector3 p : points) {
			final float a = normal.dot(p);
			if (a > maxi) maxi = a;
			if (a < mini) mini = a;
		}
		min = mini;
		max = maxi;
	}

	// Operations
	public Interval include(final float a) {
		if (a > max) return new Interval(min, a);
		if (a < min) return new Interval(a, max);
		return this;
	}

	public Interval add(final float a) {
		return new Interval(min + a, max + a);
	}

	public Interval mul(final float a) {
		return new Interval(min * a, max * a);
	}

	public Interval intInterval() {
		return new Interval(GMath.floor(min), GMath.ceil(max));
	}

	public float width() {
		return max - min;
	}

	public float center() {
		return (max + min) / 2;
	}

	public Interval union(final Interval a) {
		return new Interval(Math.min(a.min, min), Math.max(a.max, max));
	}

	public Interval intersection(final Interval a) {
		return new Interval(Math.max(a.min, min), Math.min(a.max, max));
	}

	public float distance(final Interval b) {
		return (Math.abs(min - b.min + max - b.max) + min - max + b.min - b.max) / 2;
	}

	public boolean equals(final Interval a) {
		return min == a.min && max == a.max;
	}

	// From Object
	@Override public String toString() {
		return String.format("[%f %f]", min, max);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Interval && equals((Interval) o);
	}

	@Override public final int hashCode() {
		return Hash.hash(Interval.class.hashCode(), Hash.hash(min), Hash.hash(max));
	}
}