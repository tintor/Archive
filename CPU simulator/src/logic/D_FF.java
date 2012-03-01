package logic;

public class D_FF extends SeqGate {
	private Gate d;

	public D_FF() {
		super(1);
	}
	
	public D_FF(Gate d) {
		super(1);
		assert d.bits == 1;
		this.d = d;
	}

	public void attach(Gate d) {
		assert d.bits == 1;
		this.d = d;
	}

	public @Override
	int func() {
		return d.old();
	}
}