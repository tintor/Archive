package tintor.graph.sandbox;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import tintor.graph.sandbox.DirectedGraph;

public class AlgorithmsTest {
	@Test public final void negativeCostCycle() {
		final MutableGraph<String, Integer> graph = new DirectedGraph<String, Integer>();
		graph.add("a");
		graph.add("b");
		graph.add("c");
		graph.add("d");
		graph.setEdge("a", "b", 1);
		graph.setEdge("b", "c", 2);
		graph.setEdge("c", "d", -8);
		graph.setEdge("d", "b", 3);

		List<String> cycle = Algorithms.negativeCostCycle(graph);
		for (String a : cycle)
			System.out.println(a);

		int cost = 0;
		String p = cycle.get(cycle.size() - 1);
		for (String a : cycle)
			cost += graph.edge(p, p = a);
		
		Assert.assertEquals(-3, cost);
	}
}