package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Logicka kapija sa jednim izlaznim signalom (n-bitnim).
 * 
 * @author Marko Tintor
 * @date 03/2006
 */
public abstract class Gate {
	public static final List<Gate> combGates = new ArrayList<Gate>();
	public static final List<SeqGate> seqGates = new ArrayList<SeqGate>();

	public static void resetAll() {
		for(Gate g : combGates)
			g.val = 0;
		for(SeqGate g : seqGates)
			g.val = g.old = 0;
	}

	public static void calculateCombGates() {
		for(Gate g : combGates) {
			g.val = g.func();
			assert g.val >>> g.bits == 0 : "func() je vratila više bitova nego što treba [gate=(" + g
					+ ") val=" + g.val + " bits=" + g.bits + "]";
		}
	}

	public static void nextCycle() {
		for(SeqGate g : seqGates)
			g.old = g.val;
		for(SeqGate g : seqGates) {
			g.val = g.func();
			assert g.val >>> g.bits == 0 : "func() je vratila više bitova nego što treba [gate=(" + g
					+ ") val=" + g.val + " bits=" + g.bits + "]";
		}

		calculateCombGates();
	}

	public static int[] saveState() {
		int[] a = new int[seqGates.size()];
		for(int i = 0; i < a.length; i++)
			a[i] = seqGates.get(i).val;
		return a;
	}

	public static void loadState(int[] a) {
		assert a.length == seqGates.size();
		for(int i = 0; i < a.length; i++)
			seqGates.get(i).old = seqGates.get(i).val = a[i];

		calculateCombGates();
	}

	/**
	 * tekuca vrednost na izlazu
	 */
	protected int val;

	public int val() {
		return val;
	}

	public boolean bool() {
		assert bits == 1;
		return val != 0;
	}

	/**
	 * broj bitova na izlazu
	 */
	public final int bits;

	// ovu funkciju pozivaju samo sekvencijalne mreze
	protected int old() {
		return val;
	}

	public Gate(int bits, int val) {
		assert 0 < bits && bits <= Integer.SIZE;
		this.bits = bits;
		this.val = trim(val);

		if(this instanceof SeqGate)
			seqGates.add((SeqGate)this);
		else if(!(this instanceof Value)) combGates.add(this);
	}

	public Gate(int bits) {
		this(bits, 0);
	}

	public final static Gate zero = new Gate(1, 0) {
		public @Override
		int func() {
			return 0;
		}
	};
	public final static Gate one = new Gate(1, 1) {
		public @Override
		int func() {
			return 1;
		}
	};

	public void set(boolean b) {
		assert bits == 1;
		val = b ? 1 : 0;
	}

	public void set(int a) {
		// (a je neoznačeni ili a je označeni) sa odgovarajućim brojem bitova
		assert (a >> bits) == 0 || (((a >> bits) == -1) && (((a >> (bits - 1)) & 1) == 1));
		val = trim(a);
	}

	@Override
	public String toString() {
		return getClass().getName() + "[bits:" + bits + "]";
	}

	public String toHex() {
		String a = Integer.toHexString(val).toUpperCase();
		while(a.length() < (bits + 3) / 4)
			a = '0' + a;
		return a;
	}

	public int toInt() {
		return (val << (Integer.SIZE - bits)) >> (Integer.SIZE - bits); // sign extend
	}

	public abstract int func();

	/**
	 * most significant bit
	 */
	public int msb() {
		return (val >> (bits - 1)) & 1;
	}

	public int mask() {
		return (1 << bits) - 1;
	}

	public Gate bit(final int p) {
		assert 0 <= p && p < bits;
		return new UnaryGate(this, 1) {
			public @Override
			int func() {
				return (a.val >> p) & 1;
			}
		};
	}

	/**
	 * Pocev od p-tog do q-tog bita 
	 */
	public Gate bits(final int p, int q) {
		assert q - p + 1 < bits;
		assert 0 <= p && p < q && q < bits;
		return new UnaryGate(this, q - p + 1) {
			@Override
			public String toString() {
				return "BitRange";
			}

			public @Override
			int func() {
				return trim(a.val >> p);
			}
		};
	}

