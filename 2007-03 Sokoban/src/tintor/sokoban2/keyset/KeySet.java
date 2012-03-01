package tintor.sokoban2.keyset;

import tintor.sokoban2.Key;

public interface KeySet {
	boolean add(final Key a);

	int size();

	int arraysize();
}