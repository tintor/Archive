package tintor.frpr.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tintor.frpr.util.Database;

/** Servlet za pravljenje tabela za korisnike i kompanije.
 *  
 *  @author Marko Tintor (tintor@gmail.com) */
public class Install extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	@Override protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final PrintWriter out = response.getWriter();
		try {
			Database.acquireConnection();

			final String user = "id VARCHAR(255) NOT NULL, pass_hash CHAR(32) NOT NULL DEFAULT '', "
					+ "manager VARCHAR(255) NOT NULL DEFAULT '', last_access DATETIME NULL, "
					+ "phone VARCHAR(255) NOT NULL DEFAULT '', "
					+ "status ENUM('Admin', 'MainOrganizer', 'Organizer', 'Member') NOT NULL DEFAULT 'Member', "
					+ "comment MEDIUMTEXT NOT NULL, email VARCHAR(255) NOT NULL DEFAULT '', PRIMARY KEY(id)";

			final String company = "id INT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "responsible VARCHAR(255) NOT NULL DEFAULT '', "
					+ "name VARCHAR(255) NOT NULL DEFAULT '', website VARCHAR(255) NOT NULL DEFAULT '', "
					+ "address VARCHAR(255) NOT NULL DEFAULT '', contact_person VARCHAR(255) NOT NULL DEFAULT '', "
					+ "phone VARCHAR(255) NOT NULL DEFAULT '', fax VARCHAR(255) NOT NULL DEFAULT '', "
					+ "email VARCHAR(255) NOT NULL DEFAULT '', comment MEDIUMTEXT NOT NULL, "
					+ "maildate VARCHAR(255) NOT NULL DEFAULT '', industry VARCHAR(255) NOT NULL DEFAULT '', "
					+ "status INT UNSIGNED NOT NULL DEFAULT 0, "
					+ "changed_when DATETIME NULL, changed_by VARCHAR(255) NULL, FULLTEXT(responsible, name, "
					+ "website, address, contact_person, phone, fax, email, comment, maildate, industry, "
					+ "changed_by), PRIMARY KEY(id)";

			final Statement s = Database.connection().createStatement();
			final String p = Database.table_prefix;
			s.execute("DROP TABLE IF EXISTS " + p + "User");
			s.execute("CREATE TABLE " + p + "User (" + user + ")");
			s.execute("INSERT INTO " + p + "User (id, password, status, comment) VALUES ('Admin', '', 'Admin', '')");

			s.execute("DROP TABLE IF EXISTS " + p + "Company");
			s.execute("CREATE TABLE " + p + "Company (" + company + ")");

			out.println(new Date() + " instalacija uspesna!");
		} catch (final Exception e) {
			throw new ServletException(e);
		} finally {
			Database.releaseConnection();
		}
	}
}