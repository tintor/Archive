package tintor.sokoban.gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tintor.properties.Properties;
import tintor.properties.Property;
import tintor.properties.PropertyListener;
import tintor.sokoban.Cell;
import tintor.sokoban.Deadlock;
import tintor.sokoban.Dir;
import tintor.sokoban.Key;
import tintor.sokoban.Solver;
import tintor.sokoban.Util;
import tintor.stream.DoubleOutputStream;
import tintor.stream.WriterOutputStream;
import tintor.swt.Action;
import tintor.swt.Align;
import tintor.swt.MenuBuilder;
import tintor.swt.SWT2D;
import tintor.swt.TextWidgetWriter;
import tintor.swt.XMenu;

// TODO move animation
// TODO draw compressed tunnels
// TODO faster level loading / threads
// TODO auto carry box using mouse / when box is clicked all possible destinations are shown
// TODO render push distances with red triangles
// TODO generate level thumbnails
// TODO display some stats about level when it is loaded: number of boxes, total number of keys
// TODO store best solutions
// TODO solve level in background while user is also solving, also prioritize keys user has reached

// FIXME undo is bugy on microban:153
// FIXME solution tracing is bugy on original:1

// TODO show level name and id in title

/**
 * @author Marko Tintor
 * @created 04/2007
 */
