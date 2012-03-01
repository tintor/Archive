package blockout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;

import com.sun.opengl.util.Animator;

class Model {
	static int WellX = 5, WellY = 5, WellZ = 12;
	boolean[][][] well = new boolean[WellZ][WellY][WellX];

	int pz = WellZ - 1;
	private int px = 2, py = 2; // koordinate centra figure

	private final static String[] figures = { "  #|###", " # |###", "## |## ", " ##|## ", "   |###", " # |## " };
	boolean[][][] figure = new boolean[3][3][3]; // Z Y X

	int points = 0;

	Model() {
		newFigure();
	}

	void decX() {
		if (validPosition(px - 1, py, pz)) px--;
	}

	void incX() {
		if (validPosition(px + 1, py, pz)) px++;
	}

	void decY() {
		if (validPosition(px, py - 1, pz)) py--;
	}

	void incY() {
		if (validPosition(px, py + 1, pz)) py++;
	}

	void incZ() {
		if (validPosition(px, py, pz + 1)) pz++;
	}

	boolean decZ() {
		if (validPosition(px, py, pz - 1)) {
			pz--;
			return true;
		}
		return false;
	}

	private boolean validPosition(final int pxx, final int pyy, final int pzz) {
		for (int z = 0; z < 3; z++)
			for (int y = 0; y < 3; y++)
				for (int x = 0; x < 3; x++)
					if (figure[z][y][x] && well(pxx + x - 1, pyy + y - 1, pzz + z - 1)) return false;
		return true;
	}

	private boolean well(final int x, final int y, final int z) {
		if (x < 0 || y < 0 || z < 0 || x >= WellX || y >= WellY) return true;
		if (z >= WellZ) return false;
		return well[z][y][x];
	}

	void rotateX() {
		rotx();
		if (!validPosition(px, py, pz)) {
			rotx();
			rotx();
			rotx();
		}
	}

	private void rotx() {
		for (int x = 0; x < 3; x++) {
			boolean b = figure[2][0][x];
			figure[2][0][x] = figure[0][0][x];
			figure[0][0][x] = figure[0][2][x];
			figure[0][2][x] = figure[2][2][x];
			figure[2][2][x] = b;

			b = figure[1][0][x];
			figure[1][0][x] = figure[0][1][x];
			figure[0][1][x] = figure[1][2][x];
			figure[1][2][x] = figure[2][1][x];
			figure[2][1][x] = b;
		}
	}

	void rotateZ() {
		rotz();
		if (!validPosition(px, py, pz)) {
			rotz();
			rotz();
			rotz();
		}
	}

	private void rotz() {
		for (final boolean[][] f : figure) {
			boolean b = f[2][0];
			f[2][0] = f[2][2];
			f[2][2] = f[0][2];
			f[0][2] = f[0][0];
			f[0][0] = b;

			b = f[2][1];
			f[2][1] = f[1][2];
			f[1][2] = f[0][1];
			f[0][1] = f[1][0];
			f[1][0] = b;
		}
	}

	private static boolean full(final boolean[][] a) {
		for (final boolean[] b : a)
			for (boolean c : b)
				if (!c) return false;
		return true;
	}

	void fixFigure() {
		for (int z = 0; z < 3; z++)
			for (int y = 0; y < 3; y++)
				for (int x = 0; x < 3; x++)
					if (figure[z][y][x]) well[pz + z - 1][py + y - 1][px + x - 1] = true;

		// remove full levels
		int removed = 0;
		int currZ = pz - 1, lastZ = pz + 1;
		while (currZ <= lastZ)
			if (currZ >= 0 && currZ < WellZ && full(well[currZ])) {
				removed++;
				for (int z = currZ; z + 1 < WellZ; z++)
					well[z] = well[z + 1];
				well[WellZ - 1] = new boolean[WellY][WellX];
				lastZ--;
			} else
				currZ++;
		points += removed * removed;
	}

	void newFigure() {
		final Random rand = new Random();
		figure = new boolean[3][3][3];
		final String d = figures[rand.nextInt(figures.length)];
		figure[1][2][0] = d.charAt(0) == '#';
		figure[1][2][1] = d.charAt(1) == '#';
		figure[1][2][2] = d.charAt(2) == '#';
		figure[1][1][0] = d.charAt(4) == '#';
		figure[1][1][1] = d.charAt(5) == '#';
		figure[1][1][2] = d.charAt(6) == '#';

		for (int i = rand.nextInt(4); i > 0; i--)
			rotateZ();
		px = WellX / 2;
		py = WellY / 2;
		pz = WellZ - 1;
	}

