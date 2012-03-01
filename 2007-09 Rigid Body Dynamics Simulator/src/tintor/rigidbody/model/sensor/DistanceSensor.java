package tintor.rigidbody.model.sensor;

import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Sensor;

public class DistanceSensor implements Sensor {
	public Body bodyA, bodyB;
	public Vector3 anchorA, anchorB;
	public double distance;

	@Override public void update() {
		distance = bodyA.transform.applyP(anchorA).distance(bodyB.transform.applyP(anchorB));
	}
}