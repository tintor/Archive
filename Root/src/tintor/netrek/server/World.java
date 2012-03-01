package tintor.netrek.server;

import java.nio.channels.DatagramChannel;

import tintor.netrek.util.ump.Server;

public class World {
	public static int frame() {
		return frame;
	}

	public static void processFrame() {
		receiveCommands();

		updateSpace();

		sendState();

		// TODO: remove dead torps

		accept();
		frame += 1;
	}

	public static void explosion(final float damage, final float x, final float y) {

	}

	private static void receiveCommands() {
		for (final Player player : Player.list)
			player.receiveCommands();
	}

	private static void updateSpace() {
		// update ships
		for (final Player player : Player.list)
			player.update();

		// update torps and check for hits
		for (final Torp torp : Torp.list)
			torp.update();
	}

	private static void sendState() {
		for (final Player player : Player.list)
			// TODO write buffer
			Server.send(player.channel);
	}

	private static void accept() {
		final DatagramChannel channel = Server.accept();
		if (channel != null) new Player(channel);
	}

	private static int frame;
}