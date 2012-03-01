package gui;

import logic.Gate;

import org.eclipse.swt.graphics.GC;

public class Label {
	final int x, y;
	final Gate gate;

	Label(Gate gate, int x, int y) {
		this.gate = gate;
		this.x = x;
		this.y = y;
	}

	void paint(GC gc) {
		gc.drawText(gate.toHex(), x, y);
	}
}