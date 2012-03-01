package tintor.frpr.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tintor.frpr.Permissions;
import tintor.frpr.model.User;
import tintor.frpr.model.Variant;
import tintor.frpr.pages.*;
import tintor.frpr.util.*;

/** Main je glavni servlet koji generise vise razlicitih stranica.
 * 
 *  @version avgust 2007
 *  @author Marko Tintor (tintor@gmail.com) */
public class Main extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	@SuppressWarnings("unchecked") @Override protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException, IOException {
		request.getSession().setMaxInactiveInterval(7 * 24 * 60 * 60); // 1 week

		// init response
		response.setContentType("text/html");
		//response.setCharacterEncoding("ISO_8859_1");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();
		disableCashing(response);

		Object contents = null;
		User login = (User) request.getSession().getAttribute("login");
		String message = null; // used only for login error
		try {
			Database.acquireConnection();

			// logout
			if (request.getParameter("logout") != null) login = null;
			// refresh user
			if (login != null) login = User.getByID(login.id);
			// login
			if (request.getParameter("login.submit") != null) {
				login = User.login(request.getParameter("login.id"), request.getParameter("login.password"));
				if (login == null) message = "Neuspela prijava!";
				if (login != null) {
					request.getSession().setAttribute("prev_access", login.last_access);
					login.logAccess();
					request.getSession().setAttribute("access", new Date());
				}
			}
			// log access
			if (login != null) {
				final Date access = (Date) request.getSession().getAttribute("access");
				final Date now = new Date();
				if (access == null || now.getTime() - access.getTime() > 30 * 60 * 1000) login.logAccess();
				request.getSession().setAttribute("access", now);
			}
			// save login session
			request.getSession().setAttribute("login", login);

			// generate page contents
			if (login == null)
				contents = list(error(message), form("f_main", "main", table(row("Ime i prezime", input("login.id",
						"text", "")), row("Lozinka", input("login.password", "password", "")), row("&nbsp;",
						input("login.submit", "submit", "value='OK'")))));
			else if (request.getParameter("users") != null)
				contents = PageUsers.run(login, request);
			else if (request.getParameter("companies") != null)
				contents = PageCompanies.run(login, request);
			else if (request.getParameter("user") != null)
				contents = PageUser.run(login, request);
			else if (request.getParameter("company") != null)
				contents = PageCompany.run(login, request);
			else if (request.getParameter("stats") != null)
				contents = PageStats.run(login, request);
			else
				throw new Redirect("main?stats");
		} catch (final UserError e) {
			contents = error(e.getMessage());
		} catch (final Redirect e) {
			out.print(new Node("html",
					new Node("head", "<meta http-equiv='REFRESH'  content='0;url=" + e.url + "' />")));
			return;
		} catch (final Exception e) {
			throw new ServletException(e);
		} finally {
			Database.releaseConnection();
		}

		// generate page header
		final Node head = new Node("head");
		head.add(new Node("title", Variant.current.toString().replace('_', ' ') + " Database"));
		head.add(new Node("meta http-equiv='Content-Type' content='text/html; charset=utf-8'"));
		head.add(new Node("link rel='stylesheet' type='text/css' href='my.css'"));
		head.add(new Node("script type='text/javascript' src='util.js'"));

		final Node body = new Node("body");
		if (login != null) body.add(tabs(login), "<br/><br/>");
		body.add(contents);
		body.add(new Node("table id='tooltip' style='visibility:hidden' frame='border' bgcolor='#FFFFFF'", new Node(
				"tr", new Node("td id='tooltip_text'"))));
		body.add(new Node("div align='right'", new Node("i", "Constructed by ", link("mailto:tintor@gmail.com",
				"Marko Tintor"))));

		// output page
		out.print(new Node("html", head, body));

