package physics;

import java.util.ArrayList;
import java.util.List;

public class Conversion {
	static List<Conversion> conversions = new ArrayList<Conversion>();

	static {
		add(Math.PI, BasicUnit.Angle, 180, BasicUnit.Degree);
	}

	final BasicUnit a, b;
	final double ainb, bina;

	Conversion(double va, BasicUnit a, double vb, BasicUnit b) {
		this.a = a;
		this.b = b;
		ainb = va / vb;
		bina = vb / va;
	}

	static void add(double va, BasicUnit a, double vb, BasicUnit b) {
		// TODO: ensure no conflicts
		conversions.add(new Conversion(va, a, vb, b));
	}
}