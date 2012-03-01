package tintor.license;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class License {
	static byte[] header;

	public static void main(final String[] args) throws IOException {
		header = read(new File("header.txt"));
		visit(new File("c:\\backup\\diplomski\\simulator\\src"));
	}

	static byte[] read(final File file) throws IOException {
		final byte[] bytes = new byte[(int) file.length()];
		int offset = 0, count = 0;
		final InputStream is = new BufferedInputStream(new FileInputStream(file));
		while ((count = is.read(bytes, offset, bytes.length - offset)) > 0)
			offset += count;
		return bytes;
	}

	static void visit(final File file) throws IOException {
		if (file.isDirectory()) {
			for (final File a : file.listFiles())
				visit(a);
			return;
		}
		if (!file.getName().endsWith(".java")) return;

		System.out.println(file);
		final byte[] data = read(file);
		final OutputStream os = new FileOutputStream(file);
		os.write(header);
		os.write(data);
		os.close();
	}
}