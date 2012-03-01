package logic;

public class LowZ extends Gate {
	private Bus a;

	public LowZ(Bus a) {
		super(1);
		this.a = a;
	}

	@Override
	public int func() {
		return a.isHighZ() ? 0 : 1;
	}
}