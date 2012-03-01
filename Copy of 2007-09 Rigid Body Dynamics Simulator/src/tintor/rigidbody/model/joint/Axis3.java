package tintor.rigidbody.model.joint;

import tintor.geometry.Vector3;

public interface Axis3 {
	Vector3 position();

	Vector3 velocity();

	double mass();

	void apply(Vector3 force);
}