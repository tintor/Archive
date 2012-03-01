package tintor.util;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class SoundUtils {
	public static Clip open(final String file) {
		try {
			final AudioInputStream stream = AudioSystem.getAudioInputStream(new File(file));
			final Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, stream.getFormat()));
			clip.open(stream);
			return clip;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}