package tintor.netrek_old.server;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import tintor.netrek.client.model.Planet;
import tintor.netrek_old.common.Const;
import tintor.netrek_old.common.PlayerModel;
import tintor.netrek_old.common.Race;
import tintor.netrek_old.common.Ship;


public class World {
	private int _frame;
	private final Player[] _players = new Player[Const.MaxShips];
	private final Planet[] _planets = new Planet[Const.Planets];

	public World() {
		final Ship[] pub = new Ship[Const.MaxShips];
		for (int i = 0; i < _players.length; i++) {
			_players[i] = new Player();
			_players[i]._playerModel = new PlayerModel((byte) i, pub, _players[i]._priv, _planets);
			pub[i] = _players[i]._pub;
		}

		for (int i = 0; i < _planets.length; i++)
			_planets[i] = new Planet();

		final Planet a = _planets[0];
		a.touched = true;
		a.home = true;
		a.fuel = true;
		a.repair = true;
		a.race = Race.Kli;
		a.armies = 6;
		a.name[0] = 'K';
		a.name[1] = 'l';
		a.name[2] = 'i';
		a.x = 300;
		a.y = 300;
	}

	public boolean connect(final SocketAddress address) {
		for (final Player player : _players)
			if (!player._pub.alive) {
				player.init(address);
				return true;
			}
		return false;
	}

	public void receiveCommands(final ByteBuffer buffer) {
		for (final Player player : _players)
			if (player._pub.alive) player.receiveCommands(buffer);
	}

	public void updateState() {
		_frame += 1;

		// update ships and torps
		for (final Player player : _players) {
			if (player._pub.alive) player.updateShip();
			player.updateTorps();
		}

		// detect torp hits
		// TODO optimize, divide galaxy into buckets, and put ships into them
		for (final Player playerA : _players)
			for (int i = 0; i < playerA._pub.torps; i++) {
				final Torp torp = playerA._torps[i];
				if (torp.framesLeft > 0)
					for (final Player playerB : _players)
						if (playerB != playerA && playerB._pub.alive
								&& playerB.quad(torp) <= Const.ShipRadius * Const.ShipRadius) {
							torp.framesLeft = 0;
							playerB.damage(Const.TorpDamage);
							// TODO if player dead then explode!
						}
			}

		// remove dead torps
		for (final Player player : _players)
			player.removeDeadTorps();
	}

	public void sendState(final ByteBuffer buffer) {
		for (final Player player : _players)
			if (player._pub.alive) player.sendState(buffer);
	}
}