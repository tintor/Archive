package tintor.frpr.util.gzip;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GZIPFilter implements Filter {
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException,
			ServletException {
		if (!(req instanceof HttpServletRequest)) return;
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;

		final String ae = request.getHeader("accept-encoding");
		if (ae != null && ae.indexOf("gzip") != -1) {
			System.out.println("GZIP supported, compressing.");
			final GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(response);
			chain.doFilter(req, wrappedResponse);
			wrappedResponse.finishResponse();
		} else
			chain.doFilter(req, res);
	}

	public void init(final FilterConfig filterConfig) {}

	public void destroy() {}
}