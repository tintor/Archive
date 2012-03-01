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
package tintor.geometry.extended;

import java.util.ArrayList;

import tintor.geometry.Vector3;

@SuppressWarnings("serial") public final class VList extends ArrayList<Vector3> {
	public VList add(final double x, final double y, final double z) {
		add(new Vector3(x, y, z));
		return this;
	}

	public VList add3(final double x, final double y, final double z) {
		for (int a = -1; a <= 1; a += 2)
			for (int b = -1; b <= 1; b += 2)
				for (int c = -1; c <= 1; c += 2)
					add(a * x, b * y, c * z);
		return this;
	}

	public VList addc(final double x, final double y, final double z) {
		return add(x, y, z).add(y, z, x).add(z, x, y);
	}

	public VList addc2(final double x, double y, double z) {
		return addc(x, y, z).addc(x, y, -z).addc(x, -y, z).addc(x, -y, -z);
	}

	public VList addc3(double x, final double y, final double z) {
		return addc2(x, y, z).addc2(-x, y, z);
	}

	@Override public Vector3[] toArray() {
		return toArray(new Vector3[size()]);
	}
}