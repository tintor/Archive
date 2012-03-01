package tintor.netrek.client.model;

import tintor.netrek.common.Race;
import tintor.netrek.common.ShipClass;

public class Player {
	public Race race; // null if race not yet selected
	public int id;

	public Ship ship; // null if cloacked or dead
	public ShipClass shipclass; // null if dead
	public boolean alive;
	public float kills;

	public String name;
	public String address;
}