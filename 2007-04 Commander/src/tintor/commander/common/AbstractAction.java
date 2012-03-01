package tintor.commander.common;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public abstract class AbstractAction {
	protected final String name;
	protected Image image;
	protected String description;

	public AbstractAction(final String name) {
		this.name = name;
	}

	public abstract ToolItem createToolItem(final ToolBar parent);

	public abstract MenuItem createMenuItem(final Menu parent);

	public void onSelect() {}
}