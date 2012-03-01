	public static Key solveWithIDAStar(Key start, Heuristic heuristic) {
		if (start.isGoal()) return start;
		if (Util.goals(start.agent).size() < start.boxes.length) return null;
		if (start.fullDeadlockTest()) return null;

		final Search<Key> keySearch = new DepthFirstSearch<Key>();
		final Search<Cell> cellSearch = new DepthFirstSearch<Cell>();

		int limit = heuristic.estimate(start);
		boolean more = true;
		int totalOpened = 0;

		try {
			while (more) {
				int opened = 0;
				try {
					System.out.print("limit " + limit);
					System.out.flush();
					more = false;

					keySearch.add(start);
					for (Key key : keySearch) {
						opened++;

						cellSearch.add(key.agent);
						for (Cell a : cellSearch)
							for (Cell.Edge e : a.edges)
								if (key.hasBox(e.cell)) {
									Key nkey = key.pushBox(e.cell, e.dir);
									if (nkey != null) {
										if (nkey.isGoal()) return nkey;
										if (nkey.distance + heuristic.estimate(nkey) <= limit)
											keySearch.add(nkey);
										else
											more = true;
									}
								} else
									cellSearch.add(e.cell);
						cellSearch.clear();
					}
					keySearch.clear();

					totalOpened += opened;
					limit++;
				} finally {
					System.out.println(", opened " + opened);
				}
			}
			return null;
		} finally {
			System.out.println("opened " + totalOpened);
		}
	}
