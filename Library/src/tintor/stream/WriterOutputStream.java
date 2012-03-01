package tintor.stream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class WriterOutputStream extends OutputStream {
	private final Writer writer;
	private final Reader reader;
	private final ByteStream bytes = new ByteStream();
	private final char[] buffer = new char[512];

	public WriterOutputStream(final Writer w) {
		writer = w;
		reader = new InputStreamReader(bytes);
	}

	@Override public void write(final int b) throws IOException {
		bytes.write((byte) b);
		while (!bytes.isEmpty()) {
			final int size = reader.read(buffer);
			if (size <= 0) break;
			writer.write(buffer, 0, size);
		}
	}

	@Override public void write(final byte[] b, final int off, final int len) throws IOException {
		bytes.write(b, off, len);
		while (!bytes.isEmpty()) {
			final int size = reader.read(buffer);
			if (size < 0) break;
			writer.write(buffer, 0, size);
		}
	}
}