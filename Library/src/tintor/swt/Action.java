package tintor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import tintor.properties.Property;
import tintor.properties.PropertyListener;

public class Action {
	@SuppressWarnings("hiding") private enum Type {
		Cascade, Check
	}

	public static final Type Cascade = Type.Cascade, Check = Type.Check;

	private final String name;
	private final String accelerator;
	private final Type type;
	final Property<Boolean> property;

	protected MenuItem menuItem;

	public Action(final String name) {
		this(name, null);
	}

	public Action(final String name, final String accelerator) {
		this(name, accelerator, null);
	}

	public Action(final String name, final String accelerator, final Type type) {
		this(name, accelerator, type, null);
	}

	public Action(final String name, final String accelerator, final Type type, final Property<Boolean> property) {
		this.name = name;
		this.accelerator = accelerator;
		this.type = type;
		this.property = property;
	}

	private final static SelectionListener selectionListener = new SelectionAdapter() {
		@Override public void widgetSelected(SelectionEvent e) {
			final Action action = (Action) e.widget.getData();
			if (action.property != null) action.property.set(((MenuItem) e.widget).getSelection());
			action.onSelect();
		}
	};

	public MenuItem createMenuItem(final Menu menu) {
		assert menuItem == null;
		final int style = type == Check ? SWT.CHECK : type == Cascade ? SWT.CASCADE : SWT.NONE;
		menuItem = new MenuItem(menu, style);
		menuItem.setData(this);
		menuItem.addSelectionListener(selectionListener);
		menuItem.setText(accelerator == null ? name : name + "\t" + accelerator);
		if (property != null) //menuItem.setSelection(property.get());
			property.addListener(new PropertyListener() {
				@Override public void propertyChange(final String key, final Object oldValue, final Object newValue) {
					menuItem.setSelection((Boolean) newValue);
				}
			});

		if (accelerator != null) {
			String a = accelerator;
			int key = 0;

			final char sep = '+';
			while (a.indexOf(sep) > 0)
				if (accelerator.startsWith("Ctrl" + sep)) {
					key |= SWT.CONTROL;
					a = a.substring(5);
				} else if (a.startsWith("Shift" + sep)) {
					key |= SWT.SHIFT;
					a = a.substring(6);
				} else if (a.startsWith("Alt" + sep)) {
					key |= SWT.ALT;
					a = a.substring(4);
				} else
					assert false : "Invalid accelerator: " + a;

			a = a.intern();
			if (a == "Esc")
				key |= SWT.ESC;
			else if (a == "PageUp")
				key |= SWT.PAGE_UP;
			else if (a == "Delete")
				key |= SWT.DEL;
			else if (a == "Home")
				key |= SWT.HOME;
			else if (a == "End")
				key |= SWT.END;
			else if (a == "Add")
				key |= SWT.KEYPAD_ADD;
			else if (a == "Subtract")
				key |= SWT.KEYPAD_SUBTRACT;
			else if (a == "Multiply")
				key |= SWT.KEYPAD_MULTIPLY;
			else if (a == "PageDown")
				key |= SWT.PAGE_DOWN;
			else if (a == "Space")
				key |= ' ';
			else if (a == "Enter")
				key |= '\n';
			else if (a.length() > 1 && a.charAt(0) == 'F')
				key |= SWT.F1 - 1 + Integer.parseInt(a.substring(1));
			else {
				assert a.length() == 1 : "Invalid key: " + a;
				key |= a.charAt(0);
			}

			menuItem.setAccelerator(key);
		}

		return menuItem;
	}

	public void onShow() {}

	public void onHide() {}

	public void onSelect() {}

	@Override public String toString() {
		return name;
	}
}