package tintor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class SWT2D {
	public static void drawText(final GC gc, final int x, final int y, final Align ax, final Align ay,
			final Object obj) {
		final String text = obj.toString();
		final Point p = gc.textExtent(text);
		gc.drawText(text, x + ax.offset(p.x), y + ay.offset(p.y), SWT.DRAW_TRANSPARENT);
	}
}