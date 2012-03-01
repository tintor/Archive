package tintor;

public class Pair<A, B> {
	public final A first;
	public final B second;

	public Pair(final A first, final B second) {
		this.first = first;
		this.second = second;
	}

	@Override public String toString() {
		return "Pair(" + first + ", " + second + ")";
	}

	@SuppressWarnings("unchecked") @Override public final boolean equals(final Object o) {
		if (o == this) return true;
		try {
			final Pair p = (Pair) o;
			return first.equals(p.first) && second.equals(p.second);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override public final int hashCode() {
		return first.hashCode() * 773 + second.hashCode();
	}
}