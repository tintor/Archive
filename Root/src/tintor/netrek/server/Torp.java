package tintor.netrek.server;

import java.util.List;

import tintor.netrek.Const;
import tintor.util.UnorderedArrayList;

final class Torp {
	static final List<Torp> list = new UnorderedArrayList<Torp>();

	static enum State {
		Fired, Flying, EnemyHit, EnemyDet, Disapeared
	}

	State state = State.Fired;
	float x, y;
	float dx, dy;
	int framesLeft;

	Player owner;
	int damage;

	Torp() {
		list.add(this);
	}

	@SuppressWarnings("fallthrough") void update() {
		if (state == State.Fired) state = State.Flying;
		if (state == State.Flying) {
			x += dx;
			y += dy;

			for (final Ship ship : Ship.circle(x, y, Const.ShipRadius))
				if (ship.player().race() != owner.race()) {
					state = State.EnemyHit;
					World.explosion(x, y, damage);
					break;
				}

			if (framesLeft == 0)
				state = State.Disapeared;
			else
				framesLeft -= 1;
		}
	}
}