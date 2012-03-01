package gui;

public class HotSpot {
	final String screen;
	final int x1, x2, y1, y2;

	public HotSpot(String screen, int x1, int y1, int x2, int y2) {
		this.screen = screen;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	boolean contains(int x, int y) {
		return x1 <= x && x <= x2 && y1 <= y && y <= y2;
	}
}