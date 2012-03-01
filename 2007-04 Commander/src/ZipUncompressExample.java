import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tintor.Stream;

public class ZipUncompressExample {
	public static void main(final String[] args) throws IOException {
		final File source = new File("c:/example.zip");
		final File destination = new File("c:/temp/");

		final ZipFile zip = new ZipFile(source, ZipFile.OPEN_READ);

		final Enumeration<?> entries = zip.entries();
		while (entries.hasMoreElements()) {
			final ZipEntry entry = (ZipEntry) entries.nextElement();
			System.out.println("Extracting: " + entry);

			final File destFile = new File(destination, entry.getName());
			destFile.getParentFile().mkdirs();

			if (!entry.isDirectory()) {
				final OutputStream out = new FileOutputStream(destFile);
				Stream.pipe(zip.getInputStream(entry), out, null);
				out.close();
			}
		}
		zip.close();
	}
}