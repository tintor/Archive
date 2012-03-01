package tintor.sandbox.servo;

import java.util.Random;

import tintor.geometry.GMath;

public class ServoTester2 {
	public static void main(final String[] args) {
		final Random rand = new Random();
		double steps = 0;

		for (int i = 0; i < ServoBreeder.tests; i++) {
			double r = rand.nextDouble() * 10 - 5;
			while (Math.abs(r) < 0.5)
				r = rand.nextDouble() * 10 - 5;
			double v = rand.nextDouble() - 0.5;
			double amax = rand.nextDouble() * 0.05 + 0.05;

			int s = 0;
			while ((Math.abs(r) >= 0.02 || Math.abs(v) >= 0.002) && s < ServoBreeder.maxSteps) {
				final double a = GMath.clamp(servo(r, v, amax, 0.5), -amax, amax);

				v += a * 0.5;
				r += v * 0.5;
				s++;
				System.out.printf("r=%f v=%f a=%f\n", r, v, a);
			}
			if (s == ServoBreeder.maxSteps) s *= 5;
			s /= ServoBreeder.maxSteps;
			System.out.println(s);
			steps += s * s;
		}
		System.out.println(Math.sqrt(steps / ServoBreeder.tests));
	}

	static double iv = 0, ir = 0;

	static double servo(final double r, final double v, final double amax, final double dt) {
		iv += v;
		ir += r;
		return v * v >= 2 * amax * Math.abs(r) || v * r > 0 ? -Math.signum(v) * amax : Math.signum(v) * amax;
	}
}