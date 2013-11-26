package controllers;

import static play.data.Form.form;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import controllers.Navigation.Level;
import models.Account;
import models.LDAPConnector;
import models.LDAPConnector.LDAPConnectorException;
import models.Login;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import views.html.index;
import views.html.Friends.searchresult;
import views.html.snippets.signup;
import views.html.snippets.signupSuccess;

@Transactional
public class AccountController extends BaseController {

	/**
	 * Defines a form wrapping the Account class.
	 */
	final static Form<Account> signupForm = form(Account.class);

	public static Result authenticate() {
		DynamicForm form = form().bindFromRequest();
		String username = form.field("email").value();
		if (username.contains("@")) {
			return defaultAuthenticate();
		} else if (username.length() == 0) {
			flash("error", "Bitte gebe einen Benutzernamen ein!");
			return badRequest(index.render());
		} else {
			return LDAPAuthenticate();
		}
	}

	private static Result LDAPAuthenticate() {
		DynamicForm form = form().bindFromRequest();
		String username = form.field("email").value();
		String password = form.field("password").value();

		LDAPConnector ldap = new LDAPConnector();
		try {
			ldap.connect(username, password);
		} catch (LDAPConnectorException e) {
			flash("error", e.getMessage());
			return badRequest(index.render());
		}

		Account account = Account.findByLoginName(ldap.getUsername());
		if (account == null) {
			account = new Account();
			Logger.info("New Account for " + ldap.getUsername()
					+ " will be created.");
			account.firstname = ldap.getFirstname();
			account.lastname = ldap.getLastname();
			account.loginname = ldap.getUsername();
			account.password = "LDAP - not needed";
			Random generator = new Random();
			account.avatar = "a" + generator.nextInt(10);
			account.role = ldap.getRole();
			account.create();
		} else {
			account.firstname = ldap.getFirstname();
			account.lastname = ldap.getLastname();
			account.role = ldap.getRole();
			account.update();
		}

		session().clear();
		session("id", account.id.toString());
		return redirect(routes.Application.index());
	}

	private static Result defaultAuthenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			flash("error", loginForm.globalError().message());
			return badRequest(index.render());
		} else {
			session().clear();
			session("email", loginForm.get().email);
			session("id",
					Account.findByEmail(loginForm.get().email).id.toString());
			session("firstname",
					Account.findByEmail(loginForm.get().email).firstname);
			return redirect(routes.Application.index());
		}
	}

	/**
	 * Logout and clean the session.
	 */
	public static Result logout() {
		session().clear();
		flash("success", "Du bist nun ausgeloggt");
		return redirect(routes.Application.index());
	}

	/**
	 * Handle the form submission.
	 */
	@Transactional
	public static Result submit() {
		Form<Account> filledForm = signupForm.bindFromRequest();
		System.out.println(filledForm.errors());
		// Check Mail
		if (!(Account.findByEmail(filledForm.field("email").value()) == null)) {
			filledForm.reject("email", "Diese Mail wird bereits verwendet!");
		}
		// Check repeated password
		if (!filledForm.field("password").valueOr("").isEmpty()) {
			if (!filledForm.field("password").valueOr("")
					.equals(filledForm.field("repeatPassword").value())) {
				filledForm.reject("repeatPassword",
						"Passwörter stimmen nicht überein");
			}
		}
		if (filledForm.field("password").value().length() < 6) {
			filledForm.reject("password",
					"Das Passwort muss mindestens 6 Zeichen haben.");
		}

		if (filledForm.hasErrors()) {

			return ok(signup.render(filledForm));
		} else {
			Account created = filledForm.get();
			created.password = Component.md5(created.password);
			Random generator = new Random();
			created.avatar = "a" + generator.nextInt(10);
			created.create();
			return ok(signupSuccess.render());
		}
	}

	/**
	 * Search for a user by the given keyword
	 * 
	 * @param keyword
	 * @return Returns an result object
	 */
	public static Result searchByKeyword() {
		List<Account> result = null;
		final Set<Map.Entry<String, String[]>> entries = request()
				.queryString().entrySet();
		for (Map.Entry<String, String[]> entry : entries) {
			if (entry.getKey().equals("keyword")) {
				final String keyword = entry.getValue()[0];
				Logger.debug("Value of key" + keyword);
				result = Account
						.searchForAccountByKeyword(keyword);
				if (result != null && result.size() > 1) {
					Logger.debug("Found " + result.size()
							+ " users with the keyword: " + keyword);
				}
			}

		}
		Navigation.set(Level.FRIENDS, "Suchergebnisse");
		return ok(searchresult.render(result));
	}

}