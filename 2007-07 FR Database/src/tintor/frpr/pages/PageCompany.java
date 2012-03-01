package tintor.frpr.pages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import tintor.frpr.Permissions;
import tintor.frpr.model.Company;
import tintor.frpr.model.User;
import tintor.frpr.model.Variant;
import tintor.frpr.servlets.Main;
import tintor.frpr.util.Listbox;
import tintor.frpr.util.Node;
import tintor.frpr.util.Redirect;
import tintor.frpr.util.Textbox;
import tintor.frpr.util.UserError;

/** @author Marko Tintor (tintor@gmail.com) */
public class PageCompany extends Main {
	private static String validate(final Company.Field field, String a) {
		if (a == null) a = "";
		a = cir2lat(a.trim().replaceAll("\\s+", " "));

		if (field == null) return a;
		switch (field) {
		case website:
			while (a.endsWith("/"))
				a = a.substring(0, a.length() - 1);
			while (a.startsWith("http://"))
				a = a.substring("http://".length()).trim();
			return a;
		default:
			return a;
		}
	}

	public static Object run(final User login, final HttpServletRequest request) throws SQLException {
		// Handle post
		if (request.getParameter("f_company.submit") != null || request.getParameter("f_company.submit2") != null) {
			final String c = request.getParameter("company");
			final Company company = new Company(c.length() > 0 ? Integer.parseInt(c) : -1);

			if (company.id == -1 && Permissions.company_create(login) || company.id != -1
					&& Permissions.company_modify(login, company.id)) {
				company.name = cir2lat(request.getParameter("f_company.name").trim().replaceAll("\\s+", " "));
				if (c.length() == 0 && Company.exists(company.name))
					throw new UserError("Kompanija sa imenom " + company.name + iso(" već postoji!"));

				company.responsible = request.getParameter("f_company.responsible");
				company.website = validate(Company.Field.website, request.getParameter("f_company.website"));
				company.address = validate(null, request.getParameter("f_company.address"));
				company.comment = cir2lat(request.getParameter("f_company.comment").trim());
				company.contact_person = validate(null, request.getParameter("f_company.contact_person"));
				company.phone = validate(Company.Field.phone, request.getParameter("f_company.phone"));
				company.email = validate(null, request.getParameter("f_company.email"));
				company.maildate = validate(Company.Field.maildate, request.getParameter("f_company.maildate"));
				company.industry = validate(null, request.getParameter("f_company.industry"));
				company.fax = validate(Company.Field.fax, request.getParameter("f_company.fax"));
				company.status = Integer.parseInt(request.getParameter("f_company.status"));
				company.changed_by = login.id;

				company.save(); // voila!

				if (request.getParameter("f_company.submit2") != null)
					throw new Redirect((String) request.getSession().getAttribute("before_company"));
			}
		}

		// Access company
		final String id = request.getParameter("company");
		Company company;
		try {
			company = id.length() > 0 ? Company.getByID(Integer.parseInt(id)) : new Company();
		} catch (final NumberFormatException e) {
			return error("Nema kompanije pod brojem " + id + "!");
		}
		if (company == null) return error("Nema kompanije pod brojem " + id + "!");
		if (!Permissions.company_view(login, company)) return error("Nemate pristup kompaniji pod brojem " + id + "!");

		// Generate page
		final List<Object> rows = new ArrayList<Object>();
		final Object contents = form("f_company", "main?company" + (company.id != -1 ? "=" + company.id : ""),
				table(rows));

		final boolean mutable = Permissions.company_modify(login, company.id);
		final boolean responsible_mutable = Permissions.company_change_responsible(login, company.id, null);

		// responsible
		rows.add(row(iso(Company.Field.responsible.out), responsibleField(company.responsible, responsible_mutable)));
		// name
		addFormRow(rows, Company.Field.name, company.name, mutable);
		// industry
		if (mutable || company.industry.length() > 0)
			rows.add(row(escape(iso(Company.Field.industry.out)), industryField(company.industry, mutable)));
		// text fields
		addFormRow(rows, Company.Field.website, company.website, mutable);
		addFormRow(rows, Company.Field.address, company.address, mutable);
		addFormRow(rows, Company.Field.contact_person, company.contact_person, mutable);
		addFormRow(rows, Company.Field.phone, company.phone, mutable);
		addFormRow(rows, Company.Field.fax, company.fax, mutable);
		addFormRow(rows, Company.Field.email, company.email, mutable);
		addFormRow(rows, Company.Field.maildate, company.maildate, mutable);
		// status
		rows.add(row(escape(Company.Field.status.out), statusField(company.status, mutable)));
		// comment
		if (mutable || company.comment.length() > 0)
			rows.add(row(Company.Field.comment.out, new Node("textarea name='f_company.comment' rows='10' cols='80'"
					+ (mutable ? "" : " readonly"), escape(company.comment))));
		// last change
		rows.add(row(Company.Field.changed_when.out, field(company.changed_when)));
		rows.add(row(Company.Field.changed_by.out, user_link(company.changed_by)));
		// submit
		if (mutable || responsible_mutable)
			rows.add(row("&nbsp;", list(input("f_company.submit", "submit", iso("value='Sačuvaj'")), "&nbsp;", input(
					"f_company.submit2", "submit", iso("value='Sačuvaj i vrati se'")))));

		return contents;
	}

	static void addFormRow(final List<Object> rows, final Company.Field field, final String value, final boolean mutable) {
		if (mutable || value.length() > 0)
			rows.add(row(escape(iso(field.out)), mutable ? new Textbox("f_company." + field, value)
					: bold(escape(value))));
	}

	static Object responsibleField(final String responsible, boolean mutable) throws SQLException {
		if (!mutable) return user_link(responsible);

		final Listbox list = new Listbox("f_company.responsible", responsible);
		list.add("", "{niko}");
		for (final User user : User.filterAndOrderBy(null, User.Field.id, true))
			if (user.status != User.Status.Admin) {
				final int count = Company.countWithMember(user.id);
				list.add(user.id, user.id + " (" + count + ") " + iso(user.status.out));
			}
		return list;
	}

	static Object industryField(final String industry, boolean mutable) {
		if (!mutable) return bold(escape(industry));

		switch (Variant.current) {
		case Fund_Raising:
			return new Textbox("f_company.industry", industry);
		case Public_Relations:
			final Listbox list = new Listbox("f_company.industry", industry);
			for (final String a : Company.industryPR)
				list.add(a, iso(a));
			return list;
		default:
			throw new RuntimeException();
		}
	}

	static Object statusField(final int status, final boolean mutable) {
		if (!mutable) return bold(escape(iso(Company.statusToString(status))));

		final Listbox list = new Listbox("f_company.status", String.valueOf(status));
		for (int s = 0; s < Company.statusCount(); s++)
			list.add(String.valueOf(s), iso(Company.statusToString(s)));
		return list;
	}
}