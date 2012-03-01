package tintor.netrek.common;

import java.awt.Color;

public enum Race {
	Unknown, Neutral, Fed, Rom, Kli, Ori;

	public Color color() {
		return color;
	}

	private Color color = Color.WHITE;

	static {
		Fed.color = Color.YELLOW;
		Rom.color = Color.RED;
		Kli.color = Color.GREEN;
		Ori.color = Color.BLUE;
	}
}