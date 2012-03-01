package tintor.swt;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class TextWidgetWriter extends Writer {
	public final Text text;

	public TextWidgetWriter(final Text text) {
		this.text = text;
	}

	@Override public void close() throws IOException {}

	@Override public void flush() throws IOException {}

	@Override public void write(final int c) throws IOException {
		append(String.valueOf((char) c));
	}

	@Override public void write(final char[] cbuf, final int off, final int len) throws IOException {
		append(String.valueOf(cbuf, off, len));
	}

	@Override public void write(final String str, final int off, final int len) throws IOException {
		append(str.substring(off, len));
	}

	private void append(final String str) {
		Display.getDefault().syncExec(new Runnable() {
			@Override public void run() {
				if (!text.isDisposed()) text.append(str);
			}
		});
	}
}