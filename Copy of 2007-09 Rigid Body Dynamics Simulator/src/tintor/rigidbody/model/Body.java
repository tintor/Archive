package tintor.rigidbody.model;

import tintor.geometry.GMath;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Quaternion;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public final class Body {
	public static enum State {
		Fixed, // can't move at all
		Static, Dynamic;
	}

	// constants
	public static boolean AutoSleep = true;
	public static int SleepIdleFrames = 20;
	public static float SleepMaxVelocitySquared = 1e-5f;

	public float mass, imass; // TODO add 'final' after fixing AbsoluteElasticAngularCollision
	private final Matrix3 Ibody;
	private Matrix3 I, invI;
	private Transform3 transform;

	// Misc fields
	public Vector3 color = GLA.blue;
	public final Shape shape;
	public State state = State.Dynamic;
	public String name;
	public int idleFrames;

	// State variables
	private Vector3 linPos = Vector3.Zero; // position
	private Quaternion angPos = Quaternion.Identity;
	private Vector3 linVel = Vector3.Zero, angVel = Vector3.Zero;
	private Vector3 biasLinVel = Vector3.Zero, biasAngVel = Vector3.Zero;

	// Extrenaly computed quantities
	private Vector3 force = Vector3.Zero, torque = Vector3.Zero;

	// Material properties
	public float elasticity = 0.5f;
	public float drag = 0.003f;
	public float sfriction = 0.6f, dfriction = 0.4f;

	// Constructor
	public Body(final Vector3 position, final Quaternion orientation, final Shape shape, final float density) {
		// position
		linPos = position;
		angPos = orientation;
		transform = new Transform3(orientation, position);

		// shape
		this.shape = shape;

		// init mass
		mass = density * shape.volume();
		imass = 1 / mass;

		// init body inertial moment
		Ibody = shape.inertiaTensor().mul(density);
		I = transform.m.mul(Ibody).mul(transform.m.transpose());
		invI = I.inv();

		assert invariant();
	}

	private boolean invariant() {
		assert linPos.isFinite() : linPos;
		assert angPos.isFinite();

		assert linVel.isFinite();
		assert angVel.isFinite();

		assert force.isFinite();
		assert torque.isFinite();

		assert invI.a.isFinite() && invI.b.isFinite() && invI.c.isFinite();

		return true;
	}

	public float kinetic() {
		return (mass * linVel.square() + angVel.dot(I.mul(angVel))) / 2;
	}

	public void integrateVel(final float dt) {
		if (state == State.Dynamic) {
			// integrate
			linVel = linVel.add(dt * imass, force);
			angVel = angVel.add(dt, invI.mul(torque.sub(angVel.cross(I.mul(angVel)))));
		}

		// reset accumulators
		force = torque = Vector3.Zero;

		assert invariant();
	}

	public void integratePos(final float dt) {
		if (state == State.Dynamic) {
			// integrate
			linPos = linPos.add(dt, linVel.add(biasLinVel));
			// dq/dt = w*q/2  =>  q' = q + (w*q)*(dt/2)
			angPos = angPos.add(angVel.add(biasAngVel).mul(angPos).mul(dt / 2)).unit();

			// reset bias velocities
			biasLinVel = biasAngVel = Vector3.Zero;

			// update matrices
			transform = new Transform3(angPos, linPos);
			I = transform.m.mul(Ibody).mul(transform.m.transpose());
			invI = I.inv();

			// auto sleep
			if (AutoSleep && linVel.square() <= SleepMaxVelocitySquared
					&& angVel.square() * GMath.square(shape.radius) <= SleepMaxVelocitySquared) {
				idleFrames++;
				if (idleFrames >= SleepIdleFrames) state = State.Static;
			} else
				idleFrames = 0;
		}

		assert invariant();
	}

	public void advanceTransforms(final float dt) {
		transform = new Transform3(angPos.add(angVel.mul(angPos).mul(dt / 2)), linPos.add(dt, linVel));
	}

	public void addForce(final Vector3 f) {
		assert force.isFinite();
		force = force.add(f);
		assert invariant();
	}

	public void addTorque(final Vector3 t) {
		assert torque.isFinite();
		torque = torque.add(t);
		assert invariant();
	}

	public void addLinAcc(final Vector3 acc) {
		assert acc.isFinite();
		force = force.add(mass, acc);
		assert invariant();
	}

	/** impulse is transfered from B to A
	 *  ra and rb = point - body.position */
	public static void transferImpulse(final Vector3 impulse, final Body bodyA, final Body bodyB, final Vector3 ra,
			final Vector3 rb) {
		assert impulse.isFinite();
		assert ra.cross(impulse).isFinite() : ra + " " + impulse;
		assert rb.cross(impulse).isFinite();

		bodyA.linVel = bodyA.linVel.add(bodyA.imass, impulse);
		bodyA.angVel = bodyA.angVel.add(bodyA.invI, ra.cross(impulse));
		bodyB.linVel = bodyB.linVel.sub(bodyB.imass, impulse);
		bodyB.angVel = bodyB.angVel.sub(bodyB.invI, rb.cross(impulse));

		if (bodyA.state == State.Static) bodyA.state = State.Dynamic;
		if (bodyB.state == State.Static) bodyB.state = State.Dynamic;

		assert bodyA.invariant();
		assert bodyB.invariant();
	}

	public static void transferBiasImpulse(final Vector3 impulse, final Body bodyA, final Body bodyB, final Vector3 ra,
			final Vector3 rb) {
		assert impulse.isFinite();
		assert ra.cross(impulse).isFinite() : ra + " " + impulse;
		assert rb.cross(impulse).isFinite();

		bodyA.biasLinVel = bodyA.biasLinVel.add(bodyA.imass, impulse);
		bodyA.biasAngVel = bodyA.biasAngVel.add(bodyA.invI, ra.cross(impulse));
		bodyB.biasLinVel = bodyB.biasLinVel.sub(bodyB.imass, impulse);
		bodyB.biasAngVel = bodyB.biasAngVel.sub(bodyB.invI, rb.cross(impulse));

		if (bodyA.state == State.Static) bodyA.state = State.Dynamic;
		if (bodyB.state == State.Static) bodyB.state = State.Dynamic;

		assert bodyA.invariant();
		assert bodyB.invariant();
	}

	/** r = point - position */
	public Vector3 velAt(final Vector3 r) {
		assert r.isFinite();
		return linVel.add(angVel.cross(r));
	}

	public Vector3 bVelAt(final Vector3 r) {
		assert r.isFinite();
		return biasLinVel.add(biasAngVel.cross(r));
	}

	public Matrix3 imassAt(final Vector3 r) {
		final Matrix3 rt = r.tilda();
		return new Matrix3(imass).sub(rt.mul(invI).mul(rt)); // TODO this can be simplified for special bodies
	}

	public static Matrix3 imassAt(final Body bodyA, final Body bodyB, final Vector3 ra, final Vector3 rb) {
		if (bodyA.state == Body.State.Fixed) return bodyB.imassAt(rb);
		if (bodyB.state == Body.State.Fixed) return bodyA.imassAt(ra);

		final Matrix3 rat = ra.tilda(), rbt = rb.tilda();
		final Matrix3 Ma = rat.mul(bodyA.invI).mul(rat);
		final Matrix3 Mb = rbt.mul(bodyB.invI).mul(rbt);
		return new Matrix3(bodyA.imass + bodyB.imass).sub(Ma).sub(Mb);
	}

	public Interval interval(final Vector3 axis) {
		return shape.interval(transform.iapplyV(axis)).shift(transform.v.dot(axis));
	}

	// Getters/Setters
	public Vector3 position() {
		return linPos;
	}

	public Quaternion orientation() {
		return angPos;
	}

	public Vector3 linVelocity() {
		return linVel;
	}

	public Vector3 angVelocity() {
		return angVel;
	}

	public void setLinVelocity(final Vector3 a) {
		linVel = a;
	}

	public void setAngVelocity(final Vector3 a) {
		angVel = a;
	}

	public Transform3 transform() {
		return transform;
	}

	public Matrix3 invI() {
		return invI;
	}

	// From Object
	private static int ID = 0; // NOTE can run-out of IDs
	public final int id = Body.ID++;

	@Override
	public String toString() {
		return name != null ? name : Integer.toString(id);
	}

	@Override
	public int hashCode() {
		return id;
	}
}