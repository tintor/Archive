package tintor.frpr.pages;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import tintor.frpr.Permissions;
import tintor.frpr.model.Company;
import tintor.frpr.model.User;
import tintor.frpr.model.Variant;
import tintor.frpr.model.Company.CompaniesFilter;
import tintor.frpr.servlets.Main;
import tintor.frpr.util.Node;
import tintor.frpr.util.Redirect;
import tintor.frpr.util.Table;
import tintor.frpr.util.UserError;

/** @version avgust 2007
 *  @author Marko Tintor (tintor@gmail.com) */
public class PageCompanies extends Main {
	public static Object run(final User login, final HttpServletRequest request) throws SQLException {
		handlePost(login, request);
		return generatePage(login, request);
	}

	private static void handlePost(final User login, final HttpServletRequest request) throws SQLException {
		final String responsible = request.getParameter("f_companies.responsible");

		final boolean change_responsible = responsible != null && !responsible.equals("?");
		final boolean delete = Permissions.company_delete(login) && request.getParameter("f_companies.delete") != null;

		if (change_responsible || delete) {
			final String r = responsible != null && responsible.length() > 0 ? responsible : "";

			final Enumeration<?> e = request.getParameterNames();
			while (e.hasMoreElements()) {
				final String p = (String) e.nextElement();
				if (!p.startsWith("f_companies.selected_")) continue;
				final int company = Integer.parseInt(p.substring("f_companies.selected_".length()));

				if (change_responsible && Permissions.company_change_responsible(login, company, r))
					Company.setResponsible(company, r, login.id);
				if (delete) Company.delete(company);
			}
		}
	}

	public final static int OffsetDefault = 0;
	public final static int LimitDefault = 50;

