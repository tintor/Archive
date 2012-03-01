package tintor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	static String net(String request) throws Exception {
		Socket sock = new Socket("proxy.sbb.co.yu", 8080);
		write(request, sock);
		String responce = read(sock);
		sock.close();
		return responce;
	}

	static Matcher matcher(String str, String regex) {
		return Pattern.compile(regex).matcher(str);
	}

	static void write(Object in, Object out) throws Exception {
		InputStream ins = istream(in);
		OutputStream outs = ostream(out);

		byte[] buffer = new byte[10000];
		int s;
		while ((s = ins.read(buffer)) != -1)
			outs.write(buffer, 0, s);
		
		ins.close();
		outs.flush();
	}

	static InputStream istream(Object in) throws Exception {
		if (in instanceof URL) return ((URL) in).openStream();
		if (in instanceof String) return new ByteArrayInputStream(((String) in).getBytes());
		if (in instanceof File) return new FileInputStream((File) in);
		if (in instanceof Socket) return ((Socket) in).getInputStream();
		return (InputStream) in;
	}

	static OutputStream ostream(Object out) throws Exception {
		if (out instanceof File) return new FileOutputStream((File) out);
		if (out instanceof Socket) return ((Socket) out).getOutputStream();
		return (OutputStream) out;
	}

	static String read(Object a, Object b) throws Exception {
		write(a, b);
		return read(b);
	}

	static String read(Object in) throws Exception {
		return read(new InputStreamReader(istream(in)));
	}

	static String read(Reader in) throws Exception {
		StringBuffer result = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(in);
		char[] buf = new char[10000];
		int c = 0;
		while ((c = reader.read(buf)) != -1)
			result.append(buf, 0, c);
		reader.close();
		return result.toString();
	}
}