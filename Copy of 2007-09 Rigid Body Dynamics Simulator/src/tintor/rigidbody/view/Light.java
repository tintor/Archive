package tintor.rigidbody.view;

import javax.media.opengl.GL;

import tintor.geometry.Vector3;
import tintor.opengl.GLA;

public final class Light {
	private final int id;

	public Light(int id) {
		this.id = GL.GL_LIGHT0 + id;
	}

	private static float[] array(Vector3 a) {
		return new float[] { (float) a.x, (float) a.y, (float) a.z, 1 };
	}

	public void position(double x, double y, double z) {
		GLA.gl.glLightfv(id, GL.GL_POSITION, new float[] { (float) x, (float) y, (float) z }, 0);
	}

	public void ambient(float r, float g, float b) {
		GLA.gl.glLightfv(id, GL.GL_AMBIENT, new float[] { r, g, b, 1 }, 0);
	}

	public void diffuse(float r, float g, float b) {
		GLA.gl.glLightfv(id, GL.GL_DIFFUSE, new float[] { r, g, b, 1 }, 0);
	}

	public void specular(float r, float g, float b) {
		GLA.gl.glLightfv(id, GL.GL_SPECULAR, new float[] { r, g, b, 1 }, 0);
	}

	public void position(Vector3 a) {
		GLA.gl.glLightfv(id, GL.GL_POSITION, array(a), 0);
	}

	public void ambient(Vector3 color) {
		GLA.gl.glLightfv(id, GL.GL_AMBIENT, array(color), 0);
	}

	public void diffuse(Vector3 color) {
		GLA.gl.glLightfv(id, GL.GL_DIFFUSE, array(color), 0);
	}

	public void specular(Vector3 color) {
		GLA.gl.glLightfv(id, GL.GL_SPECULAR, array(color), 0);
	}

	public void enable() {
		GLA.enable(id);
	}

	public void disable() {
		GLA.disable(id);
	}
}