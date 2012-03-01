package tintor.swt;

public enum Align {
	Left {
		@Override public int offset(@SuppressWarnings("unused") final int size) {
			return 0;
		}
	},
	Center {
		@Override public int offset(final int size) {
			return -size / 2;
		}
	},
	Right {
		@Override public int offset(final int size) {
			return -size;
		}
	};

	public abstract int offset(int size);
}