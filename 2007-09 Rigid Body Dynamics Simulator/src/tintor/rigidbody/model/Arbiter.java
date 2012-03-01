package tintor.rigidbody.model;

import java.util.HashMap;
import java.util.Map;

import tintor.Pair;
import tintor.geometry.Vector3;

public class Arbiter {
	public Vector3 axis;
	public Vector3 impulse = Vector3.Zero;

	// TODO unroll hashmap
	private static final Map<Pair<Body, Body>, Arbiter> map = new HashMap<Pair<Body, Body>, Arbiter>();

	public static Arbiter get(final Body a, final Body b) {
		final Pair<Body, Body> p = a.id < b.id ? new Pair<Body, Body>(a, b) : new Pair<Body, Body>(b, a);

		final Arbiter d = map.get(p);
		if (d != null) return d;

		final Arbiter nd = new Arbiter();
		map.put(p, nd);
		return nd;
	}
}