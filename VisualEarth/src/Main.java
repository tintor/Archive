import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import tintor.geometry.Quaternion;

public class Main extends Controller {
	public Main() {
		super("Virtual Earth", 1000, 800, false);
	}

	@Override public void keyPressed(final KeyEvent e) {
		final boolean shift = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
		final boolean ctrl = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;

		final float rot = 0.04f / view.camera.zoom;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
		case KeyEvent.VK_LEFT:
			if (shift)
				view.camera.rotate(Quaternion.axisZ(-rot));
			else
				view.camera.rotate(Quaternion.axisY(-rot));
			break;
		case KeyEvent.VK_RIGHT:
			if (shift)
				view.camera.rotate(Quaternion.axisZ(rot));
			else
				view.camera.rotate(Quaternion.axisY(rot));
			break;
		case KeyEvent.VK_UP:
			if (ctrl)
				view.camera.zoom *= 1.01;
			else
				view.camera.rotate(Quaternion.axisX(-rot));
			break;
		case KeyEvent.VK_DOWN:
			if (ctrl)
				view.camera.zoom /= 1.01;
			else
				view.camera.rotate(Quaternion.axisX(rot));
			break;
		}
	}

	@Override public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		default:
		}
	}

	@Override public void mousePressed(final MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {

		}
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {

		}
	}

	@Override public void mouseDragged(final MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {

		}
	}

	@Override public void mouseWheelMoved(final MouseWheelEvent e) {
		if (e.getWheelRotation() > 0) {

		} else {

		}
	}

	public static void main(final String[] args) throws Exception {
		new Main().start();
	}
}