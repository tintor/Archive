package tintor.netrek_old.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.logging.Logger;

import tintor.netrek_old.common.Command;
import tintor.netrek_old.common.Const;


public class Server {
	public final static int MaxMessageSize = 1500;
	private final static int FrameTime = 1000000000 / Const.UpdateFrequency;
	private final static int MinBufferSize = 128 * 1024;

	final DatagramChannel _channel;
	final ByteBuffer _buffer = ByteBuffer.allocateDirect(MaxMessageSize);
	final World world = new World();

	public static void setMinBufferSize(final DatagramChannel channel, final int size) throws SocketException {
		final DatagramSocket s = channel.socket();
		if (s.getSendBufferSize() < size) s.setSendBufferSize(size);
		if (s.getReceiveBufferSize() < size) s.setReceiveBufferSize(size);
	}

	Server() throws IOException {
		_channel = DatagramChannel.open();
		setMinBufferSize(_channel, MinBufferSize);
		_channel.configureBlocking(false);
		_channel.socket().bind(new InetSocketAddress(Const.Port));
	}

	void run() throws Exception {
		while (true) {
			final long frameStartTime = System.nanoTime();

			acceptConnections();
			world.receiveCommands(_buffer);
			world.updateState();
			world.sendState(_buffer);

			// sync
			final long timeLeft = FrameTime - (System.nanoTime() - frameStartTime);
			if (timeLeft > 0) Thread.sleep(timeLeft / 1000000, (int) (timeLeft % 1000000));
		}
	}

	void acceptConnections() throws IOException {
		while (true) {
			_buffer.clear();
			final SocketAddress address = _channel.receive(_buffer);
			if (address == null) break;

			_buffer.flip();
			final Command c = Command.read(_buffer);
			if (c.type == Command.Type.Connect) world.connect(address);
		}
	}

	final static Logger log = Logger.getLogger(Server.class.getName());
}