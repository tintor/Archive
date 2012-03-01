package tintor.util;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {
	private final Date date = new Date();
	private final StringBuffer sb = new StringBuffer();

	@Override public synchronized String format(final LogRecord record) {
		sb.setLength(0);

		date.setTime(record.getMillis());
		sb.append(String.format("%1$tH:%1$tM:%1$tS.%1$tL", date)).append(' ');
		sb.append(record.getLevel().getLocalizedName()).append(' ');
		sb.append(record.getSourceClassName() != null ? record.getSourceClassName() : record.getLoggerName());
		if (record.getSourceMethodName() != null) sb.append('.').append(record.getSourceMethodName());
		sb.append(": ").append(record.getMessage()).append('\n');

		if (record.getThrown() != null) try {
			final StringWriter sw = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(sw));
			sb.append(sw);
		} catch (final Exception ex) {}

		return sb.toString();
	}
}