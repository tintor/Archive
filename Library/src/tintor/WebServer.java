package tintor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {
	Thread server = null;
	int port;

	private static final Pattern pairPattern = Pattern.compile("\\s*([^:]+)\\s*:\\s*(.+)\\s*");

	private final Runnable runnable = new Runnable() {
		@Override public void run() {
			try {
				final ServerSocket server1 = new ServerSocket(port);
				while (true) {
					final Socket socket = server1.accept();
					try {
						final Reader reader = new InputStreamReader(socket.getInputStream());
						final Writer writer = new OutputStreamWriter(socket.getOutputStream());
						answer(new BufferedReader(reader), new BufferedWriter(writer));
					} finally {
						socket.close();
					}
				}
			} catch (final IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			server = null;
		}
	};

	void answer(final BufferedReader in, final BufferedWriter out) throws IOException {
		final String intro = in.readLine();
		if (intro == null) return;

		final StringTokenizer tok = new StringTokenizer(intro);
		final String type = tok.nextToken().intern();
		final String resource = tok.nextToken().intern();
		@SuppressWarnings("unused") final String protocol = tok.nextToken().intern();

		if (type != "GET") {
			out.write("400\n\n");
			out.flush();
			return;
		}

		final Map<String, String> header = new HashMap<String, String>();
		while (true) {
			String line = in.readLine();
			if (line == null) break;
			line = line.trim();
			if (line.equals("")) break;

			final Matcher m = pairPattern.matcher(line);
			if (!m.matches()) {
				out.write("400\n\n");
				out.flush();
				return;
			}
			header.put(m.group(1).toLowerCase(), m.group(2));
		}

		handle(resource, header, out);
		out.flush();
	}

	public void start(final int port1, final boolean daemon) {
		if (server != null) throw new IllegalStateException("Server is running!");
		server = new Thread(runnable);
		port = port1;
		server.setName("WebServer");
		server.setDaemon(daemon);
		server.start();
	}

	public void stop() {
		final Thread s = server;
		if (s != null) {
			s.interrupt();
			try {
				s.join(1000);
			} catch (final InterruptedException e) {}
		}
	}

	protected void handle(@SuppressWarnings("unused") final String command,
			@SuppressWarnings("unused") final Map<String, String> header, final Writer out) throws IOException {
		out.write("HTTP/1.1 404\n\n");
	}

	public static void main(final String[] args) {
		new WebServer().start(80, false);
	}
}