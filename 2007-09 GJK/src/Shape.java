import tintor.geometry.Vector3;

public interface Shape {
	Vector3 anyPoint();

	Vector3 support(Vector3 dir);
}