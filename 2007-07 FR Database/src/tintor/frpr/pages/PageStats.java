package tintor.frpr.pages;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import tintor.frpr.model.Company;
import tintor.frpr.model.User;
import tintor.frpr.servlets.Main;
import tintor.frpr.util.Table;

/** @author Marko Tintor (tintor@gmail.com) */
public class PageStats extends Main {
	public static Object run(final User login, final HttpServletRequest request) throws SQLException {
		final Table table = new Table(true);
		final boolean member = login.status == User.Status.Member;

		table.cell(member ? "Mojih kompanija" : "Ukupno kompanija");
		table.cell(member ? Company.countWithMember(login.id) : Company.countAll());
		table.row();

		if (!member) {
			table.cell(link("main?companies&responsible", "Nedodeljenih kompanija"));
			table.cell(Company.countWithMember(""));
			table.row();
		}

		final Table subtable = new Table(false);
		for (int i = 0; i < Company.statusCount(); i++) {
			subtable.cell(link("main?companies&status=" + i, escape(iso(Company.statusToString(i)))));
			final int c = member ? Company.countWithMemberAndStatus(login.id, i) : Company.countWithMemberAndStatus(i);
			subtable.cell(c);
			subtable.row();
		}
		table.cell((member ? "Mojih" : "Dodeljenih") + " kompanija prema statusu");
		table.cell(subtable);
		table.row();

		table.cell("Prethodni pristup");
		table.cell(request.getSession().getAttribute("prev_access"));
		table.row();

		table.cell(link("main?companies&order=changed_when&reverse&changed", "Izmenjenih kompanija"));
		table.cell(Company.countRecent((String) request.getSession().getAttribute("prev_access")));
		table.row();

		return table;
	}
}