package tintor.rigidbody.model.solid;

import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Plane3;
import tintor.geometry.Side;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Contact;
import tintor.rigidbody.model.FeaturePair;
import tintor.rigidbody.model.solid.atom.Box;
import tintor.rigidbody.model.solid.atom.Convex;
import tintor.rigidbody.model.solid.atom.Sphere;
import tintor.util.NonCashingHashSet;

public final class CollisionPair extends FeaturePair {
	public final NonCashingHashSet<Contact> contacts;

	public Transform3 transformA, transformB;
	private boolean reversed;
	private Contact contact;

	public CollisionPair(final NonCashingHashSet<Contact> contacts) {
		this.contacts = contacts;
	}

	public final void init(final Body a, final Body b) {
		bodyA = a;
		bodyB = b;
		solidA = a.solid();
		solidB = b.solid();
		transformA = a.transform();
		transformB = b.transform();
		reversed = false;
	}

	public final void findContacts() {
		if (sphereTest()) solidA.findContacts(this);
	}

	public boolean sphereTest() {
		return transformA.v.distanceSquared(transformB.v) <= GMath.square(solidA.radius + solidB.radius + Side.eps);
	}

	public void none() {
		if (reversed) return;
		reversed = false;

		final Body b = bodyA;
		bodyA = bodyB;
		bodyB = b;

		final Solid s = solidA;
		solidA = solidB;
		solidB = s;

		final Transform3 t = transformA;
		transformA = transformB;
		transformB = t;

		solidA.findContacts(this);
	}

	public void poly(final Convex A) {
		if (solidB instanceof Convex)
			poly2poly(A);
		else if (solidB instanceof Box)
			poly2box(A);
		else if (solidB instanceof Sphere)
			poly2sphere(A);
		else
			none();
	}

	public void box(final Box A) {
		if (solidB instanceof Box)
			box2box(A);
		else if (solidB instanceof Sphere)
			box2sphere(A);
		else
			none();
	}

	public void sphere() {
		if (solidB instanceof Sphere)
			sphere2sphere();
		else
			none();
	}

	public void plane(final Plane3 plane) {
		final double dist = solidB.interval(plane.normal, transformB).min + plane.offset;
		if (dist > 0) return;

		createContact();
		contact.normal = plane.normal;
		contact.dist = dist;

		contact.points = ((Atom) solidB).intersection(plane);
		addContact(contact);

	}

	private void addContact(final Contact c) {
		c.point = Vector3.average(c.points);
		contacts.put(c).update(c);
	}

	private void createContact() {
		contact = new Contact();
		contact.bodyA = bodyA;
		contact.solidA = solidA;
		contact.bodyB = bodyB;
		contact.solidB = solidB;
	}

	private void initSAT() throws SeparatingAxis {
		// try previous separating axis if exists
		contact = contacts.get(this);
		if (contact != null && contact.dist > 0) candidate(contact.normal, null, null);

		createContact();
		contact.dist = -GMath.Infinity;
	}

	private void candidate(Vector3 normal, Interval a, Interval b) throws SeparatingAxis {
		if (a == null) a = contact.solidA.interval(normal, transformA);
		if (b == null) b = contact.solidB.interval(normal, transformB);

		double z = a.min + a.max - (b.min + b.max);
		if (z < 0) {
			z = -z;
			normal = normal.neg();
		}
		candidate(normal, (z - (a.max - a.min + b.max - b.min)) / 2);
	}

	private void candidate(final Vector3 normal, final double dist) throws SeparatingAxis {
		if (dist <= contact.dist) return;
		contact.dist = dist;
		contact.normal = normal;
		if (dist > 0) throw new SeparatingAxis();
	}

