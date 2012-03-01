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
package tintor.geometry.sandbox;

import java.util.ArrayList;
import java.util.List;

import tintor.geometry.Quaternion;
import tintor.geometry.Transform3;
import tintor.geometry.Vector2;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexPolyhedrons;

public class Polyhedron {
	// Public
	public Polyhedron(final Vector3 color, final Vector3[] vertices) {
		poly = ConvexPolyhedrons.make(vertices);
		for (final Polygon3 p : poly)
			p.color = color;
	}

	public static Polyhedron prism(final Vector3 color, final int k, final double r1, final double r2, final double height) {
		return new Polyhedron(color, ConvexPolyhedrons.prism(k, r1, r2, height));
	}

	public static Polyhedron prism(final Vector3 color, final Vector2[] poly1, final Vector2[] poly2, final double height) {
		return new Polyhedron(color, ConvexPolyhedrons.prism(poly1, poly2, height));
	}

	public Polyhedron transform(final Quaternion q) {
		if (q == Quaternion.Identity) return this;
		return new Polyhedron(Polyhedrons.transform(Polyhedrons.clone(poly), new Transform3(q, Vector3.Zero)));
	}

	public Polyhedron transform(final Transform3 t) {
		if (t == Transform3.Identity) return this;
		return new Polyhedron(Polyhedrons.transform(Polyhedrons.clone(poly), t));
	}

	public Polyhedron union(final Polyhedron p) {
		return new Polyhedron(Polyhedrons.union(poly, p.poly));
	}

	public Polyhedron intersect(final Polyhedron p) {
		return new Polyhedron(Polyhedrons.intersection(poly, p.poly));
	}

	public Polyhedron diff(final Polyhedron p) {
		return new Polyhedron(Polyhedrons.difference(poly, p.poly));
	}

	public Polygon3[][] splitIntoConvex() {
		final List<Polygon3[]> list = new ArrayList<Polygon3[]>();
		Polyhedrons.splitIntoConvex(poly, list);
		return list.toArray(new Polygon3[list.size()][]);
	}

	public boolean closed() {
		throw new RuntimeException();
		// za svaku ivicu postoje dva poligona
	}

	public double signedVolume() {
		return Polyhedrons.signedVolume(poly);
	}

	public void render() {
		Polyhedrons.render(poly);
	}

	// Private
	private final Polygon3[] poly;

	private Polyhedron(final Polygon3[] f) {
		poly = f;
	}
}