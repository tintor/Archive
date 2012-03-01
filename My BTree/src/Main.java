import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class Main {
	public static void main(final String[] args) throws IOException {
		final FileLock a = new RandomAccessFile("", "").getChannel().lock();
	}
}