	public void box2box(final Box A) {
		try {
			initSAT();

			final Box B = (Box) solidB;

			final Vector3[] an = new Vector3[3];
			candidate(an[0] = transformA.m.colX(), A.x, null);
			candidate(an[1] = transformA.m.colY(), A.y, null);
			candidate(an[2] = transformA.m.colZ(), A.z, null);

			final Vector3[] bn = new Vector3[3];
			candidate(bn[0] = transformB.m.colX(), null, B.x);
			candidate(bn[1] = transformB.m.colY(), null, B.y);
			candidate(bn[2] = transformB.m.colZ(), null, B.z);

			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++) {
					final Vector3 n = an[i].cross(bn[i]).unit();
					if (n.isFinite()) candidate(n, null, null);
				}

			contact.points = Box.intersect(A, transformA, B, transformB);
			contact.point = Vector3.average(contact.points);

			addContact(contact);
		} catch (final SeparatingAxis e) {}
	}

	private void box2sphere(final Box A) {
		try {
			initSAT();

			final Vector3[] an = new Vector3[3];
			candidate(an[0] = transformA.applyV(Vector3.X), A.x, null);
			candidate(an[1] = transformA.applyV(Vector3.Y), A.y, null);
			candidate(an[2] = transformA.applyV(Vector3.Z), A.z, null);

			//				for (int i = 0; i < 3; i++) {
			//					Vector3 n = an[i].cross(bn[i]).unit();
			//					if (n.isFinite()) candidate(n, null, null);
			//				}

			// TODO contact.points =
			addContact(contact);
		} catch (final SeparatingAxis e) {}
	}

	private void poly2sphere(final Convex A) {
		try {
			initSAT();

			for (int i = 0; i < A.faces.length; i++)
				candidate(transformA.applyV(A.faces[i].plane.normal), A.intervals[i], null);

			for (final Line3 ea : A.edges) {
				final Line3 da = transformA.apply(ea);
				final Vector3 n = da.point(da.nearest(transformB.v)).direction(transformB.v);
				if (n.isFinite()) candidate(n, null, null);
			}

			//			Contact c = new Contact();
			//			c.dist = dist;
			//			c.normal = normal;
			// TODO set c.point and c.points
			addContact(contact);
		} catch (final SeparatingAxis e) {}
	}

	private void poly2poly(final Convex A) {
		try {
			initSAT();
			final Convex B = (Convex) solidB;

			for (int i = 0; i < A.faces.length; i++)
				candidate(transformA.applyV(A.faces[i].plane.normal), A.intervals[i], null);

			for (int i = 0; i < B.faces.length; i++)
				candidate(transformA.applyV(B.faces[i].plane.normal), null, B.intervals[i]);

			for (final Line3 ea : A.edges) {
				final Vector3 da = transformA.applyV(ea.direction());
				for (final Line3 eb : B.edges) {
					final Vector3 db = transformB.applyV(eb.direction());
					final Vector3 n = da.cross(db).unit();
					if (n.isFinite()) candidate(n, null, null);
				}
			}

			contact.points = Convex.intersect(A, transformA, B, transformB);
			addContact(contact);
		} catch (final SeparatingAxis e) {}
	}

	private void poly2box(final Convex A) {
		try {
			initSAT();
			final Box B = (Box) solidB;

			for (int i = 0; i < A.faces.length; i++)
				candidate(transformA.applyV(A.faces[i].plane.normal), A.intervals[i], null);

			final Vector3[] bn = new Vector3[3];
			candidate(bn[0] = transformB.applyV(Vector3.X), null, B.x);
			candidate(bn[1] = transformB.applyV(Vector3.Y), null, B.y);
			candidate(bn[2] = transformB.applyV(Vector3.Z), null, B.z);

			for (final Line3 ea : A.edges) {
				final Vector3 da = transformA.applyV(ea.direction());
				for (int j = 0; j < 3; j++) {
					final Vector3 db = transformB.applyV(bn[j]);
					final Vector3 n = da.cross(db).unit();
					if (n.isFinite()) candidate(n, null, null);
				}
			}

			// contact.points = Box.intersect(B, transformB, A, transformA);
			addContact(contact);
		} catch (final SeparatingAxis e) {}
	}

	private void sphere2sphere() {
		createContact();
		final Vector3 r = transformA.v.sub(transformB.v);

		// dist
		contact.dist = r.length() - solidA.radius - solidB.radius;
		assert contact.dist <= 0;

		// normal
		contact.normal = r.unit();
		if (!contact.normal.isFinite()) contact.normal = Vector3.X;

		// points
		contact.points = new Vector3[] { transformA.v.add(solidB.radius, contact.normal),
				transformB.v.sub(solidA.radius, contact.normal) };

		// point
		contact.point = Vector3.average(contact.points);

		addContact(contact);
	}
}

//	protected static List<Contact> cylinder2cylinder(Cylinder A, Transform3 transformA, Cylinder B,
//			Transform3 transformB) {
//		try {
//			initSAT(A, transformA, B, transformB);
//
//			Line3 lineA = transformA.apply(A.line);
//			Line3 lineB = transformB.apply(B.line);
//
//			Tuple2 nearest = lineA.nearest(lineB);
//			Vector3 r = lineA.point(nearest.x).sub(lineB.point(nearest.y));
//			double d = r.length();
//			candidate(r.div(d), d - A.r - B.r);
//
//			// TODO implement
//			// contact.dist =
//			// contact.normal =
//			contact.point = Vector3.average(contact.points);
//		} catch (SeparatingAxis e) {}
//		return Collections.singletonList(contact);
//	}