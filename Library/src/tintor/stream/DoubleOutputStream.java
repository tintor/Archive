package tintor.stream;

import java.io.IOException;
import java.io.OutputStream;

public class DoubleOutputStream extends OutputStream {
	private final OutputStream a, b;

	public DoubleOutputStream(final OutputStream a, final OutputStream b) {
		try {
			this.a = a;
		} finally {
			this.b = b;
		}
	}

	@Override public void close() throws IOException {
		try {
			a.close();
		} finally {
			b.close();
		}
	}

	@Override public void flush() throws IOException {
		try {
			a.flush();
		} finally {
			b.flush();
		}
	}

	@Override public void write(final byte[] ba, final int off, final int len) throws IOException {
		try {
			a.write(ba, off, len);
		} finally {
			b.write(ba, off, len);
		}
	}

	@Override public void write(final byte[] ba) throws IOException {
		try {
			a.write(ba);
		} finally {
			b.write(ba);
		}
	}

	@Override public void write(final int ba) throws IOException {
		try {
			a.write(ba);
		} finally {
			b.write(ba);
		}
	}
}