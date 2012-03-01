package tintor.search;

import java.util.HashMap;
import java.util.Map;

public abstract class Articulations<T> {
	private final Map<T, Integer> num = new HashMap<T, Integer>(), low = new HashMap<T, Integer>();
	private int counter, rootZ;

	public Articulations(final T root) {
		dfs(null, root);
		if (rootZ >= 2) articulation(root);
	}

	protected abstract void articulation(final T a);

	protected abstract void eachEdge(final T from, T a);

	protected final void edge(final T from, final T a, final T b) {
		if (from == null) {
			if (!num.containsKey(b)) {
				dfs(a, b);
				rootZ += 1;
			}
		} else if (b != from) {
			int z;
			if (!num.containsKey(b)) {
				dfs(a, b);
				z = low.get(b);
				if (z >= num.get(a)) articulation(a);
			} else
				z = num.get(b);
			if (z < low.get(a)) low.put(a, z);
		}
	}

	private void dfs(final T from, final T a) {
		num.put(a, counter);
		low.put(a, counter);
		counter++;
		eachEdge(from, a);
	}
}