package tintor.rigidbody.view;

import java.io.Serializable;

import tintor.geometry.GMath;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public class FreeCamera implements Serializable {
	public Vector3 eye = Vector3.Zero;
	public float zoom = 1;
	public Quaternion quat = Quaternion.Identity;

	public void setMatrix() {
		GLA.gl.glLoadIdentity();

		final Vector3 a = quat.axis();
		GLA.gl.glRotated(GMath.rad2deg(quat.angle()), -a.x, -a.y, -a.z);

		GLA.gl.glTranslated(-eye.x, -eye.y, -eye.z);

		GLA.gl.glScaled(1 / zoom, 1 / zoom, 1 / zoom);
	}

	public void rotate(final Quaternion r) {
		quat = quat.mul(r).unit();
	}

	public void moveX(final float a) {
		eye = eye.add(a, quat.idirX());
	}

	public void moveY(final float a) {
		eye = eye.add(a, quat.idirY());
	}

	public void moveZ(final float a) {
		eye = eye.add(a, quat.idirZ());
	}

	public void rotateZ(final float a) {
		rotate(Quaternion.axisZ(a));
	}
}