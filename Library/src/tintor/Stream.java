package tintor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class Stream {
	public static InputStream input(final Object in) {
		try {
			if (in instanceof URL) return ((URL) in).openStream();
			if (in instanceof URLConnection) return ((URLConnection) in).getInputStream();
			if (in instanceof File) return new FileInputStream((File) in);
			if (in instanceof Socket) return ((Socket) in).getInputStream();
			if (in instanceof InputStream) return (InputStream) in;
			return new ByteArrayInputStream(in.toString().getBytes("UTF-8"));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static OutputStream output(final Object out) {
		try {
			if (out instanceof File) return new FileOutputStream((File) out);
			if (out instanceof Socket) return ((Socket) out).getOutputStream();
			return (OutputStream) out;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void pipe(final Object in, final Object out) {
		pipe(input(in), output(out));
	}

	public static InputStream buffer(final InputStream s) {
		return s instanceof BufferedInputStream ? s : new BufferedInputStream(s);
	}

	public static OutputStream buffer(final OutputStream s) {
		return s instanceof BufferedOutputStream ? s : new BufferedOutputStream(s);
	}

	// TODO for better performance:
	// - read until buffer is full and then write
	// - split into two thread, no buffered wrappers are neaded
	public static void pipe(final InputStream is, final OutputStream os) {
		pipe(is, os, null);
	}

	public static void pipe(final InputStream is, final OutputStream os, byte[] buffer) {
		if (buffer == null) buffer = new byte[10240];
		try {
			try {
				int size;
				while ((size = is.read(buffer)) > 0)
					os.write(buffer, 0, size);

				os.flush();
			} finally {
				is.close();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String read(final Object in) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		pipe(in, out);
		try {
			return out.toString("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	//	static String net(final String request) throws Exception {
	//		final Socket sock = new Socket("proxy.sbb.co.yu", 8080);
	//		pipe(request, sock);
	//		final String responce = read(sock);
	//		sock.close();
	//		return responce;
	//	}

	//	static Matcher matcher(final String str, final String regex) {
	//		return Pattern.compile(regex).matcher(str);
	//	}

	//	static String read(final Object a, final Object b) throws Exception {
	//		pipe(a, b);
	//		return read(b);
	//	}

	//	static String read(final Reader in) throws Exception {
	//		final StringBuffer result = new StringBuffer(1000);
	//		final BufferedReader reader = new BufferedReader(in);
	//		final char[] buf = new char[10000];
	//		int c = 0;
	//		while ((c = reader.read(buf)) != -1)
	//			result.append(buf, 0, c);
	//		reader.close();
	//		return result.toString();
	//	}
}