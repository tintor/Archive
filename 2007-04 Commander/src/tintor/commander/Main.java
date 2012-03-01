package tintor.commander;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import tintor.commander.common.Action;
import tintor.commander.common.ActionList;

/**
 * @author Marko Tintor
 * @created 04/2007
 */
public class Main implements ShellListener, DisposeListener {
	private final Shell shell = new Shell();

	private final CommandLine commandLine;

	private final Composite panels;
	private final Panel left;
	private final Panel right;

	private final Action Exit = new Action("Exit", "Esc", SWT.PUSH, Icons.get("door_open")) {
		@Override public void onSelect() {
			shell.close();
		}
	};

	private final Action Refresh = new Action("Refresh", "Ctrl+R", SWT.PUSH, Icons.get("arrow_refresh")) {
		@Override public void onSelect() {
			if (left.isFocusControl()) System.out.println("left");
			if (right.isFocusControl()) System.out.println("right");
		}
	};

	private final Action NewFile = new Action("F2 New File", "F2", SWT.PUSH, Icons.get("page_white_add")) {
		@Override public void onSelect() {

		}
	};

	private final Action ViewFile = new Action("F3 View", "F3", SWT.PUSH, Icons.get("page_white_magnify")) {
		@Override public void onSelect() {

		}
	};

	private final Action EditFile = new Action("F4 Edit", "F4", SWT.PUSH, Icons.get("page_white_edit")) {
		@Override public void onSelect() {

		}
	};

	private final Action CopyFile = new Action("F5 Copy", "F5", SWT.PUSH, Icons.get("page_white_copy")) {
		@Override public void onSelect() {

		}
	};

	private final Action MoveFile = new Action("F6 Move", "F6", SWT.PUSH, Icons.get("page_white_gear")) {
		@Override public void onSelect() {

		}
	};

	private final Action NewDirectory = new Action("F7 New Directory", "F7", SWT.PUSH, Icons.get("folder_add")) {
		@Override public void onSelect() {

		}
	};

	private final ActionList TopToolbar = new ActionList(null, Refresh);

	private final ActionList BottomToolbar = new ActionList(null, NewFile, null, ViewFile, null, EditFile, null,
			CopyFile, null, MoveFile, null, NewDirectory);

	private final ActionList CommanderMenu = new ActionList("Commander", new Action("Preferences...", null,
			SWT.PUSH, Icons.get("wrench")), null, Exit);

	private final ActionList SelectMenu = new ActionList("Select");

	private final ActionList HelpMenu = new ActionList("Help");

	public Main() {
		// Init MenuBar
		final Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		CommanderMenu.createMenuItem(menubar);
		SelectMenu.createMenuItem(menubar);
		HelpMenu.createMenuItem(menubar);

		// Init TopToolbar
		final ToolBar topToolbar = TopToolbar.createToolbar(shell, SWT.HORIZONTAL | SWT.FLAT | SWT.SHADOW_OUT);
		topToolbar.setLayoutData(BorderLayout.Top);

		// Init BottomToolbar		
		final ToolBar bottomToolbar = BottomToolbar.createToolbar(shell, SWT.HORIZONTAL | SWT.FLAT
				| SWT.SHADOW_OUT);
		bottomToolbar.setLayoutData(BorderLayout.Bottom);
		bottomToolbar.addControlListener(new ControlAdapter() {
			@Override public void controlResized(final ControlEvent e) {
				int width = bottomToolbar.getSize().x;
				int buttons = 0;
				for (int i = 0; i < bottomToolbar.getItemCount(); i++) {
					final ToolItem item = bottomToolbar.getItem(i);
					if ((item.getStyle() & SWT.SEPARATOR) == 0) {
						width += item.getWidth();
						buttons++;
					}
				}
				width /= buttons;
				for (int i = 0; i < bottomToolbar.getItemCount(); i++) {
					final ToolItem item = bottomToolbar.getItem(i);
					if ((item.getStyle() & SWT.SEPARATOR) == 0) item.setWidth(width);
				}
			}
		});
		//		final FontData a = bottomToolbar.getFont().getFontData()[0];
		//		a.setStyle(a.getStyle() | SWT.BOLD);
		//		bottomToolbar.setFont(new Font(null, a));

		// Init CommandLine
		commandLine = new CommandLine(shell);
		commandLine.setLayoutData(BorderLayout.Bottom);

		// Init Panels
		panels = new Composite(shell, SWT.NO_BACKGROUND);
		panels.setLayoutData(BorderLayout.Center);
		panels.setLayout(new FillLayout(SWT.HORIZONTAL));

		left = new Panel(panels);
		right = new Panel(panels);
		left.openDirectory(new File("c:/"));
		right.openDirectory(new File("c:/"));

		// Init Shell
		shell.setImage(Icons.get("computer"));
		shell.setTabList(new Control[] { panels });
		shell.setText("Commander");
		shell.setMaximized(true);
		shell.setLayout(new BorderLayout());
		shell.addShellListener(this);
		shell.addDisposeListener(this);
		shell.layout();
		shell.open();
	}

	// DisposeListener
	@Override public void widgetDisposed(final DisposeEvent e) {
		Application.running = false;
	}

	// ShellListener
	@Override public void shellActivated(final ShellEvent e) {}

	@Override public void shellClosed(final ShellEvent e) {}

	@Override public void shellDeactivated(final ShellEvent e) {}

	@Override public void shellDeiconified(final ShellEvent e) {}

	@Override public void shellIconified(final ShellEvent e) {}
}