package controllers;

import models.Account;
import models.Friendship;
import models.Post;

import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import views.html.Profile.index;
import views.html.Profile.stream;
import views.html.Profile.snippets.editForm;
import controllers.Navigation.Level;

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
		Navigation.set(Level.FRIENDS, "Profil", account.name, routes.ProfileController.view(account.id));
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
		Navigation.set(Level.FRIENDS, "Newsstream", account.name, routes.ProfileController.view(account.id));
		
		if(Friendship.alreadyFriendly(Component.currentAccount(), account)){
			return ok(stream.render(account,Post.getFriendStream(account),postForm));
		}
		return ok(stream.render(account,Post.getPublicStream(account),postForm));
	}

	public static Result edit(Long id) {
		try {
			Thread.sleep(0);
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
			Form<Account> filledForm = accountForm.bindFromRequest();
		 	ObjectNode result = Json.newObject();
		 	Boolean error = false;
		 	
		 	if(filledForm.field("firstname").value() == "") {
		 		error = true;
		 	}
		 	if(filledForm.field("lastname").value() == "") {
		 		error = true;
		 	}
		 	
		 	if(error) {
		 		result.put("status", "response");
				String form = editForm.render(account, filledForm).toString();
			 	result.put("payload", form);	
		 	} else {
				account.firstname = filledForm.field("firstname").value();
				account.lastname = filledForm.field("lastname").value();
				account.update();
		 		result.put("status", "redirect");
			 	result.put("url", routes.ProfileController.me().toString());
				flash("success", "Profil erfolgreich gespeichert.");
		 	}
		 	
			return ok(result);
	}

}
