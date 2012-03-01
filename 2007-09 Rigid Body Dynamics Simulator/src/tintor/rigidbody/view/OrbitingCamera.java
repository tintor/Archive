package tintor.rigidbody.view;

import java.io.Serializable;

import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public class OrbitingCamera implements Serializable {
	public float distance = 50;
	public float pitch, yaw; // in degrees
	public Vector3 center = Vector3.Zero;

	public void setMatrix() {
		GLA.gl.glLoadIdentity();
		GLA.gl.glTranslated(0, 0, -distance);
		GLA.gl.glRotated(-pitch, 1, 0, 0);
		GLA.gl.glRotated(-yaw, 0, 1, 0);
		GLA.gl.glTranslated(-center.x, -center.y, -center.z);
	}
}