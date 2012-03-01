package tintor.netrek.client.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tintor.netrek.Const;
import tintor.netrek.client.Controller;
import tintor.netrek.client.model.Model;
import tintor.netrek.client.model.Planet;
import tintor.netrek.client.model.Ship;
import tintor.netrek.client.model.Torp;
import tintor.netrek.common.Race;
import tintor.netrek.common.ShipClass;

public class Window {
	public static void start() {
		panel.setBackground(Color.GRAY);

		frame.setSize(WindowSize * 2, WindowSize);
		frame.setContentPane(panel);

		frame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(final WindowEvent event) {
				System.exit(0);
			}
		});
		panel.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(final KeyEvent e) {
				Controller.keyPress(e.getKeyCode());
			}
		});
		panel.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(final MouseEvent e) {
				final int x = Model.myship.x + e.getX() - WindowSize / 2;
				final int y = Model.myship.y + e.getY() - WindowSize / 2;
				Controller.mousePressed(e.getButton(), x, y);
			}
		});

		frame.setVisible(true);

		final int dw = frame.getWidth() - panel.getWidth();
		final int dh = frame.getHeight() - panel.getHeight();
		frame.setSize(WindowSize * 2 + 15 + dw, WindowSize + 10 + dh);
	}

	static void drawLocal(final Graphics2D g) {
		g.setClip(local);

		g.setColor(Color.WHITE);
		final ShipClass sc = Model.myself.shipclass;
		g.drawString("fuel: " + Model.fuel + "/" + sc.maxfuel, 10, 10);
		g.drawString("shield: " + (int) (Model.shield + 0.5f) + "/" + sc.maxshield, 10, 22);
		g.drawString("damage: " + (int) (Model.hull + 0.5f) + "/" + sc.maxdamage, 10, 34);
		g.drawString("warp: " + Model.myship.warp + "/" + sc.maxspeed, 10, 46);
		g.drawString("torps: " + Model.torps, 10, 58);
		g.drawString("pos: " + Model.myship.x + ", " + Model.myship.y, 10, 70);

		final AffineTransform transform = AffineTransform.getTranslateInstance(local.getMinX() - Model.myship.x
				+ WindowSize / 2, local.getMinY() - Model.myship.y + WindowSize / 2);
		g.setTransform(transform);

		for (final Planet planet : Model.planets)
			drawPlanet(g, planet);

		g.setColor(Color.WHITE);
		final int radius = (int) Const.ShipRadius;
		for (final Ship ship : Model.ships) {
			final int sx = 2 * 20, sy = ship.direction * 20;
			g.drawImage(Images.fed, ship.x - 10, ship.y - 10, ship.x + 10 - 1, ship.y + 10 - 1, sx, sy,
					sx + 2 * 10 - 1, sy + 2 * 10 - 1, null);
			if (ship.shield) g.drawOval(ship.x - radius, ship.y - radius, radius * 2 - 1, radius * 2 - 1);
		}

		for (final Torp torp : Model.torps) {
			final int dx = torp.x, dy = torp.y;
			final int sx = 5, sy = 5 * torpAngle;
			g.drawImage(Images.torp, dx - 2, dy - 2, dx + 2, dy + 2, sx, sy, sx + 4, sy + 4, null);
		}

		g.setColor(Color.RED);
		g.drawRect(0, 0, Const.GalaxySize, Const.GalaxySize);
	}

	static void drawMap(final Graphics2D g) {
		g.setTransform(new AffineTransform());
		g.setClip(map);

		final float scale = (float) WindowSize / Const.GalaxySize;
		final AffineTransform transform2 = AffineTransform.getTranslateInstance(map.getMinX(), map.getMinY());
		transform2.scale(scale, scale);
		g.setTransform(transform2);

		for (final Planet planet : Model.planets)
			drawPlanet(g, planet);

		for (final Ship ship : Model.ships) {
			final int sx = 2 * 20, sy = ship.direction * 20;
			g.drawImage(Images.fed, ship.x - 10, ship.y - 10, ship.x + 10 - 1, ship.y + 10 - 1, sx, sy,
					sx + 2 * 10 - 1, sy + 2 * 10 - 1, null);

		}
		g.setColor(Color.WHITE);
		for (final Torp torp : Model.torps)
			g.drawLine(torp.x, torp.y, torp.x, torp.y);
	}

	private static void drawPlanet(final Graphics2D g2, final Planet planet) {
		final int sx = 120 * planet.race.ordinal();
		Image image = null;

		if (planet.race == Race.Unknown)
			image = Images.unknown;
		else if (planet.agri)
			image = Images.agri1;
		else if (planet.home)
			switch (planet.name.charAt(0)) {
			case 'e':
			case 'E':
				image = Images.earth;
				break;
			case 'r':
			case 'R':
				image = Images.romulus;
				break;
			case 'k':
			case 'K':
				image = Images.klingus;
				break;
			case 'o':
			case 'O':
				image = Images.orion;
				break;
			}
		else
			image = Images.rock1;

		g2.drawImage(image, planet.x - 16, planet.y - 16, planet.x + 15, planet.y + 15, sx, 0, sx + 119, 119, null);
		if (planet.repair) g2.drawImage(Images.wrench, planet.x - 15, planet.y - 26, 30, 11, null);
		if (planet.armies > 4) g2.drawImage(Images.army, planet.x - 26, planet.y - 15, 10, 30, null);
		if (planet.fuel) g2.drawImage(Images.fuel, planet.x + 15, planet.y - 15, 11, 30, null);

		g2.setColor(planet.race.color());
		g2.drawString(planet.name, planet.x - 14, planet.y + 20);
		g2.drawString(String.valueOf(planet.armies), planet.x - 26, planet.y - 26);
	}

	public static Point mouse() {
		final Point p = panel.getMousePosition();
		if (p != null) {
			p.x = Model.myship.x + p.x - WindowSize / 2;
			p.y = Model.myship.y + p.y - WindowSize / 2;
		}
		return p;
	}

	/** This method is called from main (!= swing) thread! */
	public static void update() {
		SwingUtilities.invokeLater(repaint);
		torpAngle = (torpAngle + 1) % 8;
	}

	private static final Runnable repaint = new Runnable() {
		@Override public void run() {
			frame.repaint();
		}
	};

	private static final JPanel panel = new JPanel() {
		@Override protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g;

			g2.setColor(Color.BLACK);
			g2.fill(local);
			g2.fill(map);

			g2.setFont(font);

			drawLocal(g2);
			drawMap(g2);
		}
	};

	static final JFrame frame = new JFrame("Client");
	static final Font font = new Font("Courier New", 0, 10);

	private static final int WindowSize = 500;
	static final Rectangle local = new Rectangle(5, 5, WindowSize, WindowSize);
	static final Rectangle map = new Rectangle(WindowSize + 10, 5, WindowSize, WindowSize);

	private static int torpAngle = 0;
}