package tintor.heap.scrap;

import java.util.ArrayDeque;

public final class AuxiliaryLinkedTwoPassPairingHeap<T extends Comparable<? super T>> extends HeavyLinkedPairingHeap<T> {
	@Override public Node<T> insert(final T element) {
		final Node<T> node = new Node<T>(element);
		auxList.offerLast(node);
		if (minTree == null || node.element.compareTo(minTree.element) < 0) minTree = node;
		return node;
	}

	@Override public void clear() {
		super.clear();
		minTree = null;
		auxList.clear();
	}

	// Implementation
	private Node<T> minTree;
	private final ArrayDeque<Node<T>> auxList = new ArrayDeque<Node<T>>();

	private Node<T> auxiliaryMultipass() {
		while (auxList.size() > 1)
			auxList.offerLast(link(auxList.pollFirst(), auxList.pollFirst()));
		return auxList.pollFirst();
	}
}