	void render(final GL gl) {
		// grid
		gl.glColor3d(0.2, 0.2, 0.2);
		for (int z = 0; z <= WellZ; z++) {
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3d(0, 0, z);
			gl.glVertex3d(WellX, 0, z);
			gl.glVertex3d(WellX, WellY, z);
			gl.glVertex3d(0, WellY, z);
			gl.glEnd();
		}

		gl.glBegin(GL.GL_LINES);
		for (int x = 0; x <= WellX; x++) {
			gl.glVertex3d(x, 0, 0);
			gl.glVertex3d(x, 0, WellZ);

			gl.glVertex3d(x, WellY, 0);
			gl.glVertex3d(x, WellY, WellZ);
		}
		for (int y = 1; y < WellY; y++) {
			gl.glVertex3d(0, y, 0);
			gl.glVertex3d(0, y, WellZ);

			gl.glVertex3d(WellX, y, 0);
			gl.glVertex3d(WellX, y, WellZ);
		}
		gl.glEnd();

		// blocks in well
		for (int z = 0; z < WellZ; z++) {
			final int i = z % 5 * 3;

			gl.glColor3dv(colors, i);
			gl.glBegin(GL.GL_QUADS);
			for (int y = 0; y < WellY; y++)
				for (int x = 0; x < WellX; x++)
					if (well[z][y][x]) {
						// west
						gl.glVertex3d(x, y, z + 1);
						gl.glVertex3d(x, y + 1, z + 1);
						gl.glVertex3d(x, y + 1, z);
						gl.glVertex3d(x, y, z);

						// east
						gl.glVertex3d(x + 1, y, z + 1);
						gl.glVertex3d(x + 1, y, z);
						gl.glVertex3d(x + 1, y + 1, z);
						gl.glVertex3d(x + 1, y + 1, z + 1);

						// north
						gl.glVertex3d(x, y + 1, z + 1);
						gl.glVertex3d(x + 1, y + 1, z + 1);
						gl.glVertex3d(x + 1, y + 1, z);
						gl.glVertex3d(x, y + 1, z);

						// south
						gl.glVertex3d(x, y, z + 1);
						gl.glVertex3d(x, y, z);
						gl.glVertex3d(x + 1, y, z);
						gl.glVertex3d(x + 1, y, z + 1);
					}
			gl.glEnd();

			gl.glColor3d(0, 0, 0);
			gl.glBegin(GL.GL_LINES);
			for (int y = 0; y < WellY; y++)
				for (int x = 0; x < WellX; x++)
					if (well[z][y][x]) {
						gl.glVertex3d(x, y, z);
						gl.glVertex3d(x, y, z + 1);

						gl.glVertex3d(x + 1, y, z);
						gl.glVertex3d(x + 1, y, z + 1);

						gl.glVertex3d(x + 1, y + 1, z);
						gl.glVertex3d(x + 1, y + 1, z + 1);

						gl.glVertex3d(x, y + 1, z);
						gl.glVertex3d(x, y + 1, z + 1);
					}
			gl.glEnd();

			for (int y = 0; y < WellY; y++)
				for (int x = 0; x < WellX; x++)
					if (well[z][y][x]) {
						gl.glColor3dv(colors, i);
						gl.glBegin(GL.GL_QUADS);
						gl.glVertex3d(x, y, z + 1);
						gl.glVertex3d(x + 1, y, z + 1);
						gl.glVertex3d(x + 1, y + 1, z + 1);
						gl.glVertex3d(x, y + 1, z + 1);
						gl.glEnd();

						gl.glColor3d(0, 0, 0);
						gl.glBegin(GL.GL_LINE_LOOP);
						gl.glVertex3d(x, y, z + 1);
						gl.glVertex3d(x + 1, y, z + 1);
						gl.glVertex3d(x + 1, y + 1, z + 1);
						gl.glVertex3d(x, y + 1, z + 1);
						gl.glEnd();
					}
		}

		// draw falling figure
		gl.glPushMatrix();
		gl.glTranslated(px - 1, py - 1, pz - 1);
		gl.glColor3d(1, 1, 1);

		for (int z = 0; z < 3; z++) {
			for (int y = 0; y < 3; y++)
				for (int x = 0; x < 3; x++)
					if (figure[z][y][x]) {
						gl.glBegin(GL.GL_LINE_LOOP);
						gl.glVertex3d(x, y, z);
						gl.glVertex3d(x + 1, y, z);
						gl.glVertex3d(x + 1, y + 1, z);
						gl.glVertex3d(x, y + 1, z);
						gl.glEnd();
					}

			for (int y = 0; y < 3; y++)
				for (int x = 0; x < 3; x++)
					if (figure[z][y][x]) {
						gl.glBegin(GL.GL_LINES);
						gl.glVertex3d(x, y, z);
						gl.glVertex3d(x, y, z + 1);

						gl.glVertex3d(x + 1, y, z);
						gl.glVertex3d(x + 1, y, z + 1);

						gl.glVertex3d(x + 1, y + 1, z);
						gl.glVertex3d(x + 1, y + 1, z + 1);

						gl.glVertex3d(x, y + 1, z);
						gl.glVertex3d(x, y + 1, z + 1);
						gl.glEnd();
					}

			for (int y = 0; y < 3; y++)
				for (int x = 0; x < 3; x++)
					if (figure[z][y][x]) {
						gl.glBegin(GL.GL_LINE_LOOP);
						gl.glVertex3d(x, y, z + 1);
						gl.glVertex3d(x + 1, y, z + 1);
						gl.glVertex3d(x + 1, y + 1, z + 1);
						gl.glVertex3d(x, y + 1, z + 1);
						gl.glEnd();
					}
		}
		gl.glPopMatrix();
	}
	final static double[] colors = {/*red*/1, 0, 0, /*orange*/1, 0.5, 0, /*yellow*/1, 1, 0, /*light green*/
	0.5, 1, 0, /*dark green*/0, 1, 0 };
}

