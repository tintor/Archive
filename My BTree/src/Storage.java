import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public final class Storage {
	private FileChannel _channel;
	public ByteBuffer buffer;

	public Storage(final String fileName, final String mode) {
		try {
			_channel = new RandomAccessFile(fileName, mode).getChannel();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void allocBuffer(final int capacity) {
		buffer = ByteBuffer.allocateDirect(capacity);
	}

	public void close() {
		try {
			_channel.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void sync() {
		try {
			_channel.force(true);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void truncate(final long size) {
		try {
			_channel.truncate(size);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public long size() {
		try {
			return _channel.size();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void read(final long pos, final int size) {
		buffer.position(0);
		buffer.limit(size);
		try {
			_channel.read(buffer, pos);
			buffer.flip();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(final long pos) {
		try {
			buffer.flip();
			_channel.write(buffer, pos);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MappedByteBuffer mapRead(final long position, final long size) {
		try {
			return _channel.map(FileChannel.MapMode.READ_ONLY, position, size);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MappedByteBuffer mapWrite(final long position, final long size) {
		try {
			return _channel.map(FileChannel.MapMode.READ_WRITE, position, size);
		} catch (final IOException e) {
			throw new RuntimeException("size=" + size, e);
		}
	}

	public MappedByteBuffer mapPrivate(final long position, final long size) {
		try {
			return _channel.map(FileChannel.MapMode.PRIVATE, position, size);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}