	public Gate not() {
		return new UnaryGate(this) {
			@Override
			public int func() {
				return trim(~a.val);
			}
		};
	}

	public Gate xor(Gate a) {
		return new BinaryGate(this, a) {
			public @Override
			int func() {
				return a.val ^ b.val;
			}
		};
	}

	public Gate and(Gate a) {
		return new BinaryGate(this, a) {
			public @Override
			int func() {
				return a.val & b.val;
			}
		};
	}

	public Gate or(Gate a) {
		return new BinaryGate(this, a) {
			public @Override
			int func() {
				return a.val | b.val;
			}
		};
	}

	public Gate nand(Gate a) {
		return new BinaryGate(this, a) {
			public @Override
			int func() {
				return trim(~(a.val & b.val));
			}
		};
	}

	public Gate nor(Gate a) {
		return new BinaryGate(this, a) {
			public @Override
			int func() {
				return trim(~(a.val | b.val));
			}
		};
	}

	public Gate nxor(Gate a) {
		return new BinaryGate(this, a) {
			public @Override
			int func() {
				return trim(~(a.val ^ b.val));
			}
		};
	}

	/**
	 * Sign extension.
	 */
	public Gate extend(final int b) {
		assert b > bits;
		return new UnaryGate(this, b) {
			public @Override
			int func() {
				return trim(a.val << (b - a.bits)) >> (b - a.bits);
			}
		};
	}

	public Gate equal(final int x) {
		assert 0 <= x && x <= mask();
		return new UnaryGate(this, 1) {
			public @Override
			int func() {
				return a.val == x ? 1 : 0;
			}
		};
	}

	public Gate equal(final int x, final Gate enable) {
		assert enable.bits == 1;
		assert 0 <= x && x <= mask();
		return new UnaryGate(this, 1) {
			public @Override
			int func() {
				return a.val == x && (enable.val != 0) ? 1 : 0;
			}
		};
	}

	public Gate notEqual(final int x) {
		assert 0 <= x && x <= mask();
		return new UnaryGate(this, 1) {
			public @Override
			int func() {
				return a.val != x ? 1 : 0;
			}
		};
	}

	public Gate equal(Gate x) {
		return new BinaryGate(this, x, 1) {
			public @Override
			int func() {
				return a.val == b.val ? 1 : 0;
			}
		};
	}

	public Gate lessSigned(Gate x) {
		assert x.bits < 32;
		return new BinaryGate(this, x, 1) {
			public @Override
			int func() {
				return a.toInt() < b.toInt() ? 1 : 0;
			}
		};
	}

	public Gate lessUnsigned(Gate x) {
		assert x.bits < 32;
		return new BinaryGate(this, x, 1) {
			public @Override
			int func() {
				return a.val < b.val ? 1 : 0;
			}
		};
	}

	public Gate greaterUnsigned(Gate x) {
		assert x.bits < 32;
		return new BinaryGate(this, x, 1) {
			public @Override
			int func() {
				return a.val > b.val ? 1 : 0;
			}
		};
	}

	public Gate add(Gate x) {
		return new BinaryGate(this, x) {
			public @Override
			int func() {
				return trim(a.val + b.val);
			}
		};
	}

	public Gate sub(Gate x) {
		return new BinaryGate(this, x) {
			public @Override
			int func() {
				return trim(a.val - b.val);
			}
		};
	}

	/**
	 * Cuts off extra bits
	 */
	protected int trim(int a) {
		return a & mask();
	}

	public static Gate add(final Gate ax, final Gate bx, final Gate c) {
		assert ax.bits == bx.bits && c.bits == 1;
		return new Gate(ax.bits) {
			public @Override
			int func() {
				return trim(ax.val + bx.val + c.val);
			}
		};
	}

	public static Gate and(Gate... in) {
		return new PolyGate(simetric(in), in) {
			public @Override
			int func() {
				int x = ~0;
				for(Gate c : in)
					x &= c.val;
				return x;
			}
		};
	}

