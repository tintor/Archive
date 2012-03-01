package gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author Marko Tintor
 * @date 04/2006
 */
public class Action {
	private final String title;
	private int style;
	protected MenuItem menuItem;

	public Action(String acc) {
		this(acc, 0);
	}

	public Action(String title, int type) {
		this.title = title;
		switch(type) {
		case 0:
			style = SWT.NONE;
			break;
		case 1:
			style = SWT.CHECK;
			break;
		default:
			assert false;
		}
	}

	protected void run() {}

	protected void onShow() {}

	private static class ActionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			((Action)e.widget.getData()).run();
		}
	}
	final static ActionListener actionListener = new ActionListener();

	public void makeMenuItem(Menu m) {
		assert menuItem == null;
		menuItem = new MenuItem(m, style);
		menuItem.setData(this);
		menuItem.addSelectionListener(actionListener);
		menuItem.setText(title);

		if(title.indexOf('\t') == -1) return;

		int key = 0;
		String a = title.substring(title.indexOf('\t') + 1);
		while(a.indexOf('+') > 0)
			if(a.startsWith("Ctrl+")) {
				key |= SWT.CONTROL;
				a = a.substring(5);
			} else if(a.startsWith("Shift+")) {
				key |= SWT.SHIFT;
				a = a.substring(6);
			} else if(a.startsWith("Alt+")) {
				key |= SWT.ALT;
				a = a.substring(4);
			} else
				assert false : "Invalid accelerator: " + a;

		a = a.intern();
		if(a == "Esc")
			key |= SWT.ESC;
		else if(a == "PageUp")
			key |= SWT.PAGE_UP;
		else if(a == "Delete")
			key |= SWT.DEL;
		else if(a == "Home")
			key |= SWT.HOME;
		else if(a == "End")
			key |= SWT.END;
		else if(a == "Plus")
			key |= SWT.KEYPAD_ADD;
		else if(a == "Minus")
			key |= SWT.KEYPAD_SUBTRACT;
		else if(a == "Star")
			key |= SWT.KEYPAD_MULTIPLY;
		else if(a == "PageDown")
			key |= SWT.PAGE_DOWN;
		else if(a.length() > 1 && a.charAt(0) == 'F')
			key |= SWT.F1 - 1 + Integer.parseInt(a.substring(1));
		else {
			assert a.length() == 1 : "Invalid key: " + a;
			key |= a.charAt(0);
		}

		menuItem.setAccelerator(key);
	}
}