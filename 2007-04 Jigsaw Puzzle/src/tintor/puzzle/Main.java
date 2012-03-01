/**
 * ИР4РГ Рачунарска графика
 * први домаћи задатак
 * 
 * @author Марко Тинтор
 * @date 2007-04 
 */
package tintor.puzzle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

class Piece {
	final Vector2i id;

	Vector2 position;
	Vector2i cannon;
	int angle;

	Shape shape; // in local coordinates
	Vector2i texture; // center of piece in image coordinates

	Piece(final Vector2i id) {
		this.id = id;
	}

	boolean contains(final Vector2 a) {
		final Vector2 ap = a.sub(position).rotate(-angle);
		return shape.contains(ap.x, ap.y);
	}

	void draw(final Graphics2D g2, final double deltaAngle, final Image image, final boolean border) {
		// set transform
		final AffineTransform transform = g2.getTransform();
		g2.translate(position.x, position.y);
		if (angle > 0) g2.rotate(angle * deltaAngle);
		// set clip
		final Shape clip = g2.getClip();
		g2.setClip(shape);

		// draw
		g2.drawImage(image, -texture.x, -texture.y, null);

		// restore clip
		g2.setClip(clip);

		if (border) {
			g2.setColor(Color.WHITE);
			g2.setPaint(Color.WHITE);
			g2.draw(shape);
		}

		// restore transform
		g2.setTransform(transform);
	}
}

