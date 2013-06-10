package controllers;

import play.Logger;
import play.Routes;
import play.mvc.*;
import play.api.templates.Html;
import play.data.*;
import models.*;
import views.html.*;
import views.html.snippets.*;
import play.db.jpa.*;
import static play.data.Form.*;
import controllers.Login;

@Transactional
@With(Common.class)
public class Application extends Controller {
	
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes",
				controllers.routes.javascript.GroupController.create()));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result stream() {
		return ok(views.html.stream.render());
	}
	
	public static Result index() {
		return ok(views.html.index.render());
	}

	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			flash("success", "Error in Form");
			return badRequest(index.render());
		} else {
			session().clear();
			session("email", loginForm.get().email);
			session("id", Account.findByEmail(loginForm.get().email).id.toString());
			session("firstname", Account.findByEmail(loginForm.get().email).firstname);
			return redirect(routes.Application.stream());
		}
	}
	
	public static Html loginForm() {
		Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
		return views.html.snippets.loginForm.render(loginForm);
	}
	
	/**
	 * Logout and clean the session.
	 */
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.index());
	}


}
