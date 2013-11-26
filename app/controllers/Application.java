package controllers;

import java.util.List;

import controllers.Navigation.Level;
import models.Account;
import models.Friendship;
import models.Post;
import play.Logger;
import play.Routes;
import play.mvc.*;
import views.html.*;
import play.data.Form;
import play.db.jpa.*;


@Transactional
public class Application extends BaseController {
	
	static Form<Post> postForm = Form.form(Post.class);
	static final int limit = 10;
	static final int offset = 0;
	
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes",
				controllers.routes.javascript.GroupController.create(),
				controllers.routes.javascript.GroupController.update(),
				controllers.routes.javascript.AccountController.submit()
				));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result index() {
		Navigation.set(Level.STREAM);
		Account currentAccount = Component.currentAccount();
		return ok(stream.render(currentAccount,Post.getStream(currentAccount, limit, offset),postForm,Post.countStream(currentAccount)));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result stream(int offset) {
		Navigation.set(Level.STREAM);
		Account currentAccount = Component.currentAccount();
		return ok(stream.render(currentAccount,Post.getStream(currentAccount, limit, offset),postForm,Post.countStream(currentAccount)));
	}
		
	public static Result defaultRoute(String path) {
		Logger.info(path+" nicht gefunden");
		return redirect(routes.Application.index());
	}

}
