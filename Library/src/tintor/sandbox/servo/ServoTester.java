package tintor.sandbox.servo;

import java.util.Random;

import tintor.geometry.GMath;
import tintor.sandbox.NeuralNetwork;


public class ServoTester {
	public static void main(String[] args) {
		NeuralNetwork n = new NeuralNetwork(6, 6, 6, 1);

		n.offset[0][0] = -1.323666;
		n.weight[0][0] = new double[] { -0.270086, 1.545778, -0.465576, -0.070209, 0.005303, 0.890758 };
		n.offset[0][1] = 0.184145;
		n.weight[0][1] = new double[] { 2.404629, 0.164637, 2.019692, 0.408711, -0.738273, -0.377931 };
		n.offset[0][2] = 1.586409;
		n.weight[0][2] = new double[] { 1.406522, -1.406012, 0.667525, -0.474093, -0.230201, 0.966516 };
		n.offset[0][3] = 0.560848;
		n.weight[0][3] = new double[] { 1.165444, 1.079319, 0.379413, -0.427449, -0.110211, -1.503433 };
		n.offset[0][4] = -0.749180;
		n.weight[0][4] = new double[] { 0.202784, 1.735164, 0.781609, 1.099946, 0.771943, 0.428170 };
		n.offset[0][5] = -0.811635;
		n.weight[0][5] = new double[] { -0.833549, -0.168816, -0.533189, 0.118358, -1.238089, 0.119075 };

		n.offset[1][0] = 0.335148;
		n.weight[1][0] = new double[] { 1.446617, 0.153354, 0.666179, 0.704150, -0.865391, 0.334971 };
		n.offset[1][1] = -0.109913;
		n.weight[1][1] = new double[] { 0.068599, 0.998743, -0.035962, 0.783565, 1.098323, 0.786360 };
		n.offset[1][2] = 0.854865;
		n.weight[1][2] = new double[] { 0.425021, 0.590473, -0.890699, -0.126340, -1.949470, 1.319935 };
		n.offset[1][3] = -0.359459;
		n.weight[1][3] = new double[] { -0.101420, -0.261173, -0.042448, -0.845213, 0.100493, -1.367421 };
		n.offset[1][4] = -0.608334;
		n.weight[1][4] = new double[] { 0.948009, 1.958319, -0.172504, -0.115284, -0.771017, -0.925985 };
		n.offset[1][5] = 0.378705;
		n.weight[1][5] = new double[] { 0.359653, -0.657926, 0.556158, 1.037230, 1.601540, 0.317020 };

		n.offset[2][0] = 0.260278;
		n.weight[2][0] = new double[] { -1.931716, -0.658925, 1.146196, -0.743612, -0.394445, -0.328276 };

		double ir = 0, iv = 0;
		double pr = 0, pv = 0;
		double dr = 0, dv = 0;

		Random rand = new Random();
		double r = rand.nextDouble() * 10 - 5;
		while(Math.abs(r) < 0.5) r = rand.nextDouble() * 10 - 5;
		double v = rand.nextDouble() - 0.5;
		double amax = rand.nextDouble() * 0.2 + 0.2;

		System.out.printf("r=%f v=%f ", r, v);

		int s = 0, maxSteps = 600;
		while ((Math.abs(r) >= 0.001 || Math.abs(v) >= 0.001) && s < maxSteps) {
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
			n.propagate();
			double a = GMath.clamp(n.output[0], -amax, amax);

			v += a;
			r += v;
			s++;
			//System.out.printf("r=%f v=%f a=%f out=%f\n", r, v, a, n.output[0]);
		}
		if(s == maxSteps) s *= 5;

		System.out.printf("%.3f\n",(double)s/maxSteps);
	}
}
