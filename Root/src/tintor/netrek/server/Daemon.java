package tintor.netrek.server;

import tintor.netrek.Const;
import tintor.netrek.util.ump.Server;

public class Daemon {
	public static void main(final String[] args) {
		Server.listen(Const.Port);
		while (true) {
			final long frameStart = System.nanoTime();
			World.processFrame();
			sleepUntil(frameStart + 1000000000 / Const.UpdateFrequency);
		}
	}

	public static void sleepUntil(final long time) {
		final long timeLeft = time - System.nanoTime();
		if (timeLeft > 0) try {
			Thread.sleep((timeLeft + 500000) / 1000000);
		} catch (final InterruptedException e) {}
	}
}