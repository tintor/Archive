package tintor.frpr.util.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class GZIPResponseStream extends ServletOutputStream {
	protected ByteArrayOutputStream baos = null;
	protected GZIPOutputStream gzipstream = null;
	protected boolean closed = false;
	protected HttpServletResponse response = null;
	protected ServletOutputStream output = null;

	public GZIPResponseStream(final HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		output = response.getOutputStream();
		baos = new ByteArrayOutputStream();
		gzipstream = new GZIPOutputStream(baos);
	}

	@Override public void close() throws IOException {
		if (closed) throw new IOException("This output stream has already been closed");
		gzipstream.finish();

		final byte[] bytes = baos.toByteArray();

		response.addHeader("Content-Length", Integer.toString(bytes.length));
		response.addHeader("Content-Encoding", "gzip");
		output.write(bytes);
		output.flush();
		output.close();
		closed = true;
	}

	@Override public void flush() throws IOException {
		if (closed) throw new IOException("Cannot flush a closed output stream");
		gzipstream.flush();
	}

	@Override public void write(final int b) throws IOException {
		if (closed) throw new IOException("Cannot write to a closed output stream");
		gzipstream.write((byte) b);
	}

	@Override public void write(final byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override public void write(final byte b[], final int off, final int len) throws IOException {
		System.out.println("writing...");
		if (closed) throw new IOException("Cannot write to a closed output stream");
		gzipstream.write(b, off, len);
	}

	public boolean closed() {
		return closed;
	}

	public void reset() {
	//noop
	}
}
