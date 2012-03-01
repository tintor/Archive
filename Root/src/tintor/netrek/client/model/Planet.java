package tintor.netrek.client.model;

import java.nio.ByteBuffer;

import tintor.netrek.common.Race;

public class Planet {
	public boolean home, agri, fuel, repair;
	public Race race = Race.Unknown;
	public int armies;
	public int x, y;
	public String name = "Earth";

	public void read(final ByteBuffer buffer) {
		x = buffer.getShort();
		y = buffer.getShort();

		race = Race.values()[buffer.get()];
		if (race != Race.Unknown) {
			final byte flags = buffer.get();
			home = (flags & 1) != 0;
			agri = (flags & 2) != 0;
			fuel = (flags & 4) != 0;
			repair = (flags & 8) != 0;

			armies = buffer.get();
		} else {
			home = agri = fuel = repair = false;
			armies = 0;
		}
	}

	public void write(final ByteBuffer buffer) {
		buffer.putShort((short) x);
		buffer.putShort((short) y);

		byte flags = 0;
		if (home) flags |= 1;
		if (agri) flags |= 2;
		if (fuel) flags |= 4;
		if (repair) flags |= 8;
		buffer.put(flags);

		buffer.put((byte) race.ordinal());
		if (race != Race.Unknown) buffer.put((byte) armies);
	}
}