		// ovo je potrebno za "Sacuvaj i vrati se"
		final String query = request.getQueryString() != null ? "main?" + request.getQueryString() : "main";
		if (request.getParameter("company") == null) request.getSession().setAttribute("before_company", query);
		if (request.getParameter("user") == null) request.getSession().setAttribute("before_user", query);
	}

	/** Generisi tabove na vrhu stranice u zavisnosti od funkcije korisnika */
	static Object tabs(final User login) {
		final List<Object> tabs = new ArrayList<Object>();

		tabs.add(link("main?user=" + escape(login.id), bold(escape(login.id))));
		tabs.add(link("main?stats", "pregled"));
		tabs.add(link("main?companies&responsible=" + escape(login.id), "moje kompanije"));
		if (login.status != User.Status.Member)
			tabs.add(link("main?companies", login.status == User.Status.Organizer ? "kompanije moje grupe"
					: "sve kompanije"));
		tabs.add(link("main?users", "korisnici"));
		if (login.status == User.Status.Organizer)
			tabs.add(link("main?users&manager=" + escape(login.id), "moja grupa"));
		if (Permissions.company_create(login)) tabs.add(link("main?company", "nova kompanija"));
		if (Permissions.user_create(login)) tabs.add(link("main?user", "novi korisnik"));
		tabs.add(link("main?logout", "odjava"));

		return join(tabs, " | ");
	}

	@Override protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	/** Izmedju svaka dva objekta u listi dodaj razdvajac */
	public static Object join(final List<Object> list, final Object separator) {
		final List<Object> result = new ArrayList<Object>(list.size() * 2);
		for (final Object o : list) {
			if (result.size() > 0) result.add(separator);
			result.add(o);
		}
		return result;
	}

	/** Kastovanje stringa iz jednog kodiranja u drugo */
	private static String cast(final String a, final String from, final String to) {
		try {
			return new String(a.getBytes(from), to);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/** Kastuje string iz UTF-8 u ISO-8859-1.
	 *  Sve stringovne konstante koje se salju u HTML i koje sadrze
	 *  slova koja nisu ASCII se <b>moraju</b> propustiti kroz ovu funkciju. */
	public static String iso(final String a) {
		return cast(a, "UTF-8", "ISO_8859_1");
	}

	public static String cir2lat(final String a) {
		if (a == null) return null;
		return iso(CyrLatConvertor.cyrilicToLatin(cast(a, "ISO_8859_1", "UTF-8")));
	}

	public static String error(final String message) {
		return "<big><b><i>" + escape(message) + "</i></b></big>";
	}

	/** Bolduj tekst */
	public static Node bold(final Object a) {
		return new Node("b", a);
	}

	public static Object field(final Object value) {
		if (value == null) return "&nbsp;";
		final String a = escape(value.toString());
		return a.length() > 0 ? a : "&nbsp;";
	}

	public static void main(final String[] args) {
		final String query = " marko, and-pera ostojic ,";
		final String keywords = query.replaceAll("\\W+", "|").replaceAll("^\\||\\|$", "");
		System.out.println(keywords);
		System.out.println("marko pa gde marem, marema si".replaceAll("\\b(marko|marem)\\b", "<bold>$0</bold>"));
	}

	public static Pattern highlightPattern(final String query) {
		final String keywords = query.replaceAll("\\W+", "|").replaceAll("^\\||\\|$", "");
		return Pattern.compile("\\b(" + keywords + ")\\b", Pattern.CASE_INSENSITIVE);
	}

	public static Object field(final Object value, final Pattern p) {
		if (value == null) return "&nbsp;";
		final String a = escape(value.toString());
		if (a.length() == 0) return "&nbsp;";
		return highlight(a, p);
		//		final String query = " marko, and-pera ostojic ,";
		//		final String keywords = query.replaceAll("\\W+", "|").replaceAll("^\\||\\|$", "");
		//		final Pattern p = Pattern.compile("\\b(" + keywords + ")\\b");
	}

	private static String highlight(final String a, final Pattern p) {
		if (p == null) return a;
		return p.matcher(a).replaceAll("<b style='color:black;background-color:#ffff66'>$0</b>");
	}

	public static Object comment_large(String a, final Pattern p) {
		if (a.length() == 0) return "&nbsp;";
		a = a.replaceAll("\\n", " | ");
		if (a.length() > 500) a = a.substring(0, 500) + "...";
		return color("777777", highlight(escape(a), p));
	}

	public static Object user_link(final String user, final Pattern p) throws SQLException {
		if (user == null || user.length() == 0) return "&nbsp;";
		final User u = User.getByID(user);
		return link("main?user=" + escape(user).replace("%20", "+"), color(u == null ? "000000" : u.status.color,
				highlight(escape(user), p)));
	}

	/** Escape string for insertion in HTML */
	public static String escape(final String a) {
		if (a == null) return "";

		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(a);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<')
				result.append("&lt;");
			else if (character == '>')
				result.append("&gt;");
			else if (character == '\"')
				result.append("&quot;");
			else if (character == '\'')
				result.append("&#039;");
			else if (character == '\\')
				result.append("&#092;");
			else if (character == '&')
				result.append("&amp;");
			else
				result.append(character);
			character = iterator.next();
		}
		return result.toString();
	}

	public static List<Object> list(final Object... data) {
		return Arrays.asList(data);
	}

	public static Node form(final String name, final String action, final Object... data) {
		return new Node("form enctype='text' accept-charset='utf-8' name='" + name + "' action='" + action
				+ "' method='post'", data);
	}

	public static Node button(final String url, final String text) {
		return new Node("form enctype='text' accept-charset='utf-8' action='" + url + "' method='get'", new Node(
				"input value='" + escape(text) + "' type='submit'"));
	}

	public static Node table(final Object... data) {
		return new Node("table", data);
	}

	public static Node link(final String dest, final Object... data) {
		return new Node("a href='" + dest + "'", data);
	}

	public static Node color(final String color, final Object o) {
		return new Node("font color='#" + color + "'", o);
	}

	public static Object user_link(final String user) throws SQLException {
		if (user == null || user.length() == 0) return "&nbsp;";
		final User u = User.getByID(user);
		return link("main?user=" + escape(user).replace("%20", "+"), color(u == null ? "000000" : u.status.color,
				escape(user)));
	}

	public static Object email_link(final String email) {
		return email != null && email.length() > 0 ? link("mailto:" + escape(email), escape(email)) : "&nbsp;";
	}

	public static Object email_link(final String email, final Pattern p) {
		return email != null && email.length() > 0 ? link("mailto:" + escape(email), highlight(escape(email), p))
				: "&nbsp;";
	}

	public static Node row(final Object... data) {
		final Node x = new Node("tr");
		for (final Object a : data)
			if (a != null) x.add(new Node("td nowrap", a));
		return x;
	}

	public static Node input(final String name, final String type, final String extra) {
		return new Node("input name='" + name + "' type='" + type + "' " + extra);
	}

	static void disableCashing(final HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0
		response.setDateHeader("Expires", 0); // prevents caching at the proxy server
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-store");
	}

	private static int ToolTipID = 0;

	public static Node image(final String a) {
		return new Node("image style='border:none;' src='" + a + "'");
	}

	public static Node tooltip(final String text, final Object o) {
		final String id = "ToolTip" + ToolTipID++;
		return new Node("div onmousemove='showTooltip(" + id + ".innerHTML)' onmouseout='hideTooltip()'", new Node("u",
				o), new Node("script type='text' id='" + id + "' style='visibility:hidden'", escape(text).replace(
				"\n", "<br/>")));
	}

	public static Object phone(final String a) {
		if (a.length() == 0) return "&nbsp;";
		//final int f = a.indexOf(',');
		//if (f != -1) return tooltip(escape(a), escape(a.substring(0, f)));
		//if (a.length() > 12) return tooltip(escape(a), escape(a.substring(0, 12)));
		return escape(a);
	}

	public static Object comment(final String a) {
		if (a.length() == 0) return "&nbsp;";
		int f = a.indexOf('\n');
		if (f == -1) f = a.length();
		//return tooltip(a, escape(a.substring(0, f)));
		return escape(a.substring(0, Math.min(f, 100)));
	}

	public static Object comment_large(String a) {
		if (a.length() == 0) return "&nbsp;";
		a = a.replaceAll("\\n", " | ");
		if (a.length() > 500) a = a.substring(0, 500) + "...";
		return color("777777", escape(a));
	}

	/** Dugme za brisanje sa dijalogom za potvrdu. */
	public static Node delete_button(final String name) {
		return input(name, "submit", iso("value='Obriši' onClick='return window.confirm"
				+ "(\"Da li ste sigurni da hoćete da obrišete?\")'"));
	}
}