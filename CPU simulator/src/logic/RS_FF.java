package logic;

public class RS_FF extends SeqGate {
	private Gate r, s;

	public RS_FF() {
		super(1);
	}

	public RS_FF(Gate r, Gate s) {
		super(1);
		this.r = r;
		this.s = s;
	}

	public void attach(Gate r, Gate s) {
		assert r.bits == 1 && s.bits == 1 && this.r == null && this.s == null;
		this.r = r;
		this.s = s;
	}

	public @Override
	int func() {
		return s.old() | (~r.old() & old);
	}
}