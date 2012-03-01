import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import tintor.opengl.GLA;

public class View {
	public final Projection projection = new Projection();
	public final Camera camera = new Camera();

	public List<Tile> tiles = new ArrayList<Tile>();

	public void init() {
		GLA.enable(GL.GL_CULL_FACE);
		GLA.gl.glCullFace(GL.GL_BACK);

		GLA.enable(GL.GL_CLEAR);
		GLA.gl.glClearColor(0, 0, 0, 1);

		GLA.gl.glShadeModel(GL.GL_SMOOTH);
		GLA.enable(GL.GL_LINE_SMOOTH);
		//GLA.enable(GL.GL_CLIP_PLANE0);
		GLA.enable(GL.GL_TEXTURE_2D);

		for (int a = 0; a <= 3; a++)
			for (int b = 0; b <= 3; b++)
				for (int c = 0; c <= 3; c++)
					tiles.add(new Tile("" + a + "" + b + "" + c));

		tiles.add(new Tile("1200"));
		tiles.add(new Tile("1201"));
		tiles.add(new Tile("1202"));
		tiles.add(new Tile("1203"));

		tiles.add(new Tile("12020"));
		tiles.add(new Tile("12021"));
		tiles.add(new Tile("12022"));
		tiles.add(new Tile("12023"));

		tiles.add(new Tile("12020"));
		tiles.add(new Tile("12021"));
		tiles.add(new Tile("12022"));
		tiles.add(new Tile("12023"));

		new Tile("0");
	}

	public void display() {
		GLA.gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		GLA.gl.glLoadIdentity();

		GLA.gl.glTranslated(0, 0, -camera.distance);
		//GLA.gl.glClipPlane(GL.GL_CLIP_PLANE0, new double[] { 0, 0, 1, 0 }, 0);
		GLA.gl.glRotated(-Math.toDegrees(camera.quat.angle()), camera.quat.x, camera.quat.y, camera.quat.z);
		GLA.gl.glScaled(camera.zoom, camera.zoom, camera.zoom);

		GLA.color(GLA.white);

		for (final Tile tile : tiles)
			tile.render();
	}

	public void reshape(@SuppressWarnings("unused") final int x, @SuppressWarnings("unused") final int y, final int width,
			final int height) {
		projection.aspectRatio = (float) width / (float) height;
		projection.setMatrix();
	}
}