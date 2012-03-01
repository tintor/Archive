package tintor.rigidbody.model.solid.decorator;

import javax.media.opengl.GL;

import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.Decorator;
import tintor.rigidbody.model.solid.Solid;

public final class Colored extends Decorator {
	public Vector3 color;

	public Colored(Solid shape, Vector3 color) {
		super(shape);
		this.color = color;
	}

	public Colored(Solid shape, double red, double green, double blue) {
		this(shape, new Vector3(red, green, blue));
	}

	@Override
	public void render() {
		GLA.gl.glPushAttrib(GL.GL_CURRENT_BIT);
		GLA.color(color);
		super.render();
		GLA.gl.glPopAttrib();
	}
}