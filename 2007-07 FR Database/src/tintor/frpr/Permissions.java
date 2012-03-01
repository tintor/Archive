package tintor.frpr;

import java.sql.SQLException;

import tintor.frpr.model.Company;
import tintor.frpr.model.User;

/** Staticka klasa koja definise prava korisnika.
 *  
 * @author Marko Tintor (tintor@gmail.com) */
public class Permissions {
	public static boolean user_create(final User login) {
		return login.root();
	}

	public static boolean user_delete(final User login, final String id) {
		return login.root() && !login.id.equals(id);
	}

	// TODO bug: this is not working in user page!
	public static boolean user_change_status(final User login, final String sel_user, final User.Status new_status) {
		// admin can't remove admin access from himself
		return login.root() && (!login.id.equals(sel_user) || new_status == User.Status.Admin);
	}

	public static boolean user_change_manager(final User login) {
		return login.root();
	}

	public static boolean user_change_comment(final User login, final String user) throws SQLException {
		return login.root() || login.status == User.Status.Organizer && login.is_manager_of(user);
	}

	public static boolean user_change(final User login, final String user) {
		return login.root() || login.id.equals(user);
	}

	public static boolean company_view(final User login, final Company company) throws SQLException {
		switch (login.status) {
		case Admin:
			return true;
		case MainOrganizer:
			return true;
		case Organizer:
			return login.id.equals(company.responsible) || login.is_manager_of(company.responsible);
		case Member:
			return login.id.equals(company.responsible);
		default:
			throw new RuntimeException();
		}
	}

	public static boolean company_create(final User login) {
		return login.root();
	}

	public static boolean company_modify(final User login, final int company) throws SQLException {
		if (login.root()) return true;

		final Company c = Company.getByID(company);
		if (c == null) return false;
		if (login.id.equals(c.responsible)) return true;

		final User u = User.getByID(c.responsible);
		return u != null && login.id.equals(u.manager);
	}

	public static boolean company_delete(final User login) {
		return login.root();
	}

	// TODO enforce in company modify
	public static boolean company_change_responsible(final User login, final int company, final String new_responsible)
			throws SQLException {
		if (login.root()) return true;

		final Company c = Company.getByID(company);
		if (c == null) return false;

		// manager gives responsibility to his member
		final User n = User.getByID(new_responsible);
		if (login.id.equals(c.responsible) && n != null && login.id.equals(n.manager)) return true;

		// manager takes responsibility from his member
		final User o = User.getByID(c.responsible);
		if (login.id.equals(new_responsible) && o != null && login.id.equals(o.manager)) return true;

		// manager transfers responsibility
		return o != null && login.id.equals(o.manager) && n != null && login.id.equals(n.manager);
	}
}