package tintor.frpr.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tintor.frpr.servlets.Main;
import tintor.frpr.util.Database;

/** Objekat ove klase predstavlja jednu kompaniju.
 *
 * @author Marko Tintor (tintor@gmail.com) */
public class Company {
	/** Predstavlja sva polja koja se pamte kod jedne kompanije. */
	public static enum Field {
		id("ID"), name("Naziv"), phone("Telefon"), contact_person("Kontakt osoba"), fax("Faks"), email("E-pošta"), maildate(
				"Datum dopisa"), status("Status"), changed_when("Vreme izmene"), changed_by("Izmenio"), responsible(
				"Zaduženi"), website("Website"), address("Adresa"), industry("Delatnost"), comment("Komentar");

		final static Field[] orderFR = { name, phone, contact_person, fax, email, maildate, status, changed_when,
				changed_by, responsible, website, address, industry, comment };
		final static Field[] orderPR = { name, industry, address, website, phone, fax, contact_person, email,
				responsible, status, maildate, changed_when, changed_by, comment };

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

	public String get(final Field field) {
		switch (field) {
		case id:
			return Integer.toString(id);
		case name:
			return name;
		case phone:
			return phone;
		case contact_person:
			return contact_person;
		case fax:
			return fax;
		case email:
			return email;
		case maildate:
			return maildate;
		case status:
			return statusToString(status);
		case changed_when:
			return changed_when;
		case changed_by:
			return changed_by;
		case responsible:
			return responsible;
		case website:
			return website;
		case address:
			return address;
		case industry:
			return industry;
		case comment:
			return comment;
		default:
			throw new RuntimeException();
		}
	}

	public final static String[] industryPR = { "", "TV", "Radio", "Magazin", "Dnevne novine", "Online",
			"Konsultantska kuca", "Stamparija", "Bilbordi", "Autobusi", "Ostalo", "Pekara", "Ketering", "Bioskop" };

	final static String[] statusFR = { "Nije kontaktirana", "Poslat dopis", "Zainteresovani", "Potpisan ugovor",
			"Nisu zainteresovani", "Ostalo" };
	final static String[] statusPR = { "Nije kontaktirana", "Ostavljen promo materijal", "Poslata ponuda",
			"Zainteresovani", "Potpisan ugovor", "Nisu zainteresovani", "Ostalo" };

	public static int statusCount() {
		return Variant.current.status.length;
	}

	public static String statusToString(final int status) {
		if (status < 0) throw new IllegalArgumentException("status < 0");
		if (status < Variant.current.status.length) return Variant.current.status[status];
		return "#" + status;
	}

	public static int statusToInt(final String status) {
		for (int i = 0; i < Variant.current.status.length; i++)
			if (Variant.current.status[i].equals(status)) return i;
		if (status == null || status.length() < 2 || status.charAt(0) != '#')
			throw new IllegalArgumentException("status=" + status);
		try {
			return Integer.parseInt(status.substring(1));
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("status=" + status);
		}
	}

	public final int id;
	public String name = "";
	public String responsible = "";

	public String contact_person = "", phone = "", fax = "", email = "", maildate = "2007-MM-DD";
	public int status = 0;

	public String changed_when;
	public String changed_by;

	public String website = "";
	public String address = "";
	public String industry = "";
	public String comment = "";

	public Company() {
		this(-1);
	}

	public Company(final int id) {
		this.id = id;
		changed_when = null;
		changed_by = null;
	}

	Company(final ResultSet result) throws SQLException {
		id = result.getInt("id");
		name = result.getString("name");

		responsible = result.getString("responsible");
		website = result.getString("website");
		address = result.getString("address");
		industry = result.getString("industry");

		contact_person = result.getString("contact_person");
		phone = result.getString("phone");
		fax = result.getString("fax");
		email = result.getString("email");
		maildate = result.getString("maildate");

		status = result.getInt("status");
		comment = result.getString("comment");

		changed_when = result.getString("changed_when");
		if (changed_when != null) changed_when = changed_when.substring(0, 16);
		changed_by = result.getString("changed_by");
	}

	public final static String Table = Database.table_prefix + "Company";

	public void save() throws SQLException {
		if (id == -1)
			Database.update("INSERT " + Table
					+ " (name, responsible, website, address, industry, comment, contact_person, "
					+ "phone, fax, email, maildate, status, changed_when, changed_by) VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "now(), ?)", name, responsible, website, address,
					industry, comment, contact_person, phone, fax, email, maildate, status, changed_by);
		else {
			Database.update("UPDATE " + Table + " SET name=?, website=?, address=?, industry=?, comment=?,"
					+ "contact_person=?, phone=?, fax=?, email=?, maildate=?, status=?, changed_when=now(), "
					+ "changed_by=? WHERE id=?", name, website, address, industry, comment, contact_person, phone,
					fax, email, maildate, status, changed_by, id);
			if (responsible != null)
				Database.update("UPDATE " + Table + " SET responsible=? WHERE id=?", responsible, id);
		}
	}

