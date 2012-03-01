package gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Marko Tintor
 * @date 04/2006
 */
public class GUI {
	public final static Display display = new Display();
	public final static FormAttachment fmin = new FormAttachment(0, 0), fmax = new FormAttachment(100, 0);

	public final Shell shell = new Shell();

	public final static Cursor arrow = new Cursor(display, SWT.CURSOR_ARROW);
	public final static Cursor hand = new Cursor(display, SWT.CURSOR_HAND);
	public final static Cursor cross = new Cursor(display, SWT.CURSOR_CROSS);
	public final static Cursor sizeAll = new Cursor(display, SWT.CURSOR_SIZEALL);

	public final static Color highZ = new Color(display, 168, 168, 218);
	public final static Color green_yellow = new Color(display, 128, 128, 0);
	public final static Color brown = new Color(display, 128, 64, 0);
	public final static Color green = new Color(display, 0, 255, 0);
	public final static Color yellow = new Color(display, 255, 255, 0);
	public final static Color red = new Color(display, 255, 0, 0);
	public final static Color white = new Color(display, 255, 255, 255);
	public final static Color black = new Color(display, 0, 0, 0);
	public final static Color blue = new Color(display, 0, 0, 255);
	public final static Color lightBlue = new Color(display, 127, 200, 255);

	public final Font bold;

	public GUI() {
		FontData[] fd = shell.getFont().getFontData();
		for(FontData a : fd)
			a.setStyle(a.getStyle() | SWT.BOLD);
		bold = new Font(display, fd);
	}

	public ViewForm newView(final Composite parent, String title, boolean closeable) {
		final ViewForm view = new ViewForm(parent, SWT.BORDER);

		CLabel label = new CLabel(view, SWT.NONE);
		label.setText(title);
		label.setAlignment(SWT.LEFT);
		view.setTopLeft(label);

		if(closeable) {
			ToolBar tb = new ToolBar(view, SWT.FLAT);
			ToolItem close = new ToolItem(tb, SWT.PUSH);
			close.setText("X");
			tb.setFont(bold);
			close.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					showView(view, false);
				}
			});
			view.setTopRight(tb);
		}
		return view;
	}

	public static void showView(ViewForm view, boolean visible) {
		view.setVisible(visible);
		view.getParent().layout();

		boolean v = false;
		for(Control c : view.getParent().getChildren())
			v = v || (c instanceof ViewForm && c.getVisible());
		if(v == visible) {
			view.getParent().setVisible(visible);
			view.getParent().getParent().layout();
		}
	}

	public static FormData newFormData(FormAttachment top, FormAttachment bottom) {
		FormData f = new FormData();
		f.top = top;
		f.left = fmin;
		f.right = fmax;
		f.bottom = bottom;
		return f;
	}

	public static Button newButton(Composite parent, String text, SelectionListener listener) {
		Button b = new Button(parent, SWT.NONE);
		b.setText(text);
		b.addSelectionListener(listener);
		return b;
	}

	public static ToolItem newToolItem(ToolBar tb, String text, int style, SelectionListener listener) {
		ToolItem b = new ToolItem(tb, style);
		b.setText(text);
		b.addSelectionListener(listener);
		return b;
	}

	public static TableItem newTableItem(Table t, String... value) {
		TableItem a = new TableItem(t, SWT.NONE);
		a.setText(value);
		return a;
	}

	static {
		try {
			assert false;
			throw new RuntimeException("assertions must be turned on!");
		} catch(AssertionError e) {}
	}

	public void run() {
		shell.open();
		while(!shell.isDisposed())
			if(!display.readAndDispatch()) display.sleep();
		display.dispose();
	}
}