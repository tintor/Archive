package tintor.netrek.util.ump;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import tintor.util.ByteBufferInputStream;

/** Blocking Client */
public class Client {
	public final static ByteBuffer sendBuffer = ByteBuffer.allocateDirect(Protocol.MaxMessageSize);
	public final static ByteBuffer recvBuffer = ByteBuffer.allocateDirect(Protocol.MaxMessageSize);

	public static void connect(final String host, final int port) {
		final SocketAddress serverAddress = new InetSocketAddress(host, port);

		sendBuffer.clear();
		sendBuffer.putInt(Protocol.Hello);
		sendBuffer.flip();
		try {
			_channel = DatagramChannel.open();
			_channel.send(sendBuffer, serverAddress);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		while (true) {
			recvBuffer.clear();
			try {
				final SocketAddress address = _channel.receive(recvBuffer);
				if (!address.equals(serverAddress)) continue;
				recvBuffer.flip();
				_channel.connect((SocketAddress) new ObjectInputStream(new ByteBufferInputStream(recvBuffer))
						.readObject());
				break;
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/* send and receive are safe to call from different threads */
	public static void send() {
		sendBuffer.flip();
		try {
			_channel.write(sendBuffer);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			sendBuffer.clear();
		}
	}

	public static void receive() {
		recvBuffer.clear();
		try {
			_channel.read(recvBuffer);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		recvBuffer.flip();
	}

	private static DatagramChannel _channel;
}