@SuppressWarnings("serial") class Puzzle extends Component implements MouseListener, MouseMotionListener,
		MouseWheelListener, Runnable {
	BufferedImage image;
	List<Piece> drawOrder;
	Cutting cutting;

	enum State {
		Preview, Playing
	}

	State state;
	int angles;

	double deltaAngle;
	double pieceSize;

	Puzzle() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

		new Thread(this).start();
	}

	static BufferedImage resize(final BufferedImage image, final int width, final int height) {
		final BufferedImage a = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		a.getGraphics().drawImage(image, 0, 0, width, height, null);
		return a;
	}

	final static int maxWidth = 1020, maxHeight = 800;

	void init(final String imageName, final Cutting cutting, final int columns, final int rows, final double size,
			final Vector2 offset) {
		this.cutting = cutting;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(imageName + "c.jpg"));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
			final double scale = Math
					.min((double) maxWidth / image.getWidth(), (double) maxHeight / image.getHeight());
			image = resize(image, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale));
		}

		deltaAngle = 2 * Math.PI / cutting.angles;
		drawOrder = cutting.cut(columns, rows, size, offset);

		angles = cutting.angles;
		state = State.Preview;
		time = 0;
		Main.frame.setTitle("Слагалица");
	}

	void shuffle() {
		final Random rand = new Random();

		for (int a = 0; a < drawOrder.size(); a++)
			drawOrder.get(a).angle = rand.nextInt(angles);

		for (int a = 0; a < drawOrder.size(); a++) {
			final int b = rand.nextInt(drawOrder.size());
			// swap a and b
			final Piece pa = drawOrder.get(a);
			final Piece pb = drawOrder.get(b);

			final Vector2i t = pa.cannon;
			pa.cannon = pb.cannon;
			pb.cannon = t;

			final Vector2 p = pa.position;
			pa.position = pb.position;
			pb.position = p;
		}

		repaint();
	}

	int time;

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {}
			if (state == State.Playing) {
				time++;
				Main.frame.setTitle(String.format("Слагалица %d:%02d", time / 60, time % 60));
			}
		}
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(maxWidth, maxHeight);
	}

	@Override public void paint(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		for (final Piece p : drawOrder)
			p.draw(g2, deltaAngle, image, state == State.Playing);
	}

	Piece pickPiece(final int x, final int y) {
		final Vector2 a = new Vector2(x, y);
		for (int i = drawOrder.size() - 1; i >= 0; i--) {
			final Piece piece = drawOrder.get(i);
			if (piece.contains(a)) {
				drawOrder.remove(i);
				drawOrder.add(piece);
				return piece;
			}
		}
		return null;
	}

	@Override public void mouseClicked(final MouseEvent e) {
		if (state == State.Preview) {
			shuffle();
			state = State.Playing;
			time = 0;
		}
	}

	@Override public void mouseEntered(final MouseEvent e) {}

	@Override public void mouseExited(final MouseEvent e) {}

	Piece picked;
	int startX, startY;
	Vector2 start;

	@Override public void mousePressed(final MouseEvent e) {
		if (state != State.Playing) return;
		if (e.getButton() == MouseEvent.BUTTON1) {
			picked = pickPiece(e.getX(), e.getY());
			if (picked != null) {
				startX = e.getX();
				startY = e.getY();
				start = picked.position;
				repaint();
			}
		}
	}

	@Override public void mouseReleased(final MouseEvent e) {
		if (state != State.Playing) return;
		if (e.getButton() == MouseEvent.BUTTON1 && picked != null) {
			snap();
			picked = null;

			if (solved()) {
				state = State.Preview;
				repaint();
				final int[] times = difficulty != 0 ? Main.scoreTime[difficulty - 1] : null;
				if (difficulty != 0 && (times[4] == 0 || time < times[4])) {
					final String s = (String) JOptionPane.showInputDialog(Main.frame,
							"Постигли сте једно од најбољих пет времена!\n" + "Упишите своје име:",
							"Топ листа", JOptionPane.PLAIN_MESSAGE, null, null, "");
					if (s != null) {
						final String[] names = Main.scoreName[difficulty - 1];
						for (int i = 0; i < times.length; i++)
							if (time < times[i]) {
								// shift others
								for (int j = times.length - 1; j > i; j--) {
									times[j] = times[j - 1];
									names[j] = names[j - 1];
								}
								times[i] = time;
								names[i] = s;
								break;
							}
						Main.saveScores();
						Main.refreshScoreMenu();
					}
				}
			} else
				repaint();
		}
	}

	int difficulty = 0;

	private boolean solved() {
		for (final Piece p : drawOrder)
			if (p.angle != 0) return false;
		if (cutting instanceof Squares) {
			for (final Piece p : drawOrder)
				if (p.cannon.x != p.id.x || p.cannon.y != p.id.y) return false;
			return true;
		}
		return false;
	}

	@Override public void mouseDragged(final MouseEvent e) {
		if (state != State.Playing) return;
		final boolean button1 = (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK;
		final Vector2 d = new Vector2(e.getX() - startX, e.getY() - startY);
		if (button1 && picked != null) {
			picked.position = start.add(d);
			repaint();
		}
	}

	@Override public void mouseMoved(final MouseEvent e) {}

	@Override public void mouseWheelMoved(final MouseWheelEvent e) {
		if (state != State.Playing) return;
		if (picked != null) picked.angle = (picked.angle + e.getWheelRotation() + cutting.angles) % cutting.angles;
		repaint();
	}

	void snap() {
		picked.cannon = cutting.cannon(picked.position);
		picked.position = cutting.uncannon(picked.cannon);
	}

	//	Piece grid(final int column, final int row) {
	//		return row >= 0 && row < grid.length && column >= 0 && column < grid[0].length ? grid[row][column]
	//				: null;
	//	}
}

public class Main {
	final Puzzle puzzle = new Puzzle();
	static final JFrame frame = new JFrame("Слагалица");
	static final JMenu scoreMenu = new JMenu("Резултати");

	static int scoreTime[][] = new int[3][5];
	static String scoreName[][] = new String[3][5];

	static String picture = "images/nemo4c.bmp";
	static int dimX = 8, dimY = 10;
	static int pieceSize = 120;
	static Cutting cutting = new Triangles();

	public static void loadScores() {
		try {
			final BufferedReader reader = new BufferedReader(new FileReader("scores.txt"));
			for (int a = 0; a < 3; a++)
				for (int i = 0; i < 5; i++) {
					final String[] s = reader.readLine().split("\\s+");
					scoreTime[a][i] = Integer.parseInt(s[0]);
					scoreName[a][i] = s.length > 1 ? s[1] : "";
				}
			reader.close();
		} catch (final IOException e) {}
	}

	public static void saveScores() {
		try {
			final Writer writer = new FileWriter("scores.txt");
			for (int a = 0; a < 3; a++)
				for (int i = 0; i < 5; i++)
					writer.write(scoreTime[a][i] + " " + scoreName[a][i] + "\n");
			writer.close();
		} catch (final IOException e) {}
	}

