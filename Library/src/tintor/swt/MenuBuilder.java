package tintor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;


public class MenuBuilder {
	private static final MenuListener menuListener = new MenuListener() {
		@Override public void menuShown(MenuEvent e) {
			for (MenuItem a : ((Menu) e.widget).getItems()) {
				Object d = a.getData();
				if (d instanceof Action) ((Action) d).onShow();
			}
		}

		@Override public void menuHidden(MenuEvent e) {
			for (MenuItem a : ((Menu) e.widget).getItems()) {
				Object d = a.getData();
				if (d instanceof Action) ((Action) d).onHide();
			}
		}
	};

	public MenuItem create(Menu parent, XMenu xmenu) {
		final MenuItem i = new MenuItem(parent, SWT.CASCADE);
		final Menu menu = new Menu(i);

		menu.addMenuListener(menuListener);

		i.setMenu(menu);
		i.setText(xmenu.text);

		for (Object e : xmenu.items)
			createMenuItem(menu, e);
		return i;
	}

	protected MenuItem createMenuItem(Menu menu, Object item) {
		if (item == null) return new MenuItem(menu, SWT.SEPARATOR);
		if (item instanceof XMenu) return create(menu, (XMenu) item);
		if (item instanceof Action) return ((Action) item).createMenuItem(menu);
		return newMenuItem(menu, item.toString());
	}

	public static MenuItem newMenuItem(Menu menu, String title) {
		final MenuItem i = new MenuItem(menu, SWT.NONE);
		i.setText(title);
		return i;
	}
}