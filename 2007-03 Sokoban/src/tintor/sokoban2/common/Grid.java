package tintor.sokoban2.common;

public interface Grid {
	int height();

	int width();

	boolean wall(int x, int y);

	boolean goal(int x, int y);

	boolean box(int x, int y);

	boolean agent(int x, int y);

	boolean hole(int x, int y); // agent can move across hole, but boxes pushed inside are destroyed!
}