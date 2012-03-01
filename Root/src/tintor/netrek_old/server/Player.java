package tintor.netrek_old.server;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import tintor.netrek_old.common.Command;
import tintor.netrek_old.common.Const;
import tintor.netrek_old.common.PlayerModel;
import tintor.netrek_old.common.Ship;
import tintor.netrek_old.common.ShipPrivate;


class Torp {
	float x, y, dx, dy;
	int framesLeft;
}

class Player {
	private DatagramChannel _channel;
	short _idleFrames;

	private float _warp;
	private int _targetWarp = 0;

	final Torp[] _torps = new Torp[Const.MaxTorps];
	float _x, _y, _heading, _targetHeading;

	final Ship _pub = new Ship();
	final ShipPrivate _priv = new ShipPrivate();

	PlayerModel _playerModel;

	public Player() {
		for (int i = 0; i < _torps.length; i++)
			_torps[i] = new Torp();
	}

	public float quad(final Torp torp) {
		final float dx = _x - torp.x, dy = _y - torp.y;
		return dx * dx + dy * dy;
	}

	public void init(final SocketAddress address) {
		try {
			if (_channel != null) _channel.close();
			_channel = DatagramChannel.open();
			_channel.configureBlocking(false);
			_channel.connect(address);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		_x = (float) (Math.random() * Const.GalaxySize);
		_y = (float) (Math.random() * Const.GalaxySize);
		_warp = _targetWarp = 0;
		_heading = _targetHeading = 0;

		_pub.x = (short) _x;
		_pub.y = (short) _y;
		_pub.heading = (byte) codeAngle(_heading);
		_pub.torps = 0;
		_pub.alive = true;

		_priv.fuelMax = 10000;
		_priv.shieldMax = 100;
		_priv.damageMax = 100;
		_priv.warpMax = Const.ShipMaxWarp;

		_priv.damage = 0;
		_priv.shield = _priv.shieldMax;
		_priv.fuel = _priv.fuelMax;
	}

	public void damage(float e) {
		if (_pub.shields) {
			if (_priv.shield >= e) {
				_priv.shield -= e;
				return;
			}
			e = e - _priv.shield;
			_priv.shield = 0;
		}
		_priv.damage += e;
		updateMaxWarp();
		if (_priv.damage >= _priv.damageMax) _pub.alive = false;
	}

	private void updateMaxWarp() {
		_priv.warpMax = (short) Math.max(1, (int) Math.ceil(Const.ShipMaxWarp * (1 - _priv.damage / _priv.damageMax)));
	}

	public void receiveCommands(final ByteBuffer buffer) {
		while (true) {
			buffer.clear();
			try {
				if (_channel.read(buffer) == 0) break;
			} catch (final PortUnreachableException e) {
				_pub.alive = false;
				break;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

			buffer.flip();
			execute(Command.read(buffer));
		}
	}

	void execute(final Command command) {
		switch (command.type) {
		case Warp0:
		case Warp1:
		case Warp2:
		case Warp3:
		case Warp4:
		case Warp5:
		case Warp6:
		case Warp7:
		case Warp8:
		case Warp9:
			_targetWarp = command.type.ordinal() - Command.Type.Warp0.ordinal();
			break;
		case MaxWarp:
			_targetWarp = _priv.warpMax;
			break;
		case Turn:
			_targetHeading = direction(command.destX, command.destY);
			break;
		case Torp:
			final float d = direction(command.destX, command.destY);
			if (_pub.torps < _torps.length && _priv.fuel >= Const.TorpFuelCost) {
				final Torp torp = _torps[_pub.torps++];
				torp.x = _x;
				torp.y = _y;
				torp.dx = Const.TorpSpeed * (float) Math.cos(d);
				torp.dy = Const.TorpSpeed * (float) Math.sin(d);
				torp.framesLeft = (int) (Const.TorpFuse * Const.UpdateFrequency);
				_priv.fuel -= Const.TorpFuelCost;
			}
			break;
		case ShieldsToogle:
			_pub.shields = !_pub.shields;
			break;
		case ShieldsOn:
			_pub.shields = true;
			break;
		case ShieldsOff:
			_pub.shields = false;
			break;
		case TractorPressorOff:
			_priv.tractoring = _priv.pressoring = -1;
			break;
		case DetOwn:
			_pub.torps = 0;
			break;
		}
	}

	private float direction(final int destX, final int destY) {
		return (float) Math.atan2(destY - _y, destX - _x);
	}

	public void updateShip() {
		// UPDATE SHIP FUEL
		_priv.fuel += Const.FuelGeneration / Const.UpdateFrequency * (1 - _warp / Const.CruiseSpeed);
		if (_pub.shields) _priv.fuel -= Const.ShieldsFuelCost / Const.UpdateFrequency;

		if (_priv.fuel < 0) {
			_targetWarp = Const.CruiseSpeed - 1;
			_priv.fuel = 0;
		}
		if (_priv.fuel > _priv.fuelMax) _priv.fuel = _priv.fuelMax;

		// UPDATE SHIP HEADING
		final float dh = deltaAngle(_targetHeading, _heading);
		final float d = (float) Math.exp(Math.log(2) * -_warp) * Const.ShipTurnSpeed;
		if (dh >= 0)
			_heading = dh < d ? _targetHeading : _heading + d;
		else
			_heading = -dh < d ? _targetHeading : _heading - d;

		// UPDATE SPEED
		if (_warp < _targetWarp) _warp = Math.min(_warp + Const.ShipAcc / Const.UpdateFrequency, _targetWarp);
		if (_warp > _targetWarp) _warp = Math.max(_warp - Const.ShipAcc / Const.UpdateFrequency, _targetWarp);

		// REPAIR
		_priv.shield = Math.min(_priv.shield + Const.ShieldRepair / Const.UpdateFrequency, _priv.shieldMax);
		if (!_pub.shields) {
			_priv.damage = Math.max(_priv.damage - Const.DamageRepair / Const.UpdateFrequency, 0);
			updateMaxWarp();
		}

		// UPDATE POSITION
		_x += (float) Math.cos(_heading) * _warp;
		_y += (float) Math.sin(_heading) * _warp;

		if (_x < 0) {
			_x = -_x;
			_heading = angleFlipX(_heading);
			_targetHeading = angleFlipX(_targetHeading);
		} else if (_x > Const.GalaxySize) {
			_x = 2 * Const.GalaxySize - _x;
			_heading = angleFlipX(_heading);
			_targetHeading = angleFlipX(_targetHeading);
		}
		if (_y < 0) {
			_y = -_y;
			_heading = angleFlipY(_heading);
			_targetHeading = angleFlipY(_targetHeading);
		} else if (_y > Const.GalaxySize) {
			_y = 2 * Const.GalaxySize - _y;
			_heading = angleFlipY(_heading);
			_targetHeading = angleFlipY(_targetHeading);
		}

		_pub.x = (short) _x;
		_pub.y = (short) _y;
		_pub.heading = (byte) codeAngle(_heading);
		_priv.warp = (short) (_warp + 0.5);
	}

	private static int codeAngle(final float a) {
		// -PI <= a <= PI 
		// 0 <= a / PI + 1 <= 2
		// 0 <= result <= 31
		return ((int) ((a / Math.PI + 1) * 16 + 0.5) + 24) % 32;
	}

	private static float deltaAngle(final float a, final float b) {
		float d = a - b;
		while (d > Math.PI)
			d -= Math.PI * 2;
		while (d < -Math.PI)
			d += Math.PI * 2;
		return d;
	}

	private static float angleFlipX(final float a) {
		return (float) Math.atan2(Math.sin(a), -Math.cos(a));
	}

	private static float angleFlipY(final float a) {
		return (float) Math.atan2(-Math.sin(a), Math.cos(a));
	}

	public void updateTorps() {
		for (int i = 0; i < _pub.torps; i++) {
			final Torp torp = _torps[i];

			torp.dx += (Math.random() - 0.5) * Const.TorpRand;
			torp.dy += (Math.random() - 0.5) * Const.TorpRand;

			torp.x += torp.dx;
			torp.y += torp.dy;
			_pub.torpX[i] = (short) torp.x;
			_pub.torpY[i] = (short) torp.y;
			torp.framesLeft -= 1;
		}
	}

	public void removeDeadTorps() {
		for (int i = 0; i < _pub.torps; i++) {
			final Torp torp = _torps[i];
			if (torp.x < 0 || torp.y < 0 || torp.x > Const.GalaxySize
					|| torp.y > Const.GalaxySize || torp.framesLeft == 0) {
				_pub.torps -= 1;
				_torps[i] = _torps[_pub.torps];
				_torps[_pub.torps] = torp;
				_pub.torpX[i] = _pub.torpX[_pub.torps];
				_pub.torpY[i] = _pub.torpY[_pub.torps];
			}
		}
	}

	public void sendState(final ByteBuffer buffer) {
		buffer.clear();
		_playerModel.write(buffer);

		buffer.flip();
		try {
			_channel.write(buffer);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}