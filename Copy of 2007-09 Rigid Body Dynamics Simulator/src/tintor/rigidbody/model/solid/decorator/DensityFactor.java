package tintor.rigidbody.model.solid.decorator;

import tintor.geometry.Matrix3;
import tintor.rigidbody.model.solid.Decorator;
import tintor.rigidbody.model.solid.Solid;

public final class DensityFactor extends Decorator {
	public final double density;

	public DensityFactor(final Solid shape, final double density) {
		super(shape);
		this.density = density;
	}

	@Override public double mass() {
		return solid.mass() * density;
	}

	@Override public Matrix3 inertiaTensor() {
		return solid.inertiaTensor().mul(density);
	}

	@Override public DensityFactor density(final double densityM) {
		return new DensityFactor(solid, density * densityM);
	}
}