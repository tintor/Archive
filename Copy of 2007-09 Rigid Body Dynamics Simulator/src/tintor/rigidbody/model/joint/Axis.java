package tintor.rigidbody.model.joint;

public interface Axis {
	double position(); // or angle

	double velocity(); // or angular velocity

	double mass(); // or moment of inertial

	void apply(double force); // force or torque
}