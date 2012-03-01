package tintor.sandbox;

import java.util.Arrays;
import java.util.Random;

public class Test {
	public static void main(String[] args) {
		Random r = new Random();

		double[] a = new double[10];
		for (int line = 0; line < 10; line++) {
			for (int i = 0; i < a.length; i++) {
				a[i] = r.nextDouble();
				for (int j = 0; j < line; j++)
					a[i] = Math.cos((1 - a[i]) * Math.PI) / 2 + 0.5;
			}
			Arrays.sort(a);
			for (int i = 0; i < a.length; i++)
				System.out.printf("%f ", a[i]);
			System.out.println();
		}
		System.out.println();

	}
}