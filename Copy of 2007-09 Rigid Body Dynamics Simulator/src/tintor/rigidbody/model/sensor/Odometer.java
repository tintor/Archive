package tintor.rigidbody.model.sensor;

import tintor.geometry.Vector3;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Sensor;

public class Odometer implements Sensor {
	public Body body;
	public Vector3 anchor;
	public double distance;

	private Vector3 prev;

	public void reset() {
		prev = null;
		distance = 0;
	}

	@Override public void update() {
		final Vector3 curr = body.transform.applyP(anchor);
		if (prev != null) distance += curr.distance(prev);
		prev = curr;
	}
}