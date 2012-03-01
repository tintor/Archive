package tintor.graph;

public class Test {
	public static void main(String[] args) {
		Graph<Integer, Integer> graph = new DirectedGraph<Integer, Integer>();

		graph.add(1);
		graph.add(2);
		graph.add(3);
		graph.add(4);
		graph.add(5);

		graph.setEdge(1, 3);
		graph.setEdge(3, 2);
		graph.setEdge(2, 5, -1);
		graph.setEdge(3, 4);

		assert !graph.containsEdge(3, 1);
		graph.setEdge(3, 1);
		assert graph.containsEdge(3, 1);
		graph.remove(3, 1);
		assert !graph.containsEdge(3, 1);

		System.out.println(graph);

		//		Visitor v = new Visitor() {
		//			public void visit(Object e) {
		//				System.out.println(e);
		//			}
		//		};

		//Algorithms.bfs(graph, v, 1);
	}
}