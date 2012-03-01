package tintor.rigidbody.main.worlds;

import java.awt.event.KeyEvent;

import tintor.geometry.GMath;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector2;
import tintor.geometry.Vector3;
import tintor.geometry.extended.ConvexPolyhedrons;
import tintor.opengl.GLA;
import tintor.rigidbody.model.*;

public class Car extends World {
	final double tankX = 20, tankY = 4, tankZ = 15;
	final double wheelR = 6, wheelZ = 6, wheelDist = 15;
	final int axels = 2;

	final double ftorque = 0.65 * wheelR * wheelR, storque = ftorque;
	final double sfriction = 0.6, dfriction = 0.5;

	public Car() {
		impulseIterations = 10;
		forceIterations = 20;

		randomize = true;

		final Body a = ramp(-160, 0, 0, 200, 30, 100);
		final Body b = ramp(-418, 0, 180, 200, 30, 100);
		bridge(-283, 30, b, a);
		car();

		flipFlop();

		cone(100, -80);
		cone(100, 0);
		cone(100, 80);
		cone(170, -40);
		cone(170, 40);
		cone(240, 0);

		surface(0, 5);

		stairs();
	}

	void stairs() {
		final double stepY = 2, stepX = 30;
		for (int i = 0; i < 5; i++) {
			final Body b = new Body(new Vector3(i * stepX, stepY / 2 + i * stepY, -200), Quaternion.Identity, Shape
					.box(200, stepY, 100), 1e10);
			b.dfriction = World.Space.dfriction;
			b.sfriction = World.Space.sfriction;
			b.elasticity = World.Space.elasticity;
			b.state = Body.State.Fixed;
			b.color = GLA.brown;
			add(b);

		}
	}

	void flipFlop() {
		final double sizeA = 100;
		double sizeB = 100;
		final double sizeY = 20, sizeZ = 60;

		final Vector2[] desc = { new Vector2(0, 0), new Vector2(sizeA, sizeY), new Vector2(-sizeB, sizeY) };
		final Shape ramp = new Shape(ConvexPolyhedrons.prism(desc, desc, sizeZ));

		final Body b = new Body(new Vector3(10, sizeY * 0.666, 150), Quaternion.axisY(GMath.deg2rad(90)), ramp, 0.1);
		b.dfriction = World.Space.dfriction;
		b.angVel = new Vector3(-1, 0, 0);
		b.sfriction = World.Space.sfriction;
		b.elasticity = 0;
		b.color = GLA.gray;
		add(b);

		joints.add(new BallJoint(World.Space, b, b.position().add(new Vector3(sizeZ / 2, -sizeY * 0.666, 0))));
		joints.add(new BallJoint(World.Space, b, b.position().add(new Vector3(-sizeZ / 2, -sizeY * 0.666, 0))));
	}

	final Shape plank = Shape.box(10, 2, 80);

	void bridge(final double x, final double y, final Body b1, final Body b2) {
		final Body[] b = new Body[10];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Body(new Vector3((i - b.length * 0.5) * 12 + x, y, 0), Quaternion.Identity, plank, 1);
			b[i].color = GLA.orange;
			b[i].dfriction = World.Space.dfriction;
			b[i].sfriction = World.Space.sfriction;
			b[i].elasticity = 1;
			add(b[i]);
		}

		for (int i = 1; i < b.length; i++)
			link(b[i - 1], b[i], (b[i - 1].position().x + b[i].position().x) / 2, y);

