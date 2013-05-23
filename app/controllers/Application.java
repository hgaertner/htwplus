package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import models.*;
import views.html.*;
import play.db.jpa.*;
import static play.data.Form.*;
import controllers.Login;

@Transactional
public class Application extends Controller {
	
	
	@Security.Authenticated(Secured.class)
	public static Result index() {
		return ok(views.html.index.render());
	}

	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);
			session().put("firstname", Account.findByEmail(loginForm.get().email).firstname);
			return redirect(routes.Application.index());
		}
	}
	
	public static Result login() {
		return ok(login.render(form(Login.class)));
	}
	
	/**
	 * Logout and clean the session.
	 */
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.login());
	}

	

	
}
