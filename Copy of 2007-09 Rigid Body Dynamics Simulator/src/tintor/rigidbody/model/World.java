package tintor.rigidbody.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tintor.Timer;
import tintor.geometry.Plane3;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexPolyhedrons;
import tintor.rigidbody.model.collisiondetector.BruteForce;
import tintor.rigidbody.model.effector.SurfaceGravity;
import tintor.rigidbody.view.OrbitingCamera;

public class World {
	public static final Body Space = new Body(Vector3.Zero, Quaternion.Identity, Shape.box(1e5f, 1e5f, 1e5f), 1e10f);

	static {
		Space.sfriction = 100;
		Space.dfriction = 100;
		Space.elasticity = 0;
	}

	// Parameters
	public float timeStep = 0.01f;
	public boolean randomize = false;
	public boolean showTiming = false;

	public int impulseIterations = 40;
	public int forceIterations = 80;

	// Model
	public float time = 0;
	public Body pickBody;
	public OrbitingCamera camera = new OrbitingCamera();

	// Components
	public final List<Constraint> joints = new ArrayList<Constraint>();
	public final List<Effector> effectors = new ArrayList<Effector>();
	public final List<Sensor> sensors = new ArrayList<Sensor>();

	private final CollisionDetector detector;
	public final List<Body> bodies;
	public final List<Plane3> planes;
	public final List<Contact> contacts;

	public World() {
		this(new BruteForce());
	}

	public World(final CollisionDetector detector) {
		this.detector = detector;
		bodies = detector.bodies();
		planes = detector.planes();
		contacts = detector.contacts();
	}

	// Operations
	public void step(final int n) {
		timer.time = 0;
		detector.timer.time = 0;

		for (int i = 0; i < n; i++)
			step();

		if (showTiming) System.out.printf("physics: %s, detector: %s\n", timer, detector.timer);
		detector.run(randomize);
	}

	public Timer timer = new Timer();

	private void step() {
		// advance transforms
		//for (final Body b : detector.bodies)
		//	b.advanceTransforms(timeStep);

		// collision detection
		detector.run(randomize);

		// randomization
		if (randomize) Collections.shuffle(joints);

		timer.restart();

		// prepare constraints
		prepare();

		// process collisions
		for (int i = 0; i < impulseIterations; i++)
			processCollisions();

		// calculate external forces/torques for each body
		for (final Effector m : effectors)
			m.apply(this);

		// integrate velocities
		for (final Body b : detector.bodies)
			b.integrateVel(timeStep);

		// process contacts
		final float k = 1.0f / forceIterations;
		for (int i = 1; i <= forceIterations; i++)
			processContacts(i * k - 1); // e is lineary interpolated from -1+e to 0
		//processContacts(0); // e is lineary interpolated from -1+e to 0

		// correct positions
		correct();

		// integrate positions
		for (final Body b : detector.bodies)
			b.integratePos(timeStep);

		// update time
		time += timeStep;

		timer.stop();

		// update sensors
		for (final Sensor s : sensors)
			s.update();
	}

	private void prepare() {
		for (final Constraint c : joints)
			c.prepare(timeStep);
		for (final Constraint c : contacts)
			c.prepare(timeStep);
	}

	private void processCollisions() {
		for (final Constraint c : joints)
			c.processCollision();
		for (final Constraint c : contacts)
			c.processCollision();
	}

	private void processContacts(final float e) {
		for (final Constraint c : joints)
			c.processContact(e);
		for (final Constraint c : contacts)
			c.processContact(e);
	}

	private void correct() {
		for (final Constraint c : joints)
			c.correct(timeStep);
		for (final Constraint c : contacts)
			c.correct(timeStep);
	}

	public void add(final Body b) {
		detector.add(b);
	}

	public void remove(final Body b) {
		detector.remove(b);
	}

	public void add(final Plane3 a) {
		detector.add(a);
	}

	public void surface(final float height, final float gravity) {
		effectors.add(new SurfaceGravity(gravity));
		add(new Plane3(Vector3.Y, -height));
	}

	private final Shape nail = new Shape(ConvexPolyhedrons.pyramid(3, 0.05f, 0.08f));

	public Body addNail(final Vector3 pos) {
		final Body a = new Body(pos, Quaternion.Identity, nail, 1e10f);
		a.state = Body.State.Fixed;
		a.name = "nail";
		add(a);
		return a;
	}

	public Body bodyByName(final String name) {
		for (final Body b : bodies)
			if (name.equals(b.name)) return b;
		throw new RuntimeException();
	}

	public void keyDown(@SuppressWarnings("unused")
	final int key) {}

	public void keyUp(@SuppressWarnings("unused")
	final int key) {}

	//	public float potentialEnergy(final Body a) {
	//		// TODO calculate body potential energy from effectors
	//		//return gravity.dot(zero.sub(a.pos)) * a.mass();
	//		throw new RuntimeException();
	//	}

	//	/** If pressure is negative we get implosion! */
	//	public void explosion(final Vector3 position, final Vector3 velocity, final float pressure, final float minRadius,
	//			final float maxRadius) {
	//		throw new RuntimeException();
	//	}

	//	/** Return one body that intersects this sphere. */
	//	public Body sphereTest(final Vector3 position, final float radius) {
	//		throw new RuntimeException();
	//	}
	//
	//	static class RayHit {
	//		Body body;
	//		Vector3 normal;
	//		Vector3 point;
	//		float distance;
	//	}
	//
	//	public RayHit shootRay(final Ray3 ray) {
	//		throw new RuntimeException();
	//	}

	//private static Logger logger = Logger.getLogger(World.class);
}