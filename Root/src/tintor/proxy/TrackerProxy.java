package tintor.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrackerProxy extends Thread {
	public static void main(final String args[]) throws Exception {
		final int port = Integer.parseInt(args[0]);
		final ServerSocket server = new ServerSocket(port);
		System.out.println("TrackerProxy started on port " + port);
		while (true)
			new ProxyThread(server.accept()).start();
	}
}

class ProxyThread extends Thread {
	private final byte[] buffer = new byte[128 * 1024];
	private final Socket socket;
	private Socket server;
	private final int debugLevel = 1;
	public static final int DEFAULT_TIMEOUT = 20 * 1000;

	public ProxyThread(final Socket s) throws SocketException {
		socket = s;
		socket.setSoTimeout(DEFAULT_TIMEOUT);
	}

	@Override public void run() {
		try {
			final BufferedInputStream clientIn = new BufferedInputStream(socket.getInputStream());
			final BufferedOutputStream clientOut = new BufferedOutputStream(socket.getOutputStream());

			// other variables
			byte[] request = null, response = null;
			int requestLength = 0, responseLength = 0;
			int pos = -1;
			final StringBuilder host = new StringBuilder();
			String hostName = "";
			int hostPort = 80;

			final ByteArrayOutputStream bs = new ByteArrayOutputStream();
			streamHTTPData(true, clientIn, bs, host, false);
			request = bs.toByteArray();
			requestLength = request.length;

			// separate the host name from the host port, if necessary
			// (like if it's "servername:8000")
			hostName = host.toString();
			pos = hostName.indexOf(":");
			if (pos > 0) {
				hostPort = Integer.parseInt(hostName.substring(pos + 1));
				hostName = hostName.substring(0, pos);
			}

			try {
				server = new Socket(hostName, hostPort);
			} catch (final Exception e) {
				// tell the client there was an error
				final String errMsg = "HTTP/1.0 500\nContent Type: text/plain\n\n"
						+ "Error connecting to the server:\n" + e + "\n";
				clientOut.write(errMsg.getBytes(), 0, errMsg.length());
			}

			if (server != null) {
				server.setSoTimeout(DEFAULT_TIMEOUT);
				final BufferedInputStream serverIn = new BufferedInputStream(server.getInputStream());
				final BufferedOutputStream serverOut = new BufferedOutputStream(server.getOutputStream());

				serverOut.write(request, 0, requestLength);
				serverOut.flush();

				if (debugLevel > 1) {
					final ByteArrayOutputStream bs2 = new ByteArrayOutputStream();
					streamHTTPData(false, serverIn, bs2, null, true);
					response = bs2.toByteArray();
					responseLength = response.length;
					clientOut.write(response, 0, responseLength);
					clientOut.flush();
				} else
					responseLength = streamHTTPData(false, serverIn, clientOut, null, true);

				serverIn.close();
				serverOut.close();
			}

			if (debugLevel > 0)
				synchronized (System.out) {
					System.out.println(new Date());
					System.out.println("Request from " + socket.getInetAddress().getHostAddress() + " on Port "
							+ socket.getLocalPort() + " to host " + hostName + ":" + hostPort + "\n ("
							+ requestLength + " bytes sent, " + responseLength + " bytes returned)");
					System.out.flush();
					if (debugLevel > 1) {
						System.out.println("REQUEST:\n" + new String(request));
						System.out.println("RESPONSE:\n" + new String(response));
						System.out.flush();
					}
				}

			clientOut.close();
			clientIn.close();
			socket.close();
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private int streamHTTPData(final boolean isRequest, final InputStream in, final OutputStream out,
			final StringBuilder host, boolean waitForDisconnect) {
		final StringBuilder header = new StringBuilder("");
		String data = "";
		int responseCode = 200;
		int contentLength = 0;
		int pos = -1;
		int byteCount = 0;

		try {
			// get the first line of the header, so we know the response code
			data = readLine(in);
			if (data != null) {
				final String[] parts = data.split("\\s");
				if (parts.length != 3)
					header.append(data);
				else if (isRequest) {
					header.append(parts[0]).append(' ');
					processRequestURL(parts[1], header);
					header.append(' ').append(parts[2]);
				} else {
					try {
						if (parts[0].toLowerCase().startsWith("http"))
							responseCode = Integer.parseInt(parts[1]);
					} catch (final Exception e) {
						if (debugLevel > 0) System.out.println("Error parsing response code " + parts[1]);
					}
					header.append(data);
				}
			}

			// get the rest of the header info
			while ((data = readLine(in)) != null) {
				// the header ends at the first blank line
				if (data.trim().length() == 0) {
					header.append(data);
					break;
				}

				pos = data.indexOf(':');
				if (pos != -1) {
					final String key = data.substring(0, pos).toLowerCase().intern();
					final String value = data.substring(pos + 1);

					// check for the Host header
					if (isRequest && key == "host") {
						host.setLength(0);
						host.append(value.trim());
					}

					// check for the Content-Length header
					if (key == "content-length") contentLength = Integer.parseInt(value.trim());
				}

				header.append(data);
			}

			// convert the header to a byte array, and write it to our stream
			out.write(header.toString().getBytes(), 0, header.length());

			// if the header indicated that this was not a 200 response,
			// just return what we've got if there is no Content-Length,
			// because we may not be getting anything else
			if (responseCode != 200 && contentLength == 0) {
				out.flush();
				return header.length();
			}

			// get the body, if any; we try to use the Content-Length header to
			// determine how much data we're supposed to be getting, because 
			// sometimes the client/server won't disconnect after sending us
			// information...
			if (contentLength > 0) waitForDisconnect = false;

			if (contentLength > 0 || waitForDisconnect) try {
				int bytesIn = 0;
				while ((byteCount < contentLength || waitForDisconnect) && (bytesIn = in.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesIn);
					byteCount += bytesIn;
				}
			} catch (final Exception e) {
				final String errMsg = "Error getting HTTP body: " + e;
				if (debugLevel > 0) {
					System.out.println(errMsg);
					e.printStackTrace(System.out);
				}
			}
		} catch (final Exception e) {
			if (debugLevel > 0) {
				System.out.println("Error getting HTTP data: " + e);
				e.printStackTrace(System.out);
			}
		}

		try {
			out.flush();
		} catch (final Exception e) {}
		return header.length() + byteCount;
	}

	private String readLine(final InputStream in) throws Exception {
		final StringBuilder data = new StringBuilder("");
		while (true) {
			final int c = in.read();
			if (c == -1) break;
			data.append((char) c);
			if (c == 10) break;
		}
		return data.length() > 0 ? data.toString() : null;
	}

	private void processRequestURL(String url, final StringBuilder header) {
		if (!url.toLowerCase().startsWith("http://")) {
			header.append(url);
			return;
		}

		// "http://wwwewew.ewewew.com:22/mmm" => "wwwewew.ewewew.com:22/mm"
		url = url.substring("http://".length());

		int p = url.indexOf('/');
		if (p == -1) {
			header.append('/');
			return;
		}

		final String tracker = url.substring(0, p).toLowerCase();
		// "wwwewew.ewewew.com:22/mm" => "/mm"
		url = url.substring(p);

		// "/anounce?key=value&key=value"
		p = url.indexOf('?');
		if (p == -1) {
			header.append(url);
			return;
		}
		header.append(url.substring(0, p + 1));
		url = url.substring(p + 1);

		// "key=value&key=value"
		processPairs(tracker, url.split("[&]"), header);
	}

	private void processPairs(final String host, final String[] pairs, final StringBuilder header) {
		final Map<String, String> map = new LinkedHashMap<String, String>();
		for (final String pair : pairs) {
			final int p = pair.indexOf("=");
			if (p == -1)
				map.put(pair.toLowerCase(), null);
			else
				map.put(pair.substring(0, p).toLowerCase(), pair.substring(p + 1));
		}

		processMap(host, map);

		boolean start = false;
		for (final String key : map.keySet()) {
			if (start)
				header.append('&');
			else
				start = true;
			header.append(key);

			final String value = map.get(key);
			if (value != null) header.append('=').append(value);
		}
	}

	private void processMap(final String tracker, final Map<String, String> map) {
		System.out.println("tracker: " + tracker);
		// process 1) "event=completed" => "event=stopped"
		if ("completed".equals(map.get("event"))) {
			map.put("event", "stopped");
			System.out.println("  'event=completed' => 'event=stopped'");
		}
		// process 2)
		long uploaded = itoa(map.get("uploaded"), 0);
		final long downloaded = itoa(map.get("downloaded"), 0);

		if (tracker.startsWith("eucbg.homeip.net")) {
			final long newUploaded = downloaded == 0 ? 0 : (long) (downloaded * (1 + Math.random() * 0.05));
			if (uploaded < newUploaded) {
				System.out.print("  'uploaded=" + uploaded + "' => 'uploaded=" + newUploaded + "'");
				uploaded = newUploaded;
				if (map.get("uploaded") != null) map.put("uploaded", String.valueOf(uploaded));
			}
		}

		System.out.println("  uploaded=" + uploaded);
		System.out.println("  downloaded=" + downloaded);
		System.out.println("  left=" + map.get("left"));
		System.out.println("  event=" + map.get("event"));
	}

	private static int itoa(final String a, final int def) {
		try {
			return Integer.parseInt(a);
		} catch (final NumberFormatException e) {
			return def;
		}
	}
}