package tintor.rigidbody.model.effector;

import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.Body.State;

public class Drag implements Effector {
	@Override
	public void apply(final World world) {
		// TODO add auto list to world.detector for non-fixed bodies!
		for (final Body b : world.bodies)
			if (b.state == State.Dynamic) {
				//b.addForce(b.linVel.mul(b.linVel.length() * b.linDrag));
				//b.addForce(b.linVel.mul(-b.linDrag));
				//b.addTorque(b.angVel.mul(-b.angDrag));
				b.setLinVelocity(b.linVelocity().mul(1 - b.drag));
				b.setAngVelocity(b.angVelocity().mul(1 - b.drag * b.shape.radius / 10));
				//b.angAcc = b.angAcc.sub(b.angDrag * b.angVel.square(), b.invI().mul(b.angVel.unitz()));
			}
	}
	
	@Override
	public void render() {
	}
}