package tintor.frpr.pages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import tintor.frpr.Permissions;
import tintor.frpr.model.Company;
import tintor.frpr.model.User;
import tintor.frpr.servlets.Main;
import tintor.frpr.util.Node;
import tintor.frpr.util.Table;

/** @author Marko Tintor (tintor@gmail.com) */
public class PageUsers extends Main {
	public static Object run(final User login, final HttpServletRequest request) throws SQLException {
		// Handle post
		{
			final String status = request.getParameter("f_users.status");
			final String manager = request.getParameter("f_users.manager");

			final boolean change_status = status != null && status.length() > 0;
			final boolean change_manager = manager != null && !manager.equals("?")
					&& Permissions.user_change_manager(login);
			final boolean delete = request.getParameter("f_users.delete") != null;

			if (change_status || change_manager || delete) {
				final User.Status s = change_status ? User.Status.valueOf(status) : null;
				final String m = manager != null && manager.length() > 0 ? manager : null;

				final Enumeration<?> e = request.getParameterNames();
				while (e.hasMoreElements()) {
					final String p = (String) e.nextElement();
					if (!p.startsWith("f_users.selected_")) continue;
					final String id = p.substring("f_users.selected_".length());

					if (change_status || change_manager) {
						if (change_status && Permissions.user_change_status(login, id, s))
							User.change_status(id, s);
						if (change_manager) User.change_manager(id, m);
					}
					if (delete && Permissions.user_delete(login, id)) User.delete(id);
				}
			}
		}

		// Generate page
		final boolean ascending = !"desc".equals(request.getParameter("dir"));
		final String manager = request.getParameter("manager");

		// statusbox
		final Node statusbox = new Node("select name='f_users.status' onchange='document.f_users.submit()'");
		statusbox.add(new Node("option value=''", "Funkcija..."));
		for (final User.Status s : User.Status.values())
			statusbox.add(new Node("option value='" + s + "'", escape(iso(s.out))));

		// managerbox
		final Node managerbox = new Node("select name='f_users.manager' onchange='document.f_users.submit()'");
		managerbox.add(new Node("option value='?'", iso("Vođa...")));
		managerbox.add(new Node("option value=''", escape("{niko}")));
		for (final User user : User.filterAndOrderBy(null, User.Field.id, true))
			if (user.status == User.Status.MainOrganizer || user.status == User.Status.Organizer)
				managerbox.add(new Node("option value='" + escape(user.id) + "'", escape(user.id)));

		// toolbar
		final Object toolbar = login.root() ? list(statusbox, "&nbsp;", managerbox, "&nbsp;",
				delete_button("f_users.delete"), "<br/>") : null;

		// table
		final Table table = new Table(true);

		if (login.root()) table.cell("&nbsp;");
		final String a = "main?users&" + (manager != null ? "manager=" + manager + "&" : "") + "dir="
				+ (ascending ? "desc" : "asc") + "&order=";
		for (final User.Field field : User.Field.values())
			if (field != User.Field.password) {
				if (field == User.Field.comment && login.status == User.Status.Member) continue;
				if (manager != null && field == User.Field.manager) continue;

				table.hcell(link(a + field, escape(iso(field.out))));
				if (field == User.Field.email) {
					if (login.status != User.Status.Member) table.hcell("Kompanije");
					if (manager == null) table.hcell(iso("Članovi grupe"));
				}
			}
		for (int i = 0; i < Company.statusCount(); i++)
			table.hcell(Company.statusToString(i));
		table.row();

		int id = 0;

		final User.Field order = User.field(request.getParameter("order"), User.Field.id);
		for (final User user : User.filterAndOrderBy(manager, order, ascending)) {
			final String rowID = "'tr" + ++id + "'";
			if (login.root())
				table.cell(input("f_users.selected_" + user.id, "checkbox", "onclick='highlight(this)' value="
						+ rowID));
			table.cell(user_link(user.id));
			table.cell(escape(iso(user.status.out)));
			table.cell(phone(user.phone));
			table.cell(email_link(user.email));

			// responsible for companies
			if (login.status != User.Status.Member)
				table.cell(login.status == User.Status.Organizer && !login.id.equals(user.id)
						&& !login.id.equals(user.manager) ? "{nedostupno}" : Company.companiesLinks(user.id));

			if (manager == null) {
				final List<Object> sub_users = new ArrayList<Object>();
				for (final User u : User.filterAndOrderBy(user.id, User.Field.id, true))
					sub_users.add(user_link(u.id));
				table.cell(sub_users.size() > 0 ? join(sub_users, ", ") : "&nbsp;");

				table.cell(user_link(user.manager));
			}

			table.cell(field(user.last_access));

			// comment
			if (login.status != User.Status.Member)
				table.cell(Permissions.user_change_comment(login, user.id) ? comment(user.comment) : "{nedostupno}");

			// companies by status
			for (final int s : user.companiesByStatus())
				table.hcell(s > 0 ? s : "&nbsp");

			table.row("id=" + rowID);
		}

		// page
		return form("f_users", "main?users", toolbar, table);
	}
}
