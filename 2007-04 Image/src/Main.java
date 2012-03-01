import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.sun.media.jai.widget.DisplayJAI;

public class Main implements MouseMotionListener, MouseListener {
	@SuppressWarnings("serial") public Main() throws IOException {
		final PlanarImage imageA = JAI.create("fileload", "c:/temp/super pamcenje/010.tif");

		//		final PlanarImage imageA = JAI.create("bandcombine", original, new double[][] { { 0.3333, 0.3334,
		//				0.3333, 0 } });

		// Invert image
		// image = JAI.create("invert", image);

		//		final PlanarImage edgeDown = JAI.create("convolve", imageA, new KernelJAI(3, 3, new float[] { -1,
		//				-2, -1, 0, 0, 0, 1, 2, 1 }));
		//		final PlanarImage edgeRight = JAI.create("convolve", imageA, new KernelJAI(3, 3, new float[] { -1,
		//				0, 1, -2, 0, 2, -1, 0, 1 }));

		final PlanarImage x = convolve(imageA, sharpen(5));
		//final PlanarImage imageB = JAI.create("binarize", imageA, 200.0);
		//final PlanarImage imageB = JAI.create("binarize", convolve(imageA, gaussian(5, 0.35)), 200.0);
		final PlanarImage imageB = convolve(imageA, gaussian(5, 0.45));

		//final PlanarImage imageC = invert(convolve(imageA, sharpen(7)));
		final PlanarImage imageC = convolveU(convolve(imageA, 0, -1, 0, -1, 4, -1, 0, -1, 0), 16, kernel(4,
				1 / 16.0f));
		//		final PlanarImage imageC = convolve(imageA, -1, -1, 0, -1, 0, 1, 0, 1, 1);

		final PlanarImage imageD = convolve(imageA, 0, -1, 0, -1, 4, -1, 0, -1, 0);
		//final PlanarImage imageD = convolve(imageA, 0, -1, 0, 0, 0, 0, 0, 1, 0);

		final Container container = new Container();
		container.setLayout(new GridLayout(2, 2));

		final JScrollPane a = new JScrollPane(new DisplayJAI(imageA));
		hor = a.getHorizontalScrollBar().getModel();
		ver = a.getVerticalScrollBar().getModel();
		a.addMouseListener(this);
		a.addMouseMotionListener(this);

		container.add(a);
		container.add(createScrollPane(imageB));
		container.add(createScrollPane(imageC));
		container.add(createScrollPane(imageD));

		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(container);
		frame.pack();
		frame.setVisible(true);

	}

	static float[] kernel(final int s, final float k) {
		final float[] kernel = new float[s * s];
		for (int i = 0; i < s * s; i++)
			kernel[i] = k;
		return kernel;
	}

	static float[] gaussian(final int s, final double sigma) {
		final float[] kernel = new float[s * s];
		final double k = -0.5 / sigma / sigma;
		for (int x = 0; x < s; x++)
			for (int y = 0; y < s; y++) {
				final int sx = x - s / 2, sy = y - s / 2;
				kernel[y * s + x] = (float) Math.exp((sx * sx + sy * sy) * k);
			}
		return kernel;
	}

	static float[] sharpen(final int s) {
		final float[] kernel = kernel(s, -1.0f / (s * s));
		kernel[(s * s - 1) / 2] += 1;
		return kernel;
	}

	static PlanarImage scale(final PlanarImage image, final float scale) {
		return JAI.create("scale", new ParameterBlock().addSource(image).add(scale).add(scale).add(0.0f).add(
				0.0f).add(new InterpolationBilinear()));
	}

	static PlanarImage invert(final PlanarImage image) {
		return JAI.create("invert", image);
	}

	static PlanarImage convolve(final PlanarImage image, final float... kernel) {
		final int s = (int) Math.round(Math.sqrt(kernel.length));
		return JAI.create("convolve", image, new KernelJAI(s, s, kernel));
	}

	static PlanarImage convolveU(final PlanarImage image, final int width, final float... kernel) {
		return JAI.create("convolve", image, new KernelJAI(width, kernel.length / width, kernel));
	}

	final BoundedRangeModel hor, ver;

	int startX, startY;
	int startHor, startVer;

	@Override public void mouseClicked(final MouseEvent e) {}

	@Override public void mouseEntered(final MouseEvent e) {}

	@Override public void mouseExited(final MouseEvent e) {}

	@Override public void mousePressed(final MouseEvent e) {
		startX = e.getX();
		startY = e.getY();
		startHor = hor.getValue();
		startVer = ver.getValue();
	}

	@Override public void mouseReleased(final MouseEvent e) {}

	@Override public void mouseDragged(final MouseEvent e) {
		final int dx = startX - e.getX();
		final int dy = startY - e.getY();

		hor.setValue(startHor + dx);
		ver.setValue(startVer + dy);
	}

	@Override public void mouseMoved(final MouseEvent e) {}

	JScrollPane createScrollPane(final PlanarImage image) {
		final JScrollPane b = new JScrollPane(new DisplayJAI(image));
		b.addMouseListener(this);
		b.addMouseMotionListener(this);
		b.getHorizontalScrollBar().setModel(hor);
		b.getVerticalScrollBar().setModel(ver);
		return b;
	}

	public static void main(final String[] args) throws Exception {
		new Main();
	}
}