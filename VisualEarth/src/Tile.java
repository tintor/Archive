import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.opengl.GL;

import tintor.Stream;
import tintor.opengl.GLA;
import tintor.opengl.Texture;

public class Tile {
	public final String path;
	private final int displayList;
	private final Texture texture;
	private boolean disposed = false;

	private double xmin = -Math.PI, xmax = Math.PI, ymin = -Math.PI, ymax = Math.PI;

	public Tile(final String path) {
		this.path = path;
		texture = getTexture(path);

		// 01
		// 23
		for (int i = 0; i < path.length(); i++)
			switch (path.charAt(i)) {
			case '0':
				// upper left
				xmax = (xmin + xmax) / 2;
				ymin = (ymin + ymax) / 2;
				break;
			case '1':
				// upper right
				xmin = (xmin + xmax) / 2;
				ymin = (ymin + ymax) / 2;
				break;
			case '2':
				// lower left
				xmax = (xmin + xmax) / 2;
				ymax = (ymin + ymax) / 2;
				break;
			case '3': // lower right
				xmin = (xmin + xmax) / 2;
				ymax = (ymin + ymax) / 2;
				break;
			default:
				throw new RuntimeException();
			}

		displayList = GLA.gl.glGenLists(1);
		GLA.gl.glNewList(displayList, GL.GL_COMPILE);

		texture.bind();

		GLA.gl.glBegin(GL.GL_TRIANGLE_FAN);
		vertex(0.5, 0.5);
		vertex(0.5, 0);
		vertex(1, 0);
		vertex(1, 0.5);
		vertex(1, 1);
		vertex(0.5, 1);
		vertex(0, 1);
		vertex(0, 0.5);
		vertex(0, 0);
		vertex(0.5, 0);
		GLA.gl.glEnd();

		GLA.gl.glEndList();
	}

	private void vertex(double x, double y) {
		GLA.gl.glTexCoord2d(x, 1 - y);
		x = xmin + (xmax - xmin) * x;
		y = ymin + (ymax - ymin) * y;

		final double longitude = x;
		final double latitude = 2 * Math.atan(Math.exp(y)) - Math.PI / 2;

		final double cos_lat = Math.cos(latitude);
		GLA.gl.glVertex3d(cos_lat * Math.sin(longitude), Math.sin(latitude), cos_lat * Math.cos(longitude));
	}

	public void render() {
		GLA.gl.glCallList(displayList);
	}

	@Override public void finalize() {
		if (disposed) return;
		GLA.gl.glDeleteLists(displayList, 1);
		disposed = true;
	}

	public static Texture getTexture(final String path) {
		try {
			final File file = new File("cache/a" + path + ".jpeg");
			if (!file.exists())
				Stream.pipe(new URL("http://h1.ortho.tiles.virtualearth.net/tiles/a" + path + ".jpeg?g=97"), file);

			final Texture texture = new Texture();
			texture.bind();
			texture.load(file);
			return texture;
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}