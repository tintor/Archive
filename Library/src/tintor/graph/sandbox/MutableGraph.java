package tintor.graph.sandbox;

public interface MutableGraph<T, E> extends Graph<T, E> {
	void add(T a);

	void remove(T a);

	void add(T a, T b);

	void add(T a, T b, E e);

	void remove(T a, T b);

	void clear();
}