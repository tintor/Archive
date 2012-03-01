package tintor.netrek_old.common;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import tintor.netrek.client.model.Planet;

public class PlayerModel {
	public byte id;
	public final Ship[] ships;
	public final ShipPrivate priv;
	public final Planet[] planets;
	public String message;

	public PlayerModel(final byte id, final Ship[] ship, final ShipPrivate priv, final Planet[] planets) {
		this.id = id;
		ships = ship;
		this.priv = priv;
		this.planets = planets;
	}

	public PlayerModel() {
		ships = new Ship[Const.MaxShips];
		for (int i = 0; i < ships.length; i++)
			ships[i] = new Ship();
		priv = new ShipPrivate();

		planets = new Planet[Const.Planets];
		for (int i = 0; i < planets.length; i++)
			planets[i] = new Planet();
	}

	public void read(final ByteBuffer buffer) {
		id = buffer.get();

		for (final Ship s : ships)
			s.read(buffer);

		priv.read(buffer);

		for (final Planet planet : planets)
			planet.read(buffer);

		if (buffer.hasRemaining()) {
			final byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			message = new String(bytes, Charset.forName("utf8"));
		}
	}

	public void write(final ByteBuffer buffer) {
		buffer.put(id);

		for (final Ship s : ships)
			s.write(buffer);

		priv.write(buffer);

		for (final Planet planet : planets)
			planet.write(buffer);

		if (message != null) buffer.put(message.getBytes(Charset.forName("utf8")));
	}
}