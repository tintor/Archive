package tintor.frpr.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tintor.frpr.util.Database;

/** @author Marko Tintor (tintor@gmail.com) */
public class Hash extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	@Override protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final PrintWriter out = response.getWriter();
		try {
			Database.acquireConnection();
			Database.connection().setAutoCommit(false);

			final Statement s = Database.connection().createStatement();
			final String p = Database.table_prefix;

			s.execute("ALTER TABLE " + p + "User ADD COLUMN pass_hash CHAR(32) NOT NULL DEFAULT '' AFTER password");
			final PreparedStatement z = Database.createQuery("UPDATE " + p
					+ "User SET pass_hash = MD5(CONCAT(LOWER(id), ?, password))", "eat my shorts!");
			z.executeUpdate();

			Database.connection().commit();
			out.println(new Date() + " izmena uspesna!");
		} catch (final Exception e) {
			throw new ServletException(e);
		} finally {
			Database.releaseConnection();
		}
	}
}