public class Blockout extends GLJPanel implements GLEventListener, KeyListener {
	Model model = new Model();

	public void init(final GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.setSwapInterval(1);

		final float pos[] = { 5.0f, 5.0f, 10.0f, 0.0f };

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, pos, 0);
		gl.glEnable(GL.GL_CULL_FACE);
		//gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);

		gl.glEnable(GL.GL_NORMALIZE);

		gl.glClearColor(0, 0, 0, 1);
	}

	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width,
			final int height) {
		final GL gl = drawable.getGL();
		float h = (float) height / (float) width;

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 100.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -40.0f);
	}

	public void display(final GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		switch (camera) {
		case 0:
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glTranslated(model.WellX * -0.5, model.WellY * -0.5, -73);
			gl.glScaled(1, 1, 5);
			break;
		case 1:
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glTranslated(model.WellX * -0.5, model.WellY * -0.5, -35);
			gl.glRotatef(-90, 1, 0, 0);
			gl.glTranslated(0, 0, -3.5);
			break;
		case 2:
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glTranslated(model.WellX * -0.5, model.WellY * -0.5, -73);
			gl.glScaled(1, 1, 5);
			gl.glTranslated(0, 0, 10 - model.pz);
			break;
		}
		model.render(gl);
	}

	public void displayChanged(final GLAutoDrawable drawable, final boolean modeChanged,
			final boolean deviceChanged) {}

	private long startTime;
	private int frameCount;
	private float fps;
	private static Font fpsFont = new Font("SansSerif", Font.BOLD, 20);

	int delay = 2000;
	private int camera = 0;

	public Blockout() {
		setOpaque(true);
		addGLEventListener(this);

		new Thread() {
			@Override public void run() {
				try {
					while (true) {
						sleep(delay);
						if (!model.decZ()) {
							model.fixFigure();
							model.newFigure();
						}
					}
				} catch (final InterruptedException e) {}
			}
		}.start();
	}

	@Override public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (startTime == 0) startTime = System.currentTimeMillis();

		if (++frameCount == 100) {
			final long endTime = System.currentTimeMillis();
			fps = 100.0f / (endTime - startTime) * 1000;
			frameCount = 0;
			startTime = System.currentTimeMillis();
		}

		g.setColor(Color.WHITE);
		g.setFont(fpsFont);

		if (fps > 0) g.drawString("FPS: " + (int) fps, 20, getHeight() - 60);
		g.drawString("Points: " + model.points, 20, getHeight() - 40);
		g.drawString("Delay: " + delay, 20, getHeight() - 20);
	}

	@Override public void keyPressed(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			model.decX();
			break;
		case KeyEvent.VK_RIGHT:
			model.incX();
			break;
		case KeyEvent.VK_UP:
			model.incY();
			break;
		case KeyEvent.VK_DOWN:
			model.decY();
			break;
		case KeyEvent.VK_PAGE_DOWN:
			model.decZ();
			break;
		case KeyEvent.VK_X:
			model.rotateX();
			break;
		case KeyEvent.VK_Z:
			model.rotateZ();
			break;
		case KeyEvent.VK_SPACE:
			while (model.decZ()) {}
			model.fixFigure();
			model.newFigure();
			break;
		// restart
		case KeyEvent.VK_F1:
			model = new Model();
			delay = 2000;
			break;

		// standardni pogled
		case KeyEvent.VK_F5:
			camera = 0;
			break;
		// bocni pogled uz rotaciju
		case KeyEvent.VK_F6:
			camera = 1;
			break;
		// pracenje figure
		case KeyEvent.VK_F7:
			camera = 2;
			break;

		// ubrzaj
		case KeyEvent.VK_F9:
			if (delay > 200) delay -= 100;
			break;
		// uspori
		case KeyEvent.VK_F10:
			if (delay < 2000) delay += 100;
			break;
		}
	}

	@Override public void keyReleased(final KeyEvent e) {}

	@Override public void keyTyped(final KeyEvent e) {}

	static JFrame frame;

	public static void main(final String[] args) {
		if (args.length > 0) {
			Model.WellX = Integer.parseInt(args[0]);
			Model.WellY = Integer.parseInt(args[1]);
			Model.WellZ = Integer.parseInt(args[2]);
		}

		frame = new JFrame("Blockout");
		frame.getContentPane().setLayout(new BorderLayout());
		final Blockout tetris = new Blockout();
		frame.getContentPane().add(tetris, BorderLayout.CENTER);
		frame.addKeyListener(tetris);

		frame.setSize(600, 600);
		final Animator animator = new Animator(tetris);
		frame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(final WindowEvent e) {
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		frame.setVisible(true);
		animator.start();
	}
}