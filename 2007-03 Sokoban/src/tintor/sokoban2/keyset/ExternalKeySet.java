package tintor.sokoban2.keyset;

import java.nio.charset.Charset;

import tintor.external.ExternalSet;
import tintor.sokoban2.Key;

public class ExternalKeySet implements KeySet {
	private final ExternalSet _e;
	private final Charset _c = Charset.forName("ascii");

	public ExternalKeySet(final Key level) {
		_e = ExternalSet.create("keyset", level.toBytes().length);
	}

	private int _size = 0;

	public boolean add(final Key a) {
		if (!_e.add(new String(a.toBytes(), _c))) return false;
		_size += 1;
		return true;

	}

	public int size() {
		return _size;
	}

	public int arraysize() {
		return -1;
	}
}