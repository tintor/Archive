package tintor.util;

import java.util.ArrayList;
import java.util.Set;

public class SmallSet<T> extends ArrayList<T> implements Set<T> {
	@Override public boolean add(final T v) {
		for (final T w : this)
			if (w.equals(v)) return false;
		super.add(v);
		return true;
	}
}