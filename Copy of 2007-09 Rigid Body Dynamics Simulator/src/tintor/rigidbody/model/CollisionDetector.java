package tintor.rigidbody.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tintor.Timer;
import tintor.geometry.GMath;
import tintor.geometry.Plane3;

public abstract class CollisionDetector {
	static boolean newSAT = false;

	protected final List<Body> bodies = new ArrayList<Body>();
	protected final List<Plane3> planes = new ArrayList<Plane3>();
	protected final List<Contact> contacts = new ArrayList<Contact>();

	public final Timer timer = new Timer();

	public void add(final Body a) {
		bodies.add(a);
	}

	public void remove(@SuppressWarnings("unused") final Body a) {
		bodies.remove(a);
	}

	public void add(final Plane3 a) {
		planes.add(a);
	}

	public List<Body> bodies() {
		return Collections.unmodifiableList(bodies);
	}

	public List<Plane3> planes() {
		return Collections.unmodifiableList(planes);
	}

	public List<Contact> contacts() {
		return Collections.unmodifiableList(contacts);
	}

	public void run(final boolean randomize) {
		timer.restart();

		contacts.clear();
		broadPhase();
		for (final Body body : bodies)
			if (body.state == Body.State.Dynamic) for (final Plane3 plane : planes)
				if (newSAT) {
					// cheap bounding sphere test
				if (plane.distance(body.position()) > body.shape.radius) continue;

				if (SAT.contact(body, plane))
					contacts.add(new Contact(body, World.Space, SAT.axis, SAT.point, -SAT.dist, null));
			} else {
				final Contact c = Shape.findContact(body, plane);
				if (c != null) contacts.add(c);
			}

		timer.stop();

		if (randomize) Collections.shuffle(contacts);
	}

	protected abstract void broadPhase();

	protected final void narrowPhase(final Body a, final Body b) {
		if (a.state != Body.State.Dynamic && b.state != Body.State.Dynamic) return;

		if (newSAT) {
			// cheap bounding spheres test
			if (a.transform().v.distanceSquared(b.transform().v) > GMath.square(a.shape.radius + b.shape.radius))
				return;

			if (SAT.contact(a, b)) contacts.add(new Contact(a, b, SAT.axis, SAT.point, -SAT.dist, null));
		} else {
			final Contact c = Shape.findContact(a, b);
			if (c != null) contacts.add(c);
		}
	}

	protected void bruteForce() {
		for (int a = 0; a < bodies.size(); a++)
			for (int b = a + 1; b < bodies.size(); b++)
				narrowPhase(bodies.get(a), bodies.get(b));
	}
}