package tintor.rigidbody.model;

import tintor.geometry.Vector3;

public interface IShape {
	float volume();

	Vector3 closestFromInside(Vector3 point);

	Vector3[] vertices();

	void render();
}