	public static Object generatePage(final User login, final HttpServletRequest request) throws SQLException {
		final CompaniesFilter filter = new CompaniesFilter();

		// read parameters
		filter.ascending = request.getParameter("reverse") == null;
		filter.responsible = request.getParameter("responsible");
		if (request.getParameter("changed") != null)
			filter.changed_after = (String) request.getSession().getAttribute("prev_access");

		filter.search = request.getParameter("find");
		if (filter.search != null && filter.search.trim().length() == 0)
			throw new Redirect(request.getRequestURI() + "?" + request.getQueryString().replaceFirst("&find=", ""));
		final Pattern pattern = filter.search != null ? highlightPattern(filter.search) : null;
		filter.order = Company.field(request.getParameter("order"), filter.search != null ? null : Company.Field.name);

		final EnumSet<Company.Field> hidden = EnumSet.noneOf(Company.Field.class);
		final String h = request.getParameter("hide");
		if (h != null) for (final String a : h.split("\\s+"))
			hidden.add(Company.Field.valueOf(a));

		boolean only_group = false;
		switch (login.status) {
		case Admin:
			break;
		case MainOrganizer:
			break;
		case Organizer:
			if (filter.responsible == null)
				only_group = true;
			else if (!login.id.equals(filter.responsible) && !login.is_manager_of(filter.responsible))
				throw new UserError("Nemate pristup kompanijama korisnika " + filter.responsible + "!");
			break;
		case Member:
			filter.responsible = login.id;
			break;
		}

		filter.offset = Math.max(0, getInt(request, "offset", OffsetDefault));
		filter.limit = Math.max(1, getInt(request, "limit", LimitDefault));
		filter.status = getInt(request, "status", Integer.MIN_VALUE);

		// toolbar
		final List<Object> toolbar = new ArrayList<Object>();
		if (login.status != User.Status.Member) {
			// userbox
			final List<Object> users = new ArrayList<Object>();
			final Object userbox = new Node(
					"select name='f_companies.responsible' onchange='document.f_companies.submit()'", new Node(
							"option value='?'", iso("Zaduži...")), new Node("option value=''",
							escape("{niko}")), users);
			for (final User u : User.filterAndOrderBy(null, User.Field.id, true))
				if (u.status != User.Status.Admin) {
					if (login.status == User.Status.Organizer && !u.id.equals(login.id)
							&& !login.is_manager_of(u.id)) continue;

					final String a = escape(u.id + " (" + Company.countWithMember(u.id) + ") " + iso(u.status.out));
					users.add(new Node("option value='" + escape(u.id) + "'", a));
				}
			toolbar.add(userbox);
		}
		if (Permissions.company_delete(login)) toolbar.add(delete_button("f_companies.delete"));

		if (login.status != User.Status.Member) {
			final String text = iso("prikaži ")
					+ (filter.responsible != null ? (login.status == User.Status.Organizer ? "samo moju grupu"
							: "sve") : "samo moje");
			toolbar.add(link(companies_link(filter.order, filter.ascending, filter.responsible != null ? null
					: login.id, hidden, OffsetDefault, filter.limit, filter.status, filter.changed_after != null,
					filter.search), text));
		}

		// table
		final boolean show_checkbox = login.status != User.Status.Member || Permissions.company_delete(login);
		final Table table = new Table(true);

		if (show_checkbox) table.hcell("&nbsp;");
		for (final Company.Field field : Variant.current.order)
			if (field != Company.Field.comment)
				if (hidden.contains(field))
					table.hcell(link(companies_link(filter.order, filter.ascending, filter.responsible, remove(
							hidden, field), filter.offset, filter.limit, filter.status,
							filter.changed_after != null, filter.search), image("show.png")));
				else
					table.hcell(list(link(companies_link(field, !filter.ascending, filter.responsible, hidden,
							OffsetDefault, filter.limit, filter.status, filter.changed_after != null,
							filter.search), escape(iso(field.out))), "&nbsp;", link(companies_link(
							filter.order, filter.ascending, filter.responsible, add(hidden, field),
							filter.offset, filter.limit, filter.status, filter.changed_after != null,
							filter.search), image("delete.png"))), "style='width:auto'");
		table.row();

		final int total = filter.count();
		try {
			for (final Company company : filter.list()) {
				// company filters
				if (only_group && !login.id.equals(company.responsible) && !login.is_manager_of(company.responsible))
					continue;

				// checkbox
				final String rowID = "tr" + company.id;
				if (show_checkbox)
					table.cell(input("f_companies.selected_" + company.id, "checkbox",
							"onclick='highlight(this)' value='" + rowID + "'"));

				// columns
				final Map<Company.Field, Object> map = new EnumMap<Company.Field, Object>(Company.Field.class);
				map.put(Company.Field.name, link("main?company=" + company.id, field(company.name, pattern)));
				map.put(Company.Field.phone, field(company.phone, pattern));
				map.put(Company.Field.contact_person, field(company.contact_person, pattern));
				map.put(Company.Field.fax, field(company.fax, pattern));
				map.put(Company.Field.email, email_link(company.email, pattern));
				map.put(Company.Field.maildate, field(company.maildate, pattern));
				map.put(Company.Field.status, field(Company.statusToString(company.status)));
				map.put(Company.Field.changed_when, field(company.changed_when, pattern));
				map.put(Company.Field.changed_by, user_link(company.changed_by, pattern));
				map.put(Company.Field.responsible, user_link(company.responsible, pattern));
				map.put(Company.Field.website, company.website.length() > 0 ? link("http://"
						+ escape(company.website), field(company.website, pattern)) : "&nbsp;");
				map.put(Company.Field.address, field(company.address, pattern));
				map.put(Company.Field.industry, field(company.industry, pattern));

				for (final Company.Field field : Variant.current.order)
					if (field != Company.Field.comment)
						table.cell(hidden.contains(field) ? "&nbsp;" : map.get(field));
				table.row("id='" + rowID + "'");

				// comment
				if (company.comment.length() > 0) {
					table.cell(comment_large(company.comment, pattern), "colspan='" + table.columns() + "'");
					table.row("id='" + rowID + "_c'");
				}
			}
		} finally {
			filter.finalize();
		}

		// search form
		final Node searchForm = new Node("form enctype=text accept-charset=utf-8 method=GET action=main");
		final String[] pars = companies_link(null, true, filter.responsible, hidden, OffsetDefault, filter.limit,
				filter.status, filter.changed_after != null, null).replaceFirst(".*\\?", "").split("&");
		for (final String p : pars) {
			final int f = p.indexOf('=');
			final String s = f == -1 ? p : p.substring(0, f) + "' value='" + p.substring(f + 1);
			searchForm.add(new Node("input type=hidden name='" + s + "'"));
		}
		searchForm.add(new Node("input type=text name=find size=40 value='"
				+ (filter.search != null ? escape(filter.search) : "") + "' title='Search'"));
		searchForm.add(new Node("input type=submit " + iso("value='Potraži'")));

		// page		
		final Node form = form("f_companies", companies_link(filter.order, filter.ascending, filter.responsible, hidden,
				filter.offset, filter.limit, filter.status, filter.changed_after != null, filter.search));
		form.add(link("companies.csv", "preuzmi tabelu"), "<br/>");
		form.add(join(toolbar, "&nbsp;"));
		if (toolbar.size() > 0) form.add("<br/>");
		form.add(table);
		form.add(pagesLinks(filter, total, hidden));

		return list(searchForm, form);
	}

