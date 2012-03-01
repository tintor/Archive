package tintor;

public class ComparablePair<A extends Comparable<A>, B extends Comparable<B>> extends Pair<A, B> implements
		Comparable<ComparablePair<A, B>> {
	public ComparablePair(final A first, final B second) {
		super(first, second);
	}

	@Override public final int compareTo(final ComparablePair<A, B> p) {
		final int c = first.compareTo(p.first);
		if (c != 0) return c;
		return second.compareTo(p.second);
	}
}