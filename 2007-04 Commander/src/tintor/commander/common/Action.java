package tintor.commander.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import tintor.commander.Util;

public class Action extends AbstractAction {
	private final String accelerator;
	private final int style;
	private boolean checked;

	public Action(final String name, final String accelerator, final int style, final Object... args) {
		super(name);
		if (style != SWT.PUSH && style != SWT.CHECK && style != SWT.RADIO)
			throw new IllegalArgumentException();
		this.accelerator = accelerator;
		this.style = style;
		for (final Object arg : args)
			if (arg instanceof Image)
				image = (Image) arg;
			else if (arg instanceof String) description = (String) arg;
	}

	private final static SelectionListener toolItemSelectionListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			final Action action = (Action) e.widget.getData();
			final ToolItem item = (ToolItem) e.widget;
			action.checked = item.getSelection();
			action.onSelect();
		}
	};

	private final static SelectionListener menuItemSelectionListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			final Action action = (Action) e.widget.getData();
			final MenuItem item = (MenuItem) e.widget;
			action.checked = item.getSelection();
			action.onSelect();
		}
	};

	@Override public ToolItem createToolItem(final ToolBar parent) {
		final ToolItem item = new ToolItem(parent, style != SWT.CASCADE ? style : SWT.DROP_DOWN);
		item.setText(name);
		if (image != null) item.setImage(image);
		if (description != null) item.setToolTipText(description);
		item.setSelection(checked);

		item.setData(this);
		item.addSelectionListener(toolItemSelectionListener);
		return item;
	}

	@Override public MenuItem createMenuItem(final Menu parent) {
		final MenuItem item = new MenuItem(parent, style);
		item.setText(accelerator != null ? name + "\t" + accelerator : name);
		if (image != null) item.setImage(image);
		item.setSelection(checked);
		if (accelerator != null) item.setAccelerator(Util.accelerator(accelerator));

		item.setData(this);
		item.addSelectionListener(menuItemSelectionListener);
		return item;
	}
}