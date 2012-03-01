package tintor.commander;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class BorderLayout extends Layout {
	public abstract static class Data {
		abstract void layout(final Control control, final Rectangle rect, final boolean flush);

		abstract void updateSize(final Point control, final Point size);

		static Point getSize(final Control control, final boolean flush) {
			return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		}
	}

	public static class DCenter extends Data {
		DCenter(final int left, final int right, final int top, final int bottom) {
			this.left = left;
			this.right = right;
			this.top = top;
			this.bottom = bottom;
		}

		public DCenter margins(final int left, final int right, final int top, final int bottom) {
			return new DCenter(left, right, top, bottom);
		}

		@Override void layout(final Control control, final Rectangle rect, final boolean flush) {
			control.setBounds(rect.x + left, rect.y + top, rect.width - left - right, rect.height - top
					- bottom);
		}

		@Override void updateSize(final Point control, final Point size) {
			size.x = Math.max(left + control.x + right, size.x);
			size.y = Math.max(top + control.y + bottom, size.y);
		}

		final int left, right, top, bottom;
	}

	public static class DLeft extends Data {
		final int width;

		DLeft(final int width) {
			this.width = width;
		}

		public DLeft width(final int width) {
			return new DLeft(width);
		}

		@Override void layout(final Control control, final Rectangle rect, final boolean flush) {
			final Point p = getSize(control, flush);
			control.setBounds(rect.x, rect.y, p.x, rect.height);
			rect.x += p.x;
			rect.width -= p.x;
		}

		@Override void updateSize(final Point control, final Point size) {
			size.x += Math.max(width, control.x);
			size.y = Math.max(size.y, control.y);

		}
	}

	public static class DRight extends Data {
		final int width;

		DRight(final int width) {
			this.width = width;
		}

		public DRight width(final int width) {
			return new DRight(width);
		}

		@Override void layout(final Control control, final Rectangle rect, final boolean flush) {
			final Point p = getSize(control, flush);
			control.setBounds(rect.x + rect.width - p.x, rect.y, p.x, rect.height);
			rect.width -= p.x;
		}

		@Override void updateSize(final Point control, final Point size) {
			size.x += Math.max(width, control.x);
			size.y = Math.max(size.y, control.y);

		}
	}

	public static class DTop extends Data {
		final int height;

		DTop(final int height) {
			this.height = height;
		}

		public DTop height(final int height) {
			return new DTop(height);
		}

		@Override void layout(final Control control, final Rectangle rect, final boolean flush) {
			final Point p = getSize(control, flush);
			control.setBounds(rect.x, rect.y, rect.width, p.y);
			rect.y += p.y;
			rect.height -= p.y;
		}

		@Override void updateSize(final Point control, final Point size) {
			size.x = Math.max(size.x, control.x);
			size.y += Math.max(height, control.y);
		}
	}

	public static class DBottom extends Data {
		final int height;

		DBottom(final int height) {
			this.height = height;
		}

		public DBottom height(final int height) {
			return new DBottom(height);
		}

		@Override void layout(final Control control, final Rectangle rect, final boolean flush) {
			final Point p = getSize(control, flush);
			control.setBounds(rect.x, rect.y + rect.height - p.y, rect.width, p.y);
			rect.height -= p.y;
		}

		@Override void updateSize(final Point control, final Point size) {
			size.x = Math.max(size.x, control.x);
			size.y += Math.max(height, control.y);
		}
	}

	public static final DCenter Center = new DCenter(0, 0, 0, 0);
	public static final DLeft Left = new DLeft(0);
	public static final DRight Right = new DRight(0);
	public static final DTop Top = new DTop(0);
	public static final DBottom Bottom = new DBottom(0);

	public BorderLayout() {}

	private Point size;

	@Override protected Point computeSize(final Composite composite, final int wHint, final int hHint,
			final boolean flushCache) {
		if (flushCache) size = null;
		if (size == null) {
			size = new Point(0, 0);
			final Control[] controls = composite.getChildren();
			for (int i = controls.length - 1; i >= 0; i--) {
				final Control control = controls[i];
				final Data data = (Data) control.getLayoutData();
				data.updateSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache), size);
			}
		}
		return new Point(Math.max(size.x, wHint), Math.max(size.y, hHint));
	}

	@Override protected void layout(final Composite composite, final boolean flushCache) {
		if (flushCache) size = null;

		final Rectangle rect = composite.getClientArea();
		composite.setRedraw(false);
		for (final Control control : composite.getChildren())
			((Data) control.getLayoutData()).layout(control, rect, flushCache);
		composite.setRedraw(true);
	}
}