	public static Object companiesLinks(final String responsible) throws SQLException {
		final List<Object> list = new ArrayList<Object>();

		final CompaniesFilter filter = new CompaniesFilter();
		filter.order = Company.Field.name;
		filter.ascending = true;

		final PreparedStatement p = Database.createQuery("SELECT id, name FROM " + Table
				+ " WHERE responsible=? ORDER BY name", responsible);
		try {
			final ResultSet result = p.executeQuery();
			while (result.next())
				list.add(Main.link("main?company=" + result.getInt(1), result.getString(2)));
		} finally {
			p.close();
		}

		return list.size() > 0 ? Main.join(list, ", ") : "&nbsp;";
	}

	public static class CompaniesFilter {
		public Field order;
		public boolean ascending;
		public int offset = Integer.MIN_VALUE;
		public int limit = Integer.MIN_VALUE;
		public String responsible;
		public int status = Integer.MIN_VALUE;
		public String changed_after;
		public String search;

		private final List<Object> args = new ArrayList<Object>();

		private String query(final String a, final boolean extended) {
			final StringBuilder b = new StringBuilder();
			b.append("SELECT ").append(a).append(" FROM ").append(Table);

			args.clear();
			final List<String> cond = new ArrayList<String>();

			if (status != Integer.MIN_VALUE) cond.add("status = " + status);
			if (responsible != null) {
				cond.add("responsible = ?");
				args.add(responsible);
			}
			if (changed_after != null) {
				if (changed_after.length() == 0) changed_after = "2001-01-01 00:00:00";
				cond.add("changed_when >= TIMESTAMP(?)");
				args.add(changed_after);
			}
			if (search != null) {
				// TODO add fulltext index for Install.java
				cond.add("MATCH(responsible, name, website, address, contact_person, phone, "
						+ "fax, email, comment, maildate, industry, changed_by) AGAINST(?)");
				args.add(search);
			}
			for (int i = 0; i < cond.size(); i++)
				b.append(i == 0 ? " WHERE " : " AND ").append(cond.get(i));

			if (extended) {
				if (order != null) b.append(" ORDER BY ").append(order).append(ascending ? " ASC" : " DESC");
				if (offset != Integer.MIN_VALUE && limit != Integer.MIN_VALUE)
					b.append(" LIMIT ").append(offset).append(',').append(limit);
			}

			System.out.println(b.toString() + " " + changed_after);
			return b.toString();
		}

		public int count() throws SQLException {
			return Database.intQuery(query("COUNT(*)", false), args.toArray());
		}

		@SuppressWarnings("synthetic-access") public Iterable<Company> list() throws SQLException {
			if (p != null) finalize();
			p = Database.createQuery(query("*", true), args.toArray());
			return iterableResultSet(p.executeQuery());
		}

		private PreparedStatement p;

		@Override public void finalize() {
			try {
				if (p != null) p.close();
				p = null;
			} catch (final SQLException e) {
				throw new RuntimeException();
			}
		}
	}

	private static Iterable<Company> iterableResultSet(final ResultSet result) {
		return new Iterable<Company>() {
			public Iterator<Company> iterator() {
				return new Iterator<Company>() {
					public boolean hasNext() {
						try {
							return result.next();
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public Company next() {
						try {
							return new Company(result);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public void remove() {
						throw new RuntimeException();
					}
				};
			}
		};
	}

	/** Vraca kompaniju na osnovu ID. */
	public static Company getByID(final int id) throws SQLException {
		final PreparedStatement p = Database.createQuery("SELECT * FROM " + Table + " WHERE id = ?", id);
		try {
			final ResultSet result = p.executeQuery();
			return result.next() ? new Company(result) : null;
		} finally {
			p.close();
		}
	}

	public static int setResponsible(final int company, final String responsible, final String user) throws SQLException {
		return Database.update("UPDATE " + Table + " SET responsible = ?, changed_when = now(),"
				+ " changed_by = ? WHERE id = ?", responsible, user, company);
	}

	public static void delete(final int company) throws SQLException {
		Database.update("DELETE FROM " + Table + " WHERE id = ?", company);
	}

	public static int countAll() throws SQLException {
		return Database.intQuery("SELECT COUNT(*) FROM " + Table);
	}

	public static int countRecent(String time) throws SQLException {
		if (time.length() == 0) time = "2001-01-01 00:00:00";
		return Database.intQuery("SELECT COUNT(*) FROM " + Table + " WHERE changed_when >= TIMESTAMP(?)", time);
	}

	public static int countWithMember(final String user) throws SQLException {
		return Database.intQuery("SELECT COUNT(*) FROM " + Table + " WHERE responsible = ?", user);
	}

	public static int countWithMemberAndStatus(final String user, final int status) throws SQLException {
		return Database.intQuery("SELECT COUNT(*) FROM " + Table + " WHERE responsible=? AND status=?", user, status);
	}

	public static int countWithMemberAndStatus(final int status) throws SQLException {
		return Database.intQuery("SELECT COUNT(*) FROM " + Table + " WHERE responsible<>'' AND status=?", status);
	}

	public static boolean exists(final String name) throws SQLException {
		final PreparedStatement p = Database.createQuery("SELECT id FROM " + Table + " WHERE name = ?", name);
		try {
			return p.executeQuery().next();
		} finally {
			p.close();
		}
	}
}