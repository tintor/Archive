package tintor.netrek.client.view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class Images {
	static final Image fed = transparent(open("ship/fedship.bmp"));
	static final Image rom = transparent(open("ship/romship.bmp"));
	static final Image kli = transparent(open("ship/kliship.bmp"));
	static final Image ori = transparent(open("ship/oriship.bmp"));

	static final Image torp = transparent(open("weapon/torpC.bmp"));

	static final Image army = open("planet/army.bmp");
	static final Image fuel = open("planet/fuel.bmp");
	static final Image wrench = open("planet/wrench.bmp");

	static final Image rock1 = open("planet/rock1.bmp");
	static final Image rock2 = open("planet/rock2.bmp");
	static final Image agri1 = open("planet/agri1.bmp");
	static final Image agri2 = open("planet/agri2.bmp");
	static final Image earth = open("planet/earth.bmp");
	static final Image romulus = open("planet/romulus.bmp");
	static final Image klingus = open("planet/klingus.bmp");
	static final Image orion = open("planet/orion.bmp");
	static final Image unknown = open("planet/munknown.bmp");

	static BufferedImage open(final String file) {
		try {
			return ImageIO.read(new File("data/images/" + file));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	static Image transparent(final BufferedImage im) {
		final ImageFilter filter = new RGBImageFilter() {
			public int markerRGB = im.getRGB(0, 0) | 0xFF000000;

			@Override public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) return 0x00FFFFFF & rgb;
				return rgb;
			}
		};
		return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(im.getSource(), filter));
	}
}