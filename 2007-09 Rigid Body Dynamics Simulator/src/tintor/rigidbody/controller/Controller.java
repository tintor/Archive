package tintor.rigidbody.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import tintor.opengl.GLA;
import tintor.rigidbody.model.World;
import tintor.rigidbody.view.View;

import com.sun.opengl.util.Animator;

public class Controller extends MouseAdapter implements GLEventListener, MouseMotionListener, KeyListener,
		MouseWheelListener {
	// Fields
	public World world;
	protected final View view = new View();
	protected int width, height; // of viewport

	protected final JFrame frame;
	protected final JMenuBar menubar = new JMenuBar();
	protected final GLCanvas canvas = new GLCanvas();
	protected Animator animator;

	// Parameters
	public boolean pause = true;
	public int steps = 4;

	// Constructor
	public Controller(final String title, final int width, final int height, final boolean fullscreen) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame = new JFrame(title);
		frame.add(canvas);
		frame.setSize(width, height);
		frame.setJMenuBar(menubar);

		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		frame.addKeyListener(this);

		if (fullscreen) GLA.switchToFullscreen(frame, width, height);

		animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
	}

	protected JMenu menu;
	protected JMenuItem menuItem;

	protected JMenu menu(final String text) {
		menu = new JMenu(text);
		menubar.add(menu);
		return menu;
	}

	protected JMenuItem menuItem(final String text) {
		return menuItem(text, 0);
	}

	protected JMenuItem menuItem(final String text, final int key) {
		return menuItem(text, key, 0);
	}

	protected void menuSeparator() {
		menu.addSeparator();
	}

	protected JMenuItem menuItem(final String text, final int key, final int modifiers) {
		menuItem = new JMenuItem(text);
		if (key != 0) menuItem.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
		menu.add(menuItem);
		return menuItem;
	}

	public void start() {
		frame.setVisible(true);
		animator.start();
	}

	// GLEventListener
	@Override public void init(final GLAutoDrawable drawable) {
		drawable.addMouseListener(this);
		drawable.addMouseMotionListener(this);
		drawable.addMouseWheelListener(this);

		GLA.gl = drawable.getGL();
		view.init();
	}

	@Override public void display(final GLAutoDrawable drawable) {
		if (!pause) world.step(steps);

		GLA.gl = drawable.getGL();
		view.renderText = pause;
		view.display();
	}

	@Override public void displayChanged(final GLAutoDrawable drawable, final boolean arg1, final boolean arg2) {}

	@Override public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int w, final int h) {
		width = w;
		height = h;

		GLA.gl = drawable.getGL();
		view.reshape(x, y, w, h);
	}

	// KeyListener
	@Override public void keyPressed(final KeyEvent e) {}

	@Override public void keyReleased(final KeyEvent e) {}

	@Override public void keyTyped(final KeyEvent e) {}

	// MouseMotionListener
	@Override public void mouseDragged(final MouseEvent e) {}

	@Override public void mouseMoved(final MouseEvent e) {}

	// MouseWheelListener
	@Override public void mouseWheelMoved(final MouseWheelEvent e) {}
}