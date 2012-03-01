package logic;

public class Value extends Gate {
	public Value(boolean b) {
		super(1, b ? 1 : 0);
	}

	public Value() {
		this(1);
	}

	public Value(int bits) {
		this(0, bits);
	}

	public Value(int a, int bits) {
		super(bits, a);
		assert a >> bits == 0;
		val = a;
	}

	public @Override
	int func() {
		return val;
	}
}