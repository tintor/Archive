import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Mipmaper {
	static FileChannel in, out;
	static final int iw = 10800, ih = 5400;
	static final int ow = iw / 2, oh = ih / 2;

	public static void main(final String[] args) throws IOException {
		in = new RandomAccessFile("data/world.200403.3x" + iw + "x" + ih + ".bin", "r").getChannel();
		out = new RandomAccessFile("data/world.200403.3x" + ow + "x" + oh + ".bin", "rw").getChannel();

		final ByteBuffer line1 = ByteBuffer.allocateDirect(3 * iw);
		final ByteBuffer line2 = ByteBuffer.allocateDirect(3 * iw);
		final ByteBuffer line3 = ByteBuffer.allocateDirect(3 * ow);

		for (int y = 0; y < oh; y++) {
			System.out.println(y);

			// read
			line1.clear();
			in.read(line1);
			line1.rewind();

			line2.clear();
			in.read(line2);
			line2.rewind();

			// process
			line3.clear();
			for (int x = 0; x < ow * 3; x++) {
				final int m = (line1.get() + line1.get() + line2.get() + line2.get()) / 4;
				line3.put((byte) m);
			}

			// write
			line3.rewind();
			out.write(line3);
		}

		in.close();
		out.close();
	}
}