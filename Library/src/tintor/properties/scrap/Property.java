package tintor.properties.scrap;

public class Property<T> {
	public final Properties properties;
	public final String key;

	public static <T> Property<T> instance(final Class<?> kv, final String key, final T init) {
		return Properties.properties.property(kv, key, init);
	}

	Property(final Properties properties, final String key) {
		this.properties = properties;
		this.key = key;
	}

	public void addListener(final PropertyListener listener) {
		properties.internalAddListener(key, listener);
	}

	public void removeListener(final PropertyListener listener) {
		properties.internalRemoveListener(key, listener);
	}

	@SuppressWarnings("unchecked") public T get() {
		return (T) properties.internalGet(key);
	}

	public void set(final T value) {
		properties.internalSet(key, value);
	}

	public void remove() {
		properties.internalRemove(key);
	}
}