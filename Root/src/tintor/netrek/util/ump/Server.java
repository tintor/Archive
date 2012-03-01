package tintor.netrek.util.ump;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import tintor.util.ByteBufferOutputStream;

/** Non Blocking Server */
public class Server {
	public static final ByteBuffer buffer = ByteBuffer.allocateDirect(Protocol.MaxMessageSize);

	public static void listen(final int port) {
		try {
			_serverChannel = DatagramChannel.open();
			_serverChannel.configureBlocking(false);
			_serverChannel.socket().bind(new InetSocketAddress(port));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static DatagramChannel accept() {
		try {
			buffer.clear();
			final SocketAddress address = _serverChannel.receive(buffer);
			if (address == null) return null;

			buffer.flip();
			if (buffer.limit() != 4 || buffer.getInt() != Protocol.Hello) return null;

			final DatagramChannel channel = DatagramChannel.open();
			channel.configureBlocking(false);
			setMinBufferSize(channel, MinBufferSize);
			channel.connect(address);

			buffer.clear();
			new ObjectOutputStream(new ByteBufferOutputStream(buffer)).writeObject(channel.socket().getLocalAddress());
			buffer.flip();
			if (_serverChannel.send(buffer, address) == 0) return null;

			return channel;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean send(final DatagramChannel channel) {
		buffer.flip();
		try {
			return channel.write(buffer) != 0;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			buffer.clear();
		}
	}

	public static boolean receive(final DatagramChannel channel) {
		buffer.clear();
		try {
			if (channel.read(buffer) == 0) return false;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		buffer.flip();
		return true;
	}

	private static void setMinBufferSize(final DatagramChannel channel, final int size) throws SocketException {
		final DatagramSocket s = channel.socket();
		if (s.getSendBufferSize() < size) s.setSendBufferSize(size);
		if (s.getReceiveBufferSize() < size) s.setReceiveBufferSize(size);
	}

	private static DatagramChannel _serverChannel;
	private static final int MinBufferSize = 128 * 1024;
}