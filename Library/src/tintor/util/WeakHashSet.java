package tintor.util;

import java.lang.ref.WeakReference;

public class WeakHashSet<T> {
	private final NonCashingHashSet<WeakReference<T>> set = new NonCashingHashSet<WeakReference<T>>();

	public T intern(final T a) {
		if (a == null) throw new NullPointerException();
		final T b = set.put(new WeakReference<T>(a)).get();
		return b != null ? b : a;
	}
}