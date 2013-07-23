package controllers;

import java.util.List;

import models.Account;
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
		Account account = Component.currentAccount();
		List<Post> posts = Post.getPostForAccount(account.id);
		return ok(stream.render(account,posts,postForm));
	}
		
	@Security.Authenticated(Secured.class)
	public static Result defaultRoute(String path) {
		Logger.info(path+" nicht gefunden");
		Account account = Component.currentAccount();
		List<Post> posts = Post.getPostForAccount(account.id);
		return ok(stream.render(account,posts,postForm));
	}

}
