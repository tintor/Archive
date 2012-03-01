package tintor.heap.scrap;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class PairingHeapTest {
	@Test
	public void general() {
		final HeavyLinkedPairingHeap<Integer> h = new HeavyLinkedPairingHeap<Integer>();
		int items = 10000, i = 37, j;

		for (i = 37; i != 0; i = (i + 37) % items)
			h.offer(i);
		for (i = 1; i < items; i++)
			Assert.assertEquals(i, h.poll());

		ArrayList<HeavyLinkedPairingHeap.Node<Integer>> p = new ArrayList<HeavyLinkedPairingHeap.Node<Integer>>();
		for (i = 0; i < items; i++)
			p.add(null);

		for (i = 0, j = items / 2; i < items; i++, j = (j + 71) % items)
			p.set(j, h.insert(j + items));
		for (i = 0, j = items / 2; i < items; i++, j = (j + 53) % items)
			h.decreaseKey(p.get(j), p.get(j).getValue() - items);
		i = -1;
		while (!h.isEmpty())
			Assert.assertEquals(++i, h.poll());
	}
}