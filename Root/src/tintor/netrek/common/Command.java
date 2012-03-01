package tintor.netrek.common;

import java.nio.ByteBuffer;

public class Command {
	public static enum Type {
		Quit, Turn, Navigate, Warp0, Warp1, Warp2, Warp3, Warp4, Warp5, Warp6, Warp7, Warp8, Warp9, MaxWarp, Photon,
		Phaser, Plasma, ShieldUp, ShieldDown, CloakOn, CloakOff, DetEnemy, DetOwn, TractorOn, PressorOn,
		TractorPressorOff, Refit, Bomb, BeamUp, BeampDown, Message
	}

	public static Type type;
	public static int x, y;

	public static void read(final ByteBuffer buffer) {
		type = Type.values()[buffer.get()];
		x = buffer.getShort();
		y = buffer.getShort();
	}

	public static void write(final ByteBuffer buffer) {
		buffer.put((byte) type.ordinal());
		buffer.putShort((short) x);
		buffer.putShort((short) y);
	}
}