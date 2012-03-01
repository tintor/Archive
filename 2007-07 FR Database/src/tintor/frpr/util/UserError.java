package tintor.frpr.util;

public class UserError extends RuntimeException {
	public UserError(final String msg) {
		super(msg);
	}
}