		link(b1, b[0], b[0].position().x - 6, y);
		link(b[b.length - 1], b2, b[b.length - 1].position().x + 6, y);
	}

	void link(final Body a, final Body b, final double x, final double y) {
		joints.add(new BallJoint(a, b, new Vector3(x, y, 50)));
		joints.add(new BallJoint(a, b, new Vector3(x, y, -50)));
	}

	Shape cone = new Shape(ConvexPolyhedrons.pyramid(12, 40, 10));

	void cone(final double x, final double z) {
		final Body b = new Body(new Vector3(x, 10.0 / 3, z), Quaternion.axisY(Math.random() * Math.PI * 2).mul(
				Quaternion.axisX(-Math.PI / 2)), cone, 1e10);
		b.dfriction = World.Space.dfriction;
		b.sfriction = World.Space.sfriction;
		b.elasticity = World.Space.elasticity;
		b.state = Body.State.Fixed;
		b.color = GLA.yellow;
		add(b);
	}

	Body ramp(final double x, final double z, final double a, final double sizeX, final double sizeY, final double sizeZ) {
		final Vector2[] rampDesc = { new Vector2(0, 0), new Vector2(sizeX, 0), new Vector2(0, sizeY) };
		final Shape ramp = new Shape(ConvexPolyhedrons.prism(rampDesc, rampDesc, sizeZ));

		final Body b = new Body(new Vector3(x, sizeY / 3, z), Quaternion.axisY(GMath.deg2rad(a)), ramp, 1e10);
		b.dfriction = World.Space.dfriction;
		b.sfriction = World.Space.sfriction;
		b.elasticity = 0;
		b.color = GLA.brown;
		b.state = Body.State.Fixed;
		add(b);
		return b;
	}

	void car() {
		final Body s = new Body(new Vector3(0, 1.5 * wheelR, 0), Quaternion.Identity, Shape.box(tankX, tankY, tankZ),
				0.5);
		add(s);

		final Shape wheel = new Shape(ConvexPolyhedrons.prism(24, wheelR, wheelR, wheelZ));
		//final Shape wheel = Shape.box(wheelR, wheelR * 2, wheelZ);
		wheel.bicolor(Vector3.X, GLA.green, GLA.orange);

		final Body[] aa = new Body[axels], bb = new Body[axels];

		for (int i = 0; i < axels; i++) {
			final double x = i * wheelDist - (axels - 1) * wheelDist * 0.5;

			final double ay = 0;//i % 2 == 0 ? wheelR / 2 : -wheelR / 2;
			final Body a = aa[i] = new Body(new Vector3(x, s.position().y + ay, tankZ / 2 + wheelZ / 2 + 0.1),
					Quaternion.Identity, wheel, 1);
			a.elasticity = 0.1;
			a.sfriction = sfriction;
			a.dfriction = dfriction;
			add(a);
			axis(s, a, s.position().y);

			final Body b = bb[i] = new Body(new Vector3(x, s.position().y - ay, -tankZ / 2 - wheelZ / 2 - 0.1),
					Quaternion.Identity, wheel, 1);
			add(b);
			b.elasticity = 0.1;
			b.sfriction = sfriction;
			b.dfriction = dfriction;
			axis(s, b, s.position().y);

			//			final Vector3 d = new Vector3(0, wheelR, 0);
			//			joints.add(new BarJoint(aa[i], bb[i], d, d));
			//			joints.add(new BarJoint(aa[i], bb[i], d.neg(), d.neg()));

			if (i > 0) {
				Vector3 d = new Vector3(0, wheelR, 0);
				joints.add(new BarJoint(aa[i - 1], aa[i], d, d));
				joints.add(new BarJoint(bb[i - 1], bb[i], d, d));

				d = new Vector3(0, -wheelR, 0);
				joints.add(new BarJoint(aa[i - 1], aa[i], d, d));
				joints.add(new BarJoint(bb[i - 1], bb[i], d, d));
			}

		}
		effectors.add(new Effector() {
			@Override public void apply(final World world) {
				if (front != 0 && side != 0) return;

				final Vector3 a = aa[0].transform().applyV(
						new Vector3(0, 0, (front * ftorque + side * storque) * aa[0].mass));
				final Vector3 b = bb[0].transform().applyV(
						new Vector3(0, 0, (front * ftorque - side * storque) * bb[0].mass));
				for (int i = 0; i < axels; i++) {
					aa[i].addTorque(a);
					bb[i].addTorque(b);
				}
			}
		});
	}
	int front = 0, side = 0;

	Shape box = Shape.box(1, 1, 1);

	@Override public void keyDown(final int key) {
		switch (key) {
		case KeyEvent.VK_UP:
			front = 1;
			break;
		case KeyEvent.VK_DOWN:
			front = -1;
			break;
		case KeyEvent.VK_LEFT:
			side = -1;
			break;
		case KeyEvent.VK_RIGHT:
			side = 1;
			break;
		case KeyEvent.VK_B:
			final Body b = new Body(new Vector3(100 + Math.random() * 5, 40, Math.random() * 5), Quaternion.axisX(Math
					.random()), box, 5);
			b.color = Math.random() < 0.5 ? GLA.yellow : GLA.green;
			b.elasticity = 0;
			bodies.add(b);
			break;
		}
	}

	@Override public void keyUp(final int key) {
		switch (key) {
		case KeyEvent.VK_UP:
			front = 0;
			break;
		case KeyEvent.VK_DOWN:
			front = 0;
			break;
		case KeyEvent.VK_LEFT:
			side = 0;
			break;
		case KeyEvent.VK_RIGHT:
			side = 0;
			break;
		}
	}

	void axis(final Body a, final Body b, final double y) {
		final Vector3 p = b.position();
		joints.add(new BallJoint(a, b, new Vector3(p.x, y, p.z + 1)));
		joints.add(new BallJoint(a, b, new Vector3(p.x, y, p.z - 1)));
	}
}