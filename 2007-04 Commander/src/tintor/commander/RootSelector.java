package tintor.commander;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import tintor.XArrays;

public class RootSelector extends Composite {
	private final Combo combo;
	private final Label label;

	private File[] roots;

	private final Font bold;

	public RootSelector(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new BorderLayout());
		addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(final DisposeEvent e) {
				bold.dispose();
			}
		});

		// Init Combo
		combo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
		combo.setLayoutData(BorderLayout.Left);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(final SelectionEvent e) {
				final int i = combo.getSelectionIndex();
				if (e.doit = onSelect(roots[i])) refreshLabel(roots[i]);
			}
		});

		// Init Label
		label = new Label(this, SWT.NONE);
		label.setLayoutData(BorderLayout.Center);

		final FontData fd = label.getFont().getFontData()[0];
		fd.setStyle(fd.getStyle() | SWT.BOLD);
		bold = new Font(null, fd);
		label.setFont(bold);
	}

	public final void select(final File root) {
		roots = File.listRoots();
		final int i = XArrays.find(roots, root);

		setRedraw(false);
		combo.removeAll();
		for (final File f : roots)
			combo.add(f.getAbsolutePath());

		combo.select(i);
		refreshLabel(roots[i]);
		setRedraw(true);
	}

	private void refreshLabel(final File root) {
		label.setText(String.format(" [VOLUME LABEL] %s of %s free", Util.kilobytes(root.getUsableSpace()),
				Util.kilobytes(root.getTotalSpace())));
	}

	protected boolean onSelect(final File root) {
		return true;
	}
}