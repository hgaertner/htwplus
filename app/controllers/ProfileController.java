package controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;

import models.Account;
import models.Friendship;
import models.Post;
import models.Studycourse;

import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.db.jpa.JPA;
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
		
		// JSON as return value
		ObjectNode result = Json.newObject();

		// Set fields to checked by JPA Validation
		Set<String> checkErrorSet = new HashSet<String>();
		checkErrorSet.add("firstname");
		checkErrorSet.add("lastname");
		checkErrorSet.add("email");

		// Does Email exists already
		Account exisitingAccount = Account.findByEmail(filledForm.field("email").value());
        if(exisitingAccount != null && !exisitingAccount.equals(account) ) {
            filledForm.reject("email", "Diese Mail wird bereits verwendet!");
        }
		
        // Get the JPA Errors
		Set<String> errorSet = filledForm.errors().keySet();
		
		// Check error set against desired fields
	 	if(!Collections.disjoint(errorSet, checkErrorSet)) {
	 		// Set status, so Java Script will place the form again
	 		result.put("status", "response");
			String form = editForm.render(account, filledForm).toString();
		 	result.put("payload", form);	
		 	
		 // Everything fine, save it
	 	} else {
			account.firstname = filledForm.field("firstname").value();
			account.lastname = filledForm.field("lastname").value();
			account.avatar = filledForm.field("avatar").value();	
			account.email = filledForm.field("email").value();
			
			if(filledForm.field("degree").value().equals("null")){
				account.degree = null;
			} else {
				account.degree = filledForm.field("degree").value();
			}
			
			if(filledForm.field("semester").value().equals("0")){
				account.semester = null;
			} else {
				account.semester = Integer.parseInt(filledForm.field("semester").value());
			}
			
			Long studycourseId = Long.parseLong(filledForm.field("studycourse").value());
			Studycourse studycourse;
			if(studycourseId != 0) {
				studycourse = Studycourse.findById(studycourseId);
			} else {
				studycourse = null;
			}
			account.studycourse = studycourse;
        	account.update();
        	// Set status, so Java Script will redirect
	 		result.put("status", "redirect");
		 	result.put("url", routes.ProfileController.me().toString());
			flash("success", "Profil erfolgreich gespeichert.");
	 	}
	 	
		return ok(result);
	}

}
