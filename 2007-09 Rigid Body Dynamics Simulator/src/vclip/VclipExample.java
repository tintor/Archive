package vclip;

import java.util.HashMap;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

class VclipExample {
	public static void main(final String[] args) {
		try {
			final HashMap library = new HashMap();
			PolyTree.scanLibrary("PolyTreeExamples.txt", library, true);

			final PolyTree ptree1 = (PolyTree) library.get("unit-cube");
			final PolyTree ptree2 = (PolyTree) library.get("cone");

			final DistanceReport drep = new DistanceReport();
			final ClosestFeaturesHT ht = new ClosestFeaturesHT();

			final Matrix4d X21 = new Matrix4d();
			for (double x = 10; x >= 0; x -= 1) {
				X21.set(new Vector3d(x, 0, 0));
				final double dist = ptree1.vclip(drep, ptree2, X21, 0, ht);
				if (dist > 0)
					System.out.println(dist);
				else
					System.out.println("colliding");
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}