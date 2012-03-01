import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import tintor.Stream;

public class ZipCreateExample {
	public static void main(final String[] args) throws IOException {
		final String[] filesToZip = new String[] { "src/ZipCreateExample.java", "bin/ZipCreateExample.class" };

		final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("test.zip"));
		zos.setLevel(Deflater.DEFAULT_COMPRESSION);
		for (final String file : filesToZip) {
			System.out.println(file);
			addZipEntry(zos, new ZipEntry(file), new FileInputStream(file), null);
		}
		zos.close();
	}

	public static void addZipEntry(final ZipOutputStream zos, final ZipEntry zipEntry, final InputStream is,
			final byte[] buffer) throws IOException {
		zos.putNextEntry(zipEntry);
		Stream.pipe(is, zos, buffer);
		zos.closeEntry();
	}
}