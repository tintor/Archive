package tintor.stream;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ByteStreamTest {
	@Test public void main() throws IOException {
		final ByteStream bs = new ByteStream();
		assertEquals(-1, bs.read());

		bs.write((byte) 24);
		assertEquals(24, bs.read());
		assertEquals(-1, bs.read());

		Assert.fail("incomplete");
	}
}