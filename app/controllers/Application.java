package controllers;

import play.Logger;
import play.Routes;
import play.mvc.*;
import views.html.*;
import play.db.jpa.*;


@Transactional
public class Application extends BaseController {
	
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes",
				controllers.routes.javascript.GroupController.create(),
				controllers.routes.javascript.AccountController.submit()));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result stream() {
		return ok(stream.render());
	}
	
	public static Result index() {
		return ok(index.render());
	}
	
	@Security.Authenticated(Secured.class)
	public static Result defaultRoute(String path) {
		Logger.info(path+" nicht gefunden");
		return ok(stream.render());
	}

}
