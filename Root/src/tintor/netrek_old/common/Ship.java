package tintor.netrek_old.common;

import java.nio.ByteBuffer;

public class Ship {
	public boolean alive = false;

	public short x, y;
	public byte heading;
	public boolean shields;
	public short kills; // in 0.01 units

	public byte torps;
	public short[] torpX = new short[Const.MaxTorps], torpY = new short[Const.MaxTorps];

	public void read(final ByteBuffer buffer) {
		alive = buffer.get() != 0;
		if (alive) {
			x = buffer.getShort();
			y = buffer.getShort();
			heading = buffer.get();
			shields = buffer.get() != 0;
			kills = buffer.getShort();
		}

		torps = buffer.get();
		for (int i = 0; i < torps; i++) {
			torpX[i] = buffer.getShort();
			torpY[i] = buffer.getShort();
		}
	}

	public void write(final ByteBuffer buffer) {
		buffer.put(alive ? (byte) 1 : (byte) 0);
		if (alive) {
			buffer.putShort(x);
			buffer.putShort(y);
			buffer.put(heading);
			buffer.put(shields ? (byte) 1 : (byte) 0);
			buffer.putShort(kills);
		}

		buffer.put(torps);
		for (int i = 0; i < torps; i++) {
			buffer.putShort(torpX[i]);
			buffer.putShort(torpY[i]);
		}
	}
}