package tintor.commander;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class Panel extends Composite {
	private static boolean separateDirectories = true;

	private final RootSelector fileSystemSelector;
	private final Label filter;
	private final Table table;
	private final Label footer;
	private final TableColumn name, extension, size, date, attributes;

	private final Font bold;

	private File directory;
	private File[] files;
	private final Map<File, File> positions = new HashMap<File, File>();

	private final SelectionListener columnSelectionListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			final TableColumn column = (TableColumn) e.widget;
			if (table.getSortColumn() == column) {
				int dir = SWT.DOWN + SWT.UP - table.getSortDirection();
				column.setData("direction", dir);
				table.setSortDirection(dir);
			} else {
				table.setSortColumn(column);
				table.setSortDirection((Integer) column.getData("direction"));
			}
			openDirectory(directory);
		}
	};

	private final ControlListener columnControlListener = new ControlAdapter() {
		@Override public void controlResized(ControlEvent e) {}
	};

	private final Comparator<File> comparator = new Comparator<File>() {
		@Override public int compare(final File a, final File b) {
			if (separateDirectories) if (a.isDirectory())
				return b.isDirectory() ? compare(a.getName(), b.getName()) : -1;
			else if (b.isDirectory()) return 1;
			final TableColumn column = table.getSortColumn();
			return table.getSortDirection() == SWT.DOWN ? compare(column, a, b) : compare(column, b, a);
		}

		int compare(TableColumn column, File a, File b) {
			if (column == name) return compare(a.getName(), b.getName());
			if (column == extension)
				return compare(Util.getExtension(a.getName()), Util.getExtension(b.getName()));
			if (column == size) return compare(a.length(), b.length());
			if (column == date) return compare(a.lastModified(), b.lastModified());
			if (column == attributes) return compare(attributes(a), attributes(b));
			throw new RuntimeException();
		}

		int compare(final long a, final long b) {
			return a < b ? -1 : a == b ? 0 : 1;
		}

		// TODO natural sort order!
		int compare(final String a, final String b) {
			return String.CASE_INSENSITIVE_ORDER.compare(a, b);
		}
	};

	private TableColumn newColumn(final String text, final int width, final int style) {
		final TableColumn column = new TableColumn(table, style);
		column.setText(text);
		column.addSelectionListener(columnSelectionListener);
		column.addControlListener(columnControlListener);
		column.setData("direction", SWT.DOWN);
		column.setWidth(width);
		return column;
	}

	public Panel(final Composite parrent) {
		super(parrent, SWT.NONE);
		setLayout(new BorderLayout());
		addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(final DisposeEvent e) {
				bold.dispose();
			}
		});

		// Init FileSystemSelector
		fileSystemSelector = new RootSelector(this) {
			@Override protected boolean onSelect(final File root) {
				return openDirectory(root);
			}
		};
		fileSystemSelector.setLayoutData(BorderLayout.Top);

		// Init Path
		filter = new Label(this, SWT.NONE | SWT.LEFT);
		filter.setLayoutData(BorderLayout.Top);
		final FontData a = filter.getFont().getFontData()[0];
		a.setStyle(a.getStyle() | SWT.BOLD);
		bold = new Font(null, a);
		filter.setFont(bold);

		// Init Footer
		footer = new Label(this, SWT.NONE);
		footer.setLayoutData(BorderLayout.Bottom);
		footer.setFont(bold);

		// Init Table
		table = new Table(this, SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.VIRTUAL);
		setTabList(new Control[] { table });
		table.setLayoutData(BorderLayout.Center);
		table.setHeaderVisible(true);
		table.setFont(new Font(null, "Courier New", 9, 0));
		table.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetDefaultSelected(final SelectionEvent e) {
				openSelected();
			}
		});
		table.addControlListener(new ControlAdapter() {
			@Override public void controlResized(final ControlEvent e) {
				int width = 0;
				for (int i = 0; i < table.getColumnCount(); i++)
					width += table.getColumn(i).getWidth();

				final double k = (double) table.getSize().x / width;
				table.setRedraw(false);
				for (int i = 0; i < table.getColumnCount(); i++) {
					final int s = (int) (table.getColumn(i).getWidth() * k);
					table.getColumn(i).setWidth(s > 10 ? s : 10);
				}
				table.setRedraw(true);
			}
		});
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(final Event event) {
				final TableItem item = (TableItem) event.item;
				final File parent = directory.getParentFile();
				final boolean up = event.index == 0 && parent != null;
				final File file = parent == null ? files[event.index] : event.index == 0 ? parent
						: files[event.index - 1];

				item.setData(file);
				item.setImage(up ? Icons.get("arrow_turn_left") : Util.getFileIcon(file));
				item.setText(0, !file.isDirectory() ? Util.removeExtension(file.getName()) : up ? "[..]"
						: "[" + file.getName() + "]");
				item.setText(1, file.isDirectory() ? "" : Util.getExtension(file.getName()));
				item.setText(2, file.isDirectory() ? "" : Util.numberFormat.format(file.length()));
				item.setText(3, String.format("%1$tF %1$tH:%1$tM:%1$tS", new Date(file.lastModified())));
				item.setText(4, attributes(file));
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(final KeyEvent e) {
				switch (e.keyCode) {
				case SWT.BS:
					final File parent = directory.getParentFile();
					if (parent != null) openDirectory(parent);
					break;
				}
			}
		});

		// Init Columns
		name = newColumn("Name", 410, SWT.NONE);
		extension = newColumn("Ext", 70, SWT.NONE);
		size = newColumn("Size", 120, SWT.RIGHT);
		date = newColumn("Date", 180, SWT.NONE);
		attributes = newColumn("Attr", 60, SWT.NONE);

		table.setSortColumn(name);
		table.setSortDirection(SWT.DOWN);
	}

	private void openSelected() {
		final int sel = table.getSelectionIndex();
		assert sel != -1;
		final File file = (File) table.getItem(sel).getData();
		if (file.isDirectory())
			openDirectory(file);
		else {
			final Program p = Program.findProgram(Util.getExtension(file.getName()));
			if (p != null) {
				System.err.printf("About to execute '%s' with '%s'.\n", file.getName(), p.getName());
				p.execute(file.getAbsolutePath());
			}
			//new ProcessBuilder("c:/windows/notepad2.exe", file.getAbsolutePath()).start();
		}
	}

	public boolean openDirectory(final File dir) {
		table.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
		files = dir.listFiles();
		if (files == null) {
			Display.getCurrent().beep();
			table.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
			return false;
		} else {
			fileSystemSelector.select(Util.getRoot(dir));

			final File parent = dir.getParentFile();
			if (directory != null && directory.equals(parent)) positions.put(directory, dir);
			directory = dir;

			Arrays.sort(files, comparator);
			final int i = find(files, positions.get(directory));

			// filter
			final StringBuilder b = new StringBuilder(' ');
			b.append(directory.getAbsolutePath());
			if (b.charAt(b.length() - 1) != File.separatorChar) b.append(File.separatorChar);
			b.append('*');
			filter.setText(b.toString());

			// table
			table.setRedraw(false);
			table.clearAll();
			table.setItemCount(parent != null ? files.length + 1 : files.length);
			table.setSelection(i != -1 ? i : 0);
			table.setRedraw(true);

			// footer
			long size = 0;
			int fileCount = 0, dirCount = 0;
			for (final File f : files)
				if (f.isDirectory())
					dirCount++;
				else {
					size += f.length();
					fileCount++;
				}
			footer.setText(String.format("%s / %s in %d / %d files, %d / %d dir(s)", 0,
					Util.kilobytes(size), 0, fileCount, 0, dirCount));

			table.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
			return true;
		}
	}

	private static int find(final File[] files, final File file) {
		if (file != null) for (int i = 0; i < files.length; i++)
			if (file.equals(files[i])) return i;
		return -1;
	}

	// utilities
	private static String attributes(final File file) {
		final StringBuilder b = new StringBuilder(3);
		b.append(file.canRead() ? 'r' : '-');
		b.append(file.canWrite() ? 'w' : '-');
		b.append(file.canExecute() ? 'x' : '-');
		return b.toString();
	}
}
