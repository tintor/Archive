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
package tintor.geometry.doubleN;

import tintor.util.Hash;

public final class Intervald {
	// Constants
	public final static Intervald Empty = new Intervald(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

	// Fields
	public final double min, max;

	// Constructors
	public Intervald(final double min, final double max) {
		this.min = min;
		this.max = max;
	}

	public Intervald(final Vector3d normal, final Vector3d[] points) {
		double mini = Empty.min, maxi = Empty.max;
		for (final Vector3d p : points) {
			final double a = normal.dot(p);
			if (a > maxi) maxi = a;
			if (a < mini) mini = a;
		}
		min = mini;
		max = maxi;
	}

	// Operations
	public Intervald include(final double a) {
		if (a > max) return new Intervald(min, a);
		if (a < min) return new Intervald(a, max);
		return this;
	}

	public Intervald add(final double a) {
		return new Intervald(min + a, max + a);
	}

	public Intervald mul(final double a) {
		return new Intervald(min * a, max * a);
	}

	public Intervald intInterval() {
		return new Intervald(Math.floor(min), Math.ceil(max));
	}

	public double width() {
		return max - min;
	}

	public double center() {
		return (max + min) / 2;
	}

	public Intervald union(final Intervald a) {
		return new Intervald(Math.min(a.min, min), Math.max(a.max, max));
	}

	public Intervald intersection(final Intervald a) {
		return new Intervald(Math.max(a.min, min), Math.min(a.max, max));
	}

	public double distance(final Intervald b) {
		return (Math.abs(min - b.min + max - b.max) + min - max + b.min - b.max) / 2;
	}

	public boolean equals(final Intervald a) {
		return min == a.min && max == a.max;
	}

	// From Object
	@Override public String toString() {
		return String.format("[%f %f]", min, max);
	}

	@Override public boolean equals(final Object o) {
		return o instanceof Intervald && equals((Intervald) o);
	}

	@Override public final int hashCode() {
		return Hash.hash(Intervald.class.hashCode(), Hash.hash(min), Hash.hash(max));
	}
}