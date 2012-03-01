package tintor.netrek_old.common;

import java.nio.ByteBuffer;

public class ShipPrivate {
	public short fuel, fuelMax;
	public short warp, warpMax;

	public float shield, damage;
	public short shieldMax, damageMax;

	public boolean weaponOff, engineOff;
	public short weaponTemp, weaponTempMax;
	public short engineTemp, engineTempMax;

	public byte armies, armiesMax;

	public byte tractoring, pressoring; // -1 or id of ship 

	public void read(final ByteBuffer buffer) {
		fuel = buffer.getShort();
		fuelMax = buffer.getShort();

		shield = buffer.getFloat();
		shieldMax = buffer.getShort();

		damage = buffer.getFloat();
		damageMax = buffer.getShort();

		warp = buffer.getShort();
		warpMax = buffer.getShort();

		armies = buffer.get();
		armiesMax = buffer.get();

		weaponOff = buffer.get() != 0;
		engineOff = buffer.get() != 0;
		weaponTemp = buffer.getShort();
		weaponTempMax = buffer.getShort();
		engineTemp = buffer.getShort();
		engineTempMax = buffer.getShort();

		tractoring = buffer.get();
		pressoring = buffer.get();
	}

	public void write(final ByteBuffer buffer) {
		buffer.putShort(fuel);
		buffer.putShort(fuelMax);

		buffer.putFloat(shield);
		buffer.putShort(shieldMax);

		buffer.putFloat(damage);
		buffer.putShort(damageMax);

		buffer.putShort(warp);
		buffer.putShort(warpMax);

		buffer.put(armies);
		buffer.put(armiesMax);

		buffer.put(weaponOff ? (byte) 1 : (byte) 0);
		buffer.put(engineOff ? (byte) 1 : (byte) 0);
		buffer.putShort(weaponTemp);
		buffer.putShort(weaponTempMax);
		buffer.putShort(engineTemp);
		buffer.putShort(engineTempMax);

		buffer.put(tractoring);
		buffer.put(pressoring);
	}
}