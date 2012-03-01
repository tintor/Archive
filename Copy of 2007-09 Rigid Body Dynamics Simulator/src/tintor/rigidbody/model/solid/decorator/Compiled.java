package tintor.rigidbody.model.solid.decorator;

import javax.media.opengl.GL;


import tintor.opengl.GLA;
import tintor.rigidbody.model.solid.Decorator;
import tintor.rigidbody.model.solid.Solid;

public final class Compiled extends Decorator {
	private int displayList;
	// TODO destroy list somehow

	public Compiled(Solid solid) {
		super(solid);

		displayList = GLA.gl.glGenLists(1);
		GLA.gl.glNewList(displayList, GL.GL_COMPILE);
		solid.render();
		GLA.gl.glEndList();
	}

	@Override
	public void render() {
		GLA.gl.glCallList(displayList);
	}

	@Override
	public Compiled compile() {
		return this;
	}
}