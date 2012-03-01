package tintor;

public final class Timer {
	public long time;
	private long start = Long.MIN_VALUE; // MIN_VALUE means that timer is not running!

	public long restart() {
		if (start != Long.MIN_VALUE) {
			final long now = System.nanoTime(), delta = now - start;
			time += delta;
			start = now;
			return delta;
		}
		start = System.nanoTime();
		return 0;
	}

	public void stop() {
		if (start != Long.MIN_VALUE) {
			time += System.nanoTime() - start;
			start = Long.MIN_VALUE;
		}
	}

	public static double seconds(final long time) {
		return time * 1e-9;
	}

	public double seconds() {
		return seconds(time);
	}

	private static final long micro = 1000L;
	private static final long mili = 1000L * micro;
	private static final long sec = 1000L * mili;
	private static final long min = 60L * sec;
	private static final long hour = 60L * min;
	private static final long day = 24L * hour;

	public static String format(final long t) {
		if (t < 20 * micro) return t + "ns";
		if (t < 20 * mili) return (t + micro / 2) / micro + "us";
		if (t < 20 * sec) return (t + mili / 2) / mili + "ms";
		if (t < 10 * min) return (t + sec / 2) / sec + "s";
		if (t < 10 * hour) return (t + min / 2) / min + "min";
		if (t < 10 * day) return (t + hour / 2) / hour + "hour";
		return (t + day / 2) / day + "day";
	}

	@Override
	public String toString() {
		return format(time);
	}
}