package welltris;

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
	static int WellX = 8, WellZ = 12;
	int[][] floor = new int[WellX][WellX]; // [X][Y]
	int[][] wall = new int[WellZ][4 * WellX]; // [Z][A]

	int[] blocked = new int[4];

	int pa, pz, p_color = -1;

	private final static String[] figures = { "  #|###", " # |###", "## |## ", " ##|## ", "   |###", " # |## " };
	int points = 0;

	Model() {
		newFigure();
	}

	boolean[][] figure = new boolean[3][3]; // [A][Z]

	private static int wrap(final int a) {
		if (a < 0) return a + 4 * WellX;
		if (a >= 4 * WellX) return a - 4 * WellX;
		return a;
	}

	void decA() {
		if (validPosition(pa - 1, pz)) pa = wrap(pa - 1);
	}

	void incA() {
		if (validPosition(pa + 1, pz)) pa = wrap(pa + 1);
	}

	void incZ() {
		pz++;
	}

	boolean decZ() {
		for (int i = 0; i < blocked.length; i++)
			if (blocked[i] > 0) blocked[i]--;
		if (validPosition(pa, pz - 1)) {
			pz--;
			return true;
		}
		return false;
	}

	void rotate() {
		rot();
		if (!validPosition(pa, pz)) {
			rot();
			rot();
			rot();
		}
	}

	private void rot() {
		boolean b = figure[2][0];
		figure[2][0] = figure[2][2];
		figure[2][2] = figure[0][2];
		figure[0][2] = figure[0][0];
		figure[0][0] = b;

		b = figure[2][1];
		figure[2][1] = figure[1][2];
		figure[1][2] = figure[0][1];
		figure[0][1] = figure[1][0];
		figure[1][0] = b;
	}

	private boolean validPosition(final int paa, final int pzz) {
		for (int z = 0; z < 3; z++)
			for (int a = 0; a < 3; a++)
				if (figure[a][z] && wall(paa + a - 1, pzz + z - 1)) return false;
		return true;
	}

	private boolean validPositionE(final int paa, final int pzz) {
		for (int z = 0; z < 3; z++)
			for (int a = 0; a < 3; a++)
				if (figure[a][z] && pzz + z - 1 >= 0 && wall(paa + a - 1, pzz + z - 1)) return false;
		return true;
	}

	private boolean wall(final int a, final int z) {
		if (z < 0) return true;
		if (z >= WellZ) return false;
		final int wa = wrap(a);
		return blocked[wa / WellX] > 0 || wall[z][wa] > 0;
	}

	private int floor(final int x, final int y) {
		if (x < 0 || y < 0 || x >= WellX || y >= WellX) return 0;
		return floor[x][y];
	}

	private int floorW(final int x, final int y, final int w) {
		if (w == 2 && y < 0) return 1;
		if (w == 3 && x >= WellX) return 1;
		if (w == 0 && y >= WellX) return 1;
		if (w == 1 && x < 0) return 1;
		if (x < 0 || y < 0 || x >= WellX || y >= WellX) return 0;
		return floor[x][y];
	}

	private boolean valid(final int fx, final int fy, final boolean[][] ff) {
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (ff[x][y] && floor(fx + x - 1, fy + y - 1) > 0) return false;
		return true;
	}

	private boolean validW(final int fx, final int fy, final boolean[][] ff, final int w) {
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (ff[x][y] && floorW(fx + x - 1, fy + y - 1, w) > 0) return false;
		return true;
	}

	synchronized void fixFigure() {
		if (p_color == -1) return;

		System.out.println("fix");

		final int w = pa / WellX;
		int fx = 0, fy = 0; // position of figure in floor coordinates (X,Y)
		int dx = 0, dy = 0; // moving directorion of figure
		final boolean[][] ff = new boolean[3][3]; // figure description in floor coordinates

		switch (w) {
		case 0:
			ff[0][0] = figure[0][2];
			ff[1][0] = figure[1][2];
			ff[2][0] = figure[2][2];

			ff[0][1] = figure[0][1];
			ff[1][1] = figure[1][1];
			ff[2][1] = figure[2][1];

			ff[0][2] = figure[0][0];
			ff[1][2] = figure[1][0];
			ff[2][2] = figure[2][0];

			dy = 1;
			fx = pa % WellX;
			fy = -pz - 1;
			break;
		case 1:
			ff[0][0] = figure[0][0];
			ff[0][1] = figure[1][0];
			ff[0][2] = figure[2][0];

			ff[1][0] = figure[0][1];
			ff[1][1] = figure[1][1];
			ff[1][2] = figure[2][1];

			ff[2][0] = figure[0][2];
			ff[2][1] = figure[1][2];
			ff[2][2] = figure[2][2];

			dx = -1;
			fx = pz + WellX;
			fy = pa % WellX;
			break;
		case 2:
			ff[0][0] = figure[2][0];
			ff[1][0] = figure[1][0];
			ff[2][0] = figure[0][0];

			ff[0][1] = figure[2][1];
			ff[1][1] = figure[1][1];
			ff[2][1] = figure[0][1];

			ff[0][2] = figure[2][2];
			ff[1][2] = figure[1][2];
			ff[2][2] = figure[0][2];

			dy = -1;
			fx = WellX - 1 - pa % WellX;
			fy = pz + WellX;
			break;
		case 3:
			ff[0][0] = figure[2][2];
			ff[0][1] = figure[1][2];
			ff[0][2] = figure[0][2];

			ff[1][0] = figure[2][1];
			ff[1][1] = figure[1][1];
			ff[1][2] = figure[0][1];

			ff[2][0] = figure[2][0];
			ff[2][1] = figure[1][0];
			ff[2][2] = figure[0][0];

			dx = 1;
			fx = -pz - 1;
			fy = WellX - 1 - pa % WellX;
			break;
		}

		while (validPositionE(pa, pz - 1) && validW(fx + dx, fy + dy, ff, w)) {
			fx += dx;
			fy += dy;
			pz--;
			System.out.println("tick");
		}
		System.out.println("2");

		// fix figure on floor
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (ff[x][y]) {
					final int ax = fx + x - 1, ay = fy + y - 1;
					if (ax >= 0 && ax < WellX && ay >= 0 && ay < WellX) floor[ax][ay] = p_color + 1;
				}

		// fix figure on wall
		for (int z = 0; z < 3; z++)
			for (int a = 0; a < 3; a++)
				if (figure[a][z] && pz + z - 1 >= 0) {
					final int aa = wrap(pa + a - 1);
					wall[pz + z - 1][aa] = p_color + 1;
					blocked[aa / WellX] = WellZ * 2;
				}

		// remove full lines
		int lines = 0;
		// from wall 0
		for (int y = WellX / 2; y >= 0; y--)
			if (fullRow(y)) {
				lines++;
				for (int yp = y - 1; yp >= -WellZ; yp--)
					for (int x = 0; x < WellX; x++)
						xfloorSet(x, yp + 1, xfloorGet(x, yp));
				for (int x = 0; x < WellX; x++)
					xfloorSet(x, -WellZ, 0);
			}
		// from wall 1
		for (int x = WellX / 2 + 1; x < WellX; x++)
			if (fullCol(x)) {
				lines++;
				for (int xp = x + 1; xp < WellX + WellZ; xp++)
					for (int y = 0; y < WellX; y++)
						xfloorSet(xp - 1, y, xfloorGet(xp, y));
				for (int y = 0; y < WellX; y++)
					xfloorSet(WellX + WellZ - 1, y, 0);
			}
		// from wall 2
		for (int y = WellX / 2 + 1; y < WellX; y++)
			if (fullRow(y)) {
				lines++;
				for (int yp = y + 1; yp < WellX + WellZ; yp++)
					for (int x = 0; x < WellX; x++)
						xfloorSet(x, yp - 1, xfloorGet(x, yp));
				for (int x = 0; x < WellX; x++)
					xfloorSet(x, WellX + WellZ - 1, 0);
			}
		// from wall 3
		for (int x = WellX / 2; x >= 0; x--)
			if (fullCol(x)) {
				lines++;
				for (int xp = x - 1; xp >= -WellZ; xp--)
					for (int y = 0; y < WellX; y++)
						xfloorSet(xp + 1, y, xfloorGet(xp, y));
				for (int y = 0; y < WellX; y++)
					xfloorSet(-WellZ, y, 0);
			}

		System.out.println("~~fix");

		points += lines * lines;
		p_color = -1;
	}

	private boolean fullCol(final int x) {
		for (int y = 0; y < WellX; y++)
			if (floor[x][y] == 0) return false;
		return true;
	}

	private boolean fullRow(final int y) {
		for (int x = 0; x < WellX; x++)
			if (floor[x][y] == 0) return false;
		return true;
	}

	private int xfloorGet(final int x, final int y) {
		if (y < 0)
			return wall[-y - 1][x];
		else if (y >= WellX)
			return wall[y - WellX][WellX * 3 - 1 - x];
		else if (x < 0)
			return wall[-x - 1][WellX * 4 - 1 - y];
		else if (x >= WellX)
			return wall[x - WellX][WellX + y];
		else
			return floor[x][y];
	}

	private void xfloorSet(final int x, final int y, final int c) {
		if (y < 0)
			wall[-y - 1][x] = c;
		else if (y >= WellX)
			wall[y - WellX][WellX * 3 - 1 - x] = c;
		else if (x < 0)
			wall[-x - 1][WellX * 4 - 1 - y] = c;
		else if (x >= WellX)
			wall[x - WellX][WellX + y] = c;
		else
			floor[x][y] = c;
	}

	synchronized void newFigure() {
		if (p_color != -1) return;
		if (blocked[0] > 0 && blocked[1] > 0 && blocked[2] > 0 && blocked[3] > 0) {
			p_color = 0;
			pz = WellZ - 1;
			pa = WellX / 2;
			return;
		}

		final Random rand = new Random();

		p_color = rand.nextInt(figures.length);
		final String d = figures[p_color];
		pz = WellZ - 1;

		int pw = rand.nextInt(4);
		while (blocked[pw] > 0)
			pw = (pw + 1) % 4;
		pa = pw * WellX + 1 + rand.nextInt(WellX - 2);

		figure = new boolean[3][3];
		figure[2][0] = d.charAt(0) == '#';
		figure[2][1] = d.charAt(1) == '#';
		figure[2][2] = d.charAt(2) == '#';
		figure[1][0] = d.charAt(4) == '#';
		figure[1][1] = d.charAt(5) == '#';
		figure[1][2] = d.charAt(6) == '#';
	}

	void render(final GL gl) {
		// floor
		for (int x = 0; x < WellX; x++)
			for (int y = 0; y < WellX; y++)
				if (floor[x][y] > 0) {
					gl.glColor3dv(colors, (floor[x][y] - 1) * 3);
					gl.glBegin(GL.GL_QUADS);
					gl.glVertex3d(x, y, 0);
					gl.glVertex3d(x + 1, y, 0);
					gl.glVertex3d(x + 1, y + 1, 0);
					gl.glVertex3d(x, y + 1, 0);
					gl.glEnd();
				}

		// wall tiles
		for (int z = 0; z < WellZ; z++)
			for (int a = 0; a < WellX * 4; a++)
				if (wall[z][a] > 0) {
					gl.glColor3dv(colors, (wall[z][a] - 1) * 3);
					drawTile(gl, a, z);
				}

		// grid
		gl.glBegin(GL.GL_LINES);

		// wall 0 grid
		if (blocked[0] > 0)
			gl.glColor3d(1, 1, 1);
		else
			gl.glColor3d(0.2, 0.2, 0.2);
		for (int a = 0; a < WellX; a++) {
			gl.glVertex3d(a, 0, 0);
			gl.glVertex3d(a, 0, WellZ);
		}
		for (int z = 0; z <= WellZ; z++) {
			gl.glVertex3d(0, 0, z);
			gl.glVertex3d(WellX, 0, z);
		}

		// wall 1 grid
		if (blocked[1] > 0)
			gl.glColor3d(1, 1, 1);
		else
			gl.glColor3d(0.2, 0.2, 0.2);
		for (int a = 0; a < WellX; a++) {
			gl.glVertex3d(WellX, a, 0);
			gl.glVertex3d(WellX, a, WellZ);
		}
		for (int z = 0; z <= WellZ; z++) {
			gl.glVertex3d(WellX, 0, z);
			gl.glVertex3d(WellX, WellX, z);
		}

		// wall 2 grid
		if (blocked[2] > 0)
			gl.glColor3d(1, 1, 1);
		else
			gl.glColor3d(0.2, 0.2, 0.2);
		for (int a = 0; a < WellX; a++) {
			gl.glVertex3d(a, WellX, 0);
			gl.glVertex3d(a, WellX, WellZ);
		}
		for (int z = 0; z <= WellZ; z++) {
			gl.glVertex3d(0, WellX, z);
			gl.glVertex3d(WellX, WellX, z);
		}

		// wall 3 grid
		if (blocked[3] > 0)
			gl.glColor3d(1, 1, 1);
		else
			gl.glColor3d(0.2, 0.2, 0.2);
		for (int a = 0; a < WellX; a++) {
			gl.glVertex3d(0, a, 0);
			gl.glVertex3d(0, a, WellZ);
		}
		for (int z = 0; z <= WellZ; z++) {
			gl.glVertex3d(0, WellX, z);
			gl.glVertex3d(0, 0, z);
		}

		gl.glEnd();

		// draw falling figure
		for (int a = 0; a < 3; a++)
			for (int z = 0; z < 3; z++)
				if (figure[a][z]) {
					gl.glColor3dv(colors, p_color * 3);
					drawTile(gl, wrap(pa + a - 1), pz + z - 1);
				}
	}

	private static void drawTile(final GL gl, final int a, final int z) {
		final int x = a % WellX;
		gl.glBegin(GL.GL_QUADS);
		switch (a / WellX) {
		case 0:
			gl.glVertex3d(x, 0, z);
			gl.glVertex3d(x, 0, z + 1);
			gl.glVertex3d(x + 1, 0, z + 1);
			gl.glVertex3d(x + 1, 0, z);
			break;
		case 1:
			gl.glVertex3d(WellX, x, z);
			gl.glVertex3d(WellX, x, z + 1);
			gl.glVertex3d(WellX, x + 1, z + 1);
			gl.glVertex3d(WellX, x + 1, z);
			break;
		case 2:
			gl.glVertex3d(WellX - 1 - x, WellX, z + 1);
			gl.glVertex3d(WellX - 1 - x, WellX, z);
			gl.glVertex3d(WellX - x, WellX, z);
			gl.glVertex3d(WellX - x, WellX, z + 1);
			break;
		case 3:
			gl.glVertex3d(0, WellX - 1 - x, z + 1);
			gl.glVertex3d(0, WellX - 1 - x, z);
			gl.glVertex3d(0, WellX - x, z);
			gl.glVertex3d(0, WellX - x, z + 1);
			break;
		}
		gl.glEnd();
	}

	final static double[] colors = { /*red*/1, 0, 0, /*orange*/1, 0.5, 0, /*yellow*/1, 1, 0, /*blue*/
	0, 0, 1, /*dark green*/0, 1, 0, /*white*/1, 1, 1 };
}

public class Welltris extends GLJPanel implements GLEventListener, KeyListener {
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
			gl.glTranslated(Model.WellX * -0.5, Model.WellX * -0.5, -81);
			gl.glScaled(1, 1, 5);
			break;
		case 1:
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glTranslated(Model.WellX * -0.5, Model.WellX * -0.5, -35);
			gl.glRotatef(-90, 1, 0, 0);
			gl.glTranslated(0, 0, -3.5);
			break;
		case 2:
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glTranslated(Model.WellX * -0.5, Model.WellX * -0.5, -73);
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

	public Welltris() {
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
			model.decA();
			break;
		case KeyEvent.VK_RIGHT:
			model.incA();
			break;
		case KeyEvent.VK_DOWN:
			model.rotate();
			break;
		case KeyEvent.VK_UP:
			model.decZ();
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
		frame = new JFrame("Welltris");
		frame.getContentPane().setLayout(new BorderLayout());
		final Welltris tetris = new Welltris();
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