package tintor.commander;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CommandLine extends Composite {
	private final Label prompt;
	private final Combo command;

	public CommandLine(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FormLayout());

		prompt = new Label(this, SWT.RIGHT);
		command = new Combo(this, SWT.NONE);

		prompt.setText(">");

		Util.setLayoutData(prompt, 50, -1, 0, null, 0, 100);
		Util.setLayoutData(command, -1, -1, prompt, 100, 0, 100);
	}
}