	private static Object pagesLinks(final CompaniesFilter filter, final int total, final Set<Company.Field> hidden) {
		final List<Object> pages = new ArrayList<Object>();

		// before
		final int pagesBefore = (filter.offset + filter.limit - 1) / filter.limit;
		for (int p = 1; p <= pagesBefore; p++)
			pages.add(link(companies_link(filter.order, filter.ascending, filter.responsible, hidden, Math.max(
					filter.offset - filter.limit * (pagesBefore + 1 - p), 0), filter.limit, filter.status,
					filter.changed_after != null, filter.search), p));

		// current
		pages.add(bold(pagesBefore + 1));

		// after
		final int pagesAfter = (Math.max(0, total - filter.offset - filter.limit) + filter.limit - 1) / filter.limit;
		for (int p = 1; p <= pagesAfter; p++)
			pages.add(link(companies_link(filter.order, filter.ascending, filter.responsible, hidden, filter.offset
					+ filter.limit * p, filter.limit, filter.status, filter.changed_after != null, filter.search),
					pagesBefore + 1 + p));

		return pagesBefore + pagesAfter > 0 ? list("Strana: ", join(pages, ", ")) : null;
	}

	private static int getInt(final HttpServletRequest request, final String name, final int def) {
		final String p = request.getParameter(name);
		try {
			return p != null ? Integer.parseInt(p) : def;
		} catch (final NumberFormatException e) {
			return def;
		}
	}

	/** Pravi link za tabelu kompanija sa datim parametrima */
	private static String companies_link(final Company.Field order, final boolean ascending, final String responsible,
			final Set<Company.Field> hidden, final int offset, final int limit, final int status,
			final boolean changed, final String find) {
		final StringBuilder b = new StringBuilder("main?companies");

		if (order != null) {
			b.append("&order=");
			b.append(order);

			if (!ascending) b.append("&reverse");
		}

		if (responsible != null) b.append("&responsible=").append(escape(responsible).replace("%20", "+"));

		if (hidden.size() > 0) b.append("&hide=");
		boolean first = true;
		for (final Company.Field field : hidden) {
			if (first)
				first = false;
			else
				b.append('+');
			b.append(field);
		}

		if (status != Integer.MIN_VALUE) b.append("&status=").append(status);

		if (changed) b.append("&changed");

		if (offset != OffsetDefault || limit != LimitDefault)
			b.append("&offset=").append(offset).append("&limit=").append(limit);

		if (find != null) b.append("&find=").append(escape(find).replace("%20", "+"));

		return b.toString();
	}

	/** Dodaj u EnumSet bez menjanja parametara */
	private static <T extends Enum<T>> EnumSet<T> add(final EnumSet<T> s, final T a) {
		final EnumSet<T> x = EnumSet.copyOf(s);
		x.add(a);
		return x;
	}

	/** Izbaci iz EnumSet-a bez menjanja parametara */
	private static <T extends Enum<T>> EnumSet<T> remove(final EnumSet<T> s, final T a) {
		final EnumSet<T> x = EnumSet.copyOf(s);
		x.remove(a);
		return x;
	}
}