package tintor.rigidbody.view;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;

import javax.media.opengl.GL;
import javax.vecmath.Matrix4d;

import tintor.geometry.Plane3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Constraint;
import tintor.rigidbody.model.Contact;
import tintor.rigidbody.model.Effector;
import tintor.rigidbody.model.World;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class View {
	// Fields
	public Projection projection = new Projection();

	public boolean wireframe = false;
	public boolean contacts = false;
	public boolean system = false;
	public boolean renderText = false;

	public boolean saveScreen = false;

	private final Light light = new Light(0);

	public World world;

	int systemList;
	Texture texture;
	TextRenderer renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));

	public void init() {
		GLA.gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
		GLA.enable(GL.GL_COLOR_MATERIAL);

		GLA.enable(GL.GL_CULL_FACE);
		GLA.gl.glCullFace(GL.GL_BACK);

		GLA.enable(GL.GL_DEPTH_TEST);
		GLA.gl.glCullFace(GL.GL_BACK);
		GLA.gl.glClearColor(0, 0, 0, 1);

		GLA.gl.glShadeModel(GL.GL_SMOOTH);

		light.ambient(0.1f, 0.1f, 0.1f);
		light.diffuse(0.9f, 0.9f, 0.9f);
		light.specular(1, 1, 1);
		light.enable();
		GLA.gl.glEnable(GL.GL_LIGHTING);

		final int size = 10;
		systemList = GLA.gl.glGenLists(1);
		GLA.gl.glNewList(systemList, GL.GL_COMPILE);
		GLA.disable(GL.GL_LIGHTING);
		GLA.gl.glBegin(GL.GL_LINES);
		for (int i = 0; i < size; i++) {
			// XY
			GLA.color(GLA.red);
			GLA.vertex(0, i, 0);
			GLA.vertex(size, i, 0);

			GLA.color(GLA.green);
			GLA.vertex(i, 0, 0);
			GLA.vertex(i, size, 0);

			// XZ
			GLA.color(GLA.red);
			GLA.vertex(0, 0, i);
			GLA.vertex(size, 0, i);

			GLA.color(GLA.blue);
			GLA.vertex(i, 0, 0);
			GLA.vertex(i, 0, size);

			// YZ
			GLA.color(GLA.green);
			GLA.vertex(0, 0, i);
			GLA.vertex(0, size, i);

			GLA.color(GLA.blue);
			GLA.vertex(0, i, 0);
			GLA.vertex(0, i, size);
		}
		GLA.gl.glEnd();

		GLA.color(GLA.white);
		GLA.gl.glRasterPos3i(size, 0, 0);
		GLA.glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "X");
		GLA.gl.glRasterPos3i(0, size, 0);
		GLA.glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Y");
		GLA.gl.glRasterPos3i(0, 0, size);
		GLA.glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Z");

		GLA.enable(GL.GL_LIGHTING);
		GLA.gl.glEndList();

		// load texture
		try {
			texture = TextureIO.newTexture(new File("santa.png"), true);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		GLA.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		GLA.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		GLA.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		GLA.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
	}

	public void display() {
		if (pickX >= 0 && pickY >= 0) pick();
		if (world.pickBody != null) world.camera.center = world.pickBody.position();

		// setup
		GLA.gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
		world.camera.setMatrix();

		// render
		if (system) GLA.callList(systemList);
		renderBodies();
		renderJointsAndEffectors();
		if (contacts) renderContacts();
		renderPlanes();

		//final Body a = world.bodies.get(world.bodies.size() - 1);
		//final Body b = world.bodies.get(world.bodies.size() / 2);
		//nearest(a, b);

		if (renderText) renderText();
		if (saveScreen) {
			saveScreen = false;
			saveScreen();
		}
	}

