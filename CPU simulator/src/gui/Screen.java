package gui;

import gui.util.GUI;

import java.util.ArrayList;
import java.util.List;

import logic.Gate;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import simulator.CPU;

public class Screen {
	final String title;
	final int width, height;
	Point origin = new Point(0, 0);

	protected final List<Wire> wires = new ArrayList<Wire>();
	protected final List<Label> labels = new ArrayList<Label>();
	protected final List<HotSpot> hotspots = new ArrayList<HotSpot>();
	private HotSpot selhs;

	final Image image;
	protected CPU cpu = Main.sim.cpu;

	public Screen(String title) {
		this.title = title;
		image = new Image(GUI.display, "data/" + title + ".png");
		this.width = image.getImageData().width;
		this.height = image.getImageData().height;
	}

	void paint(GC gc) {
		gc.setForeground(GUI.brown);
		gc.setLineWidth(2);
		for(HotSpot a : hotspots)
			gc.drawRectangle(a.x1, a.y1, a.x2 - a.x1 + 1, a.y2 - a.y1 + 1);

		for(Wire a : wires)
			a.paint(gc);
		gc.setLineWidth(1);

		gc.setForeground(GUI.black);
		gc.setBackground(GUI.white);

		//		Font f = gc.getFont();
		//		FontData[] fd = f.getFontData();
		//		for(FontData a : fd) {
		//			a.setStyle(a.getStyle() | SWT.BOLD);
		//			a.setHeight(10);
		//		}
		//		gc.setFont(new Font(GUI.display, fd));

		for(Label a : labels)
			a.paint(gc);
	}

	void mouseMove(MouseEvent e) {
		selhs = null;
		for(HotSpot a : hotspots)
			if(a.contains(e.x, e.y)) {
				selhs = a;
				break;
			}

		Main.main.canvas.setCursor(selhs != null ? GUI.cross : GUI.arrow);
	}

	boolean mouseDown(MouseEvent e) {
		if(selhs == null) return false;
		Main.main.gotoScreen(Main.main.findScreen(selhs.screen));
		return true;
	}

	boolean mouseUp(MouseEvent e) {
		return false;
	}

	protected void wire(Gate gate, String desc, boolean q) {
		wires.add(new Wire(gate, desc, q));
	}

	protected void wire(Gate gate, String desc) {
		wires.add(new Wire(gate, desc));
	}

	protected void hotspot(String screen, int x1, int y1, int x2, int y2) {
		hotspots.add(new HotSpot(screen, x1, y1, x2, y2));
	}

	protected void label(Gate gate, int x, int y) {
		labels.add(new Label(gate, x, y));
	}
}