package tintor.netrek.client.model;

public class Ship {
	public static enum State {
		Normal, Cloaking, Cloaked, Uncloaking, Exploding;
	}

	public State state;

	public int x, y; // true if myship or !cloaked

	public Player player;

	public boolean shield;

	public int warp;

	public int direction; // 0..31
}