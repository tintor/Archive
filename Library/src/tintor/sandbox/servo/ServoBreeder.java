package tintor.sandbox.servo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import tintor.geometry.GMath;
import tintor.sandbox.Breeder;
import tintor.sandbox.NeuralNetwork;


class PID {
	double p; // proportional
	double i; // intergral
	double d; // derivative

	private double prev;
	private final double past, present;

	PID(double past, double present) {
		this.past = past;
		this.present = present;
	}

	void next(double a) {
		p = a;
		i = i * past + a * present;
		d = a - prev;
		prev = a;
	}
}

// gen n: linearna umesto sigmoid funkcije
// gen o: linearna f-ja, bez ofseta

public class ServoBreeder extends Breeder<NeuralNetwork> {
	static int[] layers = new int[] { 7, 1 };
	static int poolsize = 100;
	static double survival = 0.5, mutation = 1, mutationFalloff = 0.995;
	static int tests = 200, maxSteps = 1000;

	static double goal_r = 1e-6, goal_v = 1e-6;
	static double sigma_r = 10, sigma_v = 5, sigma_amax = 2.5, min_a = 1;

	static String generation = "y";

	public static void main(String[] args) throws IOException {
		{
			FileWriter f = new FileWriter("gen " + generation + ".java");
			BufferedReader r = new BufferedReader(new FileReader("experimental\\servo\\ServoBreeder.java"));
			String line;
			while ((line = r.readLine()) != null)
				f.write(line + "\n");
			r.close();
			f.close();
		}

		ServoBreeder breeder = new ServoBreeder();
		for (int i = 1; mutation > 1e-3; i++) {
			mutation *= mutationFalloff;
			System.out.printf("gen=%d mut=%f :", i, mutation);
			breeder.iterate();

			if (breeder.pool[0].fitness < 1) {
				File file = new File(String.format("%s %.3f.txt", generation, breeder.pool[0].fitness));
				FileWriter f = new FileWriter(file);
				breeder.pool[0].object.print(f);
				f.close();
			}

			for (int j = 0; j < 8; j++)
				System.out.printf(" %.4f", breeder.pool[j].fitness);
			System.out.printf(" worst: %.4f", breeder.pool[breeder.pool.length - 1].fitness);
			System.out.println();
		}
	}

	public ServoBreeder() {
		super(poolsize, survival);
	}

	@Override
	public NeuralNetwork crossover(NeuralNetwork a, NeuralNetwork b, NeuralNetwork old) {
		old.crossover(a, b, mutation);
		return old;
	}

	static Random rand = new Random();

	@Override
	public double fitness(NeuralNetwork n) {
		double steps = 0;

		for (int i = 0; i < tests; i++) {
			double r = sigma_r * rand.nextGaussian();
			double v = sigma_v * rand.nextGaussian();
			double amax = sigma_amax * rand.nextGaussian();
			while(Math.abs(amax) < min_a) amax = sigma_amax * rand.nextGaussian();
			double ae = amax * (rand.nextDouble() - 0.5);

			steps += GMath.square((double) steps(n, r, v, ae, amax) / maxSteps);
		}
		return Math.sqrt(steps / tests);
	}

	int steps(NeuralNetwork n, double r, double v, double ae, double amax) {
		double ir = 0, iv = 0;
		double pr = 0, pv = 0;
		double dr = 0, dv = 0;

		int s = 0;
		while ((Math.abs(r) >= goal_r || Math.abs(v) >= goal_v) && s < maxSteps) {
			ir = ir + r;
			iv = iv + v;
			dr = r - pr;
			pr = r;
			dv = v - pv;
			pv = v;

			n.input[0] = r;
			n.input[1] = v;
			n.input[2] = ir;
			n.input[3] = iv;
			n.input[4] = dr;
			n.input[5] = dv;
			n.input[6] = ae;

			n.propagate();
			double a = ae + GMath.clamp(n.output[0], -amax, amax);

			v += a;
			r += v;
			s++;
		}
		if (s == maxSteps) s *= 5;
		return s;
	}

	@Override
	public int compete(NeuralNetwork a, NeuralNetwork b) {
		int score = 0;
		for (int i = 0; i < tests; i++) {
			double r = sigma_r * rand.nextGaussian();
			double v = sigma_v * rand.nextGaussian();
			double amax = sigma_amax * rand.nextGaussian();
			while(Math.abs(amax) < min_a) amax = sigma_amax * rand.nextGaussian();
			double ae = amax * (rand.nextDouble() - 0.5);

			int as = steps(a, r, v, ae, amax);
			int bs = steps(b, r, v, ae, amax);

			if (as > bs) score--;
			if (bs > as) score++;
		}
		return score;
	}

	@Override
	public NeuralNetwork spawn() {
		NeuralNetwork c = new NeuralNetwork(layers);
		c.randomize(1);
		return c;
	}
}