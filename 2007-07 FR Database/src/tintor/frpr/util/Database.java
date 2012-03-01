package tintor.frpr.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** @author Marko Tintor (tintor@gmail.com) */
public class Database {
	private static final String server = "localhost:3306", database = "frdb", user = "root", pass = "sifra";
	public static final String table_prefix = "";

	private static ThreadLocal<Connection> connection = new ThreadLocal<Connection>();

	private static Connection connect() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://" + Database.server + "/" + Database.database
					+ "?useEncoding=true&characterEncoding=UTF-8", Database.user, Database.pass);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void acquireConnection() throws SQLException {
		connection.set(connect());
	}

	public static void releaseConnection() {
		try {
			connection.get().close();
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
		connection.set(null);
	}

	public static Connection connection() {
		return connection.get();
	}

	public static PreparedStatement createQuery(final String query, final Object... args) throws SQLException {
		final PreparedStatement p = connection().prepareStatement(query);
		try {
			for (int i = 0; i < args.length; i++)
				p.setString(i + 1, args[i] != null ? args[i].toString() : null);
			return p;
		} catch (final SQLException e) {
			p.close();
			throw e;
		}
	}

	public static int intQuery(final String query, final Object... args) throws SQLException {
		final PreparedStatement p = createQuery(query, args);
		try {
			final ResultSet result = p.executeQuery();
			result.next();
			return result.getInt(1);
		} finally {
			p.close();
		}
	}

	public static int update(final String query, final Object... args) throws SQLException {
		final PreparedStatement p = createQuery(query, args);
		try {
			System.out.println(p.toString());
			return p.executeUpdate();
		} finally {
			p.close();
		}
	}
}