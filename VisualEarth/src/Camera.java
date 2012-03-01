import java.io.Serializable;

import tintor.geometry.Quaternion;

public class Camera implements Serializable {
	public float zoom = 1, distance = 3;
	public Quaternion quat = Quaternion.axisX((float) Math.PI).mul(Quaternion.axisY((float) Math.PI / 2));

	public void rotate(final Quaternion r) {
		quat = quat.mul(r).unit();
	}
}