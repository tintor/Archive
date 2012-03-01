package tintor.geometry.sandbox;

import javax.media.opengl.GL;

import tintor.geometry.Plane3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public class TopoPolyhedron extends Topology {
	public class Vertex extends Topology.Vertex {
		public final Vector3 vector;

		public Vertex(final Vector3 v) {
			vector = v;
		}
	}

	public class Face extends Topology.Face {
		Plane3 plane;
		Vector3 color;

		public Face(final Vertex... v) {
			super(v);
			plane = new Plane3(v[0].vector, v[1].vector, v[2].vector);
		}

		public void render() {
			GLA.color(color);
			GLA.beginPolygon();
			GLA.normal(plane.normal);
			for (final Topology.Vertex v : vertices)
				GLA.vertex(((Vertex) v).vector);
			GLA.gl.glEnd();
		}
	}

	public TopoPolyhedron(final Vector3... w) {
		final Vertex a = new Vertex(w[0]);
		final Vertex b = new Vertex(w[1]);
		final Vertex c = new Vertex(w[2]);

		new Face(a, b, c);
		new Face(c, b, a);

		for (int i = 3; i < w.length; i++)
			add(w[i]);
	}

	public void add(@SuppressWarnings("unused") final Vector3 q) {
	// remove all faces such that f.plane.distance(q) > 0
	// if none removed continue
	// add face for each open edge and q
	}

	public void render() {
		// vertices
		GLA.gl.glPointSize(8);
		GLA.gl.glBegin(GL.GL_POINTS);
		for (final Topology.Vertex v : vertices)
			GLA.vertex(((Vertex) v).vector);
		GLA.gl.glEnd();
		GLA.gl.glPointSize(1);

		// faces
		for (final Topology.Face f : faces)
			((Face) f).render();
	}
}