	public static void refreshScoreMenu() {
		if (scoreMenu.getItemCount() == 3) {
			scoreMenu.remove(2);
			scoreMenu.remove(1);
			scoreMenu.remove(0);
		}
		final String[] types = new String[] { "Лака", "Средња", "Тешка" };
		for (int n = 0; n < types.length; n++) {
			final JMenu submenu = new JMenu(types[n]);
			for (int i = 0; i < 5; i++)
				submenu.add(new JMenuItem(String.format("%d:%02d %s", scoreTime[n][i] / 60, scoreTime[n][i] % 60,
						scoreName[n][i])));
			scoreMenu.add(submenu);
		}
	}

	public Main() {
		loadScores();

		puzzle.difficulty = 1;
		puzzle.init("nemo3", new Squares(), 5, 4, 150, new Vector2(0, 0));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(puzzle);

		// create Menu
		JMenuBar menuBar;
		JMenu menu;
		JMenu submenu;
		JMenuItem menuItem;

		menuBar = new JMenuBar();

		menu = new JMenu("Слагалица");
		menuBar.add(menu);
		menuItem = new JMenuItem("Лака");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				puzzle.difficulty = 1;
				puzzle.init("nemo3", new Squares(), 5, 4, 150, new Vector2(0, 0));
				puzzle.repaint();
			}
		});
		menu.add(menuItem);
		menuItem = new JMenuItem("Средња");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				puzzle.difficulty = 2;
				puzzle.init("nemo2", new Hexagons(), 6, 8, 150, new Vector2(0, 0));
				puzzle.repaint();
			}
		});
		menu.add(menuItem);
		menuItem = new JMenuItem("Тешка");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				puzzle.difficulty = 3;
				puzzle.init("nemo4", new Triangles(), 8, 10, 120, new Vector2(0, 0));
				puzzle.repaint();
			}
		});
		menu.add(menuItem);
		menuItem = new JMenuItem("Произвољна");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				puzzle.difficulty = 0;
				puzzle.init(picture, cutting, dimX, dimY, pieceSize, new Vector2(0, 0));
				puzzle.repaint();
			}
		});
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem("Излаз");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});
		menu.add(menuItem);

		menu = new JMenu("Подешавања");
		menuBar.add(menu);

		menuItem = new JMenuItem("Слика...");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setSelectedFile(new File(picture));
				if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
					picture = fc.getSelectedFile().getAbsolutePath();
			}
		});
		menu.add(menuItem);

		submenu = new JMenu("Хоризонтална величина");
		for (int i = 4; i <= 10; i++) {
			final JMenuItem m = new JMenuItem("" + i);
			final int d = i;
			m.addActionListener(new ActionListener() {
				@Override public void actionPerformed(final ActionEvent e) {
					dimX = d;
				}
			});
			submenu.add(m);
		}
		menu.add(submenu);

		submenu = new JMenu("Вертикална величина");
		for (int i = 4; i <= 10; i++) {
			final JMenuItem m = new JMenuItem("" + i);
			final int d = i;
			m.addActionListener(new ActionListener() {
				@Override public void actionPerformed(final ActionEvent e) {
					dimY = d;
				}
			});
			submenu.add(m);
		}
		menu.add(submenu);

		submenu = new JMenu("Величина делића");
		for (int i = 50; i <= 150; i += 10) {
			final JMenuItem m = new JMenuItem("" + i);
			final int d = i;
			m.addActionListener(new ActionListener() {
				@Override public void actionPerformed(final ActionEvent e) {
					pieceSize = d;
				}
			});
			submenu.add(m);
		}
		menu.add(submenu);

		submenu = new JMenu("Тип слагалице");
		menuItem = new JMenuItem("Квадратна");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				cutting = new Squares();
			}
		});
		submenu.add(menuItem);
		menuItem = new JMenuItem("Шестоугаона");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				cutting = new Hexagons();
			}
		});
		submenu.add(menuItem);
		menuItem = new JMenuItem("Троугаона");
		menuItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(final ActionEvent e) {
				cutting = new Triangles();
			}
		});
		submenu.add(menuItem);

		menu.add(submenu);

		menuBar.add(scoreMenu);
		refreshScoreMenu();

		frame.setJMenuBar(menuBar);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(final String[] args) throws Exception {
		new Main();
	}
}