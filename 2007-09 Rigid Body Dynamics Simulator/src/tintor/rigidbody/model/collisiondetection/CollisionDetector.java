package tintor.rigidbody.model.collisiondetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tintor.Timer;
import tintor.patterns.Strategy;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Contact;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.collisiondetection.algorithms.BruteForce;

@Strategy public class CollisionDetector {
	// Classes
	public static class BroadPhase {
		protected transient CollisionDetector detector;

		protected void init() {}

		protected void add(final Body b) {}

		protected void remove(final Body b) {}

		protected void broadPhase() {}
	}

	// Constants
	public static final BroadPhase DefaultBroadPhase = new BruteForce();

	// Private fields
	private final ArrayList<Body> _bodies = new ArrayList<Body>();
	private BroadPhase algorithm = DefaultBroadPhase;

	// Public fields
	public transient final Timer timer = new Timer();
	public transient final List<Body> bodies = Collections.unmodifiableList(_bodies);
	//	public final NonCashingHashSet<Contact> contacts = new NonCashingHashSet<Contact>();
	public final List<Contact> contacts = new ArrayList<Contact>();

	// Operations
	public CollisionDetector() {
		algorithm.detector = this;
	}

	public void init() {
		algorithm.init();
	}

	public void add(final Body body) {
		_bodies.add(body);
		algorithm.add(body);
	}

	public void remove(final Body body) {
		_bodies.add(body);
		algorithm.remove(body);
	}

	public final void reset() {
		algorithm = DefaultBroadPhase;
		algorithm.detector = this;
		contacts.clear();
		_bodies.clear();
		timer.time = 0;
	}

	public void broadPhase(BroadPhase a) {
		if (a == null) a = new BroadPhase();
		algorithm = a;
		algorithm.detector = this;
		init();
	}

	public final void run() {
		// Start timer
		timer.restart();

		//		// Mark all contacts as destroyed
		//		for (final Contact c : contacts)
		//			c.status = Contact.Status.Destroyed;
		contacts.clear();

		// Run custom broad phase
		algorithm.broadPhase();

		// Iterate contacts and remove destroyed
		//		final Iterator<Contact> i = contacts.iterator();
		//		while (i.hasNext()) {
		//			final Contact c = i.next();
		//			if (event != null) event.contactEvent(c);
		//			if (c.status == Contact.Status.Destroyed) i.remove();
		//		}

		// Stop timer
		timer.stop();
	}

	public final void narrowPhase(final Body a, final Body b) {
		if (a == b || a.state == Body.State.Fixed && b.state == Body.State.Fixed) return;
		// NOTE sortiraj to broju ivica prvo / malo ubrzava SAT
		final Contact c = a.hashCode() < b.hashCode() ? Shape.findContact(a, b) : Shape.findContact(b, a);
		if (c != null) contacts.add(c);
	}
}