package logic;

public abstract class SeqGate extends Gate {
	/**
	 * vrednost na izlazu na pocetku takta
	 */
	protected int old;

	@Override
	public int old() {
		return old;
	}

	public SeqGate(int bits) {
		super(bits);
	}

	public SeqGate(int bits, int val) {
		super(bits, val);
	}

	//	@Override
	//	Gate[] dependencies() {
	//		return new Gate[] {};
	//	}

	public void prepare() {
		old = val;
	}

	public abstract int func();
}