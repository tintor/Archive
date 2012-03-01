package tintor.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
	private final ByteBuffer _buffer;

	public ByteBufferOutputStream(final ByteBuffer buffer) {
		_buffer = buffer;
	}

	@Override public void write(final int b) throws IOException {
		_buffer.put((byte) b);
	}

	@Override public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override public void write(final byte[] array, final int offset, final int len) throws IOException {
		_buffer.put(array, offset, len);
	}
}