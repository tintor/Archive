package tintor.geometry.extended;

import java.util.ArrayList;

import tintor.geometry.Vector3;

final class VList extends ArrayList<Vector3> {
	public VList add(final float x, final float y, final float z) {
		add(new Vector3(x, y, z));
		return this;
	}

	public VList add3(final float x, final float y, final float z) {
		for (int a = -1; a <= 1; a += 2)
			for (int b = -1; b <= 1; b += 2)
				for (int c = -1; c <= 1; c += 2)
					add(a * x, b * y, c * z);
		return this;
	}

	public VList addc(final float x, final float y, final float z) {
		return add(x, y, z).add(y, z, x).add(z, x, y);
	}

	public VList addc2(final float x, final float y, final float z) {
		return addc(x, y, z).addc(x, y, -z).addc(x, -y, z).addc(x, -y, -z);
	}

	public VList addc3(final float x, final float y, final float z) {
		return addc2(x, y, z).addc2(-x, y, z);
	}

	@Override public Vector3[] toArray() {
		return toArray(new Vector3[size()]);
	}
}