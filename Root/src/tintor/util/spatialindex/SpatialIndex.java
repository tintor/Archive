package tintor.util.spatialindex;

import java.util.Arrays;
import java.util.Iterator;

@SuppressWarnings("unchecked") public class SpatialIndex<E extends SpatialIndex.Vector2> {
	public static interface Vector2 {
		float x();

		float y();
	}

	// O(1)
	public void add(final E a) {
		if (size == axisX.length) {
			axisX = Arrays.copyOf(axisX, axisX.length * 2);
			axisY = Arrays.copyOf(axisY, axisY.length * 2);
		}
		axisX[size] = a;
		axisY[size] = a;
		size += 1;
	}

	// O(n)
	public void remove(final E a) {
		for (int i = 0; i < size; i++)
			if (axisX[i] == a) {
				axisX[i] = axisX[size - 1];
				break;
			}
		for (int i = 0; i < size; i++)
			if (axisY[i] == a) {
				axisY[i] = axisY[size - 1];
				break;
			}

		axisX[size - 1] = null;
		axisY[size - 1] = null;
		size -= 1;
	}

	// O(n)
	public void update() {
		for (int j, i = 1; i < size; i++) {
			final Vector2 a = axisX[i];
			for (j = i; j > 0 && axisX[j - 1].x() > a.x();)
				axisX[j] = axisX[--j];
			if (j < i) axisX[j] = a;
		}

		for (int j, i = 1; i < size; i++) {
			final Vector2 a = axisY[i];
			for (j = i; j > 0 && axisY[j - 1].y() > a.y();)
				axisY[j] = axisY[--j];
			if (j < i) axisY[j] = a;
		}
	}

	private int findFirstX(final float x) {
		int s = 0, e = size - 1;
		while (s <= e) {
			final int m = (s + e) / 2;
			if (x <= axisX[m].x())
				e = m - 1;
			else
				s = m + 1;
		}
		return e + 1;
	}

	private int findLastX(int s, final float x) {
		if (s > size) s = size;
		int e = size - 1;
		while (s <= e) {
			final int m = (s + e) / 2;
			if (x < axisX[m].x())
				e = m - 1;
			else
				s = m + 1;
		}
		return s - 1;
	}

	private int findFirstY(final float y) {
		int s = 0, e = size - 1;
		while (s <= e) {
			final int m = (s + e) / 2;
			if (y <= axisY[m].y())
				e = m - 1;
			else
				s = m + 1;
		}
		return e + 1;
	}

	private int findLastY(int s, final float y) {
		if (s > size) s = size;
		int e = size - 1;
		while (s <= e) {
			final int m = (s + e) / 2;
			if (y < axisY[m].y())
				e = m - 1;
			else
				s = m + 1;
		}
		return s - 1;
	}

	/** @return (index of first element that is >= x) or (a.length if no such element exists) */
	static int findFirst(final float[] a, final float x) {
		int s = 0, e = a.length - 1;
		while (s <= e) {
			final int m = (s + e) / 2;
			if (x <= a[m])
				e = m - 1;
			else
				s = m + 1;
		}
		return e + 1;
	}

	/** @return (index of last element that is <= x) or (-1 if no such element exists) */
	static int findLast(final float[] a, final float x) {
		int s = 0, e = a.length - 1;
		while (s <= e) {
			final int m = (s + e) / 2;
			if (x < a[m])
				e = m - 1;
			else
				s = m + 1;
		}
		return s - 1;
	}

