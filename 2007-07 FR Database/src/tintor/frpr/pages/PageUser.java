package tintor.frpr.pages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import tintor.frpr.Permissions;
import tintor.frpr.model.Company;
import tintor.frpr.model.User;
import tintor.frpr.model.Company.CompaniesFilter;
import tintor.frpr.servlets.Main;
import tintor.frpr.util.*;

/** @author Marko Tintor (tintor@gmail.com) */
public class PageUser extends Main {
	public static Object run(final User login, final HttpServletRequest request) throws SQLException {
		// Handle post
		if (request.getParameter("f_user.submit") != null || request.getParameter("f_user.submit2") != null) {
			final User user = new User();
			boolean renamed = false;

			user.id = request.getParameter("user");
			if (user.id.length() == 0) { // creation of new user
				user.id = cir2lat(request.getParameter("f_user.id"));
				if (!Permissions.user_create(login)) throw new UserError("Ne mozete praviti novog korisnika!");
				if (user.id.length() == 0) throw new UserError("Fali ime korisnika!");
				if (User.getByID(user.id) != null)
					throw new UserError("Korisnik sa imenom " + user.id + iso(" već postoji!"));
			} else {
				final String n = cir2lat(request.getParameter("f_user.id"));
				if (n != null && !n.equals(user.id)) {
					user.rename(n);
					renamed = true;
				}
			}

			user.password = request.getParameter("f_user.password");
			if (user.password != null && !user.password.equals(request.getParameter("f_user.password2"))) {
				user.password = null;
				throw new UserError("Lozinke se moraju poklapati!");
			}
			if ("".equals(user.password)) user.password = null;

			final String status = request.getParameter("f_user.status");
			if (status != null) {
				user.status = User.Status.valueOf(status);
				if (!Permissions.user_change_status(login, user.id, user.status))
					throw new UserError("Ne mozete menjati funkciju korisnika!");
			}

			user.manager = request.getParameter("f_user.manager");
			user.phone = request.getParameter("f_user.phone");
			user.email = request.getParameter("f_user.email");
			if (Permissions.user_change_comment(login, user.id))
				user.comment = request.getParameter("f_user.comment");

			user.save();

			if (request.getParameter("f_user.submit2") != null)
				throw new Redirect((String) request.getSession().getAttribute("before_user"));
			if (renamed) throw new UserError("Novo ime korisnika je " + user.id);
		}

		// Generate page
		final String user_id_get = request.getParameter("user");
		final User user = user_id_get.length() > 0 ? User.getByID(user_id_get) : User.blank();
		if (user == null) return error("Nema korisnika sa imenom " + user_id_get + "!");
		if (user.id == null && !Permissions.user_create(login)) return error("Ne mozete praviti novog korisnika!");

		// form
		final List<Object> rows = new ArrayList<Object>();
		final Object contents = form("f_user", "main?user" + (user.id.length() > 0 ? "=" + escape(user.id) : ""),
				table(rows));

		// id
		addFormRow(rows, User.Field.id, user.id, user_id_get.length() == 0 || login.root());

		final boolean mutable = Permissions.user_change(login, user.id);
		if (mutable) {
			// passwords
			rows.add(row(escape(User.Field.password.out), input("f_user.password", "password", "size='40'")));
			rows.add(row("Lozinka ponovo", input("f_user.password2", "password", "size='40'")));
		}

		if (login.root()) {
			// status
			final Listbox status = new Listbox("f_user.status", user.status.toString());
			for (final User.Status s : User.Status.values())
				status.add(s.toString(), iso(s.out));
			rows.add(row(escape(User.Field.status.out), status));

			// manager
			final Listbox manager = new Listbox("f_user.manager", user.manager);
			manager.add("", "{niko}");
			for (final User u : User.filterAndOrderBy(null, User.Field.id, true))
				if (u.status == User.Status.MainOrganizer || u.status == User.Status.Organizer)
					manager.add(u.id, u.id);
			rows.add(row(escape(iso(User.Field.manager.out)), manager));
		} else {
			addFormRow(rows, User.Field.status, iso(user.status.out), false);
			rows.add(row(escape(iso(User.Field.manager.out)), bold(user_link(user.manager))));
		}

		addFormRow(rows, User.Field.phone, user.phone, mutable);
		addFormRow(rows, User.Field.email, user.email, mutable);

		// comment
		if (Permissions.user_change_comment(login, user.id))
			rows.add(row(Company.Field.comment.out, new Node("textarea name='f_user.comment' rows='10' cols='80'"
					+ (mutable ? "" : " readonly"), escape(user.comment))));

		// submit
		if (mutable)
			rows.add(row("&nbsp;", list(input("f_user.submit", "submit", iso("value='Sačuvaj'")), "&nbsp;", input(
					"f_user.submit2", "submit", iso("value='Sačuvaj i vrati se'")))));

		// empty row
		rows.add(row("&nbsp;", "&nbsp;"));

		if (user.id.length() > 0) {
			// statistics
			final Table stable = new Table(true);
			for (int s = 0; s < Company.statusCount(); s++)
				stable.hcell(Company.statusToString(s));
			stable.row();
			for (final int s : user.companiesByStatus())
				stable.cell(s > 0 ? s : "&nbsp");
			stable.row();

			rows.add(row("Pregled", stable));

			// organizes users
			if (user.status != User.Status.Member) {
				final List<Object> users = new ArrayList<Object>();
				for (final User u : User.filterAndOrderBy(user.id, User.Field.id, true)) {
					if (users.size() > 0) users.add(", ");
					users.add(user_link(u.id));
				}
				rows.add(row(link("main?users&manager=" + escape(user.id), "Odgovaraju"), bold(users)));
			}

			// Companies subtable
			final Table ctable = new Table(true);

			for (final Company.Field field : Company.Field.values())
				if (field != Company.Field.id && field != Company.Field.responsible
						&& field != Company.Field.comment) ctable.hcell(escape(iso(field.out)));
			ctable.row();

			boolean empty = true;

			final CompaniesFilter filter = new CompaniesFilter();
			filter.order = Company.Field.name;
			filter.ascending = true;
			filter.responsible = user.id;

			try {
				for (final Company company : filter.list()) {
					ctable.cell(link("main?company=" + company.id, field(company.name)));
					ctable.cell(phone(company.phone));
					ctable.cell(field(company.contact_person));
					ctable.cell(phone(company.fax));
					ctable.cell(email_link(company.email));
					ctable.cell(field(company.maildate));
					ctable.cell(field(Company.statusToString(company.status)));
					ctable.cell(field(company.changed_when));
					ctable.cell(user_link(company.changed_by));
					ctable.cell(company.website.length() > 0 ? link("http://" + escape(company.website),
							escape(company.website)) : "&nbsp;");
					ctable.cell(field(company.address));
					ctable.cell(field(company.industry));
					ctable.row();

					if (company.comment.length() > 0) {
						ctable.cell(comment_large(company.comment), "colspan='" + ctable.columns() + "'");
						ctable.row();
					}
					empty = false;
				}
			} finally {
				filter.finalize();
			}

			Object t = ctable;
			if (empty) t = iso("{nije zadužen}");
			if (login.status == User.Status.Organizer && !login.id.equals(user.id) && !login.is_manager_of(user.id))
				t = "{nedostupno}";
			if (login.status == User.Status.Member && !login.id.equals(user.id)) t = "{nedostupno}";
			rows.add(row(link("main?companies&responsible=" + escape(user.id), "Kompanije"), t));
		}
		return contents;
	}

	static void addFormRow(final List<Object> rows, final User.Field field, final String value, final boolean mutable) {
		if (mutable || value.length() > 0)
			rows.add(row(escape(iso(field.out)), mutable ? input("f_user." + field, "text", "value='" + escape(value)
					+ "' size='40'") : bold(escape(value))));
	}
}