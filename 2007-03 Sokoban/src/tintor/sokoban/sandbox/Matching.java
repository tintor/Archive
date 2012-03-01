package tintor.sokoban.sandbox;

public class Matching {
	static int[] minimalWeightedMatching(int[][] cost) {
		final int n = cost.length;

		// napravi proizvoljno mapiranje
		int[] link = new int[n];
		for (int i = 0; i < n; i++)
			link[i] = i;

		// strukture neophodne za trazenje ciklusa
		final int[] dist = new int[n], prev = new int[n];
		final boolean[] queued = new boolean[n];

		int qwrite, qread, qmax = 4;
		while (qmax < n)
			qmax *= 2;
		final int[] queue = new int[qmax];

		loop: while (true) {
			// pronadji negativni ciklus
			for (int i = 0; i < n; i++) {
				dist[i] = Integer.MAX_VALUE;
				prev[i] = -1;
				queued[i] = false;
			}
			dist[0] = 0;
			queue[0] = 0;
			qread = 0;
			qwrite = 1;

			while (qread < qwrite) {
				final int a = queue[qread++ & (qmax - 1)];
				queued[a] = false;

				for (int b = 0; b < n; b++) {
					int d = dist[a] + cost[a][link[b]] - cost[b][link[b]];
					if (d >= dist[b]) continue;

					if (prev[b] == a) {
						System.out.println("negative cycle");

						int[] oldLink = new int[n];
						for (int i = 0; i < n; i++)
							oldLink[i] = link[i];

						int c = b;
						do {
							System.out.println(c);
							link[prev[c]] = oldLink[c];
							c = prev[c];
						} while (c != b);
						System.out.println();

						System.out.println("mapping");
						for (int i = 0; i < link.length; i++)
							System.out.printf("%d - %d\n", i, link[i]);
						continue loop;
					}

					dist[b] = d;
					prev[b] = a;
					if (!queued[b]) {
						queued[b] = true;
						queue[qwrite++ & (qmax - 1)] = b;
					}
				}
			}
			break;
		}

		return link;
	}

	static int[] initial(int n) {
		int[] link = new int[n];
		for (int i = 0; i < n; i++)
			link[i] = i;
		return link;
	}
	
	static int[] simpleMatching(int[] link, int[][] cost) {
		assert link.length == cost.length;
		assert link.length == cost[0].length;
		
		boolean more = true;
		while (more) {
			more = false;
			for (int a = 0; a < link.length; a++)
				for (int b = a + 1; b < link.length; b++) {
					int ab = cost[a][link[b]], ba = cost[b][link[a]];
					int aa = cost[a][link[a]], bb = cost[b][link[b]];
					
					int i = inf(ab) + inf(ba) - inf(aa) - inf(bb);
					if (i < 0 || (i == 0 && ab + ba < aa + bb)) {
						int t = link[a];
						link[a] = link[b];
						link[b] = t;
						more = true;
					}
				}
		}
		return link;
	}

	static int inf(int a) {
		return a == Integer.MAX_VALUE ? 1 : 0;
	}
}