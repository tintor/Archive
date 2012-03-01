package tintor.swt;

public class XMenu {
	public final String text;
	public final Object[] items;

	public XMenu(String text, Object... items) {
		this.text = text;
		this.items = items;
	}
}