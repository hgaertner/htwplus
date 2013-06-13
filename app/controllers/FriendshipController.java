package controllers;

import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import views.html.Friends.*;

@Security.Authenticated(Secured.class)
@Transactional
public class FriendshipController extends BaseController {
	
	public static Result index() {
		return ok(index.render());
	}

}
