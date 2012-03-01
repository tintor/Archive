package tintor.graph;

public class UndirectedGraph<T, E> extends AbstractUndirectedGraph<T, E, AbstractUndirectedGraph.Node<T, E>> {
	public void add(T a) {
		super.add(a, new Node<T, E>());
	}
}