	//	public void circleQuery(final List<E> results, final float x, final float y, final float radius) {
	//		final int firstX = findFirstX(x - radius);
	//		final int lastX = findLastX(firstX + 1, x + radius);
	//
	//		final int firstY = findFirstY(y - radius);
	//		final int lastY = findLastY(firstY + 1, y + radius);
	//
	//		final float radiusSqr = radius * radius;
	//
	//		if (lastX - firstX < lastY - firstY) {
	//			int index = firstX;
	//			int last = lastX;
	//			E[] axis = axisX;
	//			
	//			while(index <= last && ) {
	//				
	//			}
	//			new QueryIterator<E>(firstX, lastX, axisX) {
	//				@Override protected boolean check() {
	//					final float dy = axis[index].y() - y;
	//					if (dy > radius || dy < -radius) return false;
	//
	//					final float dx = axis[index].x() - x;
	//					return dx * dx + dy * dy <= radiusSqr;
	//				}
	//			}; }
	//		else
	//			new QueryIterator<E>(firstY, lastY, axisY) {
	//				@Override protected boolean check() {
	//					final float dx = axis[index].x() - x;
	//					if (dx > radius || dx < -radius) return false;
	//
	//					final float dy = axis[index].y() - y;
	//					return dx * dx + dy * dy <= radiusSqr;
	//				}
	//			};
	//	}

	public Iterator<E> circleIterator(final float x, final float y, final float radius) {
		final int firstX = findFirstX(x - radius);
		final int lastX = findLastX(firstX + 1, x + radius);

		final int firstY = findFirstY(y - radius);
		final int lastY = findLastY(firstY + 1, y + radius);

		final float radiusSqr = radius * radius;

		if (lastX - firstX < lastY - firstY) return new QueryIterator<E>(firstX, lastX, axisX) {
			@Override protected boolean check() {
				final float dy = axis[index].y() - y;
				if (dy > radius || dy < -radius) return false;

				final float dx = axis[index].x() - x;
				return dx * dx + dy * dy <= radiusSqr;
			}
		};

		return new QueryIterator<E>(firstY, lastY, axisY) {
			@Override protected boolean check() {
				final float dx = axis[index].x() - x;
				if (dx > radius || dx < -radius) return false;

				final float dy = axis[index].y() - y;
				return dx * dx + dy * dy <= radiusSqr;
			}
		};
	}

	public Iterator<E> rectIterator(final float xmin, final float ymin, final float xmax, final float ymax) {
		final int firstX = findFirstX(xmin);
		final int lastX = findLastX(firstX + 1, xmax);

		final int firstY = findFirstY(ymin);
		final int lastY = findLastY(firstY + 1, ymax);

		if (lastX - firstX < lastY - firstY) return new QueryIterator<E>(firstX, lastX, axisX) {
			@Override protected boolean check() {
				return ymin <= axis[index].y() && axis[index].y() <= ymax;
			}
		};

		return new QueryIterator<E>(firstY, lastY, axisY) {
			@Override protected boolean check() {
				return xmin <= axis[index].x() && axis[index].x() <= xmax;
			}
		};
	}

	public Iterable<E> circle(final float x, final float y, final float radius) {
		final Iterator<E> it = circleIterator(x, y, radius);
		return new Iterable<E>() {
			@Override public Iterator<E> iterator() {
				return it;
			}
		};
	}

	public Iterable<E> rectangle(final float xmin, final float ymin, final float xmax, final float ymax) {
		final Iterator<E> it = rectIterator(xmin, ymin, xmax, ymax);
		return new Iterable<E>() {
			@Override public Iterator<E> iterator() {
				return it;
			}
		};
	}

	private static abstract class QueryIterator<E extends Vector2> implements Iterator<E> {
		int index;
		int last;
		Vector2[] axis;

		QueryIterator(final int first, final int last, final Vector2[] axis) {
			index = first;
			this.last = last;
			this.axis = axis;
			inc();
		}

		protected abstract boolean check();

		private void inc() {
			while (index <= last && !check())
				index += 1;
		}

		@Override public boolean hasNext() {
			return index <= last;
		}

		@Override public E next() {
			final int a = index;
			index += 1;
			inc();
			return (E) axis[a];
		}

		@Override public void remove() {
			throw new RuntimeException();
		}
	}

	private int size;
	private Vector2[] axisX = new Vector2[16], axisY = new Vector2[axisX.length];
}
