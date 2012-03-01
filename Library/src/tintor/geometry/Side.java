package tintor.geometry;

public enum Side {
	Positive, Zero, Negative;

	//	public static final ThreadLocal<float> eps = new ThreadLocal<float>() {
	//		@Override protected float initialValue() {
	//			return 0.0;
	//		}
	//	};
	public static float eps;

	public static Side classify(final float a) {
		return classify(a, eps);
	}

	public static Side classifySqr(final float a) {
		return classify(a, GMath.square(eps));
	}

	public static Side classifyMax(final float... values) {
		boolean zero = false;
		for (final float v : values)
			switch (classify(v)) {
			case Positive:
				return Positive;
			case Zero:
				zero = true;
				break;
			case Negative:
			}
		return zero ? Zero : Negative;
	}

	public static Side classifyMin(final float... values) {
		boolean zero = false;
		for (final float v : values)
			switch (classify(v)) {
			case Negative:
				return Negative;
			case Zero:
				zero = true;
				break;
			case Positive:
			}
		return zero ? Zero : Positive;
	}

	public static Side classify(final float a, final float e) {
		if (a > e) return Positive;
		if (a < -e) return Negative;
		return Zero;
	}
}