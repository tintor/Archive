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
import java.util.List;

import tintor.XArrays;
import tintor.geometry.Vector2;

public class ConvexHull2 {
	// FIXME buggy!
	public static List<Vector2> discover(final Vector2... vertices) {
		final Vector2[] ordered = XArrays.sort(vertices);
		final List<Vector2> hull = new ArrayList<Vector2>();
		for (int i = ordered.length - 1; i >= 0; i--)
			add(hull, ordered[i]);
		for (int i = 1; i < ordered.length; i++)
			add(hull, ordered[i]);
		hull.remove(hull.size() - 1);
		return hull;
	}

	private static void add(final List<Vector2> hull, final Vector2 a) {
		while (hull.size() >= 2 && hull.get(hull.size() - 1).side(hull.get(hull.size() - 2), a) >= 0)
			hull.remove(hull.size() - 1);
		hull.add(a);
	}

	public static void main(final String[] args) {
		System.out.println(discover(new Vector2(0, 0), new Vector2(1, 0), new Vector2(0, 1),
				new Vector2(0, -1), new Vector2(-1, 0), new Vector2(-1, -1), new Vector2(-1, 1)));
	}
}