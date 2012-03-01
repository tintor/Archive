package tintor.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteStream extends InputStream {
	private byte[] elements;
	private int head, tail;

	private void expand(final int newCapacity) {
		if (newCapacity < 0) throw new IllegalStateException("Sorry, deque too big");
		final byte[] a = new byte[newCapacity];

		final int size = tail - head & elements.length - 1;
		final int e = Math.min(size, elements.length - head);

		System.arraycopy(elements, head, a, 0, e);
		System.arraycopy(elements, 0, a, e, size - e);

		elements = a;
		head = 0;
		tail = size;
	}

	public ByteStream() {
		elements = new byte[16];
	}

	public int size() {
		return tail - head & elements.length - 1;
	}

	public boolean isEmpty() {
		return head == tail;
	}

	@Override public int read() throws IOException {
		if (head == tail) return -1;
		final byte result = elements[head];
		head = head + 1 & elements.length - 1;
		return result;
	}

	@Override public int read(final byte[] b, final int off, final int len) throws IOException {
		if (len == 0 || head == tail) return 0;

		final int e = Math.min(Math.min(size(), elements.length - head), len);
		System.arraycopy(elements, head, b, off, e);
		head = head + e & elements.length - 1;
		if (len == e || head == tail) return e;

		final int e2 = Math.min(size(), len - e);
		System.arraycopy(elements, head, b, off + e, e2);
		head += e2;
		return e + e2;
	}

	@Override public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public void write(final byte e) {
		elements[tail] = e;
		tail = tail + 1 & elements.length - 1;
		if (tail == head) expand(elements.length << 1);
	}

	public void write(final byte[] b, final int off, final int len) {
		final int minCapacity = (tail - head & elements.length - 1) + len + 1;
		if (elements.length < minCapacity) {
			int newCapacity = elements.length << 1;
			while (newCapacity < minCapacity)
				newCapacity <<= 1;
			expand(newCapacity);
		}

		final int e = elements.length - tail;
		if (len <= e)
			System.arraycopy(b, off, elements, tail, len);
		else {
			System.arraycopy(b, off, elements, tail, e);
			System.arraycopy(b, off + e, elements, 0, len - e);
		}
		tail = tail + len & elements.length - 1;
	}

	public OutputStream outputStream() {
		return new OutputStream() {
			@Override public void write(final int b) {
				ByteStream.this.write((byte) b);
			}

			@Override public void write(final byte[] b, final int off, final int len) {
				ByteStream.this.write(b, off, len);
			}
		};
	}
}