public class Main implements PaintListener, KeyListener, MouseMoveListener, MouseTrackListener, MouseListener,
		SelectionListener, ShellListener, ControlListener {
	private static final Display display = new Display();
	private static final Main main = new Main();

	public static void main(final String[] args) {
		while (!main.shell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		display.dispose();
	}

	private final Properties properties = Properties.properties;
	private final Shell shell = new Shell();
	private final Model model = new Model();
	private final Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
	private final Text console = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL
			| SWT.DOUBLE_BUFFERED);

	private Action check(final String name, final boolean init) {
		final int a = name.lastIndexOf('.'), d = name.indexOf('|');
		final String text = d != -1 ? name.substring(a + 1, d) : name.substring(a + 1);
		final String key = name.substring(0, d).toLowerCase().replaceAll("\\s", "_");
		final Property<Boolean> property = properties.property(key, init);
		return new Action(text, d != -1 ? name.substring(d + 1) : null, Action.Check, property);
	}

	private Action check(final String name, final Property<Boolean> property) {
		final int d = name.indexOf('|');
		final String text = d != -1 ? name.substring(0, d) : name;
		return new Action(text, d != -1 ? name.substring(d + 1) : null, Action.Check, property);
	}

	private final SelectionListener openMenuListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			load(e.widget.getData().toString(), 1);
		}
	};

	private void createDirectoryMenu(final MenuItem parent, final File directory) {
		final Menu menu = new Menu(parent);
		parent.setMenu(menu);

		final File[] files = directory.listFiles();
		for (final File file : files)
			if (file.isDirectory()) {
				final MenuItem item = new MenuItem(menu, SWT.CASCADE);
				item.setText(file.getName());
				createDirectoryMenu(item, file);
			}
		for (final File file : files)
			if (!file.isDirectory()) {
				final String name = file.getName();
				if (name.substring(name.length() - 5).equalsIgnoreCase(".soko")) {
					final MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setData(file.getPath());
					item.addSelectionListener(openMenuListener);
					item.setText(name.substring(0, name.length() - 5).toLowerCase());
				}
			}
	}

	private final Action Reset = new Action("Reset", "Multiply") {
		@Override public void onSelect() {
			load(properties.getString("level.file"), properties.getInt("level.id"));
		}
	};

	private final XMenu Sokoban = new XMenu("Sokoban", new Action("Undo", "Subtract") {
		@Override public void onSelect() {
			if (model.undo()) canvas.redraw();
		}
	}, new Action("Redo", "Add") {
		@Override public void onSelect() {
			if (model.redo()) canvas.redraw();
		}
	}, Reset, null, new Action("Open", null, Action.Cascade) {
		@Override public void onShow() {
			Menu menu = menuItem.getMenu();
			if (menu != null) menu.dispose();
			menuItem.setMenu(null);
			createDirectoryMenu(menuItem, new File(properties.get("levels_directory", "levels")));
		}
	}, new Action("Open File...", "Enter") {
		@Override public void onSelect() {
			System.out.println("open from");
		}
	}, new Action("Open Prev", "PageUp") {
		@Override public void onSelect() {
			load(properties.getString("level.file"), properties.getInt("level.id") - 1);
		}
	}, new Action("Open Next", "PageDown") {
		@Override public void onSelect() {
			load(properties.getString("level.file"), properties.getInt("level.id") + 1);
		}
	}, null, new Action("Solve", "Space") {
		@Override public void onSelect() {
			final Thread a = new Thread() {
				@Override public void run() {
					int d = model.key.distance;
					System.gc();
					final Key result = Solver.solve(model.key);
					if (result == null)
						System.out.println("no solution!");
					else {
						// TODO configurable in menu
						//						for (Key k : Util.expand(result))
						//							System.out.println(k);
						System.out.printf("solution in %s pushes!\n", result.distance - d);
						model.solution(result);
					}
				}
			};
			a.setDaemon(true);
			a.setPriority(Thread.MIN_PRIORITY);
			a.start();
		}
	}, null, new Action("Exit", "Esc") {
		@Override public void onSelect() {
			main.shell.close();
		}
	});

	private final XMenu View = new XMenu("View", "Problem Set", null, check("show.Agent|Alt+A", true), check(
			"show.Boxes|Alt+B", true), check("show.Dead Cells|Alt+D", true),
			check("show.Goals|Alt+G", true), check("show.Matching Goals|Alt+M", false), check(
					"show.Push Distances|Alt+P", false), check("show.Articulations|Alt+R", false),
			check("show.Unreachable|Alt+U", true), check("show.Tunnel Lengths|Alt+T", false));

	private final XMenu Settings = new XMenu("Settings", check("Remove Deadends", Util.removeDeadends), check(
			"Compress Tunnels", Util.removeDeadends), null, check("Separate Simple Goal Rooms",
			Solver.separateGoalRooms), null, new XMenu("Heuristic", "? Closest Goals", "? Matching Goals"),
			new XMenu("Deadlock testing", check("Fast Block", Deadlock.fastBlock), check("Frozen Boxes",
					Deadlock.frozenBoxes)), check("Macro Expansion", Key.macroExpansion));

	private final XMenu Misc = new XMenu("Misc", new Action("Clear Console") {
		@Override public void onSelect() {
			console.setText("");
		}
	});

	private final XMenu[] menus = { Sokoban, Settings, View, Misc };

	public Main() {
		try {
			final String consoleFile = String.format("logs/%1$tF-%1$tH-%1$tM-%1$tS", new Date());
			final OutputStream consoleOutputStream = new BufferedOutputStream(new DoubleOutputStream(
					new WriterOutputStream(new TextWidgetWriter(console)), new FileOutputStream(
							consoleFile)));
			System.setOut(new PrintStream(consoleOutputStream, true));
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		// init properties
		properties.start(Properties.DefaultFile, true, false);
		properties.addListener("show", new PropertyListener() {
			@Override public void propertyChange(final String key, final Object oldValue,
					final Object newValue) {
				canvas.redraw();
			}
		});
		final PropertyListener a = new PropertyListener() {
			@Override public void propertyChange(final String key, final Object oldValue,
					final Object newValue) {
				Reset.onSelect();
			}
		};
		Util.compressTunnels.addListener(a);
		Util.removeDeadends.addListener(a);

		// init console
		console.setFont(new Font(null, properties.get(Main.class, "console.font.name", "Courier New"),
				properties.get(Main.class, "console.font.size", 9), SWT.NORMAL));
		final Color color = console.getBackground();
		console.setEditable(false);
		console.setBackground(color);

		// init shell
		shell.setMaximized(properties.get(Main.class, "shell.maximized", true));

		final List<Integer> location = properties.get(Main.class, "shell.location", Arrays.asList(0, 0));
		shell.setLocation(new Point(location.get(0), location.get(1)));

		final Rectangle rect = display.getBounds();
		final Point size = new Point(rect.width - location.get(0), rect.height - location.get(1));
		shell.setSize(properties.get("shell.size", size));

		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setFocus();
		shell.addKeyListener(this);
		shell.addShellListener(this);
		shell.addControlListener(this);

		// init canvas
		canvas.setBackground(gray30);
		canvas.addPaintListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addMouseTrackListener(this);
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		canvas.setFocus();

		// init menu bar
		final Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		final MenuBuilder builder = new MenuBuilder();
		for (final XMenu xmenu : menus)
			builder.create(menubar, xmenu);

		load(properties.get("level.file", "levels/original"), properties.get("level.id", 1));

		shell.open();
	}

	@Override public void controlMoved(final ControlEvent e) {
		if (!shell.getMaximized()) properties.set("shell.location", shell.getLocation());
	}

	@Override public void controlResized(final ControlEvent e) {
		properties.set("shell.maximized", shell.getMaximized());
		if (!shell.getMaximized()) properties.set("shell.size", shell.getSize());
	}

	@Override public void shellActivated(final ShellEvent e) {}

	@Override public void shellClosed(final ShellEvent e) {
		properties.stop();
	}

	@Override public void shellDeactivated(final ShellEvent e) {}

	@Override public void shellDeiconified(final ShellEvent e) {}

	@Override public void shellIconified(final ShellEvent e) {}

	@Override public void mouseDoubleClick(final MouseEvent e) {}

	@Override public void mouseDown(final MouseEvent e) {
		if (cursor != null && model.cachedReachable.contains(cursor)) {
			model.agent = cursor;
			canvas.redraw();
		}
	}

	@Override public void mouseUp(final MouseEvent e) {}

	@Override public void mouseMove(final MouseEvent e) {
		boolean redraw = false;
		if (e.x / cellSize != cursorX) {
			cursorX = e.x / cellSize;
			redraw = true;
		}
		if (e.y / cellSize != cursorY) {
			cursorY = e.y / cellSize;
			redraw = true;
		}
		if (redraw) canvas.redraw();
	}

	@Override public void mouseEnter(final MouseEvent e) {}

	@Override public void mouseExit(final MouseEvent e) {
		cursor = null;
		cursorX = -1;
		cursorY = -1;
		canvas.redraw();
	}

	@Override public void mouseHover(final MouseEvent e) {}

	@Override public void widgetDefaultSelected(final SelectionEvent e) {}

	@Override public void widgetSelected(final SelectionEvent e) {}

	@Override public void keyPressed(final KeyEvent e) {
		switch (e.keyCode) {
		case SWT.ARROW_LEFT:
			if (properties.getBool("show.agent") && model.move(Dir.West)) canvas.redraw();
			break;
		case SWT.ARROW_RIGHT:
			if (properties.getBool("show.agent") && model.move(Dir.East)) canvas.redraw();
			break;
		case SWT.ARROW_UP:
			if (properties.getBool("show.agent") && model.move(Dir.North)) canvas.redraw();
			break;
		case SWT.ARROW_DOWN:
			if (properties.getBool("show.agent") && model.move(Dir.South)) canvas.redraw();
			break;
		}
	}

	private void load(final String file, final int levelId) {
		if (model.load(file, levelId)) {
			properties.set("level.file", file);
			properties.set("level.id", levelId);
			shell.setText(properties.get("shell.title", "Markoban") + " - " + file + " - " + levelId);
			canvas.redraw();
		}
	}

	@Override public void keyReleased(final KeyEvent e) {}

	final Color green = new Color(null, 0, 255, 0);
	final Color darkGreen = new Color(null, 0, 127 + 64 + 32, 0);
	final Color lightBlue = new Color(null, 0, 127, 255);
	final Color blue = new Color(null, 0, 0, 255);
	final Color black = new Color(null, 0, 0, 0);
	final Color white = new Color(null, 255, 255, 255);
	final Color red = new Color(null, 255, 0, 0);
	final Color orange = new Color(null, 255, 200, 0);
	final Color gray75 = new Color(null, (int) (255 * 0.75), (int) (255 * 0.75), (int) (255 * 0.75));
	final Color gray60 = new Color(null, (int) (255 * 0.6), (int) (255 * 0.6), (int) (255 * 0.6));
	final Color gray30 = new Color(null, (int) (255 * 0.3), (int) (255 * 0.3), (int) (255 * 0.3));

	int cellSize = 30, cursorX, cursorY;
	Cell cursor = null;

	@Override public void paintControl(final PaintEvent e) {
		e.gc.setAdvanced(true);
		cursor = null;

		for (final Cell c : model.cachedCells) {
			e.gc.setTransform(new Transform(null, 1, 0, 0, 1, c.x * cellSize, c.y * cellSize));
			if (c.x == cursorX && c.y == cursorY) cursor = c;

			if (properties.get("show.boxes", true) && model.key.hasBox(c))
				e.gc.setBackground(orange);
			else if (properties.get("show.dead_cells", true) && c.dead())
				e.gc.setBackground(gray60);
			else
				e.gc.setBackground(gray75);
			e.gc.fillRectangle(0, 0, cellSize, cellSize);

			// Compressed tunnels
			final int tunnelSize = cellSize / 4, tunnelOff = (cellSize - tunnelSize) / 2;
			if (c.tunnel(Dir.East) > 0) {
				e.gc.setBackground(c.dead() || c.east().dead() ? gray60 : gray75);
				e.gc.fillRectangle(cellSize, tunnelOff, c.tunnel(Dir.East) * cellSize, tunnelSize);
			}
			if (c.tunnel(Dir.South) > 0) {
				e.gc.setBackground(c.dead() || c.south().dead() ? gray60 : gray75);
				e.gc.fillRectangle(tunnelOff, cellSize, tunnelSize, c.tunnel(Dir.South) * cellSize);
			}

			// Agent
			if (properties.getBool("show.agent") && c == model.agent) {
				e.gc.setBackground(lightBlue);
				final int x = cellSize / 2, y = cellSize / 2, s = (int) (cellSize * 0.45);
				e.gc.fillPolygon(new int[] { x - s, y, x, y + s, x + s, y, x, y - s });
			}

			// Articulations
			if (properties.getBool("show.articulations") && c.isArticulation()) {
				e.gc.setForeground(green);
				e.gc.setLineWidth(2);
				final double k = 0.60;
				final int off = (int) (cellSize * (1 - k) / 2), rad = (int) (cellSize * k);
				e.gc.drawOval(off, off, rad, rad);
				e.gc.setLineWidth(1);
			}

			if (properties.getBool("show.goals") && c.isGoal()) {
				e.gc.setBackground(red);
				final double k = 0.3;
				final int off = (int) (cellSize * (1 - k) / 2), rad = (int) (cellSize * k);
				e.gc.fillOval(off, off, rad, rad);
			}

			if (properties.getBool("show.unreachable") && !model.cachedReachable.contains(c)
					&& !canReachAndPush(c)) {
				e.gc.setForeground(black);
				final int lines = 3;
				final double d = cellSize / (lines + 1.0);
				for (int i = 1; i <= lines + 1; i++)
					e.gc.drawLine(0, (int) (i * d), (int) (i * d), 0);
				for (int i = 1; i <= lines; i++)
					e.gc.drawLine((int) (i * d), cellSize, cellSize, (int) (i * d));
			}
		}

		if (properties.getBool("show.matching_goals")) {
			e.gc.setTransform(new Transform(null, 1, 0, 0, 1, cellSize / 2, cellSize / 2));
			for (int i = 0; i < model.key.boxes(); i++) {
				final Cell box = model.key.box(i), goal = model.matchingGoals.goals[i];
				e.gc.drawLine(box.x * cellSize, box.y * cellSize, goal.x * cellSize, goal.y * cellSize);
			}
		}

		if (cursor != null) {
			e.gc.setTransform(new Transform(null, 1, 0, 0, 1, cursorX * cellSize, cursorY * cellSize));
			e.gc.setBackground(blue);
			final int alpha = e.gc.getAlpha();
			e.gc.setAlpha(100);
			e.gc.fillRectangle(0, 0, cellSize, cellSize);
			e.gc.setAlpha(alpha);

			if (properties.getBool("show.tunnel_lengths")) {
				e.gc.setForeground(white);
				e.gc.setFont(new Font(null, "Arial", 10, 0));
				for (final Dir dir : Dir.values())
					SWT2D.drawText(e.gc, (1 + dir.deltaX()) * cellSize / 2, (1 + dir.deltaY())
							* cellSize / 2, Align.Center, Align.Center, cursor.tunnel(dir));
			}
		}

		if (properties.getBool("show.push_distances") && cursor != null) {
			e.gc.setTransform(null);
			e.gc.setForeground(white);
			e.gc.setFont(new Font(null, "Arial", 13, 0));
			for (final Map.Entry<Cell, Integer> entry : cursor.pushes().entrySet()) {
				final Cell c = entry.getKey();
				if (c != cursor)
					SWT2D.drawText(e.gc, c.x * cellSize + cellSize / 2, c.y * cellSize + cellSize / 2,
							Align.Center, Align.Center, entry.getValue());
			}
		}
	}

	private boolean canReachAndPush(final Cell box) {
		if (!model.key.hasBox(box)) return false;
		for (final Cell.Edge e : box.edges())
			if (model.cachedReachable.contains(box.get(e.dir.opposite())) && model.key.acceptsBox(e.cell))
				return true;
		return false;
	}
}