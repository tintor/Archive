package tintor.netrek.client.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {
	public static final Player myself = new Player();
	public static final Ship myship = new Ship();

	public static final List<Planet> planets = new ArrayList<Planet>();
	public static final List<Player> players = new ArrayList<Player>();
	public static final List<Ship> ships = new ArrayList<Ship>();
	public static final List<Torp> torps = new ArrayList<Torp>();
	public static final List<Phaser> phasers = new ArrayList<Phaser>();

	public static int phaser, fuel, armies, shield, hull, etemp, wtemp;
	public static boolean etemped, wtemped;
	public static Ship tractor, pressor;

	public static void read(final ByteBuffer buffer) {
	// TODO Auto-generated method stub
	}
}