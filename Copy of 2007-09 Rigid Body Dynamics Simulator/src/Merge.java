import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

class Acc {
	float red, green, blue;

	void add(final int p, final float s, final float d) {
		red = red * d + (p >> 16 & 0xFF) / 255f * s;
		green = green * d + (p >> 8 & 0xFF) / 255f * s;
		blue = blue * d + (p & 0xFF) / 255f * s;
	}

	void mul(final float s) {
		red *= s;
		green *= s;
		blue *= s;
	}

	int get() {
		return ((int) (red * 255) << 16) + ((int) (green * 255) << 8) + (int) (blue * 255);
	}
}

public class Merge {
	public static void main(final String[] args) throws Exception {
		final int start = 0, end = 3;

		final List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (int i = start; i <= end; i += 1)
			images.add(ImageIO.read(new File(String.format("screen%03d.png", i))));

		final int w = images.get(0).getWidth();
		final int h = images.get(0).getHeight();

		final BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final int[] a = new int[images.size()];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				for (int i = 0; i < images.size(); i++)
					a[i] = images.get(i).getRGB(x, y);

				final int m = median(a);

				int p = m;

				for (int i = 0; i < images.size(); i++) {
					final int e = images.get(i).getRGB(x, y);
					if (e != m) p = e;
				}

				result.setRGB(x, y, p);
			}

		//		for (int y = 0; y < h; y++)
		//			for (int x = 0; x < w; x++) {
		//				float red = 0, green = 0, blue = 0;
		//				for (int i = 0; i < count; i++) {
		//					final int p = images[i].getRGB(x, y);
		//					red += (p >> 16 & 0xFF) / 255f;
		//					green += (p >> 8 & 0xFF) / 255f;
		//					blue += (p & 0xFF) / 255f;
		//				}
		//				red /= count;
		//				green /= count;
		//				blue /= count;
		//				final int p = ((int) (red * 255) << 16) + ((int) (green * 255) << 8) + (int) (blue * 255);
		//				result.setRGB(x, y, p);
		//			}

		ImageIO.write(result, "png", new File("result2B.png"));
	}

	public static int median(final int[] a) {
		Arrays.sort(a);
		int prev = Integer.MAX_VALUE;
		int c = 0;
		int b = -1, bc = 0;
		for (final int x : a) {
			if (x != prev) c = 0;
			c += 1;
			if (c > bc) {
				bc = c;
				b = x;
			}
			prev = x;
		}
		return b;
	}
}