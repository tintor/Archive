package tintor.frpr.util;

public class Redirect extends RuntimeException {
	public final String url;

	public Redirect(final String url) {
		this.url = url;
	}
}