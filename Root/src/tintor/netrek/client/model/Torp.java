package tintor.netrek.client.model;

public class Torp {
	public static enum Type {
		Photon, Plasma
	}

	public static enum State {
		Fired, Flying, Exploded
	}

	public Player owner;
	public int x, y;
	public State state;
	public Type type;
}