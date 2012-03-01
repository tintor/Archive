package tintor.util;

public class SimpleThreadLocal<T> extends ThreadLocal<T> {
	private final T a;

	public SimpleThreadLocal(final T a) {
		this.a = a;
	}

	@Override protected T initialValue() {
		return a;
	}
}