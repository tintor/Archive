package vclip;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

class CreateExample {
	public static void main(String[] args) {
		double[] vertexCoords = { 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 1.0 };

		int[][] faceIndices = { { 0, 3, 2, 1 }, { 4, 0, 1 }, { 4, 1, 2 }, { 4, 2, 3 }, { 4, 3, 0 } };

		ConvexPolyhedron poly = new ConvexPolyhedron(vertexCoords, faceIndices);

		PolyTree pyramid = new PolyTree("pyramid", poly);
		PolyTree hourglass = new PolyTree("hourglass");

		Matrix4d X = new Matrix4d();
		X.setIdentity();
		X.setTranslation(new Vector3d(0, 0, -1));
		hourglass.addComponent("bottom", pyramid, X);

		X.setTranslation(new Vector3d(0, 0, 1));
		X.setRotation(new AxisAngle4d(1, 0, 0, Math.PI));
		hourglass.addComponent("top", pyramid, X);

		hourglass.buildBoundingHull(PolyTree.OBB_HULL);
	}
}
