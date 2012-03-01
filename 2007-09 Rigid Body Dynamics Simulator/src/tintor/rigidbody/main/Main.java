package tintor.rigidbody.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import tintor.Classes;
import tintor.geometry.GMath;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.controller.Controller;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.view.OrbitingCamera;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Main extends Controller {
	int worldNo = -1;
	final List<Class<? extends World>> worlds = new ArrayList<Class<? extends World>>();

	int startX, startY;
	float startPitch, startYaw;

	@SuppressWarnings("unchecked")
	public Main() {
		super("Simulator", 800, 600, false);
		loadWorlds();
		createMenu();
		start();
	}

	@SuppressWarnings("unchecked")
	private void loadWorlds() {
		try {
			for (final Class<?> c : Classes.getClassesInPackage(Main.class.getPackage().getName() + ".worlds"))
				if (World.class.isAssignableFrom(c)) worlds.add((Class<? extends World>) c);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		reset(0);
	}

	@SuppressWarnings("synthetic-access")
	private void createMenu() {
		menu("Control");
		menuItem("Reset", KeyEvent.VK_ENTER).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				reset(worldNo);
			}
		});
		menuItem("Start/Stop", KeyEvent.VK_P).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				pause = !pause;
				frame.setTitle(title());
			}
		});
		menuItem("Single step", KeyEvent.VK_SPACE).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				world.step(steps);
			}
		});
		menuSeparator();
		menuItem("Increase steps", KeyEvent.VK_PLUS).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				steps *= 2;
				System.out.println("steps = " + steps);
			}
		});
		menuItem("Decrease steps", KeyEvent.VK_MINUS).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (steps > 1) steps /= 2;
				System.out.println("steps = " + steps);
			}
		});
		menuSeparator();
		menuItem("Exit", KeyEvent.VK_ESCAPE).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.dispose();
			}
		});

		menu("Scenario");
		menuItem("Previous", KeyEvent.VK_PAGE_UP).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (worldNo > 0) reset(worldNo - 1);
			}
		});
		menuItem("Next", KeyEvent.VK_PAGE_DOWN).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (worldNo < worlds.size() - 1) reset(worldNo + 1);
			}
		});
		menuSeparator();
		int k = 0;
		for (final Class<? extends World> w : worlds) {
			final int kk = k++;
			menuItem(w.getName().substring(w.getName().lastIndexOf('.') + 1)).addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					reset(kk);
				}
			});
		}

		menu("Simulator");
		menuItem("Save screen", KeyEvent.VK_O).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				view.saveScreen = true;
			}
		});
		menuSeparator();
		menuItem("Add Box", KeyEvent.VK_1).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				world.add(new Body(Vector3.Zero, Quaternion.Identity, Shape.box(4, 4, 4), 1));
			}
		});
		menuItem("Add Sphere", KeyEvent.VK_2).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Shape s = Shape.sphere(4, 8);
				s.bicolor(Vector3.X, GLA.red, GLA.blue);
				world.add(new Body(Vector3.Zero, Quaternion.Identity, s, 1));
			}
		});
		menuItem("Shoot", KeyEvent.VK_Q).addActionListener(new ActionListener() {
			Body b = null;

			@Override
			public void actionPerformed(final ActionEvent e) {
				world.remove(b);
				final float yaw = GMath.deg2rad(world.camera.yaw);
				final Vector3 pos = world.camera.center.add(world.camera.distance / 2, new Vector3(Math.sin(yaw), 0,
						Math.cos(yaw)));
				b = new Body(pos, Quaternion.Identity, Shape.sphere(3, 8, new Vector3(0, 1, 1), GLA.green), 1);
				b.setLinVelocity(world.camera.center.sub(pos).unit().mul(20));
				world.add(b);
			}
		});
		//		menuItem("Position correction");
		//		menuItem("Air damping");
		//		menuItem("Gravity");
		//		menuItem("Friction");

		menu("View");
		menuItem("Contacts", KeyEvent.VK_C).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				view.contacts = !view.contacts;
			}
		});
		menuItem("Cordinate system", KeyEvent.VK_S).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				view.system = !view.system;
			}
		});
		menuItem("Wireframe", KeyEvent.VK_W).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				view.wireframe = !view.wireframe;
			}
		});

		// OLD GEOMETRY physics: 13335ms, detector: 1624ms
		menu("Misc");
		menuItem("Benchmark 1500 steps").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				//animator.stop();
				final boolean a = world.showTiming;
				world.showTiming = true;
				world.step(1500);
				world.showTiming = a;
				//animator.start();
			}
		});
		menuItem("Timings").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				world.showTiming = !world.showTiming;
			}
		});
		menuItem("Shape hints").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Shape.useHints = !Shape.useHints;
				System.out.println("Hints " + (Shape.useHints ? "on" : "off"));
			}
		});
	}

	public void saveComponentAsJPEG(final Component myComponent, final String filename) {
		final Dimension size = myComponent.getSize();
		final BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = myImage.createGraphics();
		myComponent.paintAll(g2);
		try {
			final OutputStream out = new FileOutputStream(filename);
			final JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(myImage);
			out.close();
		} catch (final Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
			if (world.pickBody != null) {
				world.remove(world.pickBody);
				world.pickBody = null;
			}
			break;
		default:
			world.keyDown(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		default:
			world.keyUp(e.getKeyCode());
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			startX = e.getPoint().x;
			startY = e.getPoint().y;
			startPitch = world.camera.pitch;
			startYaw = world.camera.yaw;
		}
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			view.pickX = e.getPoint().x;
			view.pickY = e.getPoint().y;
			canvas.display();
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			final int x = e.getPoint().x - startX;
			final int y = e.getPoint().y - startY;

			world.camera.pitch = GMath.clamp(startPitch - (float) y / 2, -90, 90);
			world.camera.yaw = startYaw - (float) x / 2;
		}
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		if (e.getWheelRotation() > 0)
			world.camera.distance *= 1.2;
		else if (world.camera.distance > 0.1) world.camera.distance /= 1.2;
	}

	private void reset(final int w) {
		try {
			final OrbitingCamera c = world != null ? world.camera : new OrbitingCamera();
			world = worlds.get(w).newInstance();
			world.camera = w != worldNo ? new OrbitingCamera() : c;
			view.world = world;
			worldNo = w;
			frame.setTitle(title());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String title() {
		return "World - " + world.getClass().getName() + (pause ? " [Paused]" : "");
	}

	public static void main(final String[] args) throws Exception {
		new Main();
	}
}