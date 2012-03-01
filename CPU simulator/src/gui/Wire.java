package gui;

import gui.util.GUI;

import java.util.ArrayList;
import java.util.List;

import logic.Bus;
import logic.Gate;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class Wire {
	static class Line {
		int x1, x2, y1, y2;

		Line(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
	}

	static class Point {
		int x, y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	final Line[] lines;
	final Point[] points;
	final Gate gate;
	boolean yel;

	Wire(Gate gate, String desc, boolean q) {
		this(gate, desc);
		yel = true;
	}

	Wire(Gate gate, String desc) {
		List<Line> lines = new ArrayList<Line>();
		List<Point> points = new ArrayList<Point>();

		int ox = 0, oy = 0, x = 0, y = 0;
		boolean first = true;

		for(String a : desc.split("\\s+")) {
			if(a.indexOf('@') != -1) {
				x = Integer.parseInt(a.substring(0, a.indexOf('@')));
				y = Integer.parseInt(a.substring(a.indexOf('@') + 1));

				if(first) {
					ox = x;
					oy = y;
					first = false;
				} else {
					x += ox;
					y += oy;
					points.add(new Point(x, y));
				}
				continue;
			}

			assert a.charAt(0) == 'D' || a.charAt(0) == 'U' || a.charAt(0) == 'R' || a.charAt(0) == 'L';
			assert a.length() > 1;
			int d = Integer.parseInt(a.substring(1));
			switch(a.charAt(0)) {
			case 'U':
				lines.add(new Line(x, y, x, y - d));
				y -= d;
				break;
			case 'D':
				lines.add(new Line(x, y, x, y + d));
				y += d;
				break;
			case 'L':
				lines.add(new Line(x - d, y, x, y));
				x -= d;
				break;
			case 'R':
				lines.add(new Line(x + d, y, x, y));
				x += d;
				break;
			}
		}

		this.gate = gate;
		this.lines = lines.toArray(new Line[] {});
		this.points = points.toArray(new Point[] {});
	}

	void paint(GC gc) {
		Color c = gate.toInt() != 0 ? GUI.red : GUI.blue;
		if(gate instanceof Bus) {
			Bus bus = (Bus)gate;
			if(bus.isHighZ()) c = GUI.highZ;
			else if(gate.bits > 1) c = GUI.green_yellow;
		}
		if(yel) c = GUI.green;

		gc.setForeground(c);
		gc.setBackground(c);

		if(gate instanceof Bus && gate.bits > 1) {
			gc.setLineWidth(5);
			for(Line p : lines)
				gc.drawLine(p.x1, p.y1, p.x2, p.y2);
			gc.setLineWidth(2);
		} else
			for(Line p : lines)
				gc.drawLine(p.x1, p.y1, p.x2, p.y2);

		for(Point p : points)
			gc.fillOval(p.x - 3, p.y - 3, 6, 6);
	}
}