	public static Gate or(Gate... in) {
		return new PolyGate(simetric(in), in) {
			public @Override
			int func() {
				int x = 0;
				for(Gate c : in)
					x |= c.val;
				return x;
			}
		};
	}

	public static Gate nor(Gate... in) {
		return new PolyGate(simetric(in), in) {
			public @Override
			int func() {
				int x = 0;
				for(Gate c : in)
					x |= c.val;
				return trim(~x);
			}
		};
	}

	public static Gate xor(Gate... in) {
		return new PolyGate(simetric(in), in) {
			public @Override
			int func() {
				int x = 0;
				for(Gate c : in)
					x ^= c.val;
				return x;
			}
		};
	}

	private static int simetric(Gate[] x) {
		assert x.length >= 2;
		for(Gate c : x)
			assert c.bits == x[0].bits;
		return x[0].bits;
	}

	/**
	 * Kapija koja spaja više signala u jedan.
	 */
	public static Gate merge(Gate... in) {
		int b = 0;
		for(Gate c : in)
			b += c.bits;

		return new PolyGate(b, in) {
			public @Override
			int func() {
				int x = 0;
				for(Gate c : in) {
					x <<= c.bits;
					x |= c.val;
				}
				return x;
			}
		};
	}

	/**
	 * Multiplexer
	 */
	public Gate select(final Gate... m) {
		assert m.length == (1 << bits);
		final Gate xsel = this;
		return new Gate(m[0].bits) {
			private final Gate sel = xsel; // 1 k-bitni ulaz
			private final Gate[] in = m; // 2^k n-bitnih ulaza

			public @Override
			int func() {
				return in[sel.val].val;
			}

			@Override
			public String toString() {
				return "Multiplexer";
			}

			//			@Override
			//			Gate[] dependencies() {
			//				Gate[] x = new Gate[1 + in.length];
			//				System.arraycopy(in, 0, x, 1, in.length);
			//				x[0] = sel;
			//				return x;
			//			}
		};
	}
}

/**
 * Kapija sa jednim ulazom.
 */
abstract class UnaryGate extends Gate {
	protected final Gate a;

	public UnaryGate(Gate a) {
		this(a, a.bits);
	}

	public UnaryGate(Gate a, int bits) {
		super(bits);
		this.a = a;
	}

	//	@Override
	//	final Gate[] dependencies() {
	//		return new Gate[] { a };
	//	}
}

/**
 * Kapija sa dva ulaza.
 */
abstract class BinaryGate extends Gate {
	protected final Gate a, b;

	public BinaryGate(Gate a, Gate b) {
		this(a, b, a.bits);
	}

	public BinaryGate(Gate a, Gate b, int bits) {
		super(bits);
		assert a.bits == b.bits;
		this.a = a;
		this.b = b;
	}

	//	@Override
	//	final Gate[] dependencies() {
	//		return new Gate[] { a, b };
	//	}
}

/**
 * Kapija sa više ulaza.
 */
abstract class PolyGate extends Gate {
	protected final Gate[] in;

	public PolyGate(int bits, Gate... in) {
		super(bits);
		this.in = in;
	}

	//	@Override
	//	Gate[] dependencies() {
	//		return in;
	//	}
}

/*class BitRegister extends Gate {
 private Gate reset, set, load, data;

 public BitRegister() {
 super(1);
 }

 public void setLoad(Gate a) {
 assert a.bits == 1;
 this.load = a;
 }

 public void setData(Gate a) {
 assert a.bits == 1;
 this.data = a;
 }

 public void setReset(Gate a) {
 assert a.bits == 1;
 this.reset = a;
 }

 public void setSet(Gate a) {
 assert a.bits == 1;
 this.set = a;
 }

 @Override
 int func() {
 assert reset.old == 0 || set.old == 0;
 if(set.old != 0) return 1;
 if(reset.old != 0) return 0;
 if(load.old != 0) return data.old;
 return old;
 }
 }*/