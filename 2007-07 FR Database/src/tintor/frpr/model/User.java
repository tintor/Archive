package tintor.frpr.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tintor.frpr.util.Database;

/** Objekat ove klase predstavlja jednog korisnika.
 *  
 *  @author Marko Tintor (tintor@gmail.com) */
public class User {
	public static enum Field {
		id("Ime i prezime"), status("Funkcija"), password("Lozinka"), phone("Telefon"), email("E-pošta"), manager("Vođa"), last_access(
				"Poslednji pristup"), comment("Komentar");

		public final String out;

		Field(final String out) {
			this.out = out;
		}
	}

	public static Field field(final String s, final Field def) {
		for (final Field f : Field.values())
			if (f.toString().equals(s)) return f;
		return def;
	}

	public enum Status {
		Admin("Administrator", "FF7700"), MainOrganizer("Vođa tima", "FF0000"), Organizer("Vođa grupe", "00AA00"), Member(
				"Član", "4369D1");

		public final String out, color;

		Status(final String out, final String color) {
			this.out = out;
			this.color = color;
		}
	}

	public String id, password;
	public String manager;
	public Status status;
	public String phone, email;
	public String last_access;
	public String comment;

	public User() {
		last_access = null;
	}

	public static User blank() {
		final User u = new User();
		u.status = Status.Member;
		u.id = u.password = u.manager = u.phone = u.email = "";
		return u;
	}

	User(final ResultSet result) throws SQLException {
		id = result.getString("id");

		status = Status.valueOf(result.getString("status"));
		manager = result.getString("manager");

		phone = result.getString("phone");
		email = result.getString("email");

		last_access = result.getString("last_access");
		if (last_access != null) last_access = last_access.substring(0, 19);

		comment = result.getString("comment");
	}

	private final static String Table = Database.table_prefix + "User";

	private final static String PassSalt = "eat my shorts!"; // if this is changed no one can login anymore!

	public void save() throws SQLException {
		// create user if not exists
		if (User.getByID(id) == null) Database.update("INSERT " + Table + " (id, comment) VALUES (?, ?)", id, comment);

		//Database.update("INSERT " + Table + " (id) VALUES (?) ON DUPLICATE KEY UPDATE id = id", id);
		if (password != null)
			Database.update("UPDATE " + Table + " SET pass_hash = MD5(CONCAT(LOWER(?), ?, ?)) WHERE id=?", id,
					PassSalt, password, id);
		if (status != null) save(Field.status, status);
		if (manager != null) save(Field.manager, manager);
		if (phone != null) save(Field.phone, phone);
		if (email != null) save(Field.email, email);
		if (comment != null) save(Field.comment, comment);
	}

	private void save(final Field field, final Object value) throws SQLException {
		Database.update("UPDATE " + Table + " SET " + field + " = ? WHERE id = ?", value, id);
	}

	public static List<User> filterAndOrderBy(final String user, final Field field, final boolean ascending)
			throws SQLException {
		final String a = "SELECT * FROM " + Table;
		final String b = " ORDER BY " + field + (ascending ? " ASC" : " DESC");

		final PreparedStatement p = user == null ? Database.createQuery(a + b) : Database.createQuery(a
				+ " WHERE manager=?" + b, user);
		try {
			final ResultSet result = p.executeQuery();
			final List<User> list = new ArrayList<User>();
			while (result.next())
				list.add(new User(result));
			return list;
		} finally {
			p.close();
		}
	}

	public static User getByID(final String id) throws SQLException {
		return get(Database.createQuery("SELECT * FROM " + Table + " WHERE id = ?", id));
	}

	public static User login(final String id, final String password) throws SQLException {
		return get(Database.createQuery("SELECT * FROM " + Table
				+ " WHERE id = ? AND pass_hash = MD5(CONCAT(LOWER(?), ?, ?))", id, id, PassSalt, password));
	}

	private static User get(final PreparedStatement p) throws SQLException {
		try {
			final ResultSet result = p.executeQuery();
			return result.next() ? new User(result) : null;
		} finally {
			p.close();
		}
	}

	public void logAccess() throws SQLException {
		Database.update("UPDATE " + Table + " SET last_access = now() WHERE id = ?", id);
	}

	public static void delete(final String id) throws SQLException {
		Database.update("DELETE FROM " + Table + " WHERE id = ?", id);
	}

	public static void change_status(final String id, final Status status) throws SQLException {
		if (status == Status.Member) Database.update("UPDATE " + Table + " SET manager = '' WHERE manager = ?", id);
		Database.update("UPDATE " + Table + " SET status = ? WHERE id = ?", status, id);
	}

	public static void change_manager(final String id, final String manager) throws SQLException {
		Database.update("UPDATE " + Table + " SET manager = ? WHERE id = ?", manager, id);
	}

	public void rename(final String newID) throws SQLException {
		Database.connection().setAutoCommit(false);
		try {
			Database.update("UPDATE " + Company.Table + " SET responsible = ? WHERE responsible = ?", newID, id);
			Database.update("UPDATE " + Table + " SET manager = ? WHERE manager = ?", newID, id);
			Database.update("UPDATE " + Table + " SET id = ? WHERE id = ?", newID, id);
			Database.connection().commit();
			id = newID;
		} finally {
			Database.connection().setAutoCommit(true);
		}
	}

	public int[] companiesByStatus() throws SQLException {
		final int[] r = new int[Company.statusCount()];

		final PreparedStatement p = Database.createQuery("SELECT status, COUNT(*) FROM " + Company.Table
				+ " WHERE responsible=? GROUP BY status", id);
		try {
			final ResultSet result = p.executeQuery();
			while (result.next())
				r[result.getInt(1)] = result.getInt(2);
		} finally {
			p.close();
		}
		return r;
	}

	public boolean root() {
		return status == Status.Admin || status == Status.MainOrganizer;
	}

	public boolean is_manager_of(final String user) throws SQLException {
		final User u = getByID(user);
		return u != null && id.equals(u.manager);
	}
}