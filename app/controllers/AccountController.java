package controllers;

import static play.data.Form.form;

import java.util.Random;
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

}