package tintor.netrek_old.common;

public class Const {
	public final static int Port = 2000;

	public final static int GalaxySize = 1000;
	public final static int UpdateFrequency = 20;
	public final static int MaxShips = 10;

	public final static float ShipTurnSpeed = 5; // radians per frame at warp 0
	public final static int ShipMaxWarp = 9; // pixels per frame
	public final static int CruiseSpeed = 6;
	public final static float ShipAcc = 2.0f; // warps per second
	public final static int FuelGeneration = 400; // per second
	public final static int ShieldsFuelCost = 100; // per second

	public final static float ShieldRepair = 2f; // per second
	public final static float DamageRepair = 1f; // per second

	public final static int ShipRadius = 10; // pixels

	public static final int MaxTorps = 8;
	public static final int TorpFuelCost = 400;
	public static final int TorpSpeed = 10; // warps
	public static final int TorpDamage = 30; // warps
	public static final float TorpFuse = 2.5f; // in seconds
	public static final float TorpRand = 0.2f;

	public static final int Planets = 40;
}