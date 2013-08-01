package controllers;

import controllers.Navigation.Level;
import models.Account;
import models.Friendship;
import models.Post;
import play.cache.Cache;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Profile.*;
import views.html.Profile.snippets.*;

@Transactional
public class ProfileController extends BaseController {

	static Form<Account> accountForm = Form.form(Account.class);
	static Form<Post> postForm = Form.form(Post.class);
	
	public static Result me() {
		Navigation.set(Level.PROFILE);
		Account account = Component.currentAccount();
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(index.render(account, postForm));
			//return ok(index.render(account));
		}
	}

	public static Result view(Long id) {
		Account account = Account.findById(id);
		Navigation.set(Level.PROFILE, account.name);
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(index.render(account, postForm));
			//return ok(index.render(account));
		}
	}
	
	public static Result stream(Long accountId){
		Account account = Account.findById(accountId);
		Navigation.set(Level.PROFILE, "Newsstream", account.name, routes.ProfileController.view(account.id));
		
		if(Friendship.alreadyFriendly(Component.currentAccount(), account)){
			return ok(stream.render(account,Post.getFriendStream(account),postForm));
		}
		return ok(stream.render(account,Post.getPublicStream(account),postForm));
	}

	public static Result edit(Long id) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Account account = Account.findById(id);
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(editForm.render(account, accountForm.fill(account)));
		}
	}

	public static Result update(Long id) {
			Account account = Account.findById(id);
			String firstname = accountForm.bindFromRequest().data().get("firstname");
			account.firstname = firstname;
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.GroupController.index());
	}

}
