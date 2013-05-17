package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import models.*;
import views.html.*;
import play.db.jpa.*;

public class Application extends Controller {

	@Security.Authenticated(Secured.class)
	@Transactional(readOnly = true)
	public static Result index() {
		return ok(views.html.index.render(Account.findByEmail(request().username())));
	}

	public static Result login() {
		return ok(login.render(Form.form(Login.class)));
	}

	@Transactional(readOnly = true)
	public static Result authenticate() {
		Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);
			return redirect(routes.Application.index());
		}
	}

	/**
	 * Logout and clean the session.
	 */
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.login());
	}

	public static class Login {

		public String email;
		public String password;

		@Transactional(readOnly = true)
		public String validate() {
			if (Account.authenticate(email, password) == null) {
				return "Invalid user or password";
			}
			return null;
		}
	}
}
