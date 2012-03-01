package gui.util;

import org.eclipse.swt.custom.ViewForm;

/**
 * @author Marko Tintor
 * @date 04/2006
 */
public class ViewAction extends Action {
	public ViewForm view;

	public ViewAction(String acc) {
		super(acc, 1);
	}

	protected void run() {
		assert view != null;
		GUI.showView(view, !view.isVisible());
	}

	protected void onShow() {
		assert menuItem != null;
		if(view != null) menuItem.setSelection(view.isVisible());
	}
}