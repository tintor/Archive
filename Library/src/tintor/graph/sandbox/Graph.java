package tintor.graph.sandbox;

import java.util.Set;

public interface Graph<T, E> extends Set<T> {
	Set<T> in(T a);

	Set<T> out(T a);

	boolean contains(T a, T b);

	E edge(T a, T b);
}