//	void nearest(final Body a, final Body b) {
//		final Transform3 w = b.transform().icombine(a.transform());
//		copy(w, m);
//
//		report.clear();
//		a.shape.polyTree.vclip(report, b.shape.polyTree, m, Double.POSITIVE_INFINITY, ht);
//		final ClosestPointPair pair = report.getClosestPair();
//		final Vector3 pa = new Vector3(pair.pnt1.x, pair.pnt1.y, pair.pnt1.z);
//		final Vector3 pb = new Vector3(pair.pnt2.x, pair.pnt2.y, pair.pnt2.z);
//		line(a.transform().applyP(pa), b.transform().applyP(pb));
//	}

	static void line(final Vector3 pa, final Vector3 pb) {
		GLA.disable(GL.GL_LIGHTING);
		GLA.color(GLA.white);
		GLA.gl.glBegin(GL.GL_LINES);
		GLA.vertex(pa);
		GLA.vertex(pb);
		GLA.gl.glEnd();
		GLA.enable(GL.GL_LIGHTING);
	}

//	static Matrix4d m = new Matrix4d();
//	static DistanceReport report = new DistanceReport(10);
//	static ClosestFeaturesHT ht = new ClosestFeaturesHT();
//
//	static void distance(final Body a, final Body b, final Report r) {
//		final Transform3 w = b.transform().icombine(a.transform());
//		copy(w, m);
//		report.clear();
//		final double dist = a.shape.polyTree.vclip(report, b.shape.polyTree, m, Double.POSITIVE_INFINITY, ht);
//
//		final ClosestPointPair pair = report.getClosestPair();
//		final Vector3 pa = new Vector3(pair.pnt1.x, pair.pnt1.y, pair.pnt1.z);
//		final Vector3 pb = new Vector3(pair.pnt2.x, pair.pnt2.y, pair.pnt2.z);
//
//		r.dist = dist;
//		r.a = a.transform().applyP(pa);
//		r.b = b.transform().applyP(pb);
//	}
//
//	static class Report {
//		double dist;
//		Vector3 a, b;
//	}

	static void copy(final Transform3 t, final Matrix4d m) {
		m.m00 = t.m.a.x;
		m.m01 = t.m.a.y;
		m.m02 = t.m.a.z;
		m.m03 = t.v.x;

		m.m10 = t.m.b.x;
		m.m11 = t.m.b.y;
		m.m12 = t.m.b.z;
		m.m13 = t.v.y;

		m.m20 = t.m.c.x;
		m.m21 = t.m.c.y;
		m.m22 = t.m.c.z;
		m.m23 = t.v.z;

		m.m30 = 0;
		m.m31 = 0;
		m.m32 = 0;
		m.m33 = 1;
	}

	//int sc = 0;
	int screen = 0;

	private void saveScreen() {
		final IntBuffer viewport = IntBuffer.allocate(4);
		GLA.gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);
		final int width = viewport.get(2), height = viewport.get(3);

		final ByteBuffer framebytes = ByteBuffer.allocate(width * height * 3);
		GLA.gl.glReadPixels(0, 0, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, framebytes);

		final int[] pixels = new int[width * height];
		for (int i = 0; i < pixels.length; i++) {
			final int bindex = i * 3;
			pixels[i] = 0xFF000000 // A
					| (framebytes.get(bindex) & 0x000000FF) << 16
					| (framebytes.get(bindex + 1) & 0x000000FF) << 8
					| (framebytes.get(bindex + 2) & 0x000000FF) << 0; // B
		}

		savePixels(flipPixels(pixels, width, height), width, height, String.format("screen%03d.png", screen++));
	}

	private static void savePixels(final int[] pixels, final int width, final int height, final String file) {
		try {
			final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, width, height, pixels, 0, width);
			javax.imageio.ImageIO.write(image, "png", new File(file));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static int[] flipPixels(final int[] imgPixels, final int imgw, final int imgh) {
		int[] flippedPixels = null;
		if (imgPixels != null) {
			flippedPixels = new int[imgw * imgh];
			for (int y = 0; y < imgh; y++)
				for (int x = 0; x < imgw; x++)
					flippedPixels[(imgh - y - 1) * imgw + x] = imgPixels[y * imgw + x];
		}
		return flippedPixels;
	}

	private void renderBodies() {
		GLA.set(GL.GL_CULL_FACE, !wireframe);
		GLA.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, wireframe ? GL.GL_LINE : GL.GL_FILL);
		for (final Body body : world.bodies) {
			// transform into body reference frame
			GLA.gl.glPushMatrix();
			GLA.translate(body.position());
			GLA.rotate(body.orientation());

			// render shape
			GLA.color(body.color);
			body.shape.render();

			// restore reference frame
			GLA.gl.glPopMatrix();
		}
	}

	private void renderPlanes() {
		// TODO this can go to display list
		GLA.enable(GL.GL_TEXTURE_2D);

		GLA.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, wireframe ? GL.GL_LINE : GL.GL_FILL);
		GLA.color(GLA.white);

		GLA.gl.glColor4f(1, 1, 1, 0.5f);
		//GLA.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		texture.bind();
		for (final Plane3 plane : world.planes) {
			final Vector3 c = plane.normal.mul(-plane.offset);
			final Vector3 i = plane.normal.normal().unit(), j = i.cross(plane.normal).unit();

			GLA.beginQuads();
			GLA.normal(plane.normal);
			GLA.gl.glTexCoord2d(100, 0);
			GLA.vertex(c.sub(2000, j));
			GLA.gl.glTexCoord2d(100, 100);
			GLA.vertex(c.sub(2000, i));
			GLA.gl.glTexCoord2d(0, 100);
			GLA.vertex(c.add(2000, j));
			GLA.gl.glTexCoord2d(0, 0);
			GLA.vertex(c.add(2000, i));
			GLA.gl.glEnd();

			//			GLA.enable(GL.GL_BLEND);
			//
			//			GLA.beginQuads();
			//			GLA.normal(plane.normal.neg());
			//			GLA.gl.glTexCoord2d(0, 0);
			//			GLA.vertex(c.add(2000, i));
			//			GLA.gl.glTexCoord2d(0, 100);
			//			GLA.vertex(c.add(2000, j));
			//			GLA.gl.glTexCoord2d(100, 100);
			//			GLA.vertex(c.sub(2000, i));
			//			GLA.gl.glTexCoord2d(100, 0);
			//			GLA.vertex(c.sub(2000, j));
			//			GLA.gl.glEnd();
			//
			//			GLA.disable(GL.GL_BLEND);
		}

		GLA.gl.glDisable(GL.GL_TEXTURE_2D);
	}

	private void renderJointsAndEffectors() {
		GLA.disable(GL.GL_LIGHTING);
		for (final Constraint c : world.joints)
			c.render();
		for (final Effector e : world.effectors)
			e.render();
		GLA.enable(GL.GL_LIGHTING);
	}

	private void renderContacts() {
		GLA.disable(GL.GL_LIGHTING);
		GLA.gl.glDepthFunc(GL.GL_ALWAYS);

		GLA.gl.glPointSize(5);
		GLA.gl.glBegin(GL.GL_LINES);
		for (final Contact c : world.contacts) {
			GLA.gl.glColor3f(1, 0, 0);
			GLA.vertex(c.point);
			GLA.gl.glColor3f(1, 1, 1);
			GLA.vertex(c.point.add(c.normal));
		}
		GLA.gl.glEnd();

		GLA.gl.glDepthFunc(GL.GL_LESS);
		GLA.enable(GL.GL_LIGHTING);
	}

	private void renderText() {
		final IntBuffer viewport = IntBuffer.allocate(4);
		GLA.gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);

		GLA.gl.glLoadIdentity();
		GLA.gl.glMatrixMode(GL.GL_PROJECTION);
		GLA.gl.glPushMatrix();
		GLA.gl.glLoadIdentity();
		GLA.gl.glOrtho(0, viewport.get(2), viewport.get(3), 0, -1, 1);

		GLA.disable(GL.GL_DEPTH_TEST);
		GLA.disable(GL.GL_LIGHTING);
		GLA.color(GLA.white);
		rasterPosY = 0;

		Locale.setDefault(Locale.US);
		final String a = Vector3.defaultFormat.get();
		Vector3.defaultFormat.set("(%.5f, %.5f, %.5f)");
		if (world.pickBody != null) {
			final Body b = world.pickBody;
			println("linVel %.5f %s", b.linVelocity().length(), b.linVelocity());
			println("angVel %.5f %s", b.angVelocity().length(), b.angVelocity());
			println("kinetic %.2f", b.kinetic());
			println("mass %.2f", b.mass);
			println("friction %.4f %.4f", b.dfriction, b.sfriction);
			println("idle frames %s", b.idleFrames);
		}
		Vector3.defaultFormat.set(a);

		GLA.enable(GL.GL_LIGHTING);
		GLA.enable(GL.GL_DEPTH_TEST);

		GLA.gl.glPopMatrix();
		GLA.gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	private int rasterPosY;

	private void println(final String format, final Object... args) {
		GLA.gl.glRasterPos2i(0, rasterPosY += 12);
		GLA.glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, String.format(format, args));
	}

	public int pickX = -1, pickY = -1;

	private void pick() {
		// Setup selection buffer
		final int capacity = 100;
		final IntBuffer buffer = BufferUtil.newIntBuffer(100);
		GLA.gl.glSelectBuffer(capacity, buffer);

		// Change render mode
		GLA.gl.glRenderMode(GL.GL_SELECT);

		// Init pick projection matrix
		GLA.gl.glMatrixMode(GL.GL_PROJECTION);
		GLA.gl.glPushMatrix();
		GLA.gl.glLoadIdentity();
		final IntBuffer viewport = IntBuffer.allocate(4);
		GLA.gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);
		GLA.gluPickMatrix(pickX, viewport.get(3) - pickY, 2, 2, viewport); // 5 x 5 pixels pick area
		projection.rawMatrix();
		GLA.gl.glMatrixMode(GL.GL_MODELVIEW);

		// Draw the scene
		GLA.gl.glInitNames();
		GLA.gl.glPushName(0);
		world.camera.setMatrix();
		GLA.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		GLA.set(GL.GL_CULL_FACE, false);
		int id = 0;
		for (final Body body : world.bodies) {
			GLA.gl.glPushMatrix();
			GLA.translate(body.position());
			GLA.rotate(body.orientation());

			GLA.gl.glLoadName(id++);
			body.shape.render();
			GLA.gl.glPopMatrix();
		}

		// Restore projection matrix
		GLA.gl.glMatrixMode(GL.GL_PROJECTION);
		GLA.gl.glPopMatrix();
		GLA.gl.glMatrixMode(GL.GL_MODELVIEW);

		// Collect the hits
		pickX = pickY = -1;
		processHits(buffer, GLA.gl.glRenderMode(GL.GL_RENDER));
	}

	private void processHits(final IntBuffer buffer, final int hits) {
		world.pickBody = null;
		int bestMinZ = Integer.MAX_VALUE;
		for (int i = 0; i < hits; i++)
			if (buffer.get(i * 4 + 1) <= bestMinZ) {
				world.pickBody = world.bodies.get(buffer.get(i * 4 + 3));
				bestMinZ = buffer.get(i * 4 + 1);
			}
	}

	public void reshape(@SuppressWarnings("unused")
	final int x, @SuppressWarnings("unused")
	final int y, final int width, final int height) {
		projection.aspectRatio = (float) width / (float) height;
		projection.setMatrix();
	}

	void vector(final Vector3 a, final Vector3 b) {
		GLA.disable(GL.GL_LIGHTING);
		GLA.gl.glBegin(GL.GL_LINES);
		GLA.color(GLA.white);
		GLA.vertex(a);
		GLA.gl.glColor3f(1, 0, 0);
		GLA.vertex(a.add(b));
		GLA.gl.glEnd();
		GLA.enable(GL.GL_LIGHTING);
	}
}