package gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Marko Tintor
 * @date 04/2006
 */
public class XMenu {
	private final String title;
	private final Object[] items;

	public XMenu(String title, Object... items) {
		this.title = title;
		this.items = items;
	}

	static class XMenuListener extends MenuAdapter {
		public void menuShown(MenuEvent e) {
			for(MenuItem a : ((Menu)e.widget).getItems()) {
				Object d = a.getData();
				if(d != null && d instanceof Action) ((Action)d).onShow();
			}
		}
	}
	final static XMenuListener ml = new XMenuListener();

	public static void createMenuBar(Shell shell, XMenu[] xmenu) {
		Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		for(XMenu x : xmenu)
			x.makeMenu(menubar);
	}

	public void makeMenu(Menu parent) {
		MenuItem mi = new MenuItem(parent, SWT.CASCADE);
		Menu m = new Menu(mi);
		m.addMenuListener(ml);
		mi.setMenu(m);
		mi.setText(title);
		for(Object item : items) {
			if(item == null)
				new MenuItem(m, SWT.SEPARATOR);
			else if(item instanceof Action)
				((Action)item).makeMenuItem(m);
			else if(item instanceof String)
				new MenuItem(m, SWT.NONE).setText((String)item);
			else if(item instanceof XMenu)
				((XMenu)item).makeMenu(m);
			else
				assert false;
		}
	}
}