package tintor.frpr.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tintor.frpr.model.Company;
import tintor.frpr.model.User;
import tintor.frpr.model.Company.CompaniesFilter;
import tintor.frpr.util.Database;
import tintor.frpr.util.Node;

/** @author Marko Tintor (tintor@gmail.com) */
public class Export extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	@Override protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		// init request
		final HttpSession session = request.getSession(true);

		// init response
		final PrintWriter out = response.getWriter();
		response.setContentType("text/csv");
		response.setCharacterEncoding("utf-8");
		Main.disableCashing(response);

		// get login
		final User login = (User) session.getAttribute("login");
		if (login == null) {
			response.setContentType("text/html");
			out.print(new Node("html", new Node("head", "<meta http-equiv='REFRESH' content='3;url=main' />"),
					new Node("body", Main.error("Morate se prvo prijaviti!"))));
			return;
		}
		if (!login.root()) {
			response.setContentType("text/html");
			out.print(new Node("html", new Node("head", "<meta http-equiv='REFRESH' content='3;url=main' />"),
					new Node("body", Main.error("Pristup odbijen!"))));
			return;
		}

		// output csv
		final List<String> cells = new ArrayList<String>();

		// table header
		for (final Company.Field field : Company.Field.values())
			cells.add(Main.iso(field.out));
		write_row(out, cells);

		// get data from database
		try {
			Database.acquireConnection();

			final CompaniesFilter filter = new CompaniesFilter();
			filter.order = Company.Field.id;
			filter.ascending = true;
			try {
				for (final Company company : filter.list()) {
					for (final Company.Field field : Company.Field.values())
						cells.add(company.get(field));
					write_row(out, cells);
				}
			} finally {
				filter.finalize();
			}
		} catch (final Exception e) {
			throw new ServletException(e);
		} finally {
			Database.releaseConnection();
		}

	}

	static void write_row(final PrintWriter out, final List<String> cells) {
		boolean first = true;
		for (final String cell : cells) {
			if (first)
				first = false;
			else
				out.print(',');
			out.print('"');
			out.print(cell.replace("\"", "\"\""));
			out.print('"');
		}
		out.println();
		cells.clear();
	}
}