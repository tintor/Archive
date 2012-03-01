package tintor.commander;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class Util {
	public static void setLayoutData(final Control control, final int width, final int height,
			final Object left, final Object right, final Object top, final Object bottom) {
		final FormData data = new FormData();
		data.width = width;
		data.height = height;
		data.left = formAttachment(left);
		data.right = formAttachment(right);
		data.top = formAttachment(top);
		data.bottom = formAttachment(bottom);
		control.setLayoutData(data);
	}

	private static FormAttachment formAttachment(final Object a) {
		if (a instanceof Integer) return new FormAttachment((Integer) a);
		if (a instanceof Control) return new FormAttachment((Control) a);
		return (FormAttachment) a;
	}

	public static ToolItem newItem(final ToolBar toolbar, final int style, final Object... args) {
		final ToolItem item = new ToolItem(toolbar, style);
		for (final Object arg : args)
			if (arg instanceof String)
				item.setText((String) arg);
			else if (arg instanceof Image)
				item.setImage((Image) arg);
			else if (arg instanceof SelectionListener) item.addSelectionListener((SelectionListener) arg);
		return item;
	}

	// -----
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	public static String kilobytes(final long bytes) {
		return numberFormat.format((bytes + 500L) / 1000L) + " k";
	}

	public static String megabytes(final long bytes) {
		return numberFormat.format((bytes + 500000L) / 1000000L) + " M";
	}

	public static String gigabytes(final long bytes) {
		return numberFormat.format((bytes + 500000000L) / 1000000000L) + " G";
	}

	// -----
	public static File getRoot(File a) {
		File b = a.getParentFile();
		while (b != null) {
			a = b;
			b = b.getParentFile();
		}
		return a;
	}

	// -----
	public static int accelerator(final String acc) {
		final char sep = '+';
		if (acc.indexOf(sep) > 0) {
			if (acc.startsWith("Ctrl" + sep)) return SWT.CONTROL | accelerator(acc.substring(5));
			if (acc.startsWith("Shift" + sep)) return SWT.SHIFT | accelerator(acc.substring(6));
			if (acc.startsWith("Alt" + sep)) return SWT.ALT | accelerator(acc.substring(4));
			throw new RuntimeException("Invalid accelerator: " + acc);
		}

		final String a = acc.intern();
		if (a == "Esc") return SWT.ESC;
		if (a == "PageUp") return SWT.PAGE_UP;
		if (a == "PageDown") return SWT.PAGE_DOWN;
		if (a == "Insert") return SWT.INSERT;
		if (a == "Delete") return SWT.DEL;
		if (a == "Home") return SWT.HOME;
		if (a == "End") return SWT.END;
		if (a == "Num +") return SWT.KEYPAD_ADD;
		if (a == "Num -") return SWT.KEYPAD_SUBTRACT;
		if (a == "Num *") return SWT.KEYPAD_MULTIPLY;
		if (a == "Space") return ' ';
		if (a == "Enter") return '\n';
		if (a == "BackSpace") return SWT.BS;
		if (a.length() > 1 && a.charAt(0) == 'F') try {
			final int f = Integer.parseInt(acc.substring(1));
			if (1 <= f && f <= 12) return SWT.F1 - 1 + f;
		} catch (final NumberFormatException e) {}
		if (a.length() == 1 && 'A' <= a.charAt(0) && a.charAt(0) <= 'Z') return a.charAt(0);

		throw new RuntimeException("Invalid accelerator: " + a);
	}

	// -----
	public static String getExtension(final String fullName) {
		final int i = fullName.lastIndexOf('.');
		return i > 0 ? fullName.substring(i + 1) : "";
	}

	public static String removeExtension(final String fullName) {
		final int i = fullName.lastIndexOf('.');
		return i > 0 ? fullName.substring(0, i) : fullName;
	}

	// -----
	private static boolean isDigit(final char c) {
		return '0' <= c && c <= '9';
	}

	public static int compare(final String a, final String b, final boolean caseSensitive, final boolean natural) {
		int ai = 0, bi = 0;
		while (ai < a.length() && bi < b.length()) {
			char ac = a.charAt(ai), bc = b.charAt(bi);
			if (natural && isDigit(ac) && isDigit(bc)) {
				while (ai < a.length() && a.charAt(ai) == '0')
					ai++;
				while (bi < b.length() && b.charAt(bi) == '0')
					bi++;

				int an = ai + 1, bn = bi + 1;
				while (an < a.length() && isDigit(a.charAt(an)))
					an++;
				while (bn < b.length() && isDigit(b.charAt(bn)))
					bn++;

				final int d = an - ai - (bn - bi);
				if (d != 0) return d;

				while (ai < an) {
					ac = a.charAt(ai++);
					bc = b.charAt(bi++);
					if (ac != bc) return ac - bc;
				}
				continue;
			}
			if (!caseSensitive) {
				ac = Character.toLowerCase(ac);
				bc = Character.toLowerCase(bc);
			}
			if (ac != bc) return ac - bc;

			ai++;
			bi++;
		}
		return a.length() - b.length();
	}

	// -----
	public static Image getFileIcon(final File file) {
		return Icons.get(getIconName(file));
	}

	public static String getIconName(final File file) {
		if (file.isDirectory()) return file.isHidden() ? "folder_error" : "folder";
		if (file.isHidden()) return "page_white_error";

		final String ext = Util.getExtension(file.getName()).toLowerCase().intern();
		if (ext == "java" || ext == "cpp" || ext == "cc" || ext == "c" || ext == "h" || ext == "hpp")
			return "page_white_code";
		if (ext == "jpg" || ext == "jpeg" || ext == "bmp" || ext == "png" || ext == "gif" || ext == "tif")
			return "page_white_paint";
		if (ext == "jar" || ext == "zip" || ext == "7z" || ext == "tar" || ext == "rar" || ext == "gz"
				|| ext == "tgz" || ext == "bz2") return "package";
		if (ext == "pdf") return "page_white_acrobat";
		if (ext == "txt" || ext == "ini") return "page_white_text";
		if (ext == "exe") return "application";
		if (ext == "bat") return "application_xp_terminal";
		if (ext == "torrent") return "link";
		if (ext == "dll" || ext == "lib" || ext == "class") return "cog";
		if (ext == "css") return "css";
		if (ext == "chm") return "help";
		if (ext == "html" || ext == "htm") return "layout";
		if (ext == "avi" || ext == "flv" || ext == "mpg" || ext == "mpeg") return "film";
		if (ext == "mp3" || ext == "ogg" || ext == "wav") return "music";
		return "page_white";
	}

	public static String getMimeType(final File file) {
		try {
			final String mime = URLConnection.guessContentTypeFromStream(new FileInputStream(file));
			if (mime != null) return mime;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return URLConnection.guessContentTypeFromName(file.getName());
	}
}