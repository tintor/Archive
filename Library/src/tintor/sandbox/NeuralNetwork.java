package tintor.sandbox;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Random;

public class NeuralNetwork {
	public final double[] input, output;
	final double[][] value;
	public final double[][][] weight;
	public final double[][] offset;

	private static Random rand = new Random();

	static {
		Locale.setDefault(Locale.US);
	}

	public NeuralNetwork(int... layer) {
		if (layer.length < 2) throw new RuntimeException();

		value = new double[layer.length][];
		for (int i = 0; i < layer.length; i++)
			value[i] = new double[layer[i]];

		weight = new double[layer.length - 1][][];
		offset = new double[layer.length - 1][];
		for (int l = 0; l < weight.length; l++) {
			weight[l] = new double[layer[l + 1]][];
			offset[l] = new double[layer[l + 1]];
			for (int n = 0; n < weight[l].length; n++)
				weight[l][n] = new double[layer[l]];
		}

		input = value[0];
		output = value[value.length - 1];
	}

	public void crossover(NeuralNetwork netA, NeuralNetwork netB, double mutation) {
		for (int l = 0; l < weight.length; l++)
			for (int n = 0; n < weight[l].length; n++) {
				offset[l][n] = 0;//combine(netA.offset[l][n], netB.offset[l][n]) + mutation
				//* rand.nextGaussian();
				for (int w = 0; w < weight[l][n].length; w++)
					weight[l][n][w] = combine(netA.weight[l][n][w], netB.weight[l][n][w]) + mutation
							* rand.nextGaussian();
			}
	}

	static double combine(double a, double b) {
		return a + (b - a) * cosrand();
	}

	static double cosrand() {
		return Math.cos((1 - rand.nextDouble()) * Math.PI) / 2 + 0.5;
	}

	public void randomize(double factor) {
		for (int l = 0; l < weight.length; l++)
			for (int n = 0; n < weight[l].length; n++) {
				offset[l][n] = 0; //factor * rand.nextGaussian();
				for (int w = 0; w < weight[l][n].length; w++)
					weight[l][n][w] = factor * rand.nextGaussian();
			}
	}

	public void propagate() {
		for (int l = 0; l < weight.length; l++)
			for (int n = 0; n < weight[l].length; n++) {
				double v = offset[l][n];
				for (int w = 0; w < weight[l][n].length; w++)
					v += weight[l][n][w] * func(value[l][w]);
				value[l + 1][n] = v;
			}
	}

	public void print(Writer writer) {
		PrintWriter out = new PrintWriter(writer);

		out.printf("NeuralNetwork n = new NeuralNetwork(%d", value[0].length);
		for (int i = 1; i < value.length; i++)
			out.printf(", %d", value[i].length);
		out.printf(");\n");

		for (int l = 0; l < weight.length; l++) {
			out.println();
			for (int n = 0; n < weight[l].length; n++) {
				out.printf("n.offset[%d][%d] = %f;\n", l, n, offset[l][n]);
				out.printf("n.weight[%d][%d] = new double[] { %f", l, n, weight[l][n][0]);
				for (int w = 1; w < weight[l][n].length; w++)
					out.printf(", %f", weight[l][n][w]);
				out.printf(" };\n");
			}
		}
	}

	static double func(double x) {
		//return 2 / (1 + Math.exp(-x)) - 1;
		return x;
	}
}