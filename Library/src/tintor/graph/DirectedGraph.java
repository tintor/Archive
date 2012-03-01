package tintor.graph;

public class DirectedGraph<T, E> extends AbstractDirectedGraph<T, E, AbstractDirectedGraph.Node<T, E>> {
	@Override public void add(final T a) {
		if (!contains(a)) super.add(a, new Node<T, E>());
	}
}