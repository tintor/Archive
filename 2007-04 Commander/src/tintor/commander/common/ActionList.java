package tintor.commander.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ActionList extends AbstractAction {
	private final Action[] actions;

	public ActionList(final String name, final Action... actions) {
		super(name);
		this.actions = actions;
	}

	public ToolBar createToolbar(final Composite parent, final int style) {
		final ToolBar toolbar = new ToolBar(parent, style);
		for (final Action action : actions)
			if (action == null)
				new ToolItem(toolbar, SWT.SEPARATOR);
			else
				action.createToolItem(toolbar);
		return toolbar;
	}

	private final static SelectionListener toolItemSelectionListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			Object[] data = (Object[]) e.widget.getData();
			final ActionList actionList = (ActionList) data[0];
			final Menu menu = (Menu) data[1];

			final ToolItem item = (ToolItem) e.widget;
			if (e.detail == SWT.ARROW) {
				Point point = new Point(e.x, e.y);
				point = Display.getCurrent().map(item.getParent(), null, point);
				menu.setLocation(point);
				menu.setVisible(true);
			}
			actionList.onSelect();
		}
	};

	private final static SelectionListener menuItemSelectionListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			final Action action = (Action) e.widget.getData();
			action.onSelect();
		}
	};

	@Override public ToolItem createToolItem(final ToolBar parent) {
		final ToolItem item = new ToolItem(parent, SWT.DROP_DOWN);;
		item.setText(name);
		if (image != null) item.setImage(image);
		if (description != null) item.setToolTipText(description);

		final Menu menu = new Menu(parent.getShell());
		for (final Action action : actions)
			if (action == null)
				new MenuItem(menu, SWT.SEPARATOR);
			else
				action.createMenuItem(menu);

		item.setData(new Object[] { this, menu });
		item.addSelectionListener(toolItemSelectionListener);
		return item;
	}

	@Override public MenuItem createMenuItem(final Menu parent) {
		final MenuItem item = new MenuItem(parent, SWT.CASCADE);
		item.setText(name);
		if (image != null) item.setImage(image);

		final Menu menu = new Menu(item);
		for (final Action action : actions)
			if (action == null)
				new MenuItem(menu, SWT.SEPARATOR);
			else
				action.createMenuItem(menu);

		item.setMenu(menu);
		item.setData(this);
		item.addSelectionListener(menuItemSelectionListener);
		return item;
	}
}