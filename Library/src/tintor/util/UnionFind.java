package tintor.util;

public final class UnionFind {
	private UnionFind group = this;
	private int rank;

	public void union(final UnionFind a) {
		union(this, a);
	}

	public static void union(UnionFind a, UnionFind b) {
		a = a.group();
		b = b.group();
		if (a == b) return;

		if (a.rank > b.rank)
			b.group = a;
		else if (a.rank < b.rank)
			a.group = b;
		else {
			a.group = b;
			b.rank++;
		}
	}

	public UnionFind group() {
		UnionFind a = this;
		while (a.group != a)
			a = a.group;

		UnionFind p = this;
		while (p.group != p) {
			final UnionFind t = p.group;
			p.group = a;
			p = t;
		}

		return a;
	}
}