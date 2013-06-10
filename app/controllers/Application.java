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

@Transactional
public class Application extends BaseController {
	
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

}
