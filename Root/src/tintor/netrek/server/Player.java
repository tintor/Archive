package tintor.netrek.server;

import java.nio.channels.DatagramChannel;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import tintor.netrek.common.Command;
import tintor.netrek.common.Race;
import tintor.netrek.util.ump.Server;
import tintor.util.UnorderedArrayList;

class Player {
	static final List<Player> list = new UnorderedArrayList<Player>(); // for iteration only!

	final int id;
	final DatagramChannel channel;

	Player(final DatagramChannel channel) {
		id = freeIDs.isEmpty() ? list.size() : freeIDs.poll();
		this.channel = channel;
		list.add(this);
		lastFrame = World.frame();
	}

	Race race() {
		return race;
	}

	void remove() {
		list.remove(this);
		freeIDs.add(id);
		for (final Iterator<Torp> it = Torp.list.iterator(); it.hasNext();) {
			final Torp torp = it.next();
			if (torp.owner == this) it.remove();
		}
	}

	void receiveCommands() {
		while (Server.receive(channel)) {
			Command.read(Server.buffer);
			execute();
		}
	}

	void execute() {
		lastFrame = World.frame();

		switch (Command.type) {
		case Warp0:
			break;
		case Warp1:
			break;
		case Warp2:
			break;
		case Warp3:
			break;
		case Warp4:
			break;
		case Warp5:
			break;
		}
	}

	void update() {
		if (ship != null) ship.update();
	}

	private static final Queue<Integer> freeIDs = new PriorityQueue<Integer>();
	private int lastFrame = 0;
	private final Race race = Race.Unknown;
	private final Ship ship = new Ship(100, 100);
}