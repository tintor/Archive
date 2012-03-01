package tintor.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

public class ByteBufferInputStream extends InputStream {
	private final ByteBuffer _buffer;

	public ByteBufferInputStream(final ByteBuffer buffer) {
		if (buffer == null) throw new IllegalArgumentException("null buffer");
		_buffer = buffer;
	}

	@Override public int available() {
		return _buffer.limit() - _buffer.position();
	}

	@Override public void mark(final int readlimit) {
		_buffer.mark();
	}

	@Override public boolean markSupported() {
		return true;
	}

	@Override public void reset() throws IOException {
		try {
			_buffer.reset();
		} catch (final InvalidMarkException ime) {
			throw new IOException("no mark set - " + ime);
		}
	}

	@Override public int read() {
		try {
			return _buffer.get();
		} catch (final BufferOverflowException e) {
			return -1;
		}
	}

	@Override public int read(final byte[] array) {
		return read(array, 0, array.length);
	}

	@Override public int read(final byte[] array, final int offset, int len) {
		if (array == null) throw new NullPointerException();
		final int position = _buffer.position();
		final int limit = _buffer.limit();
		if (position >= limit) return -1;
		if (len == 0) return 0;
		if (offset < 0 || len < 0 || offset + len > array.length) throw new IndexOutOfBoundsException();

		if (position + len > limit) len = limit - position;
		_buffer.get(array, offset, len);
		return len;

	}

	@Override public long skip(final long n) {
		if (n < 0) throw new IllegalArgumentException("negative skips not supported");
		final int intN = (int) n;
		if (intN + _buffer.position() > _buffer.limit()) {
			final int posNow = _buffer.position();
			_buffer.position(_buffer.limit());
			return _buffer.limit() - posNow;
		}

		_buffer.position(_buffer.position() + intN);
		return intN;
	}
}