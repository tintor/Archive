package tintor.util;

import java.util.ArrayList;
import java.util.Iterator;

public class UnorderedArrayList<E> extends ArrayList<E> {
	@Override public E remove(final int index) {
		final E oldValue = get(index);
		final int last = size() - 1;
		if (index < last) set(index, get(last));
		super.remove(last);
		return oldValue;
	}

	@Override public boolean remove(final Object obj) {
		if (obj == null) {
			for (final Iterator<E> it = iterator(); it.hasNext();)
				if (it.next() == null) {
					it.remove();
					return true;
				}
		} else
			for (final Iterator<E> it = iterator(); it.hasNext();) {
				final E e = it.next();
				if (e != null && e.equals(obj)) {
					it.remove();
					return true;
